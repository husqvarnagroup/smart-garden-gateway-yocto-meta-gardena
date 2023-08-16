#!/usr/bin/python3
# coding: utf-8
#
# Author: Andreas Müller <andreas.mueller@husqvarnagroup.com>
# Date: 2018-11-14
# Version: 0.1
#
# Copyright (c) 2018 Gardena GmbH

"""
@package util
Collection of common utility functions.
"""

import subprocess
import base64
import datetime

LED_DIR = "/sys/class/leds"
LEDS_RED = ["smartgw:internet:red", "smartgw:power:red", "smartgw:radio:red"]
LEDS_GREEN = ["smartgw:internet:green", "smartgw:power:green", "smartgw:radio:green"]
LEDS_BLUE = ["smartgw:internet:blue", "smartgw:power:blue", "smartgw:radio:blue"]
LED_PINS = LEDS_RED + LEDS_GREEN + LEDS_BLUE
LED_COLOR_PINS = {
    "white": LED_PINS,
    "red": LEDS_RED,
    "green": LEDS_GREEN,
    "blue": LEDS_BLUE,
    "off": []
}

def set_led(led, brightness, trigger="oneshot"):
    """Set LED brightness."""
    f_trigger = open("%s/%s/trigger" % (LED_DIR, led), "w")
    f_trigger.write(trigger + "\n")
    f_trigger.close()
    f_value = open("%s/%s/brightness" % (LED_DIR, led), "w")
    f_value.write(str(brightness) + "\n")
    f_value.close()

def set_all_leds(color, trigger="oneshot"):
    """Set all LEDs to given color."""
    # turn off everything
    for i in LED_PINS:
        set_led(i, 1 if i in LED_COLOR_PINS[color] else 0, trigger)

def get_systemd_status():
    """Helper function to return systemd status (is-system-running)."""
    proc = subprocess.run(args=["systemctl", "--wait", "is-system-running"], stdout=subprocess.PIPE)
    return proc.stdout.decode('ascii').strip()

def get_fw_printenv():
    """Get fw_printenv values as dict."""
    data = subprocess.check_output("fw_printenv")
    data = data.decode("ascii")
    return dict([(line.split("=")[0], "=".join(line.split("=")[1:])) for line in data.split("\n") if len(line) > 0])

def fw_getenv(variable):
    """Get U-Boot environment variable value."""
    env = get_fw_printenv()
    if variable in env.keys():
        return env[variable]
    return None

def fw_setenv(variable, value):
    """Set U-Boot environment variable to given value."""
    subprocess.check_call(["fw_setenv", variable, '--', value])

def hexstr_to_b64str(string):
    """Convert hex string to base64-encoded string."""
    return base64.b64encode(bytes.fromhex(string)).decode("utf-8")

def manufacturing_event_timestamp():
    """Return a timestamp in the format suitable for manufacturing events."""
    # note: the CPMS site client has rather specific ideas
    # about the format of the timestamp and will just complain
    # about bad JSON if those aren't met
    return datetime.datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S.000+0000")
