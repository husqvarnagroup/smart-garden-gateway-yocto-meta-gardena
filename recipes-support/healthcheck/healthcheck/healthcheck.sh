#!/bin/sh
# shellcheck shell=dash
#
# Checking for known problems, reporting failing checks to syslog and stderr.

set -eu -o pipefail

readonly update_url_protocolless=@UPDATE_URL_PROTOCOLLESS@

something_failed=0

echoerr() { echo "$@" 1>&2; }

log_result() {
    local name="$1" # Name of the test
    local result="$2" # 0 = success, 1=test code failed, >=2 test failed
    local data="$3" # Payload to (potentially) be reported

    if [ "${result}" -ne 0 ];then
        echoerr "ERROR: [${name}] result=${result}, data: ${data}"
        logger -p user.error -t healthcheck "ERROR: [${name}] result=${result}, data: ${data}"
        something_failed=1
    else
        echoerr "OK: [${name}] result=${result}, data: ${data}"
    fi
}

test_portcheck_http() {
    local result=0

    # Check if HTTP connectivity is working (HEAD request, range lead to ~0.1% false positives)
    if ! curl --max-time 30 -sfI "http://${update_url_protocolless}" >/dev/null; then
        result=2
    fi

    log_result "portcheck_http" "${result}" "omitted"
}

test_portcheck_https() {
    local result=0

    # Check if HTTPS connectivity is working
    if ! curl --max-time 30 --range 0-1024 -sS "https://${update_url_protocolless}" 2>/dev/null | grep -q "Linux System Firmware for the GARDENA smart Gateway"; then
        result=2
    fi

    log_result "portcheck_https" "${result}" "omitted"
}

# check BSSID-unique disconnection events during the last 24h and compare against maximum acceptable
test_wifi_connection_stability() {
    # maximum of acceptable BSSID-unique disconnection events during the last 24h
    local readonly max_bssid_unique_disconnects=12

    local result=0
    local result_string="omitted"
    if unique_disconnects="$(journalctl -S-24h -u wpa_supplicant@wlan0 | grep CTRL-EVENT-DISCONNECTED | awk '{print $8}' | sort | uniq -c | sort -n | tail -1 | awk '{print $1}')"; then
        result_string="unique_disconnects=${unique_disconnects}"
        if [ "${unique_disconnects}" -gt "${max_bssid_unique_disconnects}" ]; then
            result=1
        fi
    fi

    log_result "wifi_connection_stability" "${result}" "${result_string}"
}

test_system_clock_synced() {
    local result=0

    # Check if NTP (outgoing) is working
    if ! timedatectl | grep -q "System clock synchronized: yes"; then
        result=2
    fi

    log_result "system_clock_synced" "${result}" "omitted"
}

test_vpn_crt_ca() {
    local result=0

    # verify client certificate against CA
    if ! openssl verify -no-CApath -CAfile /etc/ssl/certs/ca-prod.crt /etc/ssl/certs/client-prod.crt >/dev/null; then
        result=2
    fi

    log_result "vpn_crt_ca" "${result}" "omitted"
}

test_vpn_crt_subject() {
    # gatewayid and cert subject must match

    local gatewayid
    if ! gatewayid="$(/sbin/fw_printenv -n gatewayid)"; then
        log_result "vpn_crt_subject" "1" "Failed to extract gatewayid"
        return
    fi

    local subject
    if ! subject="$(openssl x509 -in /etc/ssl/certs/client-prod.crt -subject -noout | awk '{print $3}')"; then
        log_result "vpn_crt_subject" "1" "Failed to extract certificate subject"
        return
    fi

    if [ "${gatewayid}" != "${subject}" ]; then
        log_result "vpn_crt_subject" "2" "subject=${subject}"
        return
    fi

    log_result "vpn_crt_subject" "0" "omitted"
}

test_vpn_key() {
    # compare modulus

    local mod_key
    if ! mod_key=$(openssl rsa -modulus -noout -in /etc/ssl/private/client-prod.key);then
        log_result "ubootvar_vpnkey" "2" "omitted"
        return
    fi

    local mod_crt
    if ! mod_crt=$(openssl x509 -modulus -noout -in /etc/ssl/certs/client-prod.crt);then
        log_result "ubootvar_vpnkey" "3" "omitted"
        return
    fi

    if [ "${mod_key}" != "${mod_crt}" ];then
        log_result "ubootvar_vpnkey" "4" "omitted"
        return
    fi

    log_result "vpn_key" "0" "omitted"
}

test_meminfo_mem_available() {
    local result=0

    local mem_available
    if ! mem_available="$(grep MemAvailable: /proc/meminfo | awk '{print $2}')"; then
        result=1
    fi

    # Ensure we have at least 40 MB memory available
    if [ "${result}" -eq 0 ] && [ "${mem_available}" -lt "40960" ]; then
        result=2
    fi

    log_result "meminfo_mem_available" "${result}" "MemAvailable=${mem_available}"
}

