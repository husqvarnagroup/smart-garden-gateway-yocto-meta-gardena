#!/usr/bin/python3
# coding: utf-8
#
# Author: Andreas Müller <andreas.mueller@husqvarnagroup.com>
# Date: 2018-09-11
# Version: 0.1
#
# Copyright (c) 2018 Gardena GmbH

"""
@package ipr-tool
IPR tool - provides IPR integration support for gateway.
"""

import sys
import argparse
import json
import base64
import http.client
import datetime
import subprocess


from bootstrap import bootstrap_get_batch, bootstrap_server_get, bootstrap_server_put
from cpms_client import CPMSClientBootstrapped
from util import get_fw_printenv, fw_setenv, fw_getenv
from radio_module_test import RMTestFW

RANDOM_TOKEN_LENGTH = 256

KEY_BEGIN = "-----BEGIN EC PRIVATE KEY-----"
KEY_END = "-----END EC PRIVATE KEY-----"
CERT_BEGIN = "-----BEGIN CERTIFICATE-----"
CERT_END = "-----END CERTIFICATE-----"

# helper functions

def exit_error(msg, code=1):
    """Exit with given error message and code."""
    sys.stderr.write("ERROR: " + msg + "\n")
    sys.exit(code)

def get_batch_id():
    """Fetch batch ID."""
    body = bootstrap_get_batch()

    config = json.loads(body.decode("ascii"))
    return config['id']

def initialize_x509_credentials(ipr_id):
    """Fetch & store X.509 certificate & key."""
    # fetch credentials
    bootstrap_server_put('/pki/register.py',
                         {'env': 'prod', 'eth_mac': fw_getenv("ethaddr"), 'ipr_id': ipr_id})
    client_key = bootstrap_server_get('/pki/work/{}.key'.format(ipr_id)).decode("ascii")
    client_crt = bootstrap_server_get('/pki/work/{}.crt'.format(ipr_id)).decode("ascii")

    # sanity checks
    if len(client_key) < 200 or len(client_key) > 400:
        exit_error("unexpected size of X.509 key")
    if len(client_crt) < 500 or len(client_crt) > 1000:
        exit_error("unexpected size of X.509 certificate")
    if client_key.find(KEY_BEGIN) < 0 or client_key.find(KEY_END) < 0:
        exit_error("expected to find BEGIN/END PRIVATE KEY strings in X.509 key")
    if client_crt.find(CERT_BEGIN) < 0 or client_crt.find(CERT_END) < 0:
        exit_error("expected to find BEGIN/END CERTIFICATE strings in X.509 certificate")

    # process certificate with openssl
    proc = subprocess.run(["openssl", "x509", "-in", "-", "-text"],
                          input=client_crt.encode('ascii'),
                          capture_output=True,
                          check=True)
    cert_text = proc.stdout.decode('ascii')

    # check certificate validity (date)
    now = datetime.datetime.now()
    not_before_str = [l for l in cert_text.split("\n")
                      if l.strip().startswith("Not Before:")][0].split("Not Before: ")[1]
    not_before = datetime.datetime.strptime(not_before_str, "%b %d %H:%M:%S %Y %Z")
    if not_before > now:
        exit_error("X.509 certificate is not valid yet")
    not_after_str = [l for l in cert_text.split("\n")
                     if l.strip().startswith("Not After :")][0].split("Not After : ")[1]
    not_after = datetime.datetime.strptime(not_after_str, "%b %d %H:%M:%S %Y %Z")
    not_after_delta = not_after - now
    if not_after_delta.days < 49 * 365:
        exit_error("X.509 certificate is not valid for at least 49 years")

    # check subject (CN)
    subject = [l for l in cert_text.split("\n") if l.strip().startswith("Subject:")][0].split("Subject: ")[1].strip()
    if not subject == f"CN = {ipr_id}":
        exit_error("X.509 certificate subject common name does not match IPR ID")

    # check against CA
    try:
        proc = subprocess.run(["openssl", "verify", "-CApath", "/tmp", "-CAfile", "/etc/ssl/certs/ca-prod.crt"],
                              input=client_crt.encode('ascii'),
                              check=True)
    except subprocess.CalledProcessError:
        exit_error("X.509 certificate verification failed")

    # store certificate & key in U-Boot environment
    fw_setenv('x509_key', client_key.replace('\n', '%'))
    fw_setenv('x509_crt', client_crt.replace('\n', '%'))

# commands

