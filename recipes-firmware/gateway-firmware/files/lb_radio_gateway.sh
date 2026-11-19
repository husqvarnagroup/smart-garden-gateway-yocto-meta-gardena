#!/bin/sh
# shellcheck shell=dash
# Shell client for the radio module API (API version 1).
# Reimplementation of the Python client lb_radio_gateway.py in POSIX sh.
# Not implemented:
# - -i (interactive IPython shell): needs Python and has no shell equivalent.
# - TCP transport, -t, -I and -v: lemonbeatd holds the radio module session, so direct TCP fails.
# - Long options (argparse style): getopts supports short options only.
# - Python tracebacks: every error prints one line and exits 1.
# - set_tx_mac_counter values above 2^63-1: dash arithmetic is limited to 63 bits.

set -eu

UNIX_SOCKET="/run/lemonbeatd/radiomodule_api"

die() {
    echo "lb_radio_gateway: $1" >&2
    exit 1
}

print_usage() {
    cat <<'EOF'
Usage: lb_radio_gateway [-u SOCKET] COMMAND [ARGS...]
       lb_radio_gateway -c | -d COMMAND | -l | -h
EOF
}

print_command_help() {
    cat <<'EOF'
get_antenna_diversity – Get antenna diversity state (only relevant if mode is 0).
get_antenna_diversity_mode – Get antenna diversity mode (0: MCU, 1: TRX).
get_antenna_int_ext – Get antenna configuration (0: external, 1: internal; only relevant for HCGW).
get_app_version – Get lb_radio_gateway application version.
get_lb_radio_driver_state – Get state of lb_radio driver state machine.
get_mac_address – Get Lemonbeat radio MAC address of gateway.
get_network_key – Get current network key.
get_si4467_gpio <index> – Get state of Si4467 GPIO pin.
get_stack_usage – Get thread with the (relatively) biggest stack usage.
get_stats <format> – Get statistics for packets received and sent on RF interface.
restart_transceiver – Restart the Si4467 transceiver.
get_tx_mac_counter – Get TX MAC counter value.
get_uptime – Get current uptime in milliseconds.
log <command> – Start/stop logging
log_stack_usage – Print current stack usage to the console.
reboot – Reboot the device.
reset_device_nonce <mac_address> – Reset device nonce.
reset_device_nonces – Reset all device nonces.
set_antenna_diversity <state> – Set antenna diversity state (only relevant if mode is 0).
set_antenna_diversity_mode <mode> – Set antenna diversity mode.
set_antenna_int_ext <int_ext> – Set antenna configuration (only relevant for HCGW).
set_mac_address <mac_address> – Set Lemonbeat radio MAC address of gateway.
set_network_key <network_key> – Set the network key.
set_tx_mac_counter <counter_value> – Set the TX MAC counter.
si4467_start_cw <channel> – Start continuous wave TX on specified channel.
set_si4467_sdn_pin <value> – Set Si4467 SDN (shutdown) pin state.
si4467_stop_tx – Stop TX.
wakeup <mac_address> <duration_ms> <channel> <req_mac_ack> – Send a wakeup frame.
EOF
}

print_parameter_help() {
    cat <<'EOF'
get_si4467_gpio|index|Si4467 GPIO pin index.
get_stats|format|'H' for human-readable (default), 'M' for machine readable.
log|command|'go' to start, 'halt' to stop logging
reset_device_nonce|mac_address|MAC address as hex string.
set_antenna_diversity|state|0 (first antenna) or 1 (second antenna)
set_antenna_diversity_mode|mode|0 (MCU) or 1 (TRX)
set_antenna_int_ext|int_ext|0 (external) or 1 (internal)
set_mac_address|mac_address|MAC address as hex string.
set_network_key|network_key|Network key as byte string (command line: hex string).
set_tx_mac_counter|counter_value|Counter value.
si4467_start_cw|channel|Si4467 channel for transmission.
set_si4467_sdn_pin|value|0 (disable shutdown) or 1 (enable shutdown).
wakeup|mac_address|MAC address as hex string.
wakeup|duration_ms|Duration in milliseconds.
wakeup|channel|Lemonbeat channel.
wakeup|req_mac_ack|Whether to request a MAC ACK.
EOF
}

print_command_summary() {
    echo "Commands:"
    print_command_help
}

