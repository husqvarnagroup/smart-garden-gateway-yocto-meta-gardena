#!/usr/bin/env python3

import sys
import subprocess
import tempfile

FIRMWARE_PATH = "/usr/share/gardena/firmware/current/"
FIRMWARE_STACK_FILE = "gateway.bin"
FIRMWARE_STACK_ADDRESS = 0x0
FLASH_COMMAND_TEMPLATE = "openocd -f board/gardena_radio.cfg -c 'program %s verify exit %s'"


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


def do_openocd_program(filename, address):
    """Call openocd to program given file at given address."""
    flash_command = FLASH_COMMAND_TEMPLATE % (filename, hex(address))
    process = subprocess.Popen(flash_command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    out, err = process.communicate()
    if process.returncode != 0:
        print("Stdout: %s" % out.decode('ascii'))
        print("Stderr: %s" % err.decode('ascii'))
        sys.exit(1)


def flash_rm_firmware():
    """Flashing default firmware to radio module."""
    # check sgtin
    sgtin = fw_getenv("sgtin")
    if sgtin is None or len(sgtin) != 24:
        sys.stdout.write("invalid SGTIN\n")
        sys.exit(1)
    # check rmaddr
    try:
        rmaddr = fw_getenv("rmaddr").replace(":", "")
    except AttributeError:
        sys.stdout.write("No rmaddr found, using static value used in all MP devices\n")
        rmaddr = "8c05510006e2"
    if len(rmaddr) != 12:
        sys.stdout.write("invalid rmaddr")
        sys.exit(1)
    # check if pppd is running
    ppp_stopped = None
    processes = subprocess.check_output(["ps"]).decode('ascii')
    if "pppd" in processes:
        # stopping ppp, if it is running, ignore errors
        subprocess.check_call(["systemctl", "stop", "ppp"])
        ppp_stopped = True
    # reset RM
    reset_rm()
    # flash firmware
    do_openocd_program(FIRMWARE_PATH + FIRMWARE_STACK_FILE, FIRMWARE_STACK_ADDRESS)
    # reset RM
    reset_rm()
    # restart pppd if it was stopped
    if ppp_stopped:
        subprocess.check_call(["systemctl", "start", "ppp"])


def reset_rm():
    subprocess.check_call(["reset-rm"])


def main():
    flash_rm_firmware()
    sys.exit(0)


if __name__ == '__main__':
    main()
