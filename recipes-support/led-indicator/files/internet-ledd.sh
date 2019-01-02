#!/bin/sh

set -eu -o pipefail

# Internet LED daemon for SGGW

led=/usr/bin/led-indicator
shadoway_status_file=/usr/lib/seluxit/logs/led.txt

led_green_on() {
    $led red1 off
    $led green1 on
    $led blue1 off
}

led_red_on() {
    $led red1 on
    $led green1 off
    $led blue1 off
}

led_red_blink() {
    $led red1 flash
    $led green1 off
    $led blue1 off
}

led_yellow_on() {
    $led blue1 off
    $led red1 on
    $led green1 on
}

led_yellow_blink() {
    $led blue1 off
    $led red1 flash
    $led green1 flash
}

has_ip() {
    ip link show dev "$1" 2>&1 | grep -q "UP,LOWER_UP" && \
        ip addr show dev "$1" 2>&1 | grep -q "inet "
}

is_hotspot() {
    pgrep hostapd > /dev/null
}

vpn_connected() {
    ip route | grep -q '^default.* dev vpn0'
}

shadoway_status() {
    [ "$(cat $shadoway_status_file)" = "GREEN" ]
}

last_state=""

while true; do
    if is_hotspot; then
        state=led_yellow_on
    elif has_ip eth0 || has_ip wlan0; then
        if vpn_connected && shadoway_status; then
            state=led_green_on
        else
            state=led_red_blink
        fi
    else
        state=led_red_on
    fi

    if [ "$state" != "$last_state" ]; then
        last_state=$state
        $state
    fi

    sleep 10
done
