#!/bin/ash
#
# Copyright (c) 2023 GARDENA GmbH
#
# SPDX-License-Identifier: MIT
# shellcheck shell=dash
set -eu -o pipefail

readonly ALLOWLIST=https://gateway.iot.sg.dss.husqvarnagroup.net/maintenance-allowlist

if ! gw_id_hash="$(fw_printenv -n gatewayid | tr -d '\n' | openssl sha1 | \
        awk '{print $2}')"; then
    echo "Failed to generate a hash of the gateway ID" >&2
    exit 1
fi

return_code=0
curl -sfI "$ALLOWLIST/$gw_id_hash" >/dev/null || return_code=$?
if [ "$return_code" -eq 22 ]; then  # >= 400
    exit 1
elif [ "$return_code" -ne 0 ]; then
    echo "Failed to check $ALLOWLIST" >&2
    exit 255
fi
