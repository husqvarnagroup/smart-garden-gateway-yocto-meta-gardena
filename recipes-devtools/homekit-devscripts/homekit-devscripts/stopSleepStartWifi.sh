#!/bin/sh
# shellcheck shell=dash
# Restart the Wi-Fi interface

set -eu -o pipefail

time=$1
if ! [ "$time" -ge 0 ]; then
    echo "Argument must be an integer greater or equal to 0." >&2
    exit 1
fi

systemctl stop wpa_supplicant@wlan0
ip link set dev wlan0 down
sleep "$time"
ip link set dev wlan0 up
systemctl start wpa_supplicant@wlan0
