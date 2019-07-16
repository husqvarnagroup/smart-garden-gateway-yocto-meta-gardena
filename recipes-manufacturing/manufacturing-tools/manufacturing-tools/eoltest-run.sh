#!/bin/sh
#
# Run the eoltest script if we're at ELRAD (verified by DNS search
# domain) and FCT is finalized.
#
# This is expected to happen at least once during manufacturing (it
# can happen more than once and must be idempotent).

set -eu -o pipefail

eol_test_statusfile=/etc/eol_test_passed

exit_error()
{
    message=$1

    # set power LED to red
    echo 0 > /sys/class/leds/smartgw:power:green/brightness
    echo 0 > /sys/class/leds/smartgw:power:blue/brightness
    echo 100 > /sys/class/leds/smartgw:power:red/brightness

    echo "$message" >&2
    exit 1
}


# wait for system to be fully booted
# TODO once we have systemd v240, the while loop can be replaced
# by using systemctl is-system-running --wait above
systemd_status="$(systemctl is-system-running || true)"
while [ "$systemd_status" = "starting" ] || [ "$systemd_status" = "initializing" ]; do
    sleep 1
    systemd_status="$(systemctl is-system-running || true)"
done

# check preconditions
fct_finalized="$(fw_printenv -n fct_finalized 2>/dev/null || echo 0)"
if [ "${fct_finalized=}" != "1" ] || ! grep -q "^search manufacturing.husqvarnagroup.net$" /etc/resolv.conf; then
    echo "preconditions for EOL-test not met"
    exit 0
fi

# indicate with LEDs that EOL test is running now
fct-tool --set-leds blue

gatewayid="$(fw_printenv -n gatewayid 2>/dev/null)"
# shellcheck disable=SC2181
if [ $? -ne 0 ]; then
    exit_error "U-Boot variable gatewayid not set - can not run EOL test!"
fi

eoltest --ipr-id "${gatewayid}"
# shellcheck disable=SC2181
if [ $? -ne 0 ]; then
    exit_error "EOL test failed!"
fi

fw_setenv eol_test_passed 1
touch $eol_test_statusfile # note: this would also happen by eoltest-check during next boot

# As soon as the power LED is green, the gateway can be removed from power during
# manufacturing. This will likely happen faster than the 30 seconds configured
# in /proc/sys/vm/dirty_expire_centisecs. Calling sync ensures our data to be
# save once the LED turns green.
sync

# set power LED to green (EOL test success)
echo 0 > /sys/class/leds/smartgw:power:red/brightness
echo 0 > /sys/class/leds/smartgw:power:blue/brightness
echo 100 > /sys/class/leds/smartgw:power:green/brightness
