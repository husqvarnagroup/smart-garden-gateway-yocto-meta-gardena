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
    if ! gatewayid="$(fw_printenv -n gatewayid)"; then
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

    log_result "meminfo_slab" "${result}" "SUnreclaim=${s_unreclaim}"
}

test_shadoway_corrupted_directories() {
    local result=0

    local corrupted=0
    if corrupted="$(find /var/lib/shadoway/work -maxdepth 1 -type d | grep -a Device_descriptionID | grep -c -v Device_descriptionID_fc00)"; then
        result=2
    fi

    log_result "shadoway_corrupted_directories" "${result}" "corrupted=${corrupted}"
}

test_systemd_running() {
    local result=0

    # Prevent `systemctl is-system-running` from failing due to a failed
    # systemd-networkd-wait-online.service unit.
    if systemctl is-failed systemd-networkd-wait-online.service >/dev/null; then
        systemctl restart systemd-networkd-wait-online.service
    fi

    local status=0
    if ! status="$(systemctl is-system-running)"; then
        result=2
    fi

    log_result "systemd_running" "${result}" "status=${status}"
}

test_ppp0() {
    local result=0

    if ! ip_address="$(networkctl status ppp0 | grep "Address:" | awk '{print $2}')"; then
        log_result "ppp0" "2" "missing ip address"
        return
    fi

    if ! echo "${ip_address}" | grep -q "^fe80::106:94bb";then
        result=3
    fi

    log_result "ppp0" "${result}" "ip_address=${ip_address}"
}

test_rm_ping() {
    local result=0

    if ! rm_ip_address="$(fw_printenv -n rmaddr | awk -F: '{print "fc00::6:" $1$2 ":" $3$4 ":" $5$6 }')"; then
        log_result "rm_ping" "1" "missing rmaddr"
        return
    fi

    if ! ping -c1 "${rm_ip_address}" >/dev/null;then
        result=2
    fi

    log_result "rm_ping" "${result}" "rm_ip_address=${rm_ip_address}"
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
    test_vpn_crt_ca
    test_vpn_crt_subject
    test_vpn_key
    test_meminfo_mem_available
    test_meminfo_slab
    test_meminfo_s_unreclaim
    test_shadoway_corrupted_directories
    test_ppp0
    test_rm_ping

    return "${something_failed}"
}

test_all
