#!/bin/sh

# shellcheck disable=SC2039
set -eu -o pipefail

# Internet LED daemon for SGGW

readonly debug=1
readonly led=/usr/bin/led-indicatorc
readonly cloudadapter_status_file=/run/cloudadapter/led_status

info() {
    if [ $debug -ne 0 ]; then
        echo "$(awk '{print $1}' /proc/uptime) seconds after start: $*"
    fi
}

led_green_on() {
    info "Steady green"
    $led smartgw:internet:red off
    $led smartgw:internet:green on
    $led smartgw:internet:blue off
}

led_red_on() {
    info "Steady red"
    $led smartgw:internet:red on
    $led smartgw:internet:green off
    $led smartgw:internet:blue off
}

led_red_blink() {
    info "Blinking red"
    $led smartgw:internet:red flash
    $led smartgw:internet:green off
    $led smartgw:internet:blue off
}

led_yellow_on() {
    info "Steady yellow"
    $led smartgw:internet:blue off
    $led smartgw:internet:red on
    $led smartgw:internet:green on
}

led_yellow_blink() {
    info "Blinking yellow"
    $led smartgw:internet:blue off
    $led smartgw:internet:red flash
    $led smartgw:internet:green flash
}

has_ip() {
    ip link show dev "$1" 2>&1 | grep -q "UP,LOWER_UP" && \
        ip addr show dev "$1" 2>&1 | grep -q "inet "
}

is_hotspot() {
    pgrep hostapd > /dev/null
}

vpn_connected() {
    ip route | grep -q '^10.* via 10.* dev vpn0'
}

cloudadapter_status() {
    [ -f "$cloudadapter_status_file" ] && [ "$(cat $cloudadapter_status_file)" = "GREEN" ]
}

last_state=""

while true; do
    if is_hotspot; then
        state=led_yellow_on
    elif has_ip eth0 || has_ip wlan0; then
        if cloudadapter_status; then
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
