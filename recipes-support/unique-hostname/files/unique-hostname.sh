#!/bin/sh

set -e

if [ "$(cat /etc/hostname)" = "GARDENA" ] ; then
    id="$(ip link show dev eth0|grep 'link/ether'|awk '{print $2}'|sed 's/://g'|cut -b7-)"
    if [ -n "$id" ] ; then
	echo "GARDENA_$id" > /etc/hostname
	hostname -F /etc/hostname
    fi
fi