def initialize_gateway():
    """Initialize the gateway.

    The following steps are done:
    - get next free IPR ID
    - write IPR ID ('gatewayid') & serial number ('sgtin') to U-Boot
    - assemble with Linux module in IPR
    - reserve radio module & do assembly in IPR
    - write RM IPR ID ('radiomoduleid') and MAC address ('rmaddr') to U-Boot
    - initialize random secure_token; store in U-Boot & IPR
    - set up X.509 certificates
    - store HW revision ('gateway_hardware_revision') from batch configuration in U-Boot"""
    # check if we already have a gateway ID
    env = get_fw_printenv()
    if 'ipr_setup_done' in env.keys():
        sys.exit(0)

    # get batch ID from bootstrap server
    batch_id = get_batch_id()

    # get next free IPR ID
    cpms = CPMSClientBootstrapped()
    batch_item = cpms.get_next_free_item(batch_id)
    ipr_id = batch_item['ipr_id']
    print(f"GW ID: {ipr_id}")

    # associate Linux module with gateway ID in IPR
    linuxmodule_id = env['linuxmoduleid']
    print(f"LM ID: {linuxmodule_id}")
    # make sure ELRAD has the batch deployed; may take a moment for
    # the first item in the batch, but the call is synchronous so OK
    # to proceed immediately when done
    cpms.deploy_item_batch(linuxmodule_id)
    # disassemble from old gateway (usually not needed)
    try:
        cpms.disassemble_item_if_assembled(linuxmodule_id)
    except http.client.HTTPException: # parent batch may not be deployed
        sys.stderr.write("Warning: Disassembly of the Linux module failed.")
    # take item (puts it in read/write state)
    cpms.take_item(linuxmodule_id)
    # assemble Linux module with gateway
    cpms.assemble_items(ipr_id, "linux_module", linuxmodule_id)

    # store gateway ID & SGTIN in uboot env
    fw_setenv("gatewayid", ipr_id)
    sgtin = [i for i in batch_item['identifiers'] if i['name'] == "ipr:sgtin-96"][0]['value']
    fw_setenv("sgtin", sgtin)

    # assemble RM
    # note: to determine the RMs IPR ID, we read out the manufacturer
    # ID (aka producer serial number) from the radio module (which
    # still has the test firmware) via UART and use the CPMS /find
    # call.
    rm_test_fw = RMTestFW()
    manufacturer_id = rm_test_fw.get_producer_serial_number()
    rm_test_fw.close()
    print(f"RM serial#: {manufacturer_id}")
    rm_item = cpms.find_item(str(manufacturer_id))
    rm_id = rm_item['ipr_id']
    print(f"RM ID: {rm_id}")
    rm_address = [id['value'].replace('-', ':').lower()\
                  for id in rm_item['identifiers']\
                  if id['name'] == 'ipr:mac-address'][0]
    # assemble RM with gateway
    cpms.assemble_items(ipr_id, "radio_module", rm_id)

    # set U-Boot env variables
    fw_setenv("radiomoduleid", rm_id)
    fw_setenv("rmaddr", rm_address)

    # initialize secure token
    #
    # Q: why are you not using /dev/random? it is more secure!
    # A: /dev/random may block and /dev/urandom is sufficiently secure and will not block
    # See also: man 4 urandom, https://www.2uo.de/myths-about-urandom
    with open("/dev/urandom", 'rb') as devurandom:
        token = devurandom.read(RANDOM_TOKEN_LENGTH)
    token_base64 = base64.b64encode(token)
    token_base64 = token_base64.decode("ascii")
    fw_setenv('secure_token', token_base64)
    cpms.set_value(ipr_id, '/configuration/secure_token', token_base64)

    # fetch & store the X.509 certificate/key in U-Boot
    initialize_x509_credentials(ipr_id)

    # store the gateway revision number in uboot env
    fw_setenv("gateway_hardware_revision", batch_item['configuration']['hardwareVersion'])

    return ipr_id


def verify_ipr_data():
    """Verify U-Boot environment variables against IPR data.

    Note: this test is intentionally not part of the selftest, as it
    requires access to the CPMS and can not run outside
    manufacturing."""
    # TODO since we now have made selftest partially dependent on
    # bootstrapper, we should also move this (and skip it if no
    # bootstrap server present)
    gatewayid = fw_getenv("gatewayid")
    ethaddr = fw_getenv("ethaddr")
    wifiaddr = fw_getenv("wifiaddr")
    linuxmoduleid = fw_getenv("linuxmoduleid")
    linuxmodulehqvid = fw_getenv("linuxmodulehqvid")
    linuxmoduleunielecid = fw_getenv("linuxmoduleunielecid")

    cpms = CPMSClientBootstrapped()
    gateway = cpms.get_item(gatewayid)
    linuxmodule = cpms.get_item(linuxmoduleid)
    macs_from_ipr = \
        [id['value'].replace('-', ':') for id in linuxmodule['identifiers'] if id['name'] == 'ipr:mac-address']

    if linuxmoduleid != gateway['components']['linux_module']['ipr_id']:
        exit_error("IPR data / U-Boot environment mismatch for Linux Module ID")
    if ethaddr not in macs_from_ipr or wifiaddr not in macs_from_ipr:
        exit_error("IPR data / U-Boot environment mismatch for MAC addresses")
    if linuxmodulehqvid != linuxmodule['serial_number']['value']:
        exit_error("IPR data / U-Boot environment mismatch for Linux Module Husqvarna ID")
    # note: this data is now available in IPR, however it is stored
    # under UniElec_SN (which is actually correct; UnielecId whould be
    # the ID of the manufacturer, which is always 3518123467531395
    if 'UniElec_SN' in linuxmodule['configuration'] and \
       linuxmoduleunielecid != linuxmodule['configuration']['UniElec_SN']:
        exit_error("IPR data / U-Boot environment mismatch for Linux Module Unielec ID (UniElec_SN)")


# main

def main():
    """Main entry point."""
    parser = argparse.ArgumentParser()
    parser.add_argument("--init-gateway", action='store_true',
                        help="Initialize gateway with data from IPR.")
    parser.add_argument("--verify-ipr-data", action='store_true',
                        help="Verify MAC addresses & IDs from U-Boot environment against data in IPR.")
    args = parser.parse_args()

    if args.init_gateway:
        initialize_gateway()
    if args.verify_ipr_data:
        verify_ipr_data()

    sys.exit(0)


if __name__ == '__main__':
    main()
