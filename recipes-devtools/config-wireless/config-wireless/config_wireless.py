#!/usr/bin/env python3
"""
Configure WLAN-Adapter for certification tests.
"""
import argparse
import ipaddress
import logging
import signal
import subprocess
import sys
import tempfile
import threading
import time

from multiprocessing import Process
from os import getuid, system

SUBPROCESSES = dict()
RX_MODE_TIMER = ""


def signal_handler_tx_mode(sig, frame):  # pylint: disable=unused-argument
    """Catch signal and stop threads."""
    for name in SUBPROCESSES:
        if SUBPROCESSES and SUBPROCESSES[name].is_alive():
            logging.info("Killing: %s", name)
            SUBPROCESSES[name].terminate()
            SUBPROCESSES[name].join()


def signal_handler_rx_mode(sig, frame):  # pylint: disable=unused-argument
    """Catch signal and stop timer."""
    if RX_MODE_TIMER:
        RX_MODE_TIMER.cancel()


class Hostapdconf():  # pylint: disable=too-many-instance-attributes
    """Hostapd.conf generator."""

    def __init__(self):
        self.interface = ""
        self.channel = 0
        self.ssid = ""
        self.antenna = ""
        self.txpower = ""
        self.beacon_rate = ""
        self.ht_capab = ""
        self.hw_mode = ""
        self.ieee80211n = "0"
        self.wmm_enabled = "0"
        self.country_code = ""

    def set_channel(self, channel):
        """Set channel"""
        # TODO: add regulatory checks e.g. germany 1-13
        # channel 0 = Auto Channel if CONFIG_ACS selected
        if not channel or (0 > int(channel) > 14):
            logging.error("Invalid channel: %s", channel)

        self.channel = channel

    def set_ssid(self, ssid):
        """Set visible SSID string."""
        self.ssid = ssid

    def set_interface(self, interface):
        """Set WLAN interface."""
        # interfaces = netifaces.interfaces() # TODO: netifaces can only be installed via pip
        # if interface not in interfaces:
        #    logging.error("Interface not found, possible interfaces: %s", interfaces)

        self.interface = interface

    def set_ht_capab(self, ht_capab):
        """Set ht capabilities supported by wlan adapter."""
        self.ht_capab = ht_capab

    def set_beacon_rate(self, beacon_rate):
        """Set beacon rate."""
        self.beacon_rate = beacon_rate

    def set_txpower(self, txpower):
        """Set TX power on wlan adapter."""
        # txpower should be in mBm, so convert dBm into mBm
        self.txpower = int(txpower) * 100

    def set_hw_mode(self, hw_mode):
        """Set hw mode."""
        if hw_mode == "n":
            hw_mode = "g"
            self.ieee80211n = "1"
            self.wmm_enabled = "1"

        self.hw_mode = hw_mode

    def set_country_code(self, country_code):
        """Set country code."""
        self.country_code = country_code

    def write_file(self, file):
        """Create hostpad.conf file."""
        file.write("country_code=%s\n" % self.country_code)
        file.write("interface=%s\n" % self.interface)
        file.write("channel=%s\n" % self.channel)
        file.write("hw_mode=%s\n" % self.hw_mode)
        file.write("ssid=%s\n" % self.ssid)
        file.write("ht_capab=[%s]\n" % self.ht_capab)
        file.write("ieee80211n=%s\n" % self.ieee80211n)
        file.write("wmm_enabled=%s\n" % self.wmm_enabled)
        # file.write("beacon_rate=%s\n" % self.beacon_rate)


def hostapd_worker(conf):
    """Function used in separate thread to run hostapd."""
    tmpfile = tempfile.NamedTemporaryFile(mode="w", prefix="hostapd", suffix=".conf", delete=False)
    conf.write_file(file=tmpfile)
    tmpfile.close()
    hostapd_command = "hostapd -P %s.pid %s" % (tmpfile.name, tmpfile.name)
    process = subprocess.Popen([hostapd_command], shell=True,
                               stdout=subprocess.PIPE)

    logging.debug("Starting hostapd: %s", process.args)
    logging.debug("Content of hostapd.conf file:\n%s", (open(tmpfile.name).read()))
    for line in iter(process.stdout.readline, b''):
        logging.info("hostapd: %s", line)


