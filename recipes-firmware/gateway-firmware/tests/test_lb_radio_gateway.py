#!/usr/bin/env python3
"""Testing lb_radio_gateway shell client with mock commands.

The script (SUT) is run as separate process so that the implementation
can be replaced and the tests can be rerun.
"""

import os
import shutil
import subprocess
import tempfile
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
LB_RADIO_GATEWAY = os.path.join(HERE, "..", "files", "lb_radio_gateway.sh")
FAKE_COMMAND = os.path.join(HERE, "fake_command.sh")
GOLDEN_DIR = os.path.join(HERE, "golden")
COMMANDS = ["socat"]
UNIX_SOCKET = "/run/lemonbeatd/radiomodule_api"
API_VERSION = 0x01


class LbRadioGatewayTestCase(unittest.TestCase):
    def setUp(self):
        self.script = LB_RADIO_GATEWAY
        self.fake_dir = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, self.fake_dir)
        for command in COMMANDS:
            os.symlink(FAKE_COMMAND, os.path.join(self.fake_dir, command))

    def write(self, filename, data):
        with open(os.path.join(self.fake_dir, filename), "wb") as f:
            f.write(data)

    def set_output(self, name, data=b""):
        if isinstance(data, str):
            data = data.encode("utf-8")
        self.write(name + ".stdout", data)

    def set_reply(self, data=b"", result=0x00, length=None):
        """Write a socat reply: version, result, length and data bytes."""
        length = len(data) if length is None else length
        self.set_output("socat", bytes([API_VERSION, result, length]) + data)

    def set_exit(self, name, code, arg1=None):
        self.write(name + (f".{arg1}" if arg1 else "") + ".exit", str(code).encode())

    def run_tool(self, *args):
        env = dict(
            os.environ,
            PATH=self.fake_dir + ":" + os.environ["PATH"],
            FAKE_DIR=self.fake_dir,
            FAKE_RECORD_STDIN="1",
        )
        return subprocess.run(
            [self.script, *args], env=env, check=False,
            stdin=subprocess.DEVNULL, capture_output=True, encoding="utf-8",
        )

    def clear_calls(self):
        try:
            os.remove(os.path.join(self.fake_dir, "calls.log"))
        except FileNotFoundError:
            pass

    def calls(self):
        try:
            with open(os.path.join(self.fake_dir, "calls.log")) as f:
                return f.read().splitlines()
        except FileNotFoundError:
            return []

    def golden(self, filename):
        with open(os.path.join(GOLDEN_DIR, filename), encoding="utf-8") as f:
            return f.read()


class FakeCommandTest(LbRadioGatewayTestCase):
    def test_fake_records_stdin_as_hex(self):
        subprocess.run(
            ["sh", "-c", "printf '\\001\\015\\000' | socat -"],
            env=dict(os.environ, PATH=self.fake_dir + ":" + os.environ["PATH"],
                     FAKE_DIR=self.fake_dir, FAKE_RECORD_STDIN="1"),
            capture_output=True, check=False)
        self.assertIn("stdin 01 0d 00", self.calls())


class SkeletonTest(LbRadioGatewayTestCase):
    def test_help_commands_prints_golden_summary(self):
        result = self.run_tool("-c")
        self.assertEqual(result.stdout, self.golden("help_commands.txt"))
        self.assertEqual(result.returncode, 0)

    def test_list_commands_prints_golden_names(self):
        result = self.run_tool("-l")
        self.assertEqual(result.stdout, self.golden("list_commands.txt"))
        self.assertEqual(result.returncode, 0)

    def test_list_commands_works_without_paste(self):
        """The gateway has busybox without paste, so paste must not be used."""
        os.symlink(FAKE_COMMAND, os.path.join(self.fake_dir, "paste"))
        self.set_exit("paste", 127)
        result = self.run_tool("-l")
        self.assertEqual(result.stdout, self.golden("list_commands.txt"))
        self.assertEqual(result.returncode, 0)

    def test_describe_command_prints_golden_help(self):
        result = self.run_tool("-d", "get_stats")
        self.assertEqual(result.stdout, self.golden("describe_get_stats.txt"))
        self.assertEqual(result.returncode, 0)

    def test_usage_prints_usage_and_exits_zero(self):
        result = self.run_tool("-h")
        self.assertTrue(result.stdout.startswith("Usage:"))
        self.assertEqual(result.returncode, 0)

    def test_no_arguments_exits_zero_silently(self):
        result = self.run_tool()
        self.assertEqual((result.stdout, result.stderr, result.returncode), ("", "", 0))

    def test_interactive_option_is_rejected(self):
        result = self.run_tool("-i")
        self.assertEqual(result.returncode, 1)
        self.assertIn("lb_radio_gateway: unsupported option: -i", result.stderr)

    def test_tcp_and_verbose_options_are_rejected(self):
        for option in ("-t", "-v", "-I"):
            with self.subTest(option=option):
                result = self.run_tool(option, "get_app_version")
                self.assertEqual(result.returncode, 1)
                self.assertIn(f"lb_radio_gateway: unsupported option: {option}", result.stderr)

    def test_unknown_command_is_rejected(self):
        result = self.run_tool("bogus")
        self.assertEqual(result.returncode, 1)
        self.assertIn("lb_radio_gateway: unsupported command", result.stderr)


