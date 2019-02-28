#!/bin/sh
# Forget the Wi-Fi crendetials

set -eu -o pipefail

systemctl stop wpa_supplicant@wlan0
ip link set dev wlan0 down
rm -f /etc/wpa_supplicant/wpa_supplicant-wlan0.conf
ip link set dev wlan0 up
systemctl start wpa_supplicant@wlan0

systemctl restart network_management