def iperf_worker():
    """Start iperf for TX-Test"""
    process = subprocess.Popen(['iperf3 -s'], shell=True, stdout=subprocess.PIPE)
    logging.debug("Starting iperf3 server: %s", process.args)
    for line in iter(process.stdout.readline, b''):
        logging.info("iperf: %s", line)


def create_dnsmasq_config(conf, network="192.168.25.0", netmask="255.255.255.0"):
    """Create dnsmasq.conf file."""
    used_network = ipaddress.ip_network("%s/%s" % (network, netmask), strict=True)
    all_hosts = list(used_network.hosts())
    first = all_hosts[0]
    first = first + 1  # first address of the network is used by gateway itself
    last = all_hosts[-1]

    dnsmasq_conf = {
        "port": 53,
        "server": "8.8.8.8",
        "interface": conf.interface,
        "dhcp-range": "%s,%s,12h" % (first, last)
    }
    with open("dnsmasq.conf", "w") as configfile:
        for key, value in dnsmasq_conf.items():
            configfile.write("%s=%s\n" % (key, value))


def dnsmasq_worker(conf):
    """Starting dnsmasq to provide dhcp."""
    logging.info("Starting dnsmasq process")
    create_dnsmasq_config(conf)
    dnsmasq_command = 'dnsmasq -d --dhcp-authoritative -C dnsmasq.conf'
    process = subprocess.Popen([dnsmasq_command], shell=True, stdout=subprocess.PIPE)
    logging.debug("Starting dnsmasq: %s", process.args)
    logging.debug("Content of dnsmasq.conf:\n%s", open("dnsmasq.conf", "r").read())
    for line in iter(process.stdout.readline, b''):
        logging.info("dnsmasq: %s", line)


def tcpdump_worker(interface):
    """Function used in separate thread to run tcpdump in rx_mode."""
    command = "tcpdump -i %s" % interface
    subprocess.run(command.split())


def rx_mode_teardown(device, monitor):
    """Teardown rx_mode."""
    system("ifconfig %s down" % monitor)
    system("iw dev %s del" % monitor)
    system("ifconfig %s up" % device)
    # TODO add old interface


def set_interface_ip_address(conf, network="192.168.25.0", netmask="255.255.255.0"):
    """Set ip address on interface."""
    used_network = ipaddress.ip_network("%s/%s" % (network, netmask), strict=True)
    first = list(used_network.hosts())[0]
    logging.debug("Setting ip address of %s to %s", conf.interface, first)
    system("ip addr add %s/%s dev %s" % (first, used_network.prefixlen, conf.interface))


