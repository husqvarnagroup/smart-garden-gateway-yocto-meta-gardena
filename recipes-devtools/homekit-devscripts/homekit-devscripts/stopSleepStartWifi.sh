#!/bin/sh
# Restart the Wi-Fi interface

set -eu -o pipefail

time=$1

systemctl stop wpa_supplicant@wlan0
ip link set dev wlan0 down
sleep "$time"
ip link set dev wlan0 up
systemctl start wpa_supplicant@wlan0
