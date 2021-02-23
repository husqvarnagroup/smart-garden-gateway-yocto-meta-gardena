#!/bin/sh
#
# Configure Gateway so that can be tested by DEKRA Spain against the Wi-Fi
# Alliance testplan

# shellcheck disable=SC2039
set -eu -o pipefail

if [ "${#}" -ne 1 ] || [ "${1}" != "-f" ]; then
  echo "This script is destructive! Pass -f if you REALLY want to run it." >&2
  exit 1
fi

mkdir -p /etc/systemd/system/wpa_supplicant@.service.d
cat > /etc/systemd/system/wpa_supplicant@.service.d/override.conf << EOF
[Unit]
ConditionPathExists=

[Service]
ExecStart=
ExecStart=/usr/sbin/wpa_supplicant -c/etc/wpa_supplicant.conf -Dnl80211 -i%I
EOF
cp /lib/systemd/network/eth0.network.disabled /etc/systemd/network/eth0.network

systemctl enable wpa_supplicant@wlan0.service
systemctl enable openvpn.service
systemctl disable accessory-server.service
systemctl disable internet-led.service
systemctl disable mdns.service
systemctl disable ntpd.service
systemctl disable ppp.service
systemctl disable rm-flashing.service
systemctl disable rsyslog.service
systemctl disable shadoway.service
systemctl disable swupdate-progress.service
systemctl disable systemd-timesyncd.service
systemctl disable tcpdump-sherlock-ppp0.service
systemctl mask firewall.service
systemctl mask swupdate-check.timer
systemctl mask swupdate-check.service

echo 'DROPBEAR_EXTRA_ARGS="-B"' > /etc/default/dropbear

echo "Done - Restart to apply changes."
