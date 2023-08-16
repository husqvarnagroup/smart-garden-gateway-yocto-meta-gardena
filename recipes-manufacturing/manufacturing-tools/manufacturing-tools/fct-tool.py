#!/usr/bin/python3
# coding: utf-8
#
# Author: Andreas Müller <andreas.mueller@husqvarnagroup.com>
# Date: 2018-08-29
# Version: 0.1
#
# Copyright (c) 2018 Gardena GmbH

"""
@package fct-tool

Tool to support functional-circuit test (FCT) as defied by production
test specification (PTS) for Linux gateway manufacturing.
"""

import argparse
import sys
import subprocess
import time
import tempfile
import shlex
from evdev import InputDevice, ecodes, events # pylint: disable=import-error

from util import get_fw_printenv, fw_setenv, fw_getenv, get_systemd_status, set_all_leds, LED_COLOR_PINS

from cpms_client import CPMSClientBootstrapped

WLAN_INTERFACE = "wlan0"
GPIO_DIR = "/sys/class/gpio"
BUTTON_EVENT_DEVICE = "/dev/input/event0"
RM_SWD_RESET_PIN = 37
FIRMWARE_PATH = "/usr/share/gardena/firmware/current/"
FIRMWARE_BOOTLOADER_FILE = "bootloader.bin"
FIRMWARE_BOOTLOADER_ADDRESS = 0x0
FIRMWARE_STACK_FILE = "gateway.bin"
FIRMWARE_STACK_ADDRESS = 0x2000
FIRMWARE_INDIVIDUAL_PRODUCT_DATA_ADDRESS = 0x3fc00
FLASH_COMMAND_TEMPLATE = "openocd -f board/gardena_radio.cfg -c 'program %s verify exit %s'"

class FCTToolException(Exception):
    """Represents an error during command execution."""


# helper functions

def get_wlans():
    """Get full data from WLAN scan."""
    # Note: iw explicitly warns "Do NOT screenscrape this tool, we
    # don't consider its output stable.", so this might be a bad idea.
    # Otoh, this is only used during production and if we retest this
    # tool after an update of the iw package, we should be fine.
    # Ideally we should add an automated test for the whole FCT in any
    # case.
    wlans = {}
    data = subprocess.check_output(["iw", "dev", WLAN_INTERFACE, "scan"]).decode("ascii")
    bssid = None
    for row in data.split("\n"):
        if row.startswith("BSS "):
            bssid = row[4:21]
            wlans[bssid] = {}
        else:
            if bssid is None:
                continue
            if row.startswith("\t\t"):  # ignore multi-line parameters as we currently don't need them
                continue
            key = row[1:].split(": ")[0]
            value = ": ".join(row[1:].split(": ")[1:])
            wlans[bssid][key] = value
    return wlans


def export_gpio(gpio):
    """Export given GPIO pin."""
    export = open("%s/export" % GPIO_DIR, "w")
    export.write("%d\n" % gpio)
    export.close()  # note: using seek(0) rather than open/close doesn't work; not sure why


def unexport_gpio(gpio):
    """Unexport given GPIO pin."""
    export = open("%s/unexport" % GPIO_DIR, "w")
    export.write("%d\n" % gpio)
    export.close()  # note: using seek(0) rather than open/close doesn't work; not sure why


def initialize_gpio(gpio, dir_="out"):
    """Initialize GPIO pin configuration."""
    export_gpio(gpio)
    direction = open("%s/gpio%d/direction" % (GPIO_DIR, gpio), "w")
    direction.write(dir_ + "\n")
    direction.close()


def set_gpio(gpio, level=1):
    """Set GPIO pin level."""
    value = open("%s/gpio%d/value" % (GPIO_DIR, gpio), "w")
    value.write(str(level) + "\n")
    value.close()


# commands

def get_ipr_id():
    """Get IPR ID."""
    fw_env = get_fw_printenv()
    return fw_env['gatewayid']


def set_leds(color):
    """Set LED to given color or off."""
    if color in LED_COLOR_PINS.keys():
        set_all_leds(color)
    else:
        raise FCTToolException("invalid color")


def get_button_event():
    """Get button event."""
    dev = InputDevice(BUTTON_EVENT_DEVICE)

    # flush
    while dev.read_one() is not None:
        pass

    print("waiting for button event")
    for event in dev.read_loop():
        if event.type == ecodes.EV_KEY and event.code == ecodes.KEY_PROG1:
            if event.value == events.KeyEvent.key_up:
                return "up"
            if event.value == events.KeyEvent.key_down:
                return "down"


def get_wlan_ssids():
    """Get all SSIDs from scan."""
    return "SSIDs:\n" + "\n".join([wlan['SSID'] for wlan in get_wlans().values()])


def get_wlan_rssi(ssid):
    """"Get RSSI for given SSID."""
    wlan = [wlan for wlan in get_wlans().values() if wlan['SSID'] == ssid]
    if wlan:
        return wlan[0]['signal'].split(" ")[0]
    return None


def finalize():
    """Finalize gateway as last step of FCT."""
    # store the fct_finalized value to IPR and finalize item
    # (technically we should do this after EOL, but it shouldn't
    # really matter, as we don't get any interesting test results
    # there)
    cpms = CPMSClientBootstrapped()
    gwid = fw_getenv("gatewayid")
    cpms.set_value(gwid, "/configuration/fct_finalized", True)

    # store fct_finalized in uboot env
    fw_setenv("fct_finalized", "1")