test_meminfo_slab() {
    local result=0

    local slab
    if ! slab="$(grep Slab: /proc/meminfo | awk '{print $2}')"; then
        result=1
    fi

    # Ensure the kernel is not eating too much (> 30MB) memory
    if [ "${result}" -eq 0 ] && [ "${slab}" -gt "30720" ]; then
        result=2
    fi

    log_result "meminfo_slab" "${result}" "Slab=${slab}"
}

test_meminfo_s_unreclaim() {
    local result=0

    local s_unreclaim
    if ! s_unreclaim="$(grep SUnreclaim: /proc/meminfo | awk '{print $2}')"; then
        result=1
    fi

    # More than 20 MB of unreclaimable slab memory is considered problematic
    if [ "${result}" -eq 0 ] && [ "${s_unreclaim}" -gt "20480" ]; then
        result=2
    fi

    log_result "meminfo_s_unreclaim" "${result}" "SUnreclaim=${s_unreclaim}"
}

test_systemd_running() {
    local result=0

    # Prevent `systemctl is-system-running` from failing due to a failed
    # systemd-networkd-wait-online.service unit.
    if systemctl is-failed systemd-networkd-wait-online.service >/dev/null; then
        systemctl restart systemd-networkd-wait-online.service
    fi

    local status=0
    local failed_units=""
    if ! status="$(systemctl is-system-running)"; then
        result=2
        failed_units=";$(systemctl --failed --no-legend  | awk '{print $1}' | tr '\n' ',' | sed 's/,$//')"
    fi

    log_result "systemd_running" "${result}" "status=${status}${failed_units}"
}

test_ppp0() {
    local result=0

    if ! ip_address="$(networkctl status ppp0 | grep "Address:" | awk '{print $2}')"; then
        log_result "ppp0" "2" "ppp0 interface has no IP address"
        return
    fi

    # SG-16012: Having a DNS server configured on ppp0 makes no sense at all
    if dns_address="$(networkctl status ppp0 | grep "DNS:" | awk '{print $2}')"; then
        log_result "ppp0" "4" "dns_address=${dns_address}"
        return
    fi

    # SG-16012: Check for multiple IP addresses on ppp0.
    if [ "$(ip address show ppp0 | grep global | sed 's/ *$//g' | sed 's/^ *//g')" = "inet6 fc00::6:0:0:1/64 scope global" ]; then
        # for BNW GW, the only global address must be fc00::6:0:0:1
        result=0

    elif echo "${ip_address}" | grep -q "^fe80::106:94bb";then
        # for HCGW2/LCGW first expected address is fe80::106:94bb (remove after BNW migration)
        result=0

    else
        result=3
    fi

    log_result "ppp0" "${result}" "ip_address=${ip_address}"
}

test_rm_ping() {
    local result=0

    if ! rm_ip_address="$(/sbin/fw_printenv -n rmaddr | awk -F: '{print "fc00::6:" $1$2 ":" $3$4 ":" $5$6 }')"; then
        log_result "rm_ping" "1" "missing rmaddr"
        return
    fi

    local rx_bytes
    if ! rx_bytes="$(cat /sys/class/net/ppp0/statistics/rx_bytes 2>/dev/null)"; then
        log_result "rm_ping" "1" "ppp0 interface missing"
        return
    elif [ "${rx_bytes}" -eq 0 ]; then
        log_result "rm_ping" "3" "ppp0 interface inactive"
        return
    fi

    if ! ping -I ppp0 -c1 "${rm_ip_address}" >/dev/null;then
        # 2nd attempt to avoid reporting one-off, temporary failures
        sleep 1
        ping -I ppp0 -c1 "${rm_ip_address}" >/dev/null || result=2
    fi

    log_result "rm_ping" "${result}" "rm_ip_address=${rm_ip_address}"
}

# Find squashfs errors, which are potential side-effects of SG-14950
test_squashfs() {
    # dmesg exists with an error code when it can not write all its content.
    # Using -c instead of -q forces grep to read the complete dmesg output,
    # even when the first occurrence has been found.
    local count
    if count=$(dmesg | grep -c "SQUASHFS error"); then
        log_result "squashfs" 2 "count=${count}"
        return
    fi

    log_result "squashfs" 0 "omitted"
}

# Failing to load libraries is a potential side-effect of SG-14950 and, among
# others, causing SG-15858.
test_shared_library_loading() {
    # "grep -q" does not work as journalctl exits with an error when the pipe
    # gets closed early on.
    if journalctl -u shadoway | grep -c "error while loading shared libraries" > /dev/null; then
        log_result "shared_library_loading" 2 "omitted"
        return
    fi

    log_result "shared_library_loading" 0 "omitted"
}

# Check if Shadoway can not communicate with the radio module
test_shadoway_sgse_956() {
    local count
    # shadoway tries to recover from sgse-956 once a minute if it should be affected. So healthcheck
    # should only check logs since last run of checks. As timestamps are not correct during startup,
    # we however might miss such occurences at the first run of the healthcheck.
    if count="$(journalctl -S-23h -u shadoway | grep -c "Ups .... can't get radio device info...")"; then
        if [ "${count}" -gt 3 ]; then
            log_result "shadoway_sgse_956" 2 "count=${count}"
            return
        fi
    fi

    log_result "shadoway_sgse_956" 0 "omitted"
}