list_commands() {
    print_command_help | awk '{ print "  " $1 }'
}

describe_command() {
    help_line=$(print_command_help | awk -v name="$1" '$1 == name')
    [ -n "$help_line" ] || die "unsupported command"
    echo "$help_line"
    print_parameter_help | awk -F'|' -v name="$1" '$1 == name { print "- " $2 ": " $3 }'
}

result_name() {
    case "$1" in
        00) echo OKAY ;;
        01) echo IFACE_NOT_FOUND ;;
        02) echo CANT_SET_KEY ;;
        03) echo CANT_SET_MAC_ADDRESS ;;
        04) echo UNSUPPORTED_COMMAND ;;
        05) echo LOCK_TIMEOUT ;;
        06) echo WAKEUP_FAILED ;;
        07) echo CANT_SAVE ;;
        08) echo CANT_SET_TX_MAC_COUNTER ;;
        09) echo CANT_RESET_RX_MAC_COUNTER ;;
        0a) echo TX_MAC_COUNTER_WOULD_DECREASE ;;
        0b) echo CANT_GET_KEY ;;
        0c) echo CANT_RESET_TXMACCTR ;;
        0d) echo CANT_GET_MAC_ADDRESS ;;
        0e) echo SI4467_COMMAND_FAILED ;;
        0f) echo SI4467_CONTEXT_NOT_FOUND ;;
        10) echo INVALID_ARGUMENT ;;
        11) echo NO_HW_SUPPORT ;;
        12) echo CANT_GET_TX_MAC_COUNTER ;;
        13) echo MISSING_ARGUMENT ;;
        14) echo NO_NW_KEY_AVAILABLE ;;
        ff) echo INTERNAL_ERROR ;;
        *) echo "<unknown code>" ;;
    esac
}

hex_to_escapes() {
    rest_hex=$1
    while [ -n "$rest_hex" ]; do
        byte_hex=$(printf '%s' "$rest_hex" | cut -c1-2)
        printf '\\%o' "$((0x$byte_hex))"
        rest_hex=$(printf '%s' "$rest_hex" | cut -c3-)
    done
}

hex_prefix() {
    if [ "$2" -gt 0 ]; then
        printf '%s' "$1" | cut -c "1-$2"
    fi
}

hex_to_text() {
    # shellcheck disable=SC2059
    printf "$(hex_to_escapes "$1")"
}

build_request() {
    printf '01%s%02x%s' "$1" "$((${#2} / 2))" "$2"
}

write_frame() {
    # shellcheck disable=SC2059
    printf "$(hex_to_escapes "$1")" | socat -T 5 - "UNIX-CONNECT:$UNIX_SOCKET"
}

send_request() {
    request=$(build_request "$1" "$2")
    write_frame "$request" | hexdump -v -e '1/1 "%02x"'
}

api_send() {
    request=$(build_request "$1" "$2")
    write_frame "$request" > /dev/null || die "connection failed"
}

api_call() {
    reply=$(send_request "$1" "$2")
    [ -n "$reply" ] || die "connection failed"
    [ "${#reply}" -ge 6 ] || die "short reply"
    version=$(printf '%s' "$reply" | cut -c1-2)
    result=$(printf '%s' "$reply" | cut -c3-4)
    length_hex=$(printf '%s' "$reply" | cut -c5-6)
    data_rest=$(printf '%s' "$reply" | cut -c7-)
    length=$((0x$length_hex))
    [ "$version" = 01 ] || die "unsupported API version: $version"
    [ "$result" = 00 ] || die "API returned error: 0x$result ($(result_name "$result"))"
    [ "${#data_rest}" -ge $((length * 2)) ] || die "short reply"
    REPLY_DATA=$(hex_prefix "$data_rest" $((length * 2)))
}

hex_to_mac() {
    printf '%s' "$1" | sed 's/../&:/g; s/:$//'
}

hex_first_byte_decimal() {
    [ -n "$1" ] || die "empty reply"
    first_byte=$(printf '%s' "$1" | cut -c1-2)
    printf "%s" "$((0x$first_byte))"
}

