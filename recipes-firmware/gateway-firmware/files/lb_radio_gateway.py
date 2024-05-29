#!/usr/bin/env python3
#
# SPDX-FileCopyrightText: Copyright (c) 2023 GARDENA GmbH
# SPDX-License-Identifier: LicenseRef-GARDENA

"""Functionality to interface with LB radio gateway via TCP API."""

import argparse
import json
import socket
import subprocess
import sys
from contextlib import contextmanager
from typing import Any

API_VERSION = 1
DEFAULT_INTERFACE = "ppp0"
DEFAULT_PORT = 8888


class LBRadioGatewayAPIException(Exception):
    """Exception class for error when using TCP API."""


class Command:
    """Class for outgoing API command codes."""

    SET_NETWORK_KEY = b"\x01"
    SET_MAC_ADDRESS = b"\x02"
    WAKEUP_DEVICE = b"\x03"
    RESET_DEVICE_NONCE = b"\x04"
    SET_TX_MAC_COUNTER = b"\x05"
    GET_MAC_ADDRESS = b"\x06"
    GET_ANTENNA_DIVERSITY_MODE = b"\x07"
    SET_ANTENNA_DIVERSITY_MODE = b"\x08"
    GET_ANTENNA_DIVERSITY = b"\x09"
    SET_ANTENNA_DIVERSITY = b"\x0a"
    GET_ANTENNA_INT_EXT = b"\x0b"
    SET_ANTENNA_INT_EXT = b"\x0c"
    GET_APP_VERSION = b"\x0d"
    GET_TX_MAC_COUNTER = b"\x0e"
    GET_LB_RADIO_DRIVER_STATE = b"\x0f"
    GET_UPTIME = b"\x10"
    GET_STACK_USAGE = b"\x11"
    SI4467_START_CW = b"\xe0"
    SI4467_STOP_TX = b"\xe1"
    GET_SI4467_GPIO = b"\xe2"
    LOG_STACK_USAGE = b"\xe3"


class Result:
    """Class for incoming API result codes."""

    OKAY = 0x00
    IFACE_NOT_FOUND = 0x01
    CANT_SET_KEY = 0x02
    CANT_SET_MAC_ADDRESS = 0x03
    UNSUPPORTED_COMMAND = 0x04
    LOCK_TIMEOUT = 0x05
    WAKEUP_FAILED = 0x06
    CANT_SAVE = 0x07
    CANT_SET_TX_MAC_COUNTER = 0x08
    CANT_RESET_TX_MAC_COUNTER = 0x09
    TX_MAC_COUNTER_WOULD_DECREASE = 0x0A
    CANT_GET_KEY = 0x0B
    CANT_RESET_TXMACCTR = 0x0C
    CANT_GET_MAC_ADDRESS = 0x0D
    SI4467_COMMAND_FAILED = 0x0E
    SI4467_CONTEXT_NOT_FOUND = 0x0F
    INVALID_ARGUMENT = 0x10
    NO_HW_SUPPORT = 0x11
    CANT_GET_TX_MAC_COUNTER = 0x12
    MISSING_ARGUMENT = 0x13
    INTERNAL_ERROR = 0xFF

    @classmethod
    def get_name(cls, code):
        """Get name for given API result code."""
        codes = {v: k for k, v, in cls.__dict__.items() if isinstance(v, int)}
        if code in codes.keys():
            return codes[code]
        else:
            return "<unknown code>"


