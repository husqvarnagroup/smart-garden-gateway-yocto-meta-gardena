#!/usr/bin/python3
# coding: utf-8
#
# Author: Andreas Müller <andreas.mueller@husqvarnagroup.com>
# Date: 2019-07-08
# Version: 0.1
#
# Copyright (c) 2019 Gardena GmbH

"""
@package eoltest

Low-Cost Gateway EOL tests.

These tests are meant to run as root directly on the gateway during manufacturing.
"""

import argparse
import unittest
import sys
import datetime
import time
import uuid
import json
import subprocess
import re

from testing import ResultHandler
from util import get_systemd_status

bootstrap_server_present = True # pylint: disable=invalid-name


class EolTest(unittest.TestCase):
    """Collection of EOL tests."""

    def setUp(self):
        self._started_at = time.time()

    def tearDown(self):
        elapsed = time.time() - self._started_at
        self.storeValue("test_runtime", elapsed)

    def storeValue(self, key, value):
        """Wrapper function to send values for IPR to result handler."""
        if 'storeValue' in dir(self._outcome.result): # check if we have a ResultHandler with IPR integration
            self._outcome.result.storeValue(self._testMethodName, key, value)

    def test_001_systemd_status(self):
        """Test if systemd believes everything is running correctly."""
        self.assertEqual(get_systemd_status(), "running")

    def test_002_button_status(self):
        """Test that the button was never pressed.

        The idea behind this is that when assembling the cover, the
        button could accidentally end up in a constantly pressed
        state."""
        # Note: if the button is continuously pressed, we would see the following in dmesg:
        # [   12.638970] evbug: Event. Dev: input0, Type: 1, Code: 148, Value: 1
        # [   12.638978] evbug: Event. Dev: input0, Type: 0, Code: 0, Value: 0
        # (upon release, the first line with value 0 would be seen)
        data = subprocess.check_output("dmesg").decode("utf-8", errors="ignore")
        lines = [line[15:] for line in data.split("\n")
                 if line[15:] == "evbug: Event. Dev: input0, Type: 1, Code: 148, Value: 1"]
        self.assertTrue(len(lines) == 0, "button never pressed")

    def test_003_ppp0_ipv6_address(self):
        """Test that ppp0 has a valid IPv6 address.

        This test can be tripped by holding the RM in reset."""
        data = subprocess.check_output(["/sbin/ip", "-6", "address", "show", "dev", "ppp0"]).decode("ascii")
        self.assertTrue(re.search("inet6 fe80::[0-9a-f:]+/10 scope link", data))

    # def test_999_error(self):
    #     """ For testing ... """
    #     self.assertEqual(2, 4)



def main():
    """Main entry point."""

    parser = argparse.ArgumentParser()
    parser.add_argument('-?', '--unittest-help', dest='unittest_help', action='store_true',
                        help="Show unittest help.")
    parser.add_argument('-i', '--ipr-id', dest='ipr_id', action='store',
                        help="IPR ID for storing events.")
    options, args = parser.parse_known_args()
    args = [sys.argv[0]] + args

    if options.unittest_help:
        unittest.main(argv=[sys.argv[0], '-h'])

    if options.ipr_id: # regular EOL test
        from cpms_client import CPMSClientBootstrapped
        cpms = CPMSClientBootstrapped()
        runner = unittest.TextTestRunner(resultclass=ResultHandler)
        results = unittest.main(testRunner=runner, argv=args, exit=False)

        # store all values
        event = {
            "ipr_id":      options.ipr_id,
            "event_id":    str(uuid.uuid4()),
            # note: the CPMS site client has rather specific ideas
            # about the format of the timestamp and will just complain
            # about bad JSON if those aren't met
            "timestamp":   datetime.datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S.000+0000"),
            "description": "eol_test_values",
            "attachment": {
                "name":      "values.json",
                "mime_type": "application/json",
                "data":      json.dumps(results.result.ipr_data_values)
            }
        }
        cpms.add_event(options.ipr_id, event)

        # store all results
        event = {
            "ipr_id":      options.ipr_id,
            "event_id":    str(uuid.uuid4()),
            # note: the CPMS site client has rather specific ideas
            # about the format of the timestamp and will just complain
            # about bad JSON if those aren't met
            "timestamp":   datetime.datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S.000+0000"),
            "description": "eol_test_results",
            "attachment": {
                "name":      "results.json",
                "mime_type": "application/json",
                "data":      json.dumps(results.result.ipr_data_results)
            }
        }
        cpms.add_event(options.ipr_id, event)

        # store complete test result as value in configuration
        cpms.set_value(options.ipr_id,
                       "/configuration/eol_test_passed",
                       results.result.wasSuccessful())

        # exit depending on sucess
        sys.exit(not results.result.wasSuccessful())

    else: # manual EOL test run without IPR
        # run without our ResultHandler, not sending results to CPMS
        sys.stderr.write("WARNING: no IPR ID given - not sending results to CPMS\n")
        global bootstrap_server_present # pylint: disable=global-statement,invalid-name
        bootstrap_server_present = False
        unittest.main(argv=args)


if __name__ == '__main__':
    main()
