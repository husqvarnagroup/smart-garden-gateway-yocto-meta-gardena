#!/bin/sh
# shellcheck shell=dash
#
# Checking for known problems, reporting failing checks to syslog and stderr.

set -eu -o pipefail

readonly update_url_protocolless=@UPDATE_URL_PROTOCOLLESS@
readonly lemonbeatd_rm_api_socket=/runtime/radiomodule_api
readonly lb_radio_gateway_client=/usr/bin/lb_radio_gateway
readonly tc=/usr/sbin/tc

something_failed=0

echoerr() { echo "$@" 1>&2; }

log_result() {
    local name="$1" # Name of the test
    local result="$2" # 0 = success, 1=test code failed, >=2 test failed
    local data="$3" # Payload to (potentially) be reported

    if [ "${result}" -ne 0 ];then
        echoerr "ERROR: [${name}] result=${result}, data: ${data}"
        logger -p user.error -t healthcheck "[metric@55029 name=\"${name}\" value=\"false\"] ERROR: [${name}] result=${result}, data: ${data}"
        something_failed=1
    else
        echoerr "OK: [${name}] result=${result}, data: ${data}"
        logger -p user.info -t healthcheck "[bnw@55029 remote=\"true\"][metric@55029 name=\"${name}\" value=\"true\"] OK: [${name}] result=${result}, data: ${data}"
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
    if ! curl --max-time 30 --range 0-1024 -sS "https://${update_url_protocolless}" 2>/dev/null | grep -q "Linux System Firmware (.*) for the GARDENA smart Gateway"; then
        result=2
    fi

    log_result "portcheck_https" "${result}" "omitted"
}