class LBRadioGatewayAPIClient:
    """API client for interaction with LB radio gateway via TCP commands."""

    @staticmethod
    def _check_network_key(network_key: bytes):
        if len(network_key) != 16:
            raise LBRadioGatewayAPIException(f"Invalid network key length: {len(network_key)}")
        return network_key

    @staticmethod
    def _parse_mac(mac_address: str) -> bytes:
        mac_address = bytes.fromhex("".join(mac_address.split(":")))
        if len(mac_address) != 6:
            raise LBRadioGatewayAPIException(f"invalid MAC address length: {len(mac_address)}")
        return mac_address

    # Note: the following set and dicts contain generic commands. For these, the key must always
    # match the command definition (except that the command is lower case).

    # generic commands without arguments or return value
    GENERIC_COMMANDS = {
        "log_stack_usage",
        "si4467_stop_tx",
    }

    # generic get functions without arguments and a single return value
    GENERIC_GET_FUNCTIONS = {
        # value is the result parsing function
        "get_mac_address": lambda x: ':'.join([a + b for a, b in zip(*[iter(x.hex())]*2)]),
        "get_antenna_diversity_mode": lambda x: int(x[0]),
        "get_antenna_diversity": lambda x: int(x[0]),
        "get_antenna_int_ext": lambda x: int(x[0]),
        "get_app_version": lambda x: bytes(x).decode('ascii'),
        "get_stack_usage": lambda x: bytes(x).decode('ascii'),
        "get_tx_mac_counter": lambda x: int.from_bytes(x, 'little'),
        "get_lb_radio_driver_state": lambda x: bytes(x).decode('ascii'),
        "get_uptime": lambda x: int.from_bytes(x, 'little'),
    }

    # generic set functions with a single argument and no return value
    GENERIC_SET_FUNCTIONS = {
        # the value is the argument parser (checks & transformation)
        "set_network_key": _check_network_key,
        "set_mac_address": _parse_mac,
        "set_tx_mac_counter": lambda x: x.to_bytes(8, 'little'),
        "set_antenna_diversity_mode": lambda x: bytes([x]),
        "set_antenna_diversity": lambda x: bytes([x]),
        "set_antenna_int_ext": lambda x: bytes([x]),
        # note: it is explicitly OK that the following commands do not start with set_
        "reset_device_nonce": _parse_mac,
        "si4467_start_cw": lambda channel: bytes([channel]),
    }

    DOCSTRINGS = {
        "log_stack_usage": (
            "Print current stack usage to the console.",
            []
        ),
        "get_antenna_diversity": (
            "Get antenna diversity state (only relevant if mode is 0).",
            []
        ),
        "get_antenna_diversity_mode": (
            "Get antenna diversity mode (0: MCU, 1: TRX).",
            []
        ),
        "get_antenna_int_ext": (
            "Get antenna configuration (0: external, 1: internal; only relevant for HCGW).",
            []
        ),
        "get_mac_address": (
            "Get Lemonbeat radio MAC address of gateway.",
            []
        ),
        "get_app_version": (
            "Get lb_radio_gateway application version.",
            []
        ),
        "get_tx_mac_counter": (
            "Get TX MAC counter value.",
            []
        ),
        "reset_device_nonce": (
            "Reset device nonce.",
            [("mac_address", "MAC address as hex string.")]
        ),
        "set_antenna_diversity": (
            "Set antenna diversity state (only relevant if mode is 0).",
            [("state", "0 (first antenna) or 1 (second antenna)")]
        ),
        "set_antenna_diversity_mode": (
            "Set antenna diversity mode.",
            [("mode", "0 (MCU) or 1 (TRX)")]
        ),
        "set_antenna_int_ext": (
            "Set antenna configuration (only relevant for HCGW).",
            [("int_ext", "0 (external) or 1 (internal)")]
        ),
        "set_mac_address": (
            "Set Lemonbeat radio MAC address of gateway.",
            [("mac_address", "MAC address as hex string.")]
        ),
        "set_network_key": (
            "Set the network key.",
            [("network_key", "Network key as byte string (command line: hex string).")]
        ),
        "set_tx_mac_counter": (
            "Set the TX MAC counter.",
            [("counter_value", "Counter value.")]
        ),
        "get_lb_radio_driver_state": (
            "Get state of lb_radio driver state machine.",
            []
        ),
        "get_stack_usage": (
            "Get thread with the (relatively) biggest stack usage.",
            []
        ),
        "get_uptime": (
            "Get current uptime in milliseconds.",
            []
        ),
        "get_si4467_gpio": (
            "Get state of Si4467 GPIO pin.",
            [("index", "Si4467 GPIO pin index.")]
        ),
        "si4467_start_cw": (
            "Start continuous wave TX on specified channel.",
            [("channel", "Si4467 channel for transmission.")]
        ),
        "si4467_stop_tx": (
            "Stop TX.",
            []
        ),
        "wakeup": (
            "Send a wakeup frame.",
            [
                ("mac_address", "MAC address as hex string."),
                ("duration_ms", "Duration in milliseconds."),
                ("channel", "Lemonbeat channel")
            ]
        ),
    }

    def _function_docstring(self, name):
        """Generate docstring for generic function."""
        docstr, parameters = self.DOCSTRINGS[name]
        if len(parameters) > 0:
            docstr += "\n\n"
        for param, description in parameters:
            docstr += f"@param {param} {description}\n"
        return docstr

    def cmdline_argument_docstring(self, name, full=True):
        """Generate docstring for command line argument."""
        body, parameters = self.DOCSTRINGS[name]
        docstr = name
        for param, _ in parameters:
            docstr += f" <{param}>"
        docstr += " – " + body
        if full:
            for param, description in parameters:
                docstr += f"\n- {param}: {description}"
        return docstr

    def __init__(self, unix_socket=None):
        self._socket = None
        self._unix_socket = unix_socket

    def __getattr__(self, name: str):
        if name in self.GENERIC_COMMANDS:
            return self._make_generic_command(name)
        elif name in self.GENERIC_GET_FUNCTIONS.keys():
            return self._make_generic_getter(name)
        elif name in self.GENERIC_SET_FUNCTIONS.keys():
            return self._make_generic_setter(name)
        else:
            raise AttributeError(f"'{self.__class__.__name__}' object has no attribute '{name}'")

    def _make_generic_command(self, name: str):
        command = Command().__getattribute__(name.upper())

        def generic_command() -> None:
            with self._connected_socket() as s:
                self._send_command(s, command)
                self._check_result(s)

        generic_command.__doc__ = self._function_docstring(name)
        return generic_command

    def _make_generic_getter(self, name: str):
        command = Command().__getattribute__(name.upper())
        result_parser = self.GENERIC_GET_FUNCTIONS[name]

        def generic_getter() -> Any:
            with self._connected_socket() as s:
                self._send_command(s, command)
                length = self._check_result(s)
                data = self._get_data(s, length)
                return result_parser(data)

        generic_getter.__doc__ = self._function_docstring(name)
        return generic_getter

    def _make_generic_setter(self, name: str):
        command = Command().__getattribute__(name.upper())
        argument_parser = self.GENERIC_SET_FUNCTIONS[name]

        def generic_setter(arg: Any) -> None:
            if argument_parser is not None:
                arg = argument_parser(arg)
            with self._connected_socket() as s:
                self._send_command(s, command, arg)
                self._check_result(s)

        generic_setter.__doc__ = self._function_docstring(name)
        return generic_setter

    def open_connection(self):
        if self._unix_socket is None:  # use TCP API
            cmd = f"ip -6 -json address show dev {DEFAULT_INTERFACE} scope link".split(" ")
            iface = json.loads(subprocess.check_output(cmd))[0]
            addrs = [addr['address'] for addr in iface['addr_info'] if 'address' in addr.keys()]
            assert len(addrs) == 1, "failed to find exactly one suitable RM address"
            rm_address = (addrs[0], DEFAULT_PORT, 0, iface['ifindex'])

            self._socket = socket.socket(socket.AF_INET6, socket.SOCK_STREAM)
            self._socket.connect(rm_address)
        else:  # use Unix socket
            self._socket = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
            self._socket.connect(self._unix_socket)

    def close_connection(self):
        if self._socket is not None:
            self._socket.close()
            self._socket = None

    @contextmanager
    def _connected_socket(self, autoclose=False) -> socket:
        if self._socket is None:
            self.open_connection()
        yield self._socket
        if autoclose:
            self.close_connection()

    def _send_command(self, s: socket, command: bytes, data: bytes = b"") -> None:
        assert len(data) < 256
        s.sendall(bytes([API_VERSION]) + command + bytes([len(data)]) + data)

    def _read_exact(self, s: socket, num: int) -> bytearray:
        data = bytearray()

        while num > 0:
            chunk = s.recv(num)
            if len(chunk) == 0:
                raise LBRadioGatewayAPIException("connection closed")

            data.extend(chunk)
            num -= len(chunk)

        return data

    def _get_result(self, s: socket):
        api_version, result_code, length = self._read_exact(s, 3)
        assert api_version == API_VERSION, f"unsupported API version: {api_version}"
        return (result_code, length)

    def _get_data(self, s: socket, length: int) -> bytes:
        data = self._read_exact(s, length)
        return data

    def _check_result(self, s: socket) -> int:
        result, length = self._get_result(s)
        if result != Result.OKAY:
            raise LBRadioGatewayAPIException(f"API returned error: 0x{result:02x} ({Result.get_name(result)})")
        return length

    def populate_generic_functions(self):
        """Pre-build generic functions (useful for auto-completion)."""
        for name in self.GENERIC_COMMANDS:
            self.__setattr__(name, self._make_generic_command(name))
        for name in self.GENERIC_GET_FUNCTIONS.keys():
            self.__setattr__(name, self._make_generic_getter(name))
        for name in self.GENERIC_SET_FUNCTIONS.keys():
            self.__setattr__(name, self._make_generic_setter(name))

    def wakeup(self, mac_address: str, duration_ms: int, lb_channel: int) -> None:
        mac_address = self._parse_mac(mac_address)
        duration_bytes = duration_ms.to_bytes(4, "little")
        with self._connected_socket() as s:
            self._send_command(s, Command.WAKEUP_DEVICE,
                               duration_bytes + mac_address + lb_channel.to_bytes(1, "little"))
            self._check_result(s)

    def get_si4467_gpio(self, gpio_idx) -> int:
        with self._connected_socket() as s:
            self._send_command(s, Command.GET_SI4467_GPIO, gpio_idx.to_bytes(length=1))
            length = self._check_result(s)
            data = self._get_data(s, length)
            assert len(data) == 1
            return int.from_bytes(data)


