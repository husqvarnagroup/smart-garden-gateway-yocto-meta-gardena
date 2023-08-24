#!/usr/bin/env python3
# coding: utf-8
#
# Copyright (c) 2020 Gardena GmbH

"""
@package errorhandler
The purpose of this script is to collect information in case any step
during manufacturing fails and store it in CPMS as manufacturing
event.
"""

import argparse
import uuid
import json
import subprocess

from cpms_client import CPMSClientBootstrapped
from util import set_all_leds, get_fw_printenv, fw_getenv, manufacturing_event_timestamp

SYSTEMD_MANUFACTURING_SERVICES = ["ipr", "homekit-setup", "selftest", "eoltest"]
# variables that are confidential and should not be stored in manufacturing event
UBOOT_ENV_BLACKLIST_KEYS = ["x509_key", "hk-device-key", "secure_token"]

def run_process(process, args_=None, timeout=10):
    """Run process and collect output."""
    process_and_args = [process]
    if args_:
        process_and_args += args_
    result = {
        'process': process,
        'arguments': args_
    }
    try:
        proc = subprocess.run(process_and_args,
                              capture_output=True,
                              timeout=timeout)
        result['returncode'] = proc.returncode
        result['stdout'] = proc.stdout.decode("utf-8", errors="ignore")
        result['stderr'] = proc.stderr.decode("utf-8", errors="ignore")
    except Exception as ex: # pylint: disable=broad-except
        result['exception'] = repr(ex)
    return result


def get_systemctl_status():
    """Get systemctl status log."""
    return run_process("systemctl", ["--no-pager", "status"])

def get_systemctl_failed():
    """Get systemctl status log for failed units."""
    return run_process("systemctl", ["--no-pager", "--failed"])

def get_journalctl_log():
    """Get journalctl log for relevant systemd units."""
    journalctl_log = {}
    for unit in SYSTEMD_MANUFACTURING_SERVICES:
        journalctl_log[unit] = run_process("journalctl", ["--no-pager", "-u", unit])
    return journalctl_log

def get_uboot_env():
    """Get sanitized dictionary of U-Boot environment variables."""
    uboot_env = get_fw_printenv()
    uboot_env = {key: value for (key, value) in uboot_env.items() if not key in UBOOT_ENV_BLACKLIST_KEYS}
    return uboot_env

def collect_info():
    """Collect all information relating to manufacturing status."""
    info = {}
    info['systemctl_status'] = get_systemctl_status()
    info['systemctl_failed'] = get_systemctl_failed()
    info['journalctl_log'] = get_journalctl_log()
    info['uboot_env'] = get_uboot_env()
    return info

def add_event(info):
    """Add manufacturing event with given information."""
    cpms = CPMSClientBootstrapped()
    ipr_id = fw_getenv("gatewayid")
    if ipr_id:
        description = "manufacturing_error"
    else:
        # in case the device doesn't have a gatewayid yet, store the event with the Linux module
        ipr_id = fw_getenv("linuxmoduleid")
        description = "manufacturing_error_nogwid"
    event = {
        "ipr_id":      ipr_id,
        "event_id":    str(uuid.uuid4()),
        "timestamp": manufacturing_event_timestamp(),
        "description": description,
        "attachment": {
            "name":      "manufacturing_error.json",
            "mime_type": "application/json",
            "data":      json.dumps(info)
        }
    }
    cpms.add_event(ipr_id, event)

def main():
    """Main entry point."""

    # Prevent power LED from turning yellow (red + green) once GW is booted
    run_process("systemctl", ["stop", "power-led"])

    # indicate error with blinking red LEDs
    #
    # note: some step below may fail (e.g. when CPMS is unavailable).
    # by already indicating an "error in progress" here, we can
    # instruct our EMS to handle such devices specially (e.g. leave
    # them connected for futher investigation).
    set_all_leds("red", "timer")

    # parse arguments
    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action='store_true',
                        help="Dry run – just print information and do not store it as manufacturing event.")
    args = parser.parse_args()

    # collect information
    info = collect_info()

    if args.dry_run:
        # print info to console
        from pprint import pprint
        pprint(info)
    else:
        # store information as manufacturing event
        add_event(info)

    # indicate error – set LEDs to red
    set_all_leds("red")

if __name__ == '__main__':
    main()
