#!/usr/bin/python3
# coding: utf-8
#
# Author: Andreas Müller <andreas.mueller@husqvarnagroup.com>
# Date: 2018-11-14
# Version: 0.1
#
# Copyright (c) 2018 Gardena GmbH

"""
@package homekit-tool
HomeKit helper functions.
"""

import sys
import argparse
import json
import subprocess

from util import get_fw_printenv, fw_setenv, hexstr_to_b64str
from cpms_client import CPMSClientBootstrapped
from bootstrap import bootstrap_get_homekit_tokens

def get_tokens():
    """Get HomeKit tokens from bootstrap server."""
    body = bootstrap_get_homekit_tokens()

    return dict(
        zip(
            [
                "output_format_version",
                "setup_code",
                "srp_salt",
                "srp_verifier",
                "setup_id",
                "setup_payload"
            ],
            body.decode("ascii").strip().split("\n")
        )
    )


def initialize_setup_tokens():
    """Initialize Accessory Setup tokens."""
    # get tokens
    tokens = get_tokens()

    if tokens['output_format_version'] != '1':
        sys.stderr.write("unexpected output_format_version: %s\n" % tokens['output_format_version'])
        sys.exit(1)

    # save to U-Boot environment
    provdata = {
        'setup_id' : tokens['setup_id'],
        'srp_salt' : hexstr_to_b64str(tokens['srp_salt']),
        'srp_verifier' : hexstr_to_b64str(tokens['srp_verifier'])
    }

    with open('/tmp/prov.json', 'w') as file:
        json.dump(provdata, file)
    subprocess.check_call(["gardena-provision", '-r', '/tmp/prov.json'])

    # save to IPR
    ipr_id = get_fw_printenv()['gatewayid']
    cpms = CPMSClientBootstrapped()
    for key in tokens.keys():
        cpms.set_value(ipr_id,
                       "/configuration/homekit_"+key,
                       tokens[key])

	# mark homekit setup as done
    fw_setenv("hk_setup_done", "1")



# main

def main():
    """Main entry point."""
    parser = argparse.ArgumentParser()
    parser.add_argument("--init-setup-tokens", action='store_true',
                        help="Initialize accessory setup tokens on device and in IPR.")
    args = parser.parse_args()

    if args.init_setup_tokens:
        initialize_setup_tokens()

    sys.exit(0)


if __name__ == '__main__':
    main()