hex_to_decimal_le() {
    value=0
    multiplier=1
    rest_hex=$1
    while [ -n "$rest_hex" ]; do
        byte_hex=$(printf '%s' "$rest_hex" | cut -c1-2)
        value=$((value + 0x$byte_hex * multiplier))
        multiplier=$((multiplier * 256))
        rest_hex=$(printf '%s' "$rest_hex" | cut -c3-)
    done
    printf "%s" "$value"
}

run_getter() {
    case "$1" in
        get_app_version) api_call 0d ""; hex_to_text "$REPLY_DATA" ;;
        get_lb_radio_driver_state) api_call 0f ""; hex_to_text "$REPLY_DATA" ;;
        get_stack_usage) api_call 11 ""; hex_to_text "$REPLY_DATA" ;;
        get_mac_address) api_call 06 ""; hex_to_mac "$REPLY_DATA" ;;
        get_network_key) api_call 12 ""; printf '%s' "$REPLY_DATA" ;;
        get_antenna_diversity) api_call 09 ""; hex_first_byte_decimal "$REPLY_DATA" ;;
        get_antenna_diversity_mode) api_call 07 ""; hex_first_byte_decimal "$REPLY_DATA" ;;
        get_antenna_int_ext) api_call 0b ""; hex_first_byte_decimal "$REPLY_DATA" ;;
        get_uptime) api_call 10 ""; hex_to_decimal_le "$REPLY_DATA" ;;
        get_tx_mac_counter) api_call 0e ""; hex_to_decimal_le "$REPLY_DATA" ;;
    esac
    echo
}

check_decimal() {
    case "$1" in
        '' | *[!0-9]*) die "invalid number: $1" ;;
    esac
    [ "${#1}" -le 19 ] || die "number too large: $1"
}

check_uint8() {
    check_decimal "$1"
    [ "${#1}" -le 3 ] && [ "$1" -le 255 ] || die "value out of range (0 to 255): $1"
}

check_uint64() {
    check_decimal "$1"
    [ "${#1}" -eq 19 ] || return 0
    high=${1%?}
    low=${1#"$high"}
    if [ "$high" -gt 922337203685477580 ] \
        || { [ "$high" -eq 922337203685477580 ] && [ "$low" -gt 7 ]; }; then
        die "number too large: $1"
    fi
}

check_hex_digits() {
    case "$1" in
        *[!0-9a-fA-F]*) die "invalid hex string: $1" ;;
    esac
    [ "${#1}" -eq $(($2 * 2)) ] || die "invalid length, expected $2 bytes: $1"
}

mac_to_hex() {
    printf '%s' "$1" | tr -d ':'
}

text_to_hex() {
    printf '%s' "$1" | hexdump -v -e '1/1 "%02x"'
}

le_hex() {
    i=0
    while [ "$i" -lt "$2" ]; do
        printf '%02x' "$(((($1) >> (8 * i)) & 255))"
        i=$((i + 1))
    done
}

send_uint8_command() {
    check_uint8 "$2"
    api_call "$1" "$(le_hex "$2" 1)"
}

send_tx_mac_counter() {
    check_uint64 "$1"
    api_call 05 "$(le_hex "$1" 8)"
}

send_mac_command() {
    mac_hex=$(mac_to_hex "$2")
    check_hex_digits "$mac_hex" 6
    api_call "$1" "$mac_hex"
}

send_network_key() {
    check_hex_digits "$1" 16
    api_call 01 "$1"
}

send_log_text() {
    text_hex=$(text_to_hex "$1")
    [ "$((${#text_hex} / 2))" -le 255 ] || die "text too long"
    api_call e9 "$text_hex"
}

check_uint32() {
    check_decimal "$1"
    [ "${#1}" -le 10 ] && [ "$1" -le 4294967295 ] \
        || die "value out of range (0 to 4294967295): $1"
}

ack_byte() {
    case "$(printf '%s' "$1" | tr '[:upper:]' '[:lower:]')" in
        1 | y | yes | t | true) printf '01' ;;
        *) printf '00' ;;
    esac
}