# check BSSID-unique disconnection events during the last 24h and compare against maximum acceptable
test_wifi_connection_stability() {
    # maximum of acceptable BSSID-unique disconnection events during the last 24h
    local max_bssid_unique_disconnects=12

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

test_x509_crt_ca() {
    local result=0

    # only check new certificates against CA
    if /sbin/fw_printenv -n x509_crt >/dev/null 2>&1; then
        if ! openssl verify -no-CApath -CAfile /etc/ssl/certs/ca-prod.crt /etc/ssl/certs/client-prod.crt >/dev/null; then
            result=2
        fi
    fi

    log_result "x509_crt_ca" "${result}" "omitted"
}

test_x509_crt_subject() {
    # gatewayid and cert subject must match

    local gatewayid
    if ! gatewayid="$(/sbin/fw_printenv -n gatewayid)"; then
        log_result "x509_crt_subject" "1" "Failed to extract gatewayid"
        return
    fi

    local subject
    if ! subject="$(openssl x509 -in /etc/ssl/certs/client-prod.crt -subject -noout | awk '{print $3}')"; then
        log_result "x509_crt_subject" "1" "Failed to extract certificate subject"
        return
    fi

    if [ "${gatewayid}" != "${subject}" ]; then
        log_result "x509_crt_subject" "2" "subject=${subject}"
        return
    fi

    log_result "x509_crt_subject" "0" "omitted"
}

test_x509_crt_key_match() {
    # Ensure the client certificate and key belong to each other

    local key_pubkey
    if ! key_pubkey="$(openssl pkey -pubout -in /etc/ssl/private/client-prod.key)"; then
        log_result "x509_crt_key_match" "1" "Failed to extract public part from key"
        return
    fi

    local crt_pubkey
    if ! crt_pubkey="$(openssl x509 -noout -pubkey -in /etc/ssl/certs/client-prod.crt)"; then
        log_result "x509_crt_key_match" "2" "Failed to extract public key from from cert"
        return
    fi

    if [ "${key_pubkey}" != "${crt_pubkey}" ]; then
        log_result "x509_crt_key_match" "3" "key_pubkey=${key_pubkey}, crt_pubkey=${crt_pubkey}"
        return
    fi

    log_result "x509_crt_key_match" "0" "omitted"
}

test_x509_key_rsa() {
    # Check RSA-specific properties

    # Certificates and keys provided by x509_{crt,key} take precedence over the old conf_openvpn_{crt,key} U-Boot
    # variables. Therefore, if x509_crt is set, then there is no need to run the RSA-based tests.
    if /sbin/fw_printenv -n x509_crt >/dev/null 2>&1; then
        return
    fi

    # compare modulus
    local mod_key
    if ! mod_key=$(openssl rsa -modulus -noout -in /etc/ssl/private/client-prod.key); then
        log_result "x509_key_rsa" "2" "omitted"
        return
    fi

    local mod_crt
    if ! mod_crt=$(openssl x509 -modulus -noout -in /etc/ssl/certs/client-prod.crt); then
        log_result "x509_key_rsa" "3" "omitted"
        return
    fi

    if [ "${mod_key}" != "${mod_crt}" ];then
        log_result "x509_key_rsa" "4" "omitted"
        return
    fi

    # Check the consistency of the RSA private key
    if ! result="$(openssl rsa -check -noout -in /etc/ssl/private/client-prod.key 2>&1)"; then
        log_result "x509_key_rsa" "5" "${result}"
        return
    fi

    if [ "${result}" != "RSA key ok" ]; then
        log_result "x509_key_rsa" "6" "${result}"
        return
    fi

    log_result "x509_key_rsa" "0" "omitted"
}

test_x509_key_ec() {
    # Check EC-specific properties

    # Certificates and keys provided by x509_{crt,key} take precedence over the old conf_openvpn_{crt,key} U-Boot
    # variables. Therefore, if x509_crt is set, we can assume the client-prod.{crt,key} files to be based on EC.
    if ! /sbin/fw_printenv -n x509_crt >/dev/null 2>&1; then
        return
    fi

    # Check the consistency of the EC private key
    if ! result="$(openssl ec -check -noout -in /etc/ssl/private/client-prod.key 2>&1)"; then
        log_result "x509_key_ec" "1" "${result}"
        return
    fi

    # shellcheck disable=SC3003
    if [ "${result}" != "read EC key"$'\n'"EC Key valid." ]; then
        log_result "x509_key_ec" "2" "${result}"
        return
    fi

    log_result "x509_key_ec" "0" "omitted"
}

test_client_crt_longevity() {
    # Ensure certs do not expire for another 30 years.
    # 30 years chosen because of weird behavior when checking for 100 years.
    # See also https://stackoverflow.com/questions/21297853/how-to-determine-ssl-cert-expiration-date-from-a-pem-encoded-certificate#comment113143488_31718838

    local result=0
    if ! openssl x509 -checkend $(( 3600 * 24 * 365 * 30 )) -noout -in /etc/ssl/certs/client-prod.crt > /dev/null; then
        result=1
    fi

    log_result "client_crt_longevity" "${result}" "omitted"
}

test_meminfo_mem_available() {
    local result=0

    local mem_available
    if ! mem_available="$(grep MemAvailable: /proc/meminfo | awk '{print $2}')"; then
        result=1
    fi

    # Ensure we have at least 30 MB memory available
    if [ "${result}" -eq 0 ] && [ "${mem_available}" -lt "30720" ]; then
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

    local status=0
    local failed_units=""
    if ! status="$(systemctl is-system-running)"; then
        result=2
        failed_units=";$(systemctl --failed --no-legend  | awk '{print $1}' | tr '\n' ',' | sed 's/,$//')"
    fi

    log_result "systemd_running" "${result}" "status=${status}${failed_units}"
}

test_ppp0_sg_16012() {
    local result=0

    # SG-16012: Having a DNS server configured on ppp0 makes no sense at all
    if dns_address="$(networkctl status ppp0 | grep "DNS:" | awk '{print $2}')"; then
        log_result "ppp0" "4" "dns_address=${dns_address}"
        return
    fi

    # SG-16012: Check for multiple IP addresses on ppp0.
    if [ "$(/sbin/ip address show ppp0 | grep global | sed 's/ *$//g' | sed 's/^ *//g')" = "inet6 fc00::6:0:0:1/64 scope global" ]; then
        # The only global address must be fc00::6:0:0:1 when using legacy RM firmwares
        result=0
    elif [ "$(/sbin/ip address show ppp0 | grep global | sed 's/ *$//g' | sed 's/^ *//g')" = "inet6 fc00::6:100:0:0/64 scope global" ]; then
        # The only global address must be fc00::6:100:0:0 when using Zephyr based RM firmwares
        result=0
    else
        result=3
    fi

    log_result "ppp0" "${result}" "omitted"
}

test_rm_address() {
    if ! /sbin/fw_printenv -n rmaddr > /dev/null; then
        log_result "rm_address" "1" "missing rmaddr"
        return
    fi

    log_result "rm_address" 0 "omitted"

}

test_rm_ping() {
    local result=0
    local rm_ip_address

    if ! rm_ip_address="$(/sbin/ip -6 route list | grep -e "^fe80::.*dev ppp0" | head -n1 | awk '{print $1}')"; then
        log_result "ppp0" "2" "ppp0 interface has no IP address"
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

# Check if zram compression ratio is above minimum
test_zram_compr_ratio() {
    local compr_ratio_min=3

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
    local huge_pages_max=10 # maximum allowed uncompressed pages, none are expected, 10 is an arbitrary number

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
}

# Run netstat on listening tcp and udp sockets to get socket queue utilization.
# Truncate the first two lines of the output. Report sockets with a backlog of
# a certain amount of bytes in the send queue. This may indicate that the radio
# module has a problem accepting packets from ppp reported in SG-20421.
test_socket_queue_ppp0_sg_20421() {
    local result=0
    # Set check limit in bytes. Use a few bytes more than current MTU of 1500
    # bytes to allow one large packet in queue before warning.
    readonly RECVQ_LIMIT=1600
    readonly SENDQ_LIMIT=1600

    local netstat_lines
    netstat_lines=$(netstat -ltun | tail -n +3 | sed -E 's/[[:space:]]+/,/g')
    for line in $netstat_lines; do
        local recvq_bytes
        recvq_bytes=$(echo "${line}" | cut -d , -f2)
        if [ "${recvq_bytes}" -gt ${RECVQ_LIMIT} ]; then
            result=2
            log_result "test_socket_queue_ppp0_sg_20421" "${result}" "${line}"
        fi
        local sendq_bytes
        sendq_bytes=$(echo "$line" | cut -d , -f3)
        if [ "${sendq_bytes}" -gt ${SENDQ_LIMIT} ]; then
            result=3
            log_result "test_socket_queue_ppp0_sg_20421" "${result}" "${line}"
        fi
    done
    if [ "${result}" -eq 0 ]; then
        log_result "test_socket_queue_ppp0_sg_20421" "${result}" "omitted"
    fi
}

# Check lb_radio_gateway API. This ensures the TCP API on the radio
# module works and the forwarding from the Unix socket provided by
# lemonbeatd works.
test_lb_radio_gateway_api() {
    local result=0
    local version

    if ! version="$(timeout 15 ${lb_radio_gateway_client} -u ${lemonbeatd_rm_api_socket} get_app_version)"; then
        version="undetermined"
        result=1
    fi

    log_result "lb_radio_gateway_api" "${result}" "${version}"
}

# Check lb_radio driver state to make sure state machine is not stuck.
# The state may legitimately be something else than `listen` if the
# gateway is currently sending or receiving a Lemonbeat packet. For
# this reason, we run the check multiple times and accept it as passed
# if the state is `listen` at least once.
test_lb_radio_driver_state() {
    local result=1
    local state

    for _ in $(seq 1 10); do
        if ! state="$(timeout 15 ${lb_radio_gateway_client} -u ${lemonbeatd_rm_api_socket} get_lb_radio_driver_state)"; then
            result=2
            state="undetermined"
            break
        fi
        if [ "$state" = "listen" ]; then
            result=0
            break
        fi
    done

    log_result "lb_radio_driver_state" "${result}" "${state}"
}

# Check for dropped packets on ppp0. This happens when the application
# sends too many packets and back-pressure leads to an overflow in the
# network queue.
test_ppp0_dropped_packets() {
    local name="ppp0_dropped_packets"
    local result=0
    local dropped_packets

    if ! dropped_packets="$(tc -json -s qdisc show dev ppp0 2>/dev/null | jq --monochrome-output .[0].drops)"; then
        log_result "${name}" 2 "tc command failed"
        return
    fi

    if [ "${dropped_packets}" -gt 0 ]; then
        result=1
    fi

    log_result "${name}" "${result}" "${dropped_packets}"
}

# Check for an fc00:: network on any interface other than ppp0
#
# Note: this test likely underreports the issue, as if there is
# acutally another interface with an fc00::/64 network and it breaks
# the smart system, a customer would probably either fix it or
# disconnect the gateway.
test_fc00_networks() {
    local name="fc00_networks"
    local result=0
    local data="omitted"
    local route

    if route="$(ip -6 route show | grep -v 'dev ppp0' | grep '^fc00::' | head -n1)"; then
        result=2
        data="${route}"
    fi

    log_result "${name}" "${result}" "${data}"
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
    test_x509_crt_ca
    test_x509_crt_subject
    test_x509_crt_key_match
    test_x509_key_ec
    test_x509_key_rsa
    test_client_crt_longevity
    test_meminfo_mem_available
    test_meminfo_slab
    test_meminfo_s_unreclaim
    test_zram_compr_ratio
    test_zram_huge_pages
    test_wifi_device \
      && test_wifi_connection_stability

    test_ppp0_sg_16012
    test_rm_address
    test_rm_ping
    test_socket_queue_ppp0_sg_20421

    test_network_key_sgse_1024

    if [ -x "${lb_radio_gateway_client}" ]; then
        # tests for Zephyr-based gateways
        test_lb_radio_gateway_api
        test_lb_radio_driver_state
    fi

    if [ -x "${tc}" ]; then
        test_ppp0_dropped_packets
    fi

    test_fc00_networks

    return "${something_failed}"
}

test_all
