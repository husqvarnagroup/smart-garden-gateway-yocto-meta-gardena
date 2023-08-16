#!/usr/bin/env python3
# coding: utf-8
#
# Copyright (c) 2020 Gardena GmbH

"""
@package radio_module_test

Classes for interfacting with the radio module test firmware.

See
https://confluence-husqvarna.riada.se/display/SGS/Radio+Module+Production+Test
for specifiation and required firmware.
"""

from enum import IntEnum
import struct
import time

import crcmod
import serial

class RMTestFWMessage:
    """Radio Module Test Firmware Message object."""

    STX = b"\x02"
    ETX = b"\x03"

    class MessageTypes(IntEnum): # pylint: disable=too-few-public-methods
        """Message types (also referred to as commands in the specification)."""
        # pylint: disable=bad-whitespace
        PRODUCTION_TEST_COMMANDS    = 21
        PCBA_PRODUCTION_INFORMATION = 22
        NAK_RESPONSE                = 63

    class RequestResponse(IntEnum): # pylint: disable=too-few-public-methods
        """Request / response."""
        # pylint: disable=bad-whitespace
        REQUEST  = 0
        RESPONSE = 1

    class CommandResults(IntEnum): # pylint: disable=too-few-public-methods
        """Production test command results."""
        # pylint: disable=bad-whitespace
        CMD_OK  = 0
        CMD_ERR = 1

    class ProductionTestCommands(IntEnum): # pylint: disable=too-few-public-methods
        """Production test command commands (sic)."""
        # pylint: disable=bad-whitespace
        GET_REAL_TIME_CLOCK            = 7
        GET_ON_BOARD_TEMPERATURE       = 10
        GET_RADIOMODULE_STATUS         = 250
        GET_RADIOMODULE_SW_INFORMATION = 251
        RECEIVE_ONE_BURST              = 252
        GET_GPIO_PORTS                 = 253
        ADJUST_32_KHZ_XTAL             = 254
        ADJUST_26_MHZ_XTAL             = 255
        TEST_DATA_EEPROM               = 256
        SET_SLEEP_MODE                 = 257
        SET_DIVERSITY_SWITCH           = 258
        CONFIGURE_TX_PARAMETERS        = 259
        SET_CW_OUTPUT                  = 260
        TRANSMIT_ONE_BURST             = 261
        GET_MODULE_SUPPLY_VOLTAGE      = 262
        SET_26_MHZ_XTAL_MODE           = 263
        SET_LED_CONTROL                = 264
        SET_ANTENNA_OUTPUT             = 265

    class PCBAProductionInformationCommands(IntEnum): # pylint: disable=too-few-public-methods
        """PCBA production information commands (i.e. subcommands)."""
        # pylint: disable=bad-whitespace
        SET_PRODUCTION_INFO        = 0
        SET_PRODUCER_SERIAL_NUMBER = 1
        GET_PRODUCTION_INFO        = 250
        GET_PRODUCER_SERIAL_NUMBER = 251

    def __init__(self, message_type, req_res, data):
        """Initialise an RM test firmware message object.
        @param message_type: message_type
        @param req_res: request (0) or response (1)
        @param data: message content (bytes)
        """
        assert len(data) <= 59
        assert message_type.value < 64
        assert req_res.value in [0, 1]
        self.message_type = message_type
        self.req_res = req_res
        self.data = data

    def __repr__(self):
        return \
            f"{self.message_type.name}[{self.req_res.name}, {len(self.data)} byte(s), crc: {self.crc()}]: {self.data}"

    def __content_bytes(self):
        return \
            ((self.message_type.value << 1) + self.req_res.value).to_bytes(1, 'little') + \
            (len(self.data)).to_bytes(1, 'little') + \
            self.data

    def crc(self):
        """Calculate checksum for message."""
        crc = crcmod.predefined.Crc('crc-8-maxim')
        crc.update(self.__content_bytes())
        return crc.digest()

    @classmethod
    def from_bytes(cls, data):
        """Create message object from bytes."""
        assert len(data) >= 5 and len(data) <= 64
        assert data[0:1] == cls.STX
        assert data[-1:] == cls.ETX
        assert len(data) == data[2] + 5
        message_type = cls.MessageTypes((data[1] & 0x7f) >> 1)
        req_res = cls.RequestResponse(data[1] & 0x01)
        message = cls(message_type, req_res, data[3:-2])
        assert message.crc() == data[-2:-1]
        return message

    def to_bytes(self):
        """Convert message to bytes."""
        return \
            self.STX + \
            self.__content_bytes() + \
            self.crc() + \
            self.ETX

