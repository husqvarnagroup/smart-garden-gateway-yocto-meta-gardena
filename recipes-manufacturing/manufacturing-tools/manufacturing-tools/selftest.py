#!/usr/bin/python3
# coding: utf-8
#
# Author: Andreas Müller <andreas.mueller@husqvarnagroup.com>
# Date: 2018-07-30
# Version: 0.1
#
# Copyright (c) 2018 Gardena GmbH

"""
@package selftest

Low-Cost Gateway self tests.

These tests are meant to run as root directly on the gateway during manufacturing.
"""

import argparse
import datetime
import hashlib
import json
import re
import subprocess
import sys
import time
import unittest
import uuid
import zlib

from bootstrap import bootstrap_get_batch
from radio_module_test import RMTestFW, RMTestFWMessage
from testing import ResultHandler
from util import fw_getenv, manufacturing_event_timestamp

bootstrap_server_present = True  # pylint: disable=invalid-name


def get_batch_config_json():
    """Fetch batch ID."""
    body = bootstrap_get_batch()

    return json.loads(body.decode("ascii"))


def md5(fname):
    """Calculate MD5 sum for given file."""
    hash_md5 = hashlib.md5()
    with open(fname, "rb") as infile:
        for chunk in iter(lambda: infile.read(4096), b""):
            hash_md5.update(chunk)
    return hash_md5.hexdigest()


class ChronyTacking:
    """
    chronyc CSV output:
    chronyc -c tacking
    Example for synced state:
    D8EF2300,216.239.35.0,2,1724225367.720284720,-0.000036268,-0.000005936,0.000214715,-0.946,0.001,0.081,0.013953905,0.000123833,261.0,Normal
    Example for not synced:
    7F7F0101,,10,1724743035.534557617,0.000000000,0.000000000,0.000000000,-0.818,0.000,0.000,0.000000000,0.000000000,0.0,Normal
    """

    def __init__(
        self, reference_id: str, address: str, stratum: str, ref_time: str, *args
    ):
        self.reference_id = reference_id
        self.address = address
        self.stratum = stratum
        self.ref_time = ref_time
        self.args = args

    @classmethod
    def parse(cls, csv_string: str) -> "ChronyTacking":
        return cls(*csv_string.split(","))


