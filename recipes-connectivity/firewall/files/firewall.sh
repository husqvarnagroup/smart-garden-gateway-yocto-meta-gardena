#!/bin/sh
# shellcheck shell=dash

# Author: Adrian Friedli <adrian.friedli@husqvarnagroup.com>
# Author: Andreas Müller <andreas.mueller@husqvarnagroup.com>
# Author: Marc Lasch <marc.lasch@husqvarnagroup.com>
#
# Copyright (c) 2019, 2023 Gardena GmbH

set -eu -o pipefail

hap_port="8001"
ws_port="8443"
allowed_tcp_ports="http, https, $hap_port"
allowed_udp_ports="mdns"

if [ -f "/etc/enable-websocketd" ]; then
    allowed_tcp_ports="$ws_port, $allowed_tcp_ports"
fi

# always allow SSH during development and manufacturing
if [ "$(fw_printenv -n dev_debug_allow_local_ssh 2>/dev/null || true)" = "1" ] \
    || [ "$(fw_printenv -n eol_test_passed 2>/dev/null || true)" != "1" ] \
    || [ -f "/etc/allow-local-ssh" ]; then
    allowed_tcp_ports="ssh, $allowed_tcp_ports"
fi

# The ruleset is loaded as a single transaction, so it either applies as a
# whole or leaves the previously loaded one in place.
if ! nft -f - <<EOF
# Create the table first, so that deleting it succeeds on an empty ruleset.
table inet filter
delete table inet filter

table inet filter {
	# rules to reject with appropriate protocol
	chain rejectclosed {
		meta l4proto tcp reject with tcp reset
		# rejects with ICMP resp. ICMPv6 port unreachable
		meta l4proto udp reject
		meta nfproto ipv4 reject with icmp type prot-unreachable
		meta nfproto ipv6 reject with icmpv6 type admin-prohibited
	}

	chain input {
		type filter hook input priority filter; policy drop;

		# loopback is always allowed
		iif lo accept

		# allow open connections and their related packets
		ct state established,related accept

		# Traffic Class based filtering on ppp0. Only allow unencrypted traffic on specific ports.
		# 0x0c -> unencrypted (default key)
		# 0x1c -> encrypted with network key
		# More information: https://confluence-husqvarna.riada.se/display/SGS/Brave+New+World+Development+Radio+Module+Ports
		iifname "ppp0" ip6 dscp 0x03 ip6 ecn not-ect udp dport { 20001, 20003, 20017 } accept
		iifname "ppp0" ip6 dscp 0x07 ip6 ecn not-ect meta l4proto udp accept
		iifname "ppp0" ip6 dscp 0x03 ip6 ecn not-ect meta l4proto udp drop

		# allow ICMP
		ip protocol icmp accept
		meta l4proto ipv6-icmp accept

		# allow TCP
		tcp dport { $allowed_tcp_ports } accept

		# allow UDP
		udp dport { $allowed_udp_ports } accept

		# allow DHCPv4 server access in AP mode
		meta nfproto ipv4 udp dport bootps accept

		# allow DHCPv6 server(547)->client(546) communication in client mode
		ip6 daddr fe80::/64 udp dport dhcpv6-client accept

		# reject the rest
		jump rejectclosed
	}

	chain forward {
		type filter hook forward priority filter; policy drop;
	}

	chain output {
		type filter hook output priority filter; policy accept;
	}
}
EOF
then
    echo "Failed to install firewall." >&2
    exit 1
fi
