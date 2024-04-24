#!/bin/sh
# shellcheck shell=dash

# Author: Adrian Friedli <adrian.friedli@husqvarnagroup.com>
# Author: Andreas Müller <andreas.mueller@husqvarnagroup.com>
# Author: Marc Lasch <marc.lasch@husqvarnagroup.com>
#
# Copyright (c) 2019, 2023 Gardena GmbH

set -eu -o pipefail

hap_port="8001"
allowed_tcp_ports="http https $hap_port"
allowed_udp_ports="mdns"

# always allow SSH during development and manufacturing
if [ "$(fw_printenv -n dev_debug_allow_local_ssh 2>/dev/null || true)" = "1" ] \
    || [ "$(fw_printenv -n eol_test_passed 2>/dev/null || true)" != "1" ]; then
    allowed_tcp_ports="ssh $allowed_tcp_ports"
fi

# a convenience function for ipv4 and ipv6
ip46tables() {
    iptables "$@"
    ip6tables "$@"
}

# clear all on error
cleanup_error() {
    trap - EXIT TERM INT
    set +e
    ip46tables -P INPUT ACCEPT
    ip46tables -P FORWARD ACCEPT
    ip46tables -P OUTPUT ACCEPT
    ip46tables -F
    ip46tables -X
    echo "Failed to install firewall." >&2
    exit 1
}
trap cleanup_error EXIT TERM INT

# default policies
ip46tables -P INPUT DROP
ip46tables -P FORWARD DROP
ip46tables -P OUTPUT ACCEPT

# clear all
ip46tables -F
ip46tables -X

# rules to reject with appropriate protocol
ip46tables -N rejectclosed
ip46tables -A rejectclosed -p tcp -j REJECT --reject-with tcp-reset
iptables -A rejectclosed -p udp -j REJECT --reject-with icmp-port-unreachable
ip6tables -A rejectclosed -p udp -j REJECT --reject-with icmp6-port-unreachable
iptables -A rejectclosed -j REJECT --reject-with icmp-proto-unreachable
ip6tables -A rejectclosed -j REJECT --reject-with icmp6-adm-prohibited

# loopback is always allowed
ip46tables -A INPUT -i lo -j ACCEPT

# allow open connections and their related packets
ip46tables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

# Traffic Class based filtering on ppp0. Only allow unencrypted traffic on specific ports.
# 0x0c -> unencrypted (default key)
# 0x1c -> encrypted with network key
# More information: https://confluence-husqvarna.riada.se/display/SGS/Brave+New+World+Development+Radio+Module+Ports
ip6tables -A INPUT -i ppp0 -p udp --match multiport --dport 20001,20003,20017 -m tos --tos 0x0c -j ACCEPT
ip6tables -A INPUT -i ppp0 -p udp -m tos --tos 0x1c -j ACCEPT
ip6tables -A INPUT -i ppp0 -p udp -m tos --tos 0x0c -j DROP

# allow ICMP
iptables -A INPUT -p icmp -j ACCEPT
ip6tables -A INPUT -p icmpv6 -j ACCEPT

# allow TCP
for port in $allowed_tcp_ports; do
    ip46tables -A INPUT -p tcp -m tcp --dport "$port" -j ACCEPT
done

# allow UDP
for port in $allowed_udp_ports; do
    ip46tables -A INPUT -p udp -m udp --dport "$port" -j ACCEPT
done

# allow DHCPv4 server access in AP mode
iptables -A INPUT -p udp -m udp --dport bootps -j ACCEPT

# allow DHCPv6 server(547)->client(546) communication in client mode
ip6tables -A INPUT -p udp -m udp --dport dhcpv6-client -d fe80::/64 -j ACCEPT

# reject the rest
ip46tables -A INPUT -j rejectclosed

trap - EXIT TERM INT