run_gpio_getter() {
    [ $# -ge 2 ] || die "missing argument"
    check_uint8 "$2"
    api_call e2 "$(le_hex "$2" 1)"
    [ "${#REPLY_DATA}" -eq 2 ] || die "invalid reply length"
    printf '%s\n' "$((0x$REPLY_DATA))"
}

run_wakeup() {
    [ $# -ge 4 ] || die "missing argument"
    wakeup_mac=$(mac_to_hex "$2")
    check_hex_digits "$wakeup_mac" 6
    check_uint32 "$3"
    check_uint8 "$4"
    wakeup_data="$(le_hex "$3" 4)${wakeup_mac}$(le_hex "$4" 1)"
    if [ $# -ge 5 ]; then
        wakeup_data="${wakeup_data}$(ack_byte "$5")"
    fi
    api_call 03 "$wakeup_data"
}

run_setter() {
    [ $# -ge 2 ] || die "missing argument"
    case "$1" in
        set_antenna_diversity) send_uint8_command 0a "$2" ;;
        set_antenna_diversity_mode) send_uint8_command 08 "$2" ;;
        set_antenna_int_ext) send_uint8_command 0c "$2" ;;
        si4467_start_cw) send_uint8_command e0 "$2" ;;
        set_si4467_sdn_pin) send_uint8_command e8 "$2" ;;
        set_tx_mac_counter) send_tx_mac_counter "$2" ;;
        set_mac_address) send_mac_command 02 "$2" ;;
        reset_device_nonce) send_mac_command 04 "$2" ;;
        set_network_key) send_network_key "$2" ;;
        log) send_log_text "$2" ;;
    esac
}

print_stats_table() {
    cat <<'EOF'
0|TAG_RSSI_ACK_AVERAGE|MAC ACK RSSI (average) [dBm]|1
1|TAG_RSSI_ACK_LATEST|MAC ACK RSSI (latest) [dBm]|1
2|TAG_RSSI_MEASURED_AVERAGE|Measured RSSI (average) [dBm]|1
3|TAG_RSSI_MEASURED_LATEST|Measured RSSI (latest) [dBm]|1
50|TAG_RX_COUNT|RX total|0
51|TAG_RX_SUCCESS_COUNT|RX success|0
52|TAG_RX_FAIL_COUNT|RX fail|0
53|TAG_RX_FAIL_UNKNOWN_CONTENT_TYPE_COUNT|RX unknown content type|0
54|TAG_RX_FAIL_OVERWRITE_NOT_SET_COUNT|RX overwrite not set|0
55|TAG_RX_FAIL_PHY_DECODING_FAILS_COUNT|RX PHY decoding|0
56|TAG_RX_FAIL_AES_DECRYPTION_FAILS_COUNT|RX AES decryption|0
57|TAG_RX_FAIL_MAC_DECODING_FAILS_COUNT|RX MAC decoding|0
58|TAG_RX_FAIL_WRONG_ADDRESSEE_COUNT|RX other device|0
59|TAG_RX_FAIL_INVALID_NONCE_COUNT|RX invalid nonce|0
60|TAG_RX_FAIL_IPV6_PARSING_ERROR_COUNT|RX IPv6 parsing|0
61|TAG_RX_FAIL_NOENT_COUNT|RX no entity|0
62|TAG_RX_FAIL_UNKNOWN_COUNT|RX unknown|0
100|TAG_TX_COUNT|TX total|0
101|TAG_TX_UNCONFIRMED_SUCCESS_COUNT|TX success (unconfirmed)|0
102|TAG_TX_UNCONFIRMED_FAIL_COUNT|TX fail (unconfirmed)|0
150|TAG_TX_ACKED_SUCCESS_COUNT|TX success (ACKed)|0
151|TAG_TX_ACKED_SUCCESS_RETRY_COUNT_0|TX ACKed without retries|0
152|TAG_TX_ACKED_SUCCESS_RETRY_COUNT_1|TX ACKed after 1 retries|0
153|TAG_TX_ACKED_SUCCESS_RETRY_COUNT_2|TX ACKed after 2 retries|0
154|TAG_TX_ACKED_SUCCESS_RETRY_COUNT_3|TX ACKed after 3 retries|0
155|TAG_TX_ACKED_SUCCESS_RETRY_COUNT_4|TX ACKed after 4 retries|0
156|TAG_TX_ACKED_SUCCESS_RETRY_COUNT_5|TX ACKed after 5 retries|0
170|TAG_TX_ACKED_FAIL_COUNT|TX fail (missing ACK)|0
EOF
}

stats_entry() {
    entry=$(print_stats_table | awk -F'|' -v tag="$1" '$1 == tag { print $2 "|" $3 "|" $4 }')
    if [ -z "$entry" ]; then
        entry="unknown statistics tag|unknown statistics tag|0"
    fi
    printf '%s\n' "$entry"
}

