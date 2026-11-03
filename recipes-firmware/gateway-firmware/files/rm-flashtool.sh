#!/bin/sh
# shellcheck shell=dash

set -eu


FIRMWARE_PATH="/usr/share/gardena/firmware/current/"
FIRMWARE_STACK_FILE="gateway.bin"
FIRMWARE_FILE="${FIRMWARE_PATH}${FIRMWARE_STACK_FILE}"
FIRMWARE_STACK_ADDRESS="0x0"
DEFAULT_RMADDR="8c05510006e2"

fw_getenv() {
    printf '%s\n' "$1" | sed -n "s/^$2=//p" | tail -n 1
}

fw_hasenv() {
    printf '%s\n' "$1" | grep -q "^$2="
}

check_env() {
    env_dump=$(fw_printenv)

    sgtin=$(fw_getenv "$env_dump" sgtin)
    if [ "${#sgtin}" -ne 24 ]; then
        echo "invalid SGTIN"
        exit 1
    fi

    if fw_hasenv "$env_dump" rmaddr; then
        rmaddr=$(fw_getenv "$env_dump" rmaddr | tr -d ':')
    else
        echo "No rmaddr found, using static value used in all MP devices"
        rmaddr="${DEFAULT_RMADDR}"
    fi
    if [ "${#rmaddr}" -ne 12 ]; then
        echo "invalid rmaddr"
        exit 1
    fi
}

flash_firmware() {
    stderr_file=$(mktemp)
    trap 'rm -f "${stderr_file}"' EXIT
    if stdout=$(openocd -f board/gardena_radio.cfg \
        -c "program ${FIRMWARE_FILE} verify exit ${FIRMWARE_STACK_ADDRESS}" 2>"${stderr_file}"); then
        return 0
    fi
    [ "${ppp_stopped}" = "1" ] && systemctl start ppp
    echo "Stdout: ${stdout}"
    echo "Stderr: $(cat "${stderr_file}")"
    exit 1
}

main() {
    ppp_stopped=0
    check_env

    case "$(ps)" in
        *pppd*)
            systemctl stop ppp
            ppp_stopped=1
            ;;
    esac

    reset-rm
    flash_firmware
    reset-rm

    if [ "${ppp_stopped}" = "1" ]; then
        systemctl start ppp
    fi
}

main
