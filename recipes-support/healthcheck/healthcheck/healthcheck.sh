#!/bin/sh
# shellcheck shell=dash
#
# Checking for known problems, reporting failing checks to syslog and stderr.

set -eu -o pipefail

readonly update_server=gateway.iot.sg.dss.husqvarnagroup.net
readonly update_url_protocolless=gateway.iot.sg.dss.husqvarnagroup.net/images/gardena-update-image-prod-gardena-sg-mt7688.swu

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

    # Check if HTTP connectivity is working
    if ! curl --max-time 30 --range 0-1024 -sS "http://${update_url_protocolless}" 2>/dev/null | grep -q "Linux System Firmware for the GARDENA smart Gateway"; then
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

test_system_clock_synced() {
    local result=0

    # Check if NTP (outgoing) is working
    if ! timedatectl | grep -q "System clock synchronized: yes"; then
        result=2
    fi

    log_result "portcheck_ntp" "${result}" "omitted"
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
# SGSE-770
test_shadoway_corrupted_directories() {
    local result=0

    local corrupted=0
    if corrupted="$(find /var/lib/shadoway/work -maxdepth 1 -type d | grep -a Device_descriptionID | grep -c -v Device_descriptionID_fc00)"; then
        result=2
    fi

    log_result "shadoway_corrupted_directories" "${result}" "corrupted=${corrupted}"
}

# Allow finding devices which have been affected by SGSE-965.
# Intended allow attributing long-term side effects of a too low MAC counter.
test_shadoway_sgse_965() {
    if ! ls -1 /var/shadoway/work/Device_descriptionID_*/*/Partner_information_*.json >/dev/null 2>&1; then
        log_result "shadoway_sgse_965" 0 "Gateway has no partners"
        return
    fi

    # Dongles have a wakeup_interval of 0. Having anything else here is a
    # (harmless) side effect of SGSE-965 which does not get fixed by Shadoway
    # and allows us to single out (previously) affected gateways.
    local affected_devices
    if ! affected_devices="$(jq -r "select(.wakeup_interval > 0).address" /var/shadoway/work/Device_*/Partner_information/Partner_information_1.json 2>/dev/null)"; then
        log_result "shadoway_sgse_965" 1 "failed to extract affected device addresses"
        return
    fi

    for affected_device in ${affected_devices}; do
        log_result "shadoway_sgse_995" 2 "device=${affected_device}"
    done

    if [ -z "${affected_devices}" ]; then
        log_result "shadoway_sgse_965" 0 "no affected devices"
    fi
}

# Find devices which have too high (>30) partner IDs
test_shadoway_sgse_1020() {
    if ! ls -1 /var/shadoway/work/Device_descriptionID_*/*/Partner_information_*.json >/dev/null 2>&1; then
        log_result "shadoway_sgse_1020" 0 "Gateway has no partners"
        return
    fi

    local count
    if ! count="$(jq --slurp 'map(select(.id > 30)) | length' /var/shadoway/work/Device_descriptionID_*/*/Partner_information_*.json 2>/dev/null)"; then
        log_result "shadoway_sgse_1020" 1 "failed to extract number of affected partners"
        return
    fi

    if [ "${count}" -gt 0 ]; then
        log_result "shadoway_sgse_1020" 2 "count=${count}"
        return
    fi

    log_result "shadoway_sgse_1020" 0 "All partner IDs <=30"
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
        log_result "ppp0" "2" "ppp0 interface not operational"
        return
    fi

    # SG-16012: Having a DNS server configured on ppp0 makes no sense at all
    if dns_address="$(networkctl status ppp0 | grep "DNS:" | awk '{print $2}')"; then
        log_result "ppp0" "4" "dns_address=${dns_address}"
        return
    fi

    # SG-16012: Assuming non-link-local addresses get listed first (directly
    # after "Address:"), this check detects multiple IP addresses on ppp0.
    if ! echo "${ip_address}" | grep -q "^fe80::106:94bb";then
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
    if count="$(journalctl -u shadoway | grep -c "Ups .... can't get radio device info...")"; then
        if [ "${count}" -gt 3 ]; then
            log_result "shadoway_sgse_956" 2 "count=${count}"
            return
        fi
    fi

    log_result "shadoway_sgse_956" 0 "omitted"
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
    test_shadoway_corrupted_directories
    test_shadoway_sgse_965
    test_shadoway_sgse_956
    test_shadoway_sgse_1020

    test_ppp0
    test_rm_ping

    return "${something_failed}"
}

test_all