stats_row() {
    entry=$(stats_entry "$1")
    value=$(hex_to_decimal_le "$2")
    IFS='|' read -r name text is_rssi <<EOF
$entry
EOF
    if [ "$is_rssi" = 1 ] && [ "$value" -ge 2147483648 ]; then
        value=$((value - 4294967296))
    fi
    if [ "$is_rssi" = 1 ] && [ "$value" -eq 255 ]; then
        value="unset"
    fi
    printf '%s|%s|%s\n' "$name" "$text" "$value"
}

stats_rows() {
    rest_hex=$1
    while [ -n "$rest_hex" ]; do
        tag_hex=$(printf '%s' "$rest_hex" | cut -c1-2)
        length_hex=$(printf '%s' "$rest_hex" | cut -c3-4)
        [ "$length_hex" = 04 ] || die "invalid statistics value length"
        raw_hex=$(printf '%s' "$rest_hex" | cut -c5-12)
        [ "${#raw_hex}" -eq 8 ] || die "short reply"
        rest_hex=$(printf '%s' "$rest_hex" | cut -c13-)
        stats_row "$((0x$tag_hex))" "$raw_hex"
    done
}

print_stats_text() {
    if [ -z "$1" ]; then
        echo
    else
        printf '%s\n' "$1" | awk -F'|' '{ print $2 ": " $3 }'
    fi
}

print_stats_json() {
    if [ -z "$1" ]; then
        echo "{}"
        return
    fi
    printf '%s\n' "$1" | awk -F'|' '
        !($1 in value) { names[++count] = $1 }
        { value[$1] = ($3 == "unset") ? "\"unset\"" : $3 }
        END {
            print "{"
            for (i = 1; i <= count; i++) {
                printf "  \"%s\": %s%s\n", names[i], value[names[i]], (i < count ? "," : "")
            }
            print "}"
        }'
}

run_stats() {
    stats_mode=${2:-H}
    case "$stats_mode" in
        H | M) ;;
        *) die "invalid statistics mode: $stats_mode" ;;
    esac
    api_call e6 ""
    stats_output=$(stats_rows "$REPLY_DATA")
    if [ "$stats_mode" = H ]; then
        print_stats_text "$stats_output"
    else
        print_stats_json "$stats_output"
    fi
}

run_no_argument_command() {
    case "$1" in
        log_stack_usage) api_call e3 "" ;;
        reset_device_nonces) api_call e4 "" ;;
        restart_transceiver) api_call e7 "" ;;
        si4467_stop_tx) api_call e1 "" ;;
        reboot) api_send e5 "" ;;
    esac
}

run_command() {
    case "$1" in
        get_antenna_diversity | get_antenna_diversity_mode | get_antenna_int_ext \
            | get_app_version | get_lb_radio_driver_state | get_mac_address \
            | get_network_key | get_stack_usage | get_tx_mac_counter | get_uptime)
            run_getter "$1" ;;
        log_stack_usage | reboot | reset_device_nonces | restart_transceiver | si4467_stop_tx)
            run_no_argument_command "$1" ;;
        set_antenna_diversity | set_antenna_diversity_mode | set_antenna_int_ext \
            | si4467_start_cw | set_si4467_sdn_pin | set_tx_mac_counter \
            | set_mac_address | reset_device_nonce | set_network_key | log)
            run_setter "$@" ;;
        get_si4467_gpio) run_gpio_getter "$@" ;;
        wakeup) run_wakeup "$@" ;;
        get_stats) run_stats "$@" ;;
        *) die "unsupported command" ;;
    esac
}

main() {
    while getopts ":hcd:lu:" option; do
        case $option in
            h) print_usage; exit 0 ;;
            c) print_command_summary; exit 0 ;;
            d) describe_command "$OPTARG"; exit 0 ;;
            l) list_commands ;;
            u) UNIX_SOCKET=$OPTARG ;;
            :) die "option requires an argument: -$OPTARG" ;;
            *) die "unsupported option: -$OPTARG" ;;
        esac
    done
    shift $((OPTIND - 1))
    [ $# -gt 0 ] || exit 0
    run_command "$@"
}

main "$@"
