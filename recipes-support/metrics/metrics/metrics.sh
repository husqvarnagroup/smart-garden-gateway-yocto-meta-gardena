#!/bin/sh
# shellcheck shell=dash
#
# Collecting metrics to improve system.

set -eu -o pipefail

something_failed=0

echoerr() { echo "$@" 1>&2; }

log_result() {
    local name="$1" # Name of the metric
    local result="$2" # 0 = success, 1=metric code failed
    local data="$3" # Payload to (potentially) be reported

    if [ "${result}" -eq 0 ];then
        echoerr "OK: [${name}], data: ${data}"
        logger -p user.info -t metrics "[bnw@55029 remote=\"true\"][metric@55029 name=\"${name}\" value=\"${data}\"] [${name}], data: ${data}"
    else
        echoerr "ERROR: [${name}] result=${result}, data: ${data}"
        logger -p user.error -t metrics "[${name}] result=${result}, data: ${data}"
        something_failed=1
    fi
}

# Check if Thread Border Routers are advertised on the local network.
thread_border_router() {
    # Not set local, because of trap. `local -r` is not available. Keep it as it,
    # happy to learn about a better solution.
    temp_file="$(mktemp)"
    trap 'rm -f -- "${temp_file}"' EXIT
    dns-sd -B _meshcop._udp local. > "${temp_file}" &
    sleep 2
    kill %1
    local thread_br
    thread_br=$(awk '{$1=$2=$3=$4=$5=$6=""; print $0}' "${temp_file}" | tail -n +5 | tr '\n' ';' | sed -E 's/[[:space:]]{2,}//g')
    if [ "${thread_br}" ]; then
        log_result "thread_border_router" "0" "true"
    else
        log_result "thread_border_router" "0" "false"
    fi
}

collect_all() {
    thread_border_router

    return "${something_failed}"
}

collect_all