class SelfTest(unittest.TestCase):
    """Collection of self tests."""

    def setUp(self):
        self._started_at = time.time()

    def tearDown(self):
        elapsed = time.time() - self._started_at
        self.storeValue("test_runtime", elapsed)

    def storeValue(self, key, value):
        """Wrapper function to send values for IPR to result handler."""
        if "storeValue" in dir(
            self._outcome.result
        ):  # check if we have a ResultHandler with IPR integration
            self._outcome.result.storeValue(self._testMethodName, key, value)

    def test_001_time_not_1970(self):
        """Test for reasonable time (not 1970)."""
        # Note: I only want to test that we're not stuck in 1970 here.
        # Unless there is a good reason, please don't replace this
        # with something that
        # - is more complicated
        # - has a chance to fail if there is a small time drift
        self.assertTrue(datetime.datetime.now().year >= 2018)

    def test_002_mfi_chip(self):
        """MFi chip I²C connection test.

        Note: see section 62 in Accessory Interface Specification for parameters, etc.
        """
        # read one byte to wake up chip
        try:
            subprocess.check_call(
                ["i2ctransfer", "-y", "0", "r1@0x10"],
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL,
            )
            self.fail("Chip is expected to sleep and i2ctransfer to fail")
        except subprocess.CalledProcessError as error:
            self.assertEqual(error.returncode, 1)

        # set read adress: 0
        retval = subprocess.check_call(["i2ctransfer", "-y", "0", "w1@0x10", "0x0"])
        self.assertEqual(retval, 0)
        # read 8 bytes (see section 62.5.7 Registers):
        # - Device Version (expected: 0x07)
        # - Authentication Revision (expected: 0x00)
        # - Authentication Protocol Major Version (expected: 0x03)
        # - Authentication Protocol Minor Version (expected: 0x00)
        # - Device ID (expected: 0x00 0x00 0x03 0x00)
        data = subprocess.check_output(["i2ctransfer", "-y", "0", "r8@0x10"])
        hexdigits = data.strip().decode("ascii").split(" ")
        # Note: 2nd byte should be zero; check back with Apple?
        self.assertEqual(
            hexdigits, ["0x07", "0x01", "0x03", "0x00", "0x00", "0x00", "0x03", "0x00"]
        )

    # note: test_003 was to check for systemd status
    # (is-system-running); however, since selftest is part of the
    # startup itself, is it will be not (yet) "running" but e.g.
    # "starting; this test is however now part of the EOL test suite
    # and no longer needed here.

    def test_004_spi_nand_badblocks(self):
        """SPI-NAND bad block count test.
        According to the GD5F1GQ4xBxIG datasheet version 2.7 page 35, the "Minimum number of valid
        blocks" is 1004 out of 1024 during the endurance life of the product.
        Since UBI switches to read-only mode when all reserved PEBs are used, and already starts to
        emit warnings when less than two are left, we want UBI to reserve 22 blocks.
        """

        output = subprocess.check_output(["ubinfo", "-d", "0"]).decode("ascii")
        bad_blocks_count = int(
            re.search("Count of bad physical eraseblocks: +(.+?)\n", output).group(1)
        )
        reserved_blocks_count = int(
            re.search("Count of reserved physical eraseblocks: +(.+?)\n", output).group(
                1
            )
        )

        self.storeValue("bad_blocks_count", bad_blocks_count)
        self.storeValue("reserved_blocks_count", reserved_blocks_count)

        # Note: Feel free to adapt this test if it turns out to be too restrictive.
        self.assertLess(
            bad_blocks_count,
            reserved_blocks_count,
            "Less than 50% of reserved blocks can be bad.",
        )

        self.assertEqual(
            bad_blocks_count + reserved_blocks_count,
            22,
            "Reserve 22 PEBs for bad PEBs handling",
        )

    def test_005_ram_size(self):
        """Test RAM size.
        According to Denx/Stefan Roese, this could help detect a shortcut between RAM address lines."""
        data = subprocess.check_output("dmesg").decode("utf8")
        meminfo = [line[23:] for line in data.split("\n") if line[15:22] == "Memory:"][
            0
        ]
        size = int(meminfo.split(" ")[0].split("/")[1].replace("K", ""))
        self.assertTrue(
            112640 <= size <= 131072, "RAM size between 112'640 and 131'072 kilobytes."
        )

    def test_006_u_boot_variables(self):
        """Test for existence of vital U-Boot variables."""
        for variable in [
            # "fct_finalized", # selftest runs before FCT
            "board_name",
            "gateway_hardware_revision",
            "flashing_done",
            "hk_setup_done",
            "linuxmodulehqvid",
            "linuxmoduleid",
            "linuxmoduleunielecid",
            "ubi_setup_done",
            "secure_token",
            "rmaddr",
        ]:
            value = fw_getenv(variable)
            self.assertTrue(value is not None and len(value) > 0, msg=variable)

        subprocess.check_call(
            ["gardena-provision", "-b", "/tmp/prov-test.json"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )

    def test_007_factory_data(self):
        """Test consistency of U-Boot and factory data."""
        mtdfile = "/dev/mtd3"
        fdata_vars = {
            "wifiaddr": [0x4, 0x6],
            "ethaddr": [0x28, 0x6],
            "crc": [0x140, 0x4],
            "magic_value": [0x144, 0x4],
            "layout_version": [0x148, 0x4],
            "linuxmoduleid": [0x14C, 0x24],
            "linuxmodulehqvid": [0x170, 0x24],
            "linuxmoduleunielecid": [0x194, 0x24],
        }
        fdata_end = 0x1F0

        uboot_vars = (
            "wifiaddr",
            "ethaddr",
            "linuxmoduleid",
            "linuxmodulehqvid",
            "linuxmoduleunielecid",
        )
        static_vars = {
            "magic_value": b"\xbe\xba\xfe\xca",
            "layout_version": b"\x01\x00\x00\x00",
        }

        # fetch factory data
        fdata = {}
        with open(mtdfile, "rb") as fdata_file:
            for key in fdata_vars:
                fdata_file.seek(fdata_vars[key][0])
                fdata[key] = fdata_file.read(fdata_vars[key][1])

        # reformat factory data
        for key in ["wifiaddr", "ethaddr"]:
            fdata[key] = ":".join(["{:02x}".format(b) for b in fdata[key]])
        for key in ["linuxmoduleid", "linuxmodulehqvid", "linuxmoduleunielecid"]:
            fdata[key] = fdata[key].decode("ascii").strip("\x00")

        # calculate CRC for verification
        with open(mtdfile, "rb") as fdata_file:
            fdata_file.seek(fdata_vars["crc"][0] + fdata_vars["crc"][1])
            crc_data = fdata_file.read(
                fdata_end - (fdata_vars["crc"][0] + fdata_vars["crc"][1])
            )
        crc = zlib.crc32(crc_data)
        crc_bytes = crc.to_bytes(4, byteorder="little")

        # check uboot variables
        for var in uboot_vars:
            self.assertEqual(
                fdata[var],
                fw_getenv(var),
                "Factory data / U-Boot variable mismatch: %s" % var,
            )

        # check static variables
        for var in static_vars:
            self.assertEqual(
                fdata[var],
                static_vars[var],
                "Factory data / static variable mismatch: %s" % key,
            )

        # check CRC
        self.assertEqual(fdata["crc"], crc_bytes, "Factory data CRC mismatch")

    def test_008_mac_addresses(self):
        """Test consistency of MAC addresses with U-Boot variables.

        Note: Linux currently gets the MAC addresses from the factory
        data, but since we already verified U-Boot / Factory data
        consistency in the previous test, this should be OK."""
        ip_cmd = ["ip", "link", "show", "dev"]
        ethernet_mac = (
            subprocess.check_output(ip_cmd + ["eth0"])
            .decode("ascii")
            .split("\n")[1]
            .strip(" ")
            .split(" ")[1]
        )
        wlan_mac = (
            subprocess.check_output(ip_cmd + ["wlan0"])
            .decode("ascii")
            .split("\n")[1]
            .strip(" ")
            .split(" ")[1]
        )
        self.assertEqual(
            ethernet_mac, fw_getenv("ethaddr"), "Ethernet MAC address mismatch"
        )
        self.assertEqual(wlan_mac, fw_getenv("wifiaddr"), "WLAN MAC address mismatch")

    def test_009_wifi_calibration_data(self):
        """Test WiFi calibration data in factory data."""
        mtdfile = "/dev/mtd3"
        # Note: the names used below are the same as in the
        # MT7628_EEPROM_Guideline_v2_00. although these may not be the
        # most sensible names, consistency goes before sensibility
        # here. The list doesn't contain all parameters, but rather a
        # selection of those most likely to be useful.
        factory_data_vars = {
            "CHIP_ID": [0x01, 2],
            "EEPROM_REV": [0x02, 2],
            "NIC_CONFG_0": [0x34, 2],
            "NIC_CONFG_1": [0x36, 2],
            "NIC_CONFG_2": [0x42, 2],
            "COUNTRY_REG": [0x39, 1],
            "RSSI_OFST": [0x46, 2],
            "TX_POWER_DELTA": [0x50, 1],
            "TEMP_SEN_CAL": [0x55, 1],
            "TX0_PA_TSSI_LSB": [0x56, 1],
            "TX0_PA_TSSI_MSB": [0x57, 1],
            "TX0_POWER": [0x58, 1],
            "TX0_PWR_OFST_L": [0x59, 1],
            "TX0_PWR_OFST_M": [0x5A, 1],
            "TX0_PWR_OFST_H": [0x5B, 1],
            "TX1_PA_TSSI_LSB": [0x5C, 1],
            "TX1_PA_TSSI_MSB": [0x5D, 1],
            "TX1_POWER": [0x5E, 1],
            "TX1_PWR_OFST_L": [0x5F, 1],
            "TX1_PWR_OFST_M": [0x60, 1],
            "TX1_PWR_OFST_H": [0x61, 1],
            "TX_PWR_CCK0": [0xA0, 1],
            "TX_PWR_CCK1": [0xA1, 1],
            "TX_PWR_OFDM_0": [0xA2, 1],
            "TX_PWR_OFDM_1": [0xA3, 1],
            "TX_PWR_OFDM_2": [0xA4, 1],
            "TX_PWR_OFDM_3": [0xA5, 1],
            "TX_PWR_OFDM_4": [0xA6, 1],
            "TX_PWR_HT_MCS0": [0xA7, 1],
            "TX_PWR_HT_MCS1": [0xA8, 1],
            "TX_PWR_HT_MCS2": [0xA9, 1],
            "TX_PWR_HT_MCS3": [0xAA, 1],
            "TX_PWR_HT_MCS4": [0xAB, 1],
            "TX_PWR_HT_MCS5": [0xAC, 1],
            "TX_PWR_HT_MCS6": [0xAD, 1],
            "STEP_NUM_NEG_7": [0xC6, 1],
            "STEP_NUM_NEG_6": [0xC7, 1],
            "STEP_NUM_NEG_5": [0xC8, 1],
            "STEP_NUM_NEG_4": [0xC9, 1],
            "STEP_NUM_NEG_3": [0xCA, 1],
            "STEP_NUM_NEG_2": [0xCB, 1],
            "STEP_NUM_NEG_1": [0xCC, 1],
            "STEP_NUM_NEG_0": [0xCD, 1],
            "STEP_NUM_REF": [0xCE, 1],
            "STEP_NUM_TEMP": [0xCF, 1],
            "STEP_NUM_POS_1": [0xD0, 1],
            "STEP_NUM_POS_2": [0xD1, 1],
            "STEP_NUM_POS_3": [0xD2, 1],
            "STEP_NUM_POS_4": [0xD3, 1],
            "STEP_NUM_POS_5": [0xD4, 1],
            "STEP_NUM_POS_6": [0xD5, 1],
            "STEP_NUM_POS_7": [0xD6, 1],
            "XTAL_CAL": [0xF4, 1],
            "XTAL_TRIM2": [0xF5, 1],
            "XTAL_TRIM3": [0xF6, 1],
        }

        # fetch factory data
        fdata = {}
        with open(mtdfile, "rb") as fdata_file:
            for key in factory_data_vars:
                fdata_file.seek(factory_data_vars[key][0])
                fdata[key] = fdata_file.read(factory_data_vars[key][1]).hex()

        # save data as event for storing to manufacturing events
        self.storeValue("factory_data", fdata)

        # check data
        #
        # note: it would be nice to check a little bit more here, but
        # for that we need to analyze/understand the data we get from
        # MP first.
        self.assertEqual(fdata["EEPROM_REV"], "0002")

    def test_010_kernel(self):
        """Verify kernel checksum."""
        if not bootstrap_server_present:
            self.skipTest("requires checksum from bootstrap server")
        bootslot = int(fw_getenv("bootslot"))
        md5sum = md5("/dev/ubi0_%s" % str(bootslot + 2))
        self.assertEqual(
            md5sum, get_batch_config_json()["md5sums"]["fitImage-gardena-sg-mt7688.bin"]
        )

    def test_011_rootfs(self):
        """Verify rootfs checksum."""
        if not bootstrap_server_present:
            self.skipTest("requires checksum from bootstrap server")
        bootslot = fw_getenv("bootslot")
        md5sum = md5("/dev/ubi0_%s" % bootslot)
        self.assertEqual(
            md5sum,
            get_batch_config_json()["md5sums"][
                "gardena-image-factory-gardena-sg-mt7688.squashfs-xz"
            ],
        )

    def test_012_x509_client_key(self):
        """Check the plausibility of the stored X.509 key"""
        if x509_key := fw_getenv("x509_key"):
            self.assertTrue(x509_key)
            self.assertTrue("-----BEGIN EC PRIVATE KEY-----%" in x509_key)
            self.assertTrue("%-----END EC PRIVATE KEY-----" in x509_key)
        else:
            x509_key = fw_getenv("conf_openvpn_key")
            self.assertTrue(x509_key)
            self.assertTrue("-----BEGIN PRIVATE KEY-----%" in x509_key)
            self.assertTrue("%-----END PRIVATE KEY-----" in x509_key)

    def test_013_x509_client_certificate(self):
        """Check the plausibility of the stored X.509 certificate"""
        x509_crt = fw_getenv("x509_crt")
        if x509_crt is None:
            x509_crt = fw_getenv("conf_openvpn_crt")
        self.assertTrue(x509_crt)
        self.assertTrue("-----BEGIN CERTIFICATE-----%" in x509_crt)
        self.assertTrue("%-----END CERTIFICATE-----" in x509_crt)

    def test_014_check_sysfs_nand_stats(self):
        """Read out MTD info for NAND flash from sysfs (mostly for statistics)."""
        path = "/sys/class/mtd/mtd5"
        keys = [  # basically everything, though some values are currently not reported correctly
            "bad_blocks",
            "bbt_blocks",
            "bitflip_threshold",
            "corrected_bits",
            "ecc_failures",
            "ecc_step_size",
            "ecc_strength",
            "erasesize",
            "flags",
            "name",
            "numeraseregions",
            "offset",
            "oobavail",
            "oobsize",
            "size",
            "subpagesize",
            "type",
            "writesize",
        ]
        values = {}
        # get data
        for key in keys:
            with open(path + "/" + key, "r") as sysfsfile:
                values[key] = sysfsfile.read().strip()
                self.storeValue(key, values[key])
        # checks
        self.assertLessEqual(
            int(values["bad_blocks"]), 10
        )  # slightly redundant with test_004_spi_nand_badblocks

    def test_015_check_gw_config_interface_certs(self):
        """Check gateway config interface certificates."""
        cert_filename = "/etc/gateway-config-interface/cert.pem"
        key_filename = "/etc/gateway-config-interface/key.pem"
        with open(cert_filename, "r") as certfile:
            cert = certfile.read()
            self.assertTrue(len(cert) > 500)
            self.assertTrue("-----BEGIN CERTIFICATE-----" in cert)
            self.assertTrue("-----END CERTIFICATE-----" in cert)
        with open(key_filename, "r") as keyfile:
            key = keyfile.read()
            self.assertTrue(len(key) > 500)
            self.assertTrue("-----BEGIN PRIVATE KEY-----" in key)
            self.assertTrue("-----END PRIVATE KEY-----" in key)

    def test_016_verify_ntp_sync(self):
        """Check NTP clock synchronization."""
        status = {
            items[0].strip(): ": ".join(items[1:]).strip()
            for items in [
                line.split(": ")
                for line in subprocess.check_output(["timedatectl", "status"])
                .decode("ascii")
                .split("\n")
                if len(line) > 0
            ]
        }
        self.assertTrue(status["System clock synchronized"] == "yes")
        # Ensure systemd-timesyncd is not responsible for NTP
        self.assertTrue(status["NTP service"] == "n/a")
        # chronyc CSV output: chronyc -c tacking
        # Example for synced state:
        # D8EF2300,216.239.35.0,2,1724225367.720284720,-0.000036268,-0.000005936,0.000214715,-0.946,0.001,0.081,0.013953905,0.000123833,261.0,Normal
        # Example for not synced state:
        # 7F7F0101,,10,1724743035.534557617,0.000000000,0.000000000,0.000000000,-0.818,0.000,0.000,0.000000000,0.000000000,0.0,Normal
        chrony_tracking = subprocess.check_output(["chronyc", "-c", "tracking"]).decode(
            "ascii"
        )
        reference_id, address, stratum, *_ = chrony_tracking.split(",")
        self.assertNotEqual(reference_id, "7F7F0101")
        self.assertNotEqual(address, "")
        self.assertTrue(int(stratum) <= 5)

    def test_017_radio_module(self):
        """Test radio module using production test firmware."""
        if not bootstrap_server_present:
            self.skipTest(
                "requires radio module with production test firmware and no ppp running"
            )
        rm_test_fw = RMTestFW()
        rm_status = rm_test_fw.get_rm_status()
        self.assertTrue(rm_status == RMTestFWMessage.CommandResults.CMD_OK)
        rm_fw_version = rm_test_fw.get_rm_sw_information()
        self.assertTrue(rm_fw_version == "0.5.7")
        self.storeValue("radio_module_firmware_version", rm_fw_version)
        producer_serial_number = rm_test_fw.get_producer_serial_number()
        self.storeValue("radio_module_producer_serial_number", producer_serial_number)
        production_information = rm_test_fw.get_production_information()
        self.assertTrue(
            production_information["pcba_part_number"]
            == RMTestFW.PCBAPartNumber.RADIO_MODULE_TYPE_1
        )
        self.storeValue(
            "radio_module_pcba_part_number",
            production_information["pcba_part_number"].value,
        )
        self.storeValue(
            "radio_module_pcba_revision", production_information["pcba_revision"]
        )
        self.storeValue(
            "radio_module_pcba_production_time",
            production_information["pcba_production_time"],
        )
        self.storeValue(
            "radio_module_serial_number", production_information["pcba_serial_number"]
        )
        rm_temperature = rm_test_fw.get_temperature()
        self.assertTrue(10 <= rm_temperature <= 45)
        self.storeValue("radio_module_temperature", rm_temperature)
        rm_test_fw.close()

    # def test_999_error(self):
    #     """ For testing ... """
    #     self.assertEqual(2, 4)


def main():
    """Main entry point."""

    parser = argparse.ArgumentParser()
    parser.add_argument(
        "-?",
        "--unittest-help",
        dest="unittest_help",
        action="store_true",
        help="Show unittest help.",
    )
    parser.add_argument(
        "-i",
        "--ipr-id",
        dest="ipr_id",
        action="store",
        help="IPR ID for storing events.",
    )
    options, args = parser.parse_known_args()
    args = [sys.argv[0]] + args

    if options.unittest_help:
        unittest.main(argv=[sys.argv[0], "-h"])

    if options.ipr_id:
        from cpms_client import CPMSClientBootstrapped

        cpms = CPMSClientBootstrapped()
        # note: cleaner method might be to do something here like:
        #
        # suite = unittest.TestSuite()
        # suite.addTest(unittest.makeSuite(SelfTest))
        # runner = unittest.TextTestRunner(resultclass=ResultHandler)
        # results = runner.run(suite)
        #
        # however, the method below allows us to retain the existing command line interface
        runner = unittest.TextTestRunner(resultclass=ResultHandler)
        results = unittest.main(testRunner=runner, argv=args, exit=False)

        # store all values
        event = {
            "ipr_id": options.ipr_id,
            "event_id": str(uuid.uuid4()),
            "timestamp": manufacturing_event_timestamp(),
            "description": "self_test_values",
            "attachment": {
                "name": "values.json",
                "mime_type": "application/json",
                "data": json.dumps(results.result.ipr_data_values),
            },
        }
        cpms.add_event(options.ipr_id, event)

        # store all results
        event = {
            "ipr_id": options.ipr_id,
            "event_id": str(uuid.uuid4()),
            "timestamp": manufacturing_event_timestamp(),
            "description": "self_test_results",
            "attachment": {
                "name": "results.json",
                "mime_type": "application/json",
                "data": json.dumps(results.result.ipr_data_results),
            },
        }
        cpms.add_event(options.ipr_id, event)

        # store complete test result as value in configuration
        cpms.set_value(
            options.ipr_id,
            "/configuration/self_test_passed",
            results.result.wasSuccessful(),
        )

        # exit depending on sucess
        sys.exit(not results.result.wasSuccessful())
    else:  # run with ResultHandler (default)
        # run without our ResultHandler, not sending results to CPMS
        sys.stderr.write("WARNING: no IPR ID given - not sending results to CPMS\n")
        global bootstrap_server_present  # pylint: disable=global-statement,invalid-name
        bootstrap_server_present = False
        unittest.main(argv=args)


if __name__ == "__main__":
    main()

# TODO
# - Check that the calibration data (mainly: WiFi) are not missing and in correct range