def do_openocd_program(filename, address):
    """Call openocd to program given file at given address."""
    flash_command = FLASH_COMMAND_TEMPLATE % (filename, hex(address))
    process = subprocess.Popen(flash_command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    out, err = process.communicate()
    if process.returncode != 0:
        print("Stdout: %s" % out.decode('ascii'))
        print("Stderr: %s" % err.decode('ascii'))
        raise FCTToolException("openocd failed")


def flash_rm_firmware():
    """Flashing default firmware to radio module."""
    # check sgtin
    sgtin = fw_getenv("sgtin")
    if sgtin is None or len(sgtin) != 24:
        raise FCTToolException("invalid sgtin")
    # check rmaddr
    try:
        rmaddr = fw_getenv("rmaddr").replace(":", "")
    except AttributeError:
        sys.stdout.write("No rmaddr found, using static value used in all MP devices\n")
        rmaddr = "8c05510006e2"
    if len(rmaddr) != 12:
        raise FCTToolException("invalid rmaddr")
    # check if pppd is running
    ppp_stopped = None
    processes = subprocess.check_output(["ps"]).decode('ascii')
    if "pppd" in processes:
        # stopping ppp, if it is running, ignore errors
        subprocess.check_call(["systemctl", "stop", "ppp"])
        ppp_stopped = True
    # reset RM
    reset_rm()
    # flash bootloader
    do_openocd_program(FIRMWARE_PATH + FIRMWARE_BOOTLOADER_FILE, FIRMWARE_BOOTLOADER_ADDRESS)
    # flash stack/application
    do_openocd_program(FIRMWARE_PATH + FIRMWARE_STACK_FILE, FIRMWARE_STACK_ADDRESS)
    # flash individual product data
    with tempfile.NamedTemporaryFile() as tmp:
        # SGTIN
        tmp.write(bytes.fromhex(sgtin))
        # RM MAC address
        tmp.write(bytes.fromhex(rmaddr))
        # public, common & private key
        tmp.write(b'\x00' * (64 * 3))
        # default channel map
        tmp.write(b'\xff' * 4)
        # flush & program
        tmp.flush()
        do_openocd_program(tmp.name, FIRMWARE_INDIVIDUAL_PRODUCT_DATA_ADDRESS)
    # reset RM
    reset_rm()
    # restart pppd if it was stopped
    if ppp_stopped:
        subprocess.check_call(["systemctl", "start", "ppp"])


def reset_rm():
    """Export, assert, de-assert and un-export reset pin."""
    initialize_gpio(RM_SWD_RESET_PIN)
    set_gpio(RM_SWD_RESET_PIN, 0)
    time.sleep(0.1)
    set_gpio(RM_SWD_RESET_PIN, 1)
    unexport_gpio(RM_SWD_RESET_PIN)


def interactive_shell():
    """Interactive shell."""
    commands = {             # output expected
        "get_ipr_id"         : True,
        "get_button_event"   : True,
        "get_wlan_ssids"     : True,
        "get_wlan_rssi"      : True,
        "get_systemd_status" : True,
        "set_leds"           : False,
        "finalize"           : False,
        "flash_rm_firmware"  : False,
        "reset_rm"           : False,
    }
    print("READY")
    while True:
        line = sys.stdin.readline().strip()
        cmd = line.split(" ")[0].lower().replace("-", "_")
        args = shlex.split(line)[1:]
        if cmd in commands:
            function = globals()[cmd]
            output_expected = commands[cmd]
            try:
                output = function(*args)
                if output_expected:
                    if not output:
                        print("ERROR: no output")
                        continue
                    else:
                        print("OUTPUT: " + output)
                print("OK")
            except Exception as ex: # pylint: disable=broad-except
                print("ERROR: exception")
                print(ex)

        elif cmd == "help":
            print("OUTPUT: available commands\n" + "\n".join(commands.keys()))
        elif cmd == "quit":
            print("OK")
            break
        else:
            print("ERROR: unknown command: " + cmd)


# main

def main():  # pylint: disable=too-many-branches
    """Main entry point."""
    parser = argparse.ArgumentParser()
    parser.add_argument("--get-ipr-id", action='store_true', help="return the IPR ID of this gateway")
    parser.add_argument("--set-leds", action='store', metavar='state', choices=["white", "red", "green", "blue", "off"],
                        help="set state of all LEDs")
    parser.add_argument("--get-button-event", action='store_true', help="get button event")
    parser.add_argument("--get-wlan-ssids", action='store_true', help="list currently visible WLAN SSIDs")
    parser.add_argument("--get-wlan-rssi", action='store', type=str,
                        help="get signal strength of given SSID", metavar="SSID")
    parser.add_argument("--get-systemd-status", action='store_true', help="get systemd status")
    parser.add_argument("--finalize", action='store_true', help="perform FCT finishing actions")
    parser.add_argument("--flash-rm-firmware", action='store_true', help="flash default firmware to radio module")
    parser.add_argument("--reset-rm", action='store_true', help="Reset the radio module")
    parser.add_argument("--interactive", action='store_true', help="Start interactive shell.")
    args = parser.parse_args()

    if args.interactive:
        interactive_shell()
    elif args.get_ipr_id:
        print(get_ipr_id())
    elif args.set_leds:
        set_leds(args.set_leds)
    elif args.get_button_event:
        print(get_button_event())
    elif args.get_wlan_ssids:
        print(get_wlan_ssids())
    elif args.get_wlan_rssi:
        rssi = get_wlan_rssi(args.get_wlan_rssi)
        if rssi:
            print(rssi)
        else:
            print("SSID not found")
            sys.exit(1)
    elif args.get_systemd_status:
        print(get_systemd_status())
    elif args.finalize:
        finalize()
    elif args.flash_rm_firmware:
        flash_rm_firmware()
    elif args.reset_rm:
        reset_rm()
    elif len(sys.argv) == 1:  # no arguments given
        parser.print_help()
    else:
        print("ERROR: not implemented yet")
        sys.exit(1)

    sys.exit(0)


if __name__ == '__main__':
    main()