# Check if zram compression ratio is above minimum
test_zram_compr_ratio() {
    local readonly compr_ratio_min=4 # 3 is a hard limit, but we want the healthcheck to trigger earlier

    if ! orig_data_size="$(awk '{print $1}' /sys/block/zram0/mm_stat)"; then
        log_result "zram_compr_ratio" 1 "omitted"
        return
    fi
    if ! compr_data_size="$(awk '{print $2}' /sys/block/zram0/mm_stat)"; then
        log_result "zram_compr_ratio" 2 "omitted"
        return
    fi

    local result=0
    if [ "${orig_data_size}" -lt $((compr_ratio_min * compr_data_size)) ]; then
        result=3
    fi

    log_result "zram_compr_ratio" "${result}" "ratio=${orig_data_size}/${compr_data_size}"
}

# Check if zram has not more than a limited amount of uncompressed pages
test_zram_huge_pages() {
    local readonly huge_pages_max=10 # maximum allowed uncompressed pages, none are expected, 10 is an arbitrary number

    if ! huge_pages="$(awk '{print $8}' /sys/block/zram0/mm_stat)"; then
        log_result "zram_huge_pages" 1 "omitted"
        return
    fi

    local result=0
    if [ "${huge_pages}" -gt "${huge_pages_max}" ]; then
        result=2
    fi

    log_result "zram_huge_pages" "${result}" "huge_pages=${huge_pages}"
}

test_wifi_device() {
    local result=0

    # Test not executed on MT7688 because it is not affected
    if [ "$(uname -m)" = "armv5tejl" ] \
       && [ ! -d "/sys/bus/usb/devices/1-2:1.0" ]; then
        result=1
    fi

    log_result "wifi_device" "${result}" "omitted"

    return "${result}"
}

test_network_key_sgse_1024() {
    local result=0
    local key_file=/var/lib/lemonbeatd/Network_management/Network_key.json

    if [ -f ${key_file} ]; then
      if jq .encrypted_key ${key_file} | grep -q "[A-Z]"; then
        result=1
      fi
    fi

    log_result "network_key_sgse_1024" "${result}" "omitted"

    return "${result}"
}

test_rmver() {
    # Only for version 1.4.2 (and potentially newer!) we have not locked the
    # flash after programming.
    local result=1
    local data="unknown"
    local filename
    local dongle_count

    # shellcheck disable=SC2126
    if ! dongle_count="$(grep -l DONGLE /var/lib/lemonbeatd/Device_descriptionID_*/Device_descriptionID_*.json | wc -l)"; then
        log_result "rmver" "3" "Dongle work folders could not be counted"
        return
    fi
    if [ "${dongle_count}" -ne "1" ]; then
        log_result "rmver" "4" "${dongle_count} dongle folders folders found"
        return
    fi

    if filename="$(grep -l DONGLE /var/lib/lemonbeatd/Device_descriptionID_*/Device_descriptionID_*.json)" \
        && [ -n "${filename}" ]; then
        # Normalize whitespace to support Shadoway and lemonbeatd serialized files
        local dongle_device_description="$(sed -En "s/([a-z_]+\")\ *:/\1 :/p" "$filename")"
        # Extract the needed information
        local radio_module_app_ver="unknown"
        local etc_radio_module_app_ver_latest="$(cat /etc/rm-firmware-version.latest)"
        if radio_module_app_ver="$(echo "$dongle_device_description" | awk '/version_app/ {print $3}' | cut -d '"' -f2)"; then
            if [ "${radio_module_app_ver}" = "$etc_radio_module_app_ver_latest" ]; then
                result=0;
            else
                result=2;
            fi
            data="${radio_module_app_ver}"
        fi
    fi

    log_result "rmver" "${result}" "${data}"
}

test_all() {
    if ping -c1 gateway.iot.sg.dss.husqvarnagroup.net >/dev/null 2>&1 \
       || ping -c1 www.husqvarnagroup.com >/dev/null 2>&1; then
        # Internet connectivity required during test run
        test_portcheck_http
        test_portcheck_https

        # Internet connectivity required (but not necessarily during test run)
        test_system_clock_synced
        test_systemd_running
    fi

    # No dependencies on internet connectivity
    test_squashfs
    test_shared_library_loading
    test_vpn_crt_ca
    test_vpn_crt_subject
    test_vpn_key
    test_meminfo_mem_available
    test_meminfo_slab
    test_meminfo_s_unreclaim
    test_shadoway_sgse_956
    test_zram_compr_ratio
    test_zram_huge_pages
    test_wifi_device \
      && test_wifi_connection_stability

    test_ppp0
    test_rm_ping

    test_network_key_sgse_1024
    test_rmver

    return "${something_failed}"
}

test_all
