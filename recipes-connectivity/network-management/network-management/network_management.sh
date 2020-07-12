#!/bin/sh
# shellcheck shell=dash

set -eu -o pipefail

DEBUG=1
HOMEKIT_SOCKET="/tmp/wifi_interface"
HOMEKIT_TIMEOUT=300
WIFI_CONFIG_FILE="/etc/wpa_supplicant/wpa_supplicant-wlan0.conf"
WIFI_CONFIG_FILE_TMP="${WIFI_CONFIG_FILE}.tmp"
WPA_SERVICE="wpa_supplicant@wlan0"
DHCP_SERVICE="dhcpcd"
VPN_SERVICE="openvpn"

ETH_CARRIER_OLD=-1
ETH_CARRIER=0

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

restart_wifi() {
    systemctl restart "$WPA_SERVICE"
}

vpn_restart() {
    # Restart only if already running
    systemctl try-restart $VPN_SERVICE
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
    if eth_up; then
        info "LAN connected, not starting AP"
        return 1
    fi

    if has_ip_address wlan0; then
        info "WiFi is connected, not starting AP"
        return 1
    fi

    # SG-14607 Only rtl8192cu offers AP mode
    # This needs to be done here (instead of just in hostapd.service) because
    # the accessory server starts hostapd without systemd.
    if [ "$(uname -m)" = "armv5tejl" ]; then
        rmmod rtl8192cu rtl_usb rtl8192c_common rtlwifi rtl8xxxu || true
        modprobe rtl8192cu
        # Give the driver some time to initialize
        sleep 3
    fi

    ###
    # Notify Homekit accessory server to start the access point
    # WAC server stops automatically after 15 minutes
    ###
    stop_networking
    printf '{"action":"start_ap"}' | socat - unix-sendto:"$HOMEKIT_SOCKET"
}

stop_ap() {
    printf '{"action":"stop_ap"}' | socat - unix-sendto:"$HOMEKIT_SOCKET" || true
    # add delay to ensure accessory-server has stopped the wifi interface
    sleep 2
    start_networking
}

derive_psk() {
    ssid=$1
    passphrase=$2

    if [ "$(printf "%s" "$passphrase" | wc -c)" -eq 64 ]; then
        echo "$passphrase"
    else
        wpa_passphrase "$ssid" "$passphrase" | sed -ne 's/^\s*psk=\(.*\)$/\1/p'
    fi
}

set_wifi_config_noconnect() {
    mkdir -p /etc/wpa_supplicant

    if [ $# -lt 2 ]; then
        encryption="key_mgmt=NONE"
    else
        encryption="key_mgmt=WPA-PSK
    psk=$(derive_psk "$1" "$2")"
    fi

    echo "network={
    ssid=\"$1\"
    scan_ssid=1
    ${encryption}
}" > "$WIFI_CONFIG_FILE_TMP"
    sync
    mv $WIFI_CONFIG_FILE_TMP $WIFI_CONFIG_FILE
}

set_wifi_config() {
    set_wifi_config_noconnect "$@"

    {
        # Fork to background and use delay for the client to get the response.
        sleep 1
        if ap_is_running; then
            # stop ap and start networking
            stop_ap || true
        elif ! eth_up; then
            # reconnect to newly configured Wi-Fi
            restart_wifi || true
        fi
    } &
}

remove_wifi_config() {
    rm -f -- "$WIFI_CONFIG_FILE"
    # Wait to start the AP, so that the client gets the response.
    { sleep 1; stop_wifi || true; start_ap || true; } &
}

wifi_config_exists() {
    [ -f "$WIFI_CONFIG_FILE" ]
}

read_eth_carrier() {
    if [ "$(cat /sys/class/net/eth0/carrier 2>/dev/null)" = "1" ]; then
        ETH_CARRIER=1
    else
        ETH_CARRIER=0
    fi
}

eth_up() {
    [ $ETH_CARRIER -ne 0 ]
}

has_ip_address() {
    ip link show dev "$1" 2>&1 | grep -q "UP,LOWER_UP" && \
        ip addr show dev "$1" 2>&1 | grep -q "inet "
}

ap_is_running() {
    pgrep hostapd > /dev/null
}

wait_for_homekit_socket() {
    # wait for HomeKit service to provide socket
    # note: we can't simply start this service after the accessory server
    # service, as the accessory server service already depends on this one
    if ! [ -S $HOMEKIT_SOCKET ]; then
        info "Waiting for HomeKit socket ..."
        delay=0
        while ! [ -S $HOMEKIT_SOCKET ] && [ $delay -lt $HOMEKIT_TIMEOUT ] ; do
            delay=$((delay + 1))
            printf .
            sleep 1
        done
        echo
    fi
}

# call function and exit, if given as cli parameter
if [ $# -ge 1 ]; then
    case "$1" in
        "start_networking" | "start_ap" | "stop_ap" | "set_wifi_config" | "set_wifi_config_noconnect" | "remove_wifi_config" | "stop_wifi")
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

# start AP if necessary
wait_for_homekit_socket
read_eth_carrier
if ! wifi_config_exists && ! eth_up; then
    info "No Wi-Fi config is present, no cable connection. Starting the Access Point mode."
    if ! start_ap; then
        info "failed to start AP, try again later"
    fi
fi

# Main loop
while true; do
    read_eth_carrier

    if [ $ETH_CARRIER -ne $ETH_CARRIER_OLD ]; then
        if ! eth_up; then
            info "LAN changed to DOWN, trying to connect to Wi-Fi."
            start_wifi
        else
            info "LAN changed to UP, stopping AP and disabling Wi-Fi."
            stop_ap
            stop_wifi
        fi

        # SG-16090 Restarting on the first round of this loop, during startup,
        # is not helpful and just causes Shadoway to take more time to connect
        # to the backend.
        if [ $ETH_CARRIER_OLD -ne -1 ]; then
            vpn_restart
        else
            info "Connectivity change during startup, skipping OpenVPN restart"
        fi
        ETH_CARRIER_OLD=$ETH_CARRIER
    fi

    sleep 1
done
