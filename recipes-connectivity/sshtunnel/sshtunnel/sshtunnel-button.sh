#!/bin/ash
#
# Copyright (c) 2025 GARDENA GmbH
#
# SPDX-License-Identifier: MIT
# shellcheck shell=dash
set -euo pipefail

readonly BUTTON_PRESSED=1
readonly BUTTON_RELEASED=0

readonly EXPECTED_BUTTON_PRESS_TIME=10

extract_time() {
    # Extract the timestamp from the line (without fractions of seconds)
    echo "$1" | awk '{ printf "%d", $3 }'
}

is_button_event() {
    line="$1"
    val="$2"
    echo "$line" | grep -q "EV_KEY.*KEY_PROG1.*value $val"
}

is_online() {
    # Check if we can connect to the maintenance access server and quit immediately.
    echo q | telnet maintenance-access.iot.sg.dss.husqvarnagroup.net 443 2> /dev/null | grep -q Connected
}

evtest /dev/input/event0 | while read -r line; do
    if is_button_event "$line" "$BUTTON_PRESSED"; then
        start_time=$(extract_time "$line")
    elif is_button_event "$line" "$BUTTON_RELEASED"; then
        end_time=$(extract_time "$line")
        time=$(( end_time - start_time ))
        if [ "$time" -gt "$EXPECTED_BUTTON_PRESS_TIME" ] && is_online; then
            systemctl --no-block start sshtunnel.service
            systemctl --no-block restart sshtunnel-shutdown.timer || true
        fi
    fi
done