def main():  # pylint: disable=too-many-statements
    """Main routine."""
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(title="WLAN modes", dest='mode')
    subparsers.required = True
    parser.add_argument('-d', '--device',
                        help="Wireless adapter name. (default wlan0)",
                        default="wlan0")

    parser.add_argument('-c', '--channel',
                        help="Select WLAN channel. (default 1)",
                        choices=["1", "2", "3", "4", "5", "6", "7",
                                 "8", "9", "10", "11", "12", "13", "14"],
                        default=1)

    tx_mode_parser = subparsers.add_parser("tx_mode")

    tx_mode_parser.add_argument('-hw', '--hw_mode',
                                choices=['b', 'g', 'n'],
                                help="802.11 hw mode.",
                                default="g")

    tx_mode_parser.add_argument('-s', '--ssid',
                                help="SSID name. (default \"gardena_ap\")",
                                default="gardena_ap")

    tx_mode_parser.add_argument('-ht', '--ht-capab',
                                help="HT capabilities (default HT20), e.g. 'HT20', "
                                     "'HT40', 'GF', 'SHORT-GI-40'",
                                default="HT20",
                                dest='ht_capab')

    tx_mode_parser.add_argument('-tx', '--txpower',
                                help="Set Transmit power of WLAN adapter. (default 20 dBm)",
                                default=20)

    # TODO: enable again, our hostapd do not support it for some reasone
    tx_mode_parser.add_argument('--beacon_rate',
                                help="legacy: <rate> * 100kbps. HT-MCS: <ht:rate>.\n"
                                     "Example: --beacon_rate 10 -> 1Mbps, \n"
                                     "--beacon_rate ht:1 -> MCS1. (default 10)",
                                default="10")

    tx_mode_parser.add_argument('--country-code', dest="country_code",
                                default="DE")

    rx_mode_parser = subparsers.add_parser("rx_mode")

    rx_mode_parser.add_argument('--rx-mode-timeout',
                                help="Enable RX Only mode for <n> seconds. (default 60)",
                                metavar="<n>", default=60, type=int)

    logging_format = "[%(asctime)-15s %(filename)s:%(lineno)4s - %(funcName)20s() ] %(message)s"
    logging.basicConfig(stream=sys.stderr, level=logging.DEBUG, format=logging_format)
    args = parser.parse_args()
    logging.info("Args: %s", args)
    if getuid() != 0:
        logging.error("Run this script as root")
        # exit(1)

    if args.mode == 'rx_mode':
        signal.signal(signal.SIGINT, signal_handler_rx_mode)
        monitor_interface = "mon0"
        try:
            command = "ifconfig %s down" % args.device
            logging.info("Running command: %s", command)
            subprocess.check_call(command.split())
        except subprocess.CalledProcessError as exc:
            logging.exception(exc)
            exit(1)

        try:
            phy_name_file = open("/sys/class/net/%s/phy80211/name" % args.device)
            phy_from_device = phy_name_file.read().splitlines()[0]
            command = "iw phy %s interface add %s type monitor" % \
                      (phy_from_device, monitor_interface)
            logging.info("Running command: %s", command)
            subprocess.check_call(command.split())
        except subprocess.CalledProcessError as exc:
            rx_mode_teardown(args.device, monitor_interface)
            logging.exception(exc)
            exit(1)

        try:
            command = "iw dev %s del" % args.device
            logging.info("Running command: %s", command)
            subprocess.check_call(command.split())
        except subprocess.CalledProcessError as exc:
            rx_mode_teardown(args.device, monitor_interface)
            logging.exception(exc)
            exit(1)

        try:
            command = "ifconfig %s up" % monitor_interface
            logging.info("Running command: %s", command)
            subprocess.check_call(command.split())
        except subprocess.CalledProcessError as exc:
            rx_mode_teardown(args.device, monitor_interface)
            logging.exception(exc)
            exit(1)

        try:
            command = "iw dev %s set channel %s" % (monitor_interface, args.channel)
            logging.info("Running command: %s", command)
            subprocess.check_call(command.split())
        except subprocess.CalledProcessError as exc:
            rx_mode_teardown(args.device, monitor_interface)
            logging.exception(exc)
            exit(1)

        if args.rx_mode_timeout > 0:
            global RX_MODE_TIMER
            RX_MODE_TIMER = threading.Timer(args.rx_mode_timeout, rx_mode_teardown,
                                            args=[args.device, monitor_interface])
            RX_MODE_TIMER.start()

        tcpdump = Process(target=tcpdump_worker, args=(monitor_interface,))
        tcpdump.start()
        tcpdump.join()

    else:
        signal.signal(signal.SIGINT, signal_handler_tx_mode)
        # stop dnsmasq if running, we start our own dnsmasq
        if subprocess.run(["pidof", "dnsmasq"]).returncode == 0:
            subprocess.check_call(["systemctl", "stop", "dnsmasq"])

        conf = Hostapdconf()
        conf.set_channel(args.channel)
        conf.set_hw_mode(args.hw_mode)

        conf.set_ssid(args.ssid)
        conf.set_interface(args.device)
        conf.set_ht_capab(args.ht_capab)
        conf.set_txpower(args.txpower)
        conf.set_country_code(args.country_code)

        conf.set_beacon_rate(args.beacon_rate)

        set_interface_ip_address(conf)

        SUBPROCESSES['hostapd'] = Process(target=hostapd_worker, args=(conf,))
        SUBPROCESSES['hostapd'].start()
        time.sleep(2)  # let wait for wlan adapter to properly coming up

        SUBPROCESSES['dnsmasq'] = Process(target=dnsmasq_worker, args=(conf,))
        SUBPROCESSES['dnsmasq'].start()

        SUBPROCESSES['iperf'] = Process(target=iperf_worker)
        SUBPROCESSES['iperf'].start()

        # set tx power with iw
        if args.txpower:
            command = "iw dev %s set txpower fixed %s" % (conf.interface, conf.txpower)
            logging.info("Running command: %s", command)
            ret = system(command)
            if ret != 0:
                logging.error("TXPower cannot be set: %s", ret)
                exit(1)


# TODO: graceful shutdown


if __name__ == '__main__':
    main()