class RMTestFW:
    """Radio Module Test Firmware interface."""

    DEFAULT_UART_PORT = "/dev/ttyS1"
    UART_SPEED = 115200

    class PCBAPartNumber(IntEnum): # pylint: disable=too-few-public-methods
        """PCBA part numbers."""
        RADIO_MODULE_TYPE_1 = 0x1901

    def __init__(self, uart_port=DEFAULT_UART_PORT):
        self.uart = serial.Serial(uart_port, self.UART_SPEED)

    def close(self):
        """Close connection via UART."""
        self.uart.close()

    def __request(self, message_type, content, delay=0.1):
        request = RMTestFWMessage(message_type,
                                  RMTestFWMessage.RequestResponse.REQUEST,
                                  content)
        self.uart.write(request.to_bytes())
        time.sleep(delay)
        response = self.uart.read_all()
        msg = RMTestFWMessage.from_bytes(response)
        return msg

    def __production_test_command(self, command, data=b"", delay=0.1):
        msg = self.__request(RMTestFWMessage.MessageTypes.PRODUCTION_TEST_COMMANDS,
                             command.value.to_bytes(2, 'little') + data,
                             delay)
        assert len(msg.data) in [1, 7] # 1 for get radiomodule status, 7 otherwise
        status = RMTestFWMessage.CommandResults(msg.data[0])
        data = msg.data[1:]
        return (status, data)

    def __pcba_production_information_command(self, command, data=b"", delay=0.1):
        msg = self.__request(RMTestFWMessage.MessageTypes.PCBA_PRODUCTION_INFORMATION,
                             command.value.to_bytes(1, 'little') + data,
                             delay)
        status = RMTestFWMessage.CommandResults(msg.data[0])
        data = msg.data[1:]
        return (status, data)

    def get_rtc(self):
        """Get time from RM RTC."""
        status, data = self.__production_test_command(RMTestFWMessage.ProductionTestCommands.GET_REAL_TIME_CLOCK)
        assert status == RMTestFWMessage.CommandResults.CMD_OK
        assert len(data) == 6
        return struct.unpack("<hxxxx", data)[0]

    def get_temperature(self):
        """Get temperature from RM."""
        status, data = self.__production_test_command(RMTestFWMessage.ProductionTestCommands.GET_ON_BOARD_TEMPERATURE)
        assert status == RMTestFWMessage.CommandResults.CMD_OK
        assert len(data) == 6
        return struct.unpack("<hxxxx", data)[0] / 10.0

    def get_rm_status(self):
        """Get radio module status."""
        status, data = self.__production_test_command(RMTestFWMessage.ProductionTestCommands.GET_RADIOMODULE_STATUS)
        # note: it is unclear from the specification whether this is
        # the command status or the result; per specification the
        # first two data bytes should contain the radio module status,
        # but data is empty
        assert status == RMTestFWMessage.CommandResults.CMD_OK
        assert not data
        return status

    def get_rm_sw_information(self):
        """Get radio module software version."""
        status, data = \
            self.__production_test_command(RMTestFWMessage.ProductionTestCommands.GET_RADIOMODULE_SW_INFORMATION)
        assert status == RMTestFWMessage.CommandResults.CMD_OK
        assert len(data) == 6
        major, minor, revision = struct.unpack("<hhh", data)
        return f"{major}.{minor}.{revision}"

    def get_production_information(self):
        """Get RM production info."""
        status, data = self.__pcba_production_information_command(
            RMTestFWMessage.PCBAProductionInformationCommands.GET_PRODUCTION_INFO)
        assert status == RMTestFWMessage.CommandResults.CMD_OK
        pcba_part_number, pcba_revision, pcba_production_time, pcba_serial_number = struct.unpack("<HBIQ", data)
        return {"pcba_part_number": self.PCBAPartNumber(pcba_part_number),
                "pcba_revision": pcba_revision,
                "pcba_production_time": pcba_production_time,
                "pcba_serial_number": pcba_serial_number}

    def get_producer_serial_number(self):
        """Get RM producer serial number."""
        status, data = self.__pcba_production_information_command(
            RMTestFWMessage.PCBAProductionInformationCommands.GET_PRODUCER_SERIAL_NUMBER)
        assert status == RMTestFWMessage.CommandResults.CMD_OK
        return struct.unpack("<Q", data)[0]

def main():
    """Main for testing (can be used from host)."""
    import sys
    if len(sys.argv) != 2:
        sys.stderr.write(f"usage: {sys.argv[0]} [UART port]\n")
        sys.exit(1)
    uart_port = sys.argv[1]
    fw = RMTestFW(uart_port) # pylint: disable=invalid-name
    print(f"RTC: {fw.get_rtc()}")
    print(f"Temperature: {fw.get_temperature()}")
    print(f"FW version: {fw.get_rm_sw_information()}")
    print(f"Production information: {fw.get_production_information()}")
    print(f"Producer serial number: {fw.get_producer_serial_number()}")

if __name__ == '__main__':
    main()
