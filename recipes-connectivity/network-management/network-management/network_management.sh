#!/bin/sh
# shellcheck shell=dash

set -eu -o pipefail

DEBUG=1
HOMEKIT_SOCKET="/tmp/wifi_interface"
HOMEKIT_TIMEOUT=30
WIFI_CONFIG_FILE="/etc/wpa_supplicant/wpa_supplicant-wlan0.conf"
WPA_SERVICE="wpa_supplicant@wlan0"
DHCP_SERVICE="dhcpcd"
VPN_SERVICE="openvpn@prod"

FIRST_RUN=1
BUTTON_PRESSED=0
ETH_CARRIER_OLD=-1
ETH_CARRIER=0

MAIN_PROCESS_PID=$$

info() {
    if [ $DEBUG -ne 0 ]; then
        echo "* $*"
    fi
}


start_wifi() {
    if wifi_config_exists; then
        systemctl start "$WPA_SERVICE"
    else
        info "No Wi-Fi config exists, staying disconnected."
    fi
}

stop_wifi() {
    systemctl stop "$WPA_SERVICE"
}

start_networking() {
    if ! eth_up; then
        start_wifi
    fi
    systemctl start "$DHCP_SERVICE"
    systemctl start "$VPN_SERVICE" || true
}

stop_networking() {
    systemctl stop "$VPN_SERVICE" || true
    systemctl stop "$DHCP_SERVICE"
    stop_wifi
}

start_ap() {
    ###
    # Notify Homekit accessory server to start the access point
    # WAC server stops automatically after 15 minutes
    ###
    stop_networking
    echo -n '{"action":"start_ap"}' | socat - unix-sendto:"$HOMEKIT_SOCKET"
}

stop_ap() {
    echo -n '{"action":"stop_ap"}' | socat - unix-sendto:"$HOMEKIT_SOCKET" || true
    # add delay to ensure accessory-server has stopped the wifi interface
    sleep 2
    start_networking
}

set_wifi_config() {
    mkdir -p /etc/wpa_supplicant

    if [ $# -lt 2 ]; then
        encryption="key_mgmt=NONE"
    else
        encryption="key_mgmt=WPA-PSK
    psk=\"$2\""
    fi

    echo "network={
    ssid=\"$1\"
    scan_ssid=1
    ${encryption}
}" > "$WIFI_CONFIG_FILE"
}

remove_wifi_config() {
    rm -f -- "$WIFI_CONFIG_FILE"
    if ! eth_up; then
        start_ap
    fi
}


wifi_config_exists() {
    [ -f "$WIFI_CONFIG_FILE" ]
}

read_eth_carrier() {
    if [ "$(cat /sys/class/net/eth0/carrier)" = "1" ]; then
        ETH_CARRIER=1
    else
        ETH_CARRIER=0
    fi
}

eth_up() {
    [ $ETH_CARRIER -ne 0 ]
}

is_first_run() {
    [ $FIRST_RUN -ne 0 ]
}

button_pressed() {
    [ $BUTTON_PRESSED -ne 0 ]
}

has_ip_address() {
    ip link show dev "$1" 2>&1 | grep -q "UP,LOWER_UP" && \
        ip addr show dev "$1" 2>&1 | grep -q "inet "
}

ap_is_running() {
    pgrep hostapd > /dev/null
}

button_check() {
    BTN_GPIO_PIN=11
    BTN_GPIO=/sys/class/gpio/gpio${BTN_GPIO_PIN}
    BTN_VAL=$BTN_GPIO/value

    # Make sure the button GPIO is exported
    if [ ! -d $BTN_GPIO ]; then
        echo $BTN_GPIO_PIN > /sys/class/gpio/export
    fi
    echo none > "$BTN_GPIO/edge"
    echo both > "$BTN_GPIO/edge"

    # Use inotify events
    while inotifywait -qq -e modify $BTN_VAL > /dev/null; do
        if [ "$(cat $BTN_VAL)" = "0" ]; then
            info "Button pressed!"
            pkill -P $MAIN_PROCESS_PID sleep
        fi
    done
}

stop() {
    info "Exit on trap"
    kill "$BTN_PID" > /dev/null 2>&1
    exit 0
}


# call function and exit, if given as cli parameter
if [ $# -ge 1 ]; then
    case "$1" in
        "start_networking" | "start_ap" | "stop_ap" | "set_wifi_config" | "remove_wifi_config" | "stop_wifi")
            read_eth_carrier
            "$@"
            exit 0
            ;;
        *)
            echo "Unknown argument." >&2
            exit 1
            ;;
    esac
fi


# Catch the stop signal and act on it
trap "stop" HUP INT QUIT TERM
# Start the button check service
button_check&
BTN_PID=$!

# Main loop
while true; do
    read_eth_carrier

    # Button is pressed, evaluate the state
    if button_pressed; then
        if ! eth_up && ! has_ip_address wlan0 && ! ap_is_running; then
            info "Button pressed, no LAN, no WLAN, (re-)starting AP."
            start_ap
        else
            info "Button pressed, ignoring."
        fi
    fi

    if [ $ETH_CARRIER -ne $ETH_CARRIER_OLD ]; then
        ETH_CARRIER_OLD=$ETH_CARRIER
        if ! eth_up; then
            info "LAN changed to DOWN, trying to connect to Wi-Fi."
            start_wifi
        else
            info "LAN changed to UP, stopping AP and disabling Wi-Fi."
            stop_ap
            stop_wifi
        fi
    fi

    # Check only on first run
    if is_first_run; then
    FIRST_RUN=0
    if ! wifi_config_exists && ! eth_up; then
            info "No Wi-Fi config is present, no cable connection. Starting the Access Point mode."
            # wait for HomeKit service to provide socket
            # note: we can't simply start this service after the
            # accessory server service, as the accessory server
            # service already depends on this one
            if ! [ -S $HOMEKIT_SOCKET ]; then
                info "Waiting for HomeKit socket ..."
                delay=0
                while ! [ -S $HOMEKIT_SOCKET ] && [ $delay -lt $HOMEKIT_TIMEOUT ] ; do
                    delay=$((delay + 1))
                    echo -n .
                    sleep 1
                done
                echo
            fi

            if ! start_ap; then
                info "failed to start AP, try again later"
            fi
        fi
    fi

    BUTTON_PRESSED=0
    if ! sleep 1; then
        # Process was killed by the button check
        BUTTON_PRESSED=1
    fi
done