class UnixTransportTest(LbRadioGatewayTestCase):
    def run_app_version(self):
        return self.run_tool("-u", UNIX_SOCKET, "get_app_version")

    def test_default_transport_is_lemonbeatd_socket(self):
        self.set_reply(b"3.6.2")
        result = self.run_tool("get_app_version")
        self.assertIn(f"socat -T 5 - UNIX-CONNECT:{UNIX_SOCKET}", self.calls())
        self.assertEqual((result.stdout, result.returncode), ("3.6.2\n", 0))

    def test_app_version_sends_request_over_unix_socket(self):
        self.set_reply(b"3.6.2")
        result = self.run_app_version()
        self.assertIn(f"socat -T 5 - UNIX-CONNECT:{UNIX_SOCKET}", self.calls())
        self.assertIn("stdin 01 0d 00", self.calls())
        self.assertEqual((result.stdout, result.returncode), ("3.6.2\n", 0))

    def test_error_result_exits_one_with_name(self):
        self.set_reply(result=0x10)
        result = self.run_app_version()
        self.assertEqual(result.returncode, 1)
        self.assertEqual(
            result.stderr, "lb_radio_gateway: API returned error: 0x10 (INVALID_ARGUMENT)\n")

    def test_known_result_code_is_named(self):
        self.set_reply(result=0x11)
        self.assertIn("(NO_HW_SUPPORT)", self.run_app_version().stderr)

    def test_unknown_result_code_is_named_unknown(self):
        self.set_reply(result=0x7F)
        self.assertIn("(<unknown code>)", self.run_app_version().stderr)

    def test_failed_connection_exits_one(self):
        self.set_exit("socat", 1)
        result = self.run_app_version()
        self.assertEqual(result.returncode, 1)
        self.assertIn("connection failed", result.stderr)

    def test_short_reply_exits_one(self):
        self.set_reply(b"ab", length=5)
        result = self.run_app_version()
        self.assertEqual(result.returncode, 1)
        self.assertIn("short reply", result.stderr)

    def test_list_then_command_prints_list_and_runs_command(self):
        self.set_reply(b"3.6.2")
        result = self.run_tool("-l", "-u", UNIX_SOCKET, "get_app_version")
        self.assertEqual(result.stdout, self.golden("list_commands.txt") + "3.6.2\n")
        self.assertEqual(result.returncode, 0)


GETTER_CASES = [
    # command, reply data, expected output, request hex
    ("get_app_version", b"3.6.2", "3.6.2", "01 0d 00"),
    ("get_mac_address", bytes([0x8C, 0x05, 0x51, 0x00, 0x06, 0xE2]),
     "8c:05:51:00:06:e2", "01 06 00"),
    ("get_network_key", bytes(range(16)), "000102030405060708090a0b0c0d0e0f", "01 12 00"),
    ("get_antenna_diversity", b"\x01", "1", "01 09 00"),
    ("get_antenna_diversity_mode", b"\x00", "0", "01 07 00"),
    ("get_antenna_int_ext", b"\x01", "1", "01 0b 00"),
    ("get_lb_radio_driver_state", b"idle", "idle", "01 0f 00"),
    ("get_stack_usage", b"main 1234", "main 1234", "01 11 00"),
    ("get_uptime", bytes([0, 0, 0, 0, 0, 1, 0, 0]), "1099511627776", "01 10 00"),
    ("get_tx_mac_counter", bytes([0, 0, 0, 0, 0, 1, 0, 0]), "1099511627776", "01 0e 00"),
]


class GetterTest(LbRadioGatewayTestCase):
    def test_getters_print_reply_and_send_request(self):
        for command, data, expected, request in GETTER_CASES:
            with self.subTest(command=command):
                self.clear_calls()
                self.set_reply(data)
                result = self.run_tool("-u", UNIX_SOCKET, command)
                self.assertEqual((result.stdout, result.returncode), (expected + "\n", 0))
                self.assertIn(f"stdin {request}", self.calls())

    def test_antenna_getter_with_empty_reply_exits_one(self):
        self.set_reply(b"")
        result = self.run_tool("-u", UNIX_SOCKET, "get_antenna_diversity")
        self.assertEqual(result.returncode, 1)
        self.assertIn("empty reply", result.stderr)


