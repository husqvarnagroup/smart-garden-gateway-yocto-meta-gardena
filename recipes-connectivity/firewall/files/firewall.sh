#!/bin/sh

# Author: Adrian Friedli <adrian.friedli@husqvarnagroup.com>
# Author: Andreas Müller <andreas.mueller@husqvarnagroup.com>
#
# Copyright (c) 2019 Gardena GmbH

set -eu -o pipefail

unfiltered_interfaces="ppp0 vpn0"
hap_port="8001"
allowed_tcp_ports="http https $hap_port"
allowed_udp_ports="bootps mdns"

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

# unfiltered interfaces are allowed
for iface in $unfiltered_interfaces; do
    ip46tables -A INPUT -i "$iface" -j ACCEPT
done

# allow open connections and their related packets
ip46tables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

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

# reject the rest
ip46tables -A INPUT -j rejectclosed

trap - EXIT TERM INT
