#!/bin/sh
#
# Run the eoltest script if we're at the manufacturer (verified by DNS
# search domain) and FCT is finalized. Additionally make sure the EOL
# test status file is in the correct state.
#
# This is expected to happen at least once during manufacturing (it
# can happen more than once and must be idempotent).

set -eu

eol_test_statusfile=/etc/eol_test_passed

# with systemd, we wait only for network.target; this means,
# dhcpcd has just been started, but may net have completed yet. as a
# workaround, we wait up to 20 seconds (DCHP usually takes ~3
# seconds), if an ethernet cable is connected. this is not ideal, but
# we cannot use systemd-networkd-wait-online, as the eoltest-check
# must run, even if there is no network connection.
carrier="$(cat /sys/class/net/eth0/carrier || true)"
if [ "$carrier" = "1" ]; then
    i=1
    while [ $i -le 20 ] && [ "$(ip -4 -o address show dev eth0||true)" = "" ]; do
        printf .
        i=$(( i + 1 ))
        sleep 1
    done
    echo
fi

# make sure EOL test status file is in the correct state
#
# note: this must happen here, rather than in eoltest-run, so that
# other services can depend on it.
#
# note: during manufacturing, we can never be past the EOL test by
# definition (we're either in bootstrapping, FCT or EOL test phase).
# this also means, we will never try to do a system update or try to
# contact the backend servers during manufacturing.
eol_test_passed="$(fw_printenv -n eol_test_passed 2>/dev/null || echo 0)"
if [ "$eol_test_passed" = "1" ]; then
    if grep -q "^search manufacturing.husqvarnagroup.net$" /etc/resolv.conf; then
        # we're still at manufacturing; this is a re-run of the EOL test
        # note: we remove the EOL test statusfile (so that VPN, etc.
        # will not be started), but don't unset the U-Boot variable,
        # so that if this boot is interrupted, the previous successful
        # EOL test run still counts and the gateway will be usable
        rm -f $eol_test_statusfile
    else
        # normal run; might be first run or after factory reset - make
        # sure status file exists
        [ -f $eol_test_statusfile ] || touch $eol_test_statusfile
    fi
else
    # EOL test not yet passed
    rm -f $eol_test_statusfile
fi