class NoArgumentCommandTest(LbRadioGatewayTestCase):
    def test_commands_send_request_and_exit_zero(self):
        cases = [
            ("log_stack_usage", "01 e3 00"),
            ("reset_device_nonces", "01 e4 00"),
            ("restart_transceiver", "01 e7 00"),
            ("si4467_stop_tx", "01 e1 00"),
        ]
        for command, request in cases:
            with self.subTest(command=command):
                self.clear_calls()
                self.set_reply()
                result = self.run_tool("-u", UNIX_SOCKET, command)
                self.assertEqual((result.stdout, result.returncode), ("", 0))
                self.assertIn(f"stdin {request}", self.calls())

    def test_reboot_sends_request_and_reads_no_reply(self):
        result = self.run_tool("-u", UNIX_SOCKET, "reboot")
        self.assertEqual(result.returncode, 0)
        self.assertIn("stdin 01 e5 00", self.calls())


class NoArgumentCommandTest(LbRadioGatewayTestCase):
    def test_commands_send_request_and_exit_zero(self):
        cases = [
            ("log_stack_usage", "01 e3 00"),
            ("reset_device_nonces", "01 e4 00"),
            ("restart_transceiver", "01 e7 00"),
            ("si4467_stop_tx", "01 e1 00"),
        ]
        for command, request in cases:
            with self.subTest(command=command):
                self.clear_calls()
                self.set_reply()
                result = self.run_tool("-u", UNIX_SOCKET, command)
                self.assertEqual((result.stdout, result.returncode), ("", 0))
                self.assertIn(f"stdin {request}", self.calls())

    def test_reboot_sends_request_and_reads_no_reply(self):
        result = self.run_tool("-u", UNIX_SOCKET, "reboot")
        self.assertEqual(result.returncode, 0)
        self.assertIn("stdin 01 e5 00", self.calls())


SETTER_CASES = [
    # command and arguments, request stdin hex
    (["set_antenna_diversity", "1"], "01 0a 01 01"),
    (["set_antenna_diversity_mode", "0"], "01 08 01 00"),
    (["set_antenna_int_ext", "1"], "01 0c 01 01"),
    (["si4467_start_cw", "5"], "01 e0 01 05"),
    (["set_si4467_sdn_pin", "1"], "01 e8 01 01"),
    (["set_tx_mac_counter", "1"], "01 05 08 01 00 00 00 00 00 00 00"),
    (["set_tx_mac_counter", "9223372036854775807"], "01 05 08 ff ff ff ff ff ff ff 7f"),
    (["set_mac_address", "8c:05:51:00:06:e2"], "01 02 06 8c 05 51 00 06 e2"),
    (["reset_device_nonce", "8c:05:51:00:06:e2"], "01 04 06 8c 05 51 00 06 e2"),
    (["set_network_key", "00112233445566778899aabbccddeeff"],
     "01 01 10 00 11 22 33 44 55 66 77 88 99 aa bb cc dd ee ff"),
    (["log", "go"], "01 e9 02 67 6f"),
]

INVALID_SETTER_ARGUMENTS = [
    ["set_antenna_diversity", "abc"],
    ["set_antenna_diversity", "256"],
    ["set_tx_mac_counter", "-1"],
    ["set_tx_mac_counter", "9223372036854775808"],
    ["set_mac_address", "8c:05:51:00:06"],
    ["set_mac_address", "zz:05:51:00:06:e2"],
    ["set_network_key", "0011223344556677889900aabbccdd"],
    ["set_antenna_diversity"],
    ["log", "a" * 256],
]


class SetterTest(LbRadioGatewayTestCase):
    def test_setters_send_request_and_exit_zero(self):
        for arguments, request in SETTER_CASES:
            with self.subTest(arguments=arguments):
                self.clear_calls()
                self.set_reply()
                result = self.run_tool("-u", UNIX_SOCKET, *arguments)
                self.assertEqual((result.stdout, result.returncode), ("", 0))
                self.assertIn(f"stdin {request}", self.calls())

    def test_invalid_arguments_exit_one_without_sending(self):
        for arguments in INVALID_SETTER_ARGUMENTS:
            with self.subTest(arguments=arguments):
                self.clear_calls()
                result = self.run_tool("-u", UNIX_SOCKET, *arguments)
                self.assertEqual(result.returncode, 1)
                self.assertIn("lb_radio_gateway:", result.stderr)
                self.assertFalse(any(c.startswith("socat") for c in self.calls()))


