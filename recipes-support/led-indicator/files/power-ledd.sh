#!/bin/sh

# This script/daemon handles the power LED during the boot process.

set -eu -o pipefail


LED=/sys/class/leds/smartgw:power:

# green blinking during boot
for color in blue red; do
    echo none > ${LED}${color}/trigger
    echo 0 > ${LED}${color}/brightness
done
echo timer > ${LED}green/trigger
echo 500 > ${LED}green/delay_on
echo 500 > ${LED}green/delay_off

# wait for boot to complete
# note: once we have systemd version 240, the following can be replaced with
# status="$(systemctl is-system-running --wait || true)"
# (saving status for use below)
status="$(systemctl is-system-running || true)"
while [ "$status" = "initializing" ] || [ "$status" = "starting" ] ; do
    sleep 5
    status="$(systemctl is-system-running || true)"
done

# indicate success
echo none > ${LED}green/trigger
echo 1 > ${LED}green/brightness

# show error during development
if [ "$status" != "running" ]; then
    if [ "$(fw_printenv -n dev_debug 2>/dev/null || true)" = "1" ]; then
        echo 1 > ${LED}red/brightness
    fi
fi

# SG-12933 / SGISSUE-1896 indicate WiFi MCU crash with LED blink pattern
# TODO SGISSUE-1896 remove once issue is fixed
while true; do
    wifistatus="fail"
    dmesg | grep -q "mt76_wmac 10300000.wmac: MCU message 8 (seq [0-9]\+) timed out$" || wifistatus="ok"
    if [ "$wifistatus" = "fail" ]; then
        echo none > ${LED}green/trigger
        echo 0 > ${LED}green/brightness
        echo timer > ${LED}red/trigger
        echo 1700 > ${LED}red/delay_on
        echo 300 > ${LED}red/delay_off
        # unrecoverable, so we might as well exit
        exit 0
    fi
    sleep 60
done