def main():
    # parse & execute command
    parser = argparse.ArgumentParser(allow_abbrev=False)
    parser.add_argument("-i", "--interactive", action="store_true",
                        help="Start interactive shell.")
    parser.add_argument("-c", "--help-commands", action="store_true",
                        help="Help for available commands.")
    parser.add_argument("-d", "--describe-command", type=str, metavar="COMMAND",
                        help="Describe specified command.")
    parser.add_argument("-l", "--list-commands", action="store_true",
                        help="List available commands.")
    parser.add_argument("-u", "--unix-socket", type=str, metavar="FILE",
                        help="Use specified Unix socket instead of TCP communication.")
    parser.add_argument("command", type=str, nargs="?",
                        help="API call command to execute.")
    parser.add_argument("command_arguments", type=str, nargs="*",
                        help="API call command arguments.")
    args = parser.parse_args()

    client = LBRadioGatewayAPIClient(args.unix_socket)

    if args.help_commands:
        print("Commands:")
        for arg in client.DOCSTRINGS.keys():
            print(client.cmdline_argument_docstring(arg, full=False))
        sys.exit(0)

    if args.describe_command is not None:
        print(client.cmdline_argument_docstring(args.describe_command))
        sys.exit(0)

    if args.list_commands:
        print(" ".join(client.DOCSTRINGS.keys()))

    if args.interactive:
        # interactive mode
        print("no command specified - starting interactive mode")
        # pre-populate command names to enable auto-completion
        client.populate_generic_functions()
        # start IPython
        from IPython import embed
        embed()
        # done
        client.close_connection()
        sys.exit(0)

    if args.command is None:
        sys.exit(0)

    cmd = args.command
    cmd_args = args.command_arguments

    if cmd == "wakeup":
        address = cmd_args[0]
        duration_ms = int(cmd_args[1], 10)
        lb_channel = int(cmd_args[2], 10)
        client.wakeup(address, duration_ms, lb_channel)
    elif cmd == "set_network_key":
        # Note: this is a generic command, but on the command line we expect a hex string, whereas
        # the function expects a byte string, so we parse it here.
        network_key = bytes.fromhex(cmd_args[0])
        client.set_network_key(network_key)
    elif cmd in {"si4467_start_cw", "set_antenna_diversity", "set_antenna_diversity_mode",
                 "set_antenna_int_ext", "set_tx_mac_counter"}:
        # Note: these are generic set commands, but on the command line we get a string, whereas the
        # function expects an integer, so we parse it here.
        function = getattr(client, cmd)
        function(int(cmd_args[0], 10))
    elif cmd == "get_si4467_gpio":
        gpio_idx = int(cmd_args[0], 10)
        print(client.get_si4467_gpio(gpio_idx))
    elif cmd in client.GENERIC_COMMANDS:
        function = getattr(client, cmd)
        function()
    elif cmd in client.GENERIC_GET_FUNCTIONS:
        function = getattr(client, cmd)
        print(function())
    elif cmd in client.GENERIC_SET_FUNCTIONS:
        function = getattr(client, cmd)
        function(cmd_args[0])
    else:
        raise Exception("unsupported command")

    client.close_connection()


if __name__ == "__main__":
    main()