class GpioAndWakeupTest(LbRadioGatewayTestCase):
    def test_gpio_getter_prints_state(self):
        self.set_reply(b"\x01")
        result = self.run_tool("-u", UNIX_SOCKET, "get_si4467_gpio", "0")
        self.assertEqual((result.stdout, result.returncode), ("1\n", 0))
        self.assertIn("stdin 01 e2 01 00", self.calls())

    def test_gpio_getter_with_two_bytes_exits_one(self):
        self.set_reply(b"\x01\x00")
        result = self.run_tool("-u", UNIX_SOCKET, "get_si4467_gpio", "0")
        self.assertEqual(result.returncode, 1)

    def test_wakeup_without_acknowledgement_omits_byte(self):
        self.set_reply()
        result = self.run_tool("-u", UNIX_SOCKET, "wakeup", "8c:05:51:00:06:e2", "1000", "3")
        self.assertEqual(result.returncode, 0)
        self.assertIn("stdin 01 03 0b e8 03 00 00 8c 05 51 00 06 e2 03", self.calls())

    def test_wakeup_acknowledgement_values(self):
        cases = [
            ("yes", "01"), ("1", "01"), ("y", "01"), ("t", "01"), ("TRUE", "01"),
            ("no", "00"), ("maybe", "00"),
        ]
        for text, ack in cases:
            with self.subTest(text=text):
                self.clear_calls()
                self.set_reply()
                result = self.run_tool(
                    "-u", UNIX_SOCKET, "wakeup", "8c:05:51:00:06:e2", "1000", "3", text)
                self.assertEqual(result.returncode, 0)
                self.assertIn(f"stdin 01 03 0c e8 03 00 00 8c 05 51 00 06 e2 03 {ack}",
                              self.calls())

    def test_wakeup_rejects_out_of_range_duration(self):
        result = self.run_tool(
            "-u", UNIX_SOCKET, "wakeup", "8c:05:51:00:06:e2", "4294967296", "3")
        self.assertEqual(result.returncode, 1)
        self.assertFalse(any(c.startswith("socat") for c in self.calls()))


RSSI_ACK_AVERAGE_MINUS_ONE = bytes([0x00, 0x04, 0xFF, 0xFF, 0xFF, 0xFF])
RX_COUNT_FIVE = bytes([0x32, 0x04, 0x05, 0x00, 0x00, 0x00])


class GetStatsTest(LbRadioGatewayTestCase):
    def run_stats(self, data, *mode):
        self.set_reply(data)
        return self.run_tool("-u", UNIX_SOCKET, "get_stats", *mode)

    def test_human_readable_is_default(self):
        result = self.run_stats(RSSI_ACK_AVERAGE_MINUS_ONE + RX_COUNT_FIVE)
        self.assertEqual(
            (result.stdout, result.returncode),
            ("MAC ACK RSSI (average) [dBm]: -1\nRX total: 5\n", 0))
        self.assertIn("stdin 01 e6 00", self.calls())

    def test_human_readable_mode_h(self):
        result = self.run_stats(RX_COUNT_FIVE, "H")
        self.assertEqual(result.stdout, "RX total: 5\n")

    def test_unset_rssi_prints_unset(self):
        result = self.run_stats(bytes([0x00, 0x04, 0xFF, 0x00, 0x00, 0x00]), "H")
        self.assertEqual(result.stdout, "MAC ACK RSSI (average) [dBm]: unset\n")

    def test_machine_readable_mode_m(self):
        result = self.run_stats(RSSI_ACK_AVERAGE_MINUS_ONE + RX_COUNT_FIVE, "M")
        self.assertEqual(result.stdout, (
            "{\n"
            '  "TAG_RSSI_ACK_AVERAGE": -1,\n'
            '  "TAG_RX_COUNT": 5\n'
            "}\n"))

    def test_unset_rssi_is_json_string_in_mode_m(self):
        result = self.run_stats(bytes([0x00, 0x04, 0xFF, 0x00, 0x00, 0x00]), "M")
        self.assertIn('"TAG_RSSI_ACK_AVERAGE": "unset"', result.stdout)

    def test_empty_reply_prints_empty_object_or_empty_line(self):
        self.assertEqual(self.run_stats(b"", "M").stdout, "{}\n")
        self.assertEqual(self.run_stats(b"", "H").stdout, "\n")

    def test_unknown_tags_keep_first_position_and_last_value(self):
        data = bytes([0x63, 0x04, 0x01, 0x00, 0x00, 0x00, 0x62, 0x04, 0x02, 0x00, 0x00, 0x00])
        result = self.run_stats(data, "M")
        self.assertEqual(result.stdout, '{\n  "unknown statistics tag": 2\n}\n')

    def test_invalid_mode_exits_one_without_request(self):
        result = self.run_stats(RX_COUNT_FIVE, "X")
        self.assertEqual(result.returncode, 1)
        self.assertFalse(any(c.startswith("socat") for c in self.calls()))

    def test_value_length_other_than_four_exits_one(self):
        result = self.run_stats(bytes([0x32, 0x03, 0x05, 0x00, 0x00]), "H")
        self.assertEqual(result.returncode, 1)



if __name__ == "__main__":
    unittest.main()
