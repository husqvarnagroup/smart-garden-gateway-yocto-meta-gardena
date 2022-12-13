#!/bin/ash
#
# Copyright (c) 2022 GARDENA GmbH
#
# SPDX-License-Identifier: MIT
# shellcheck shell=dash
set -eu -o pipefail

readonly ALLOWLIST=https://gateway.iot.sg.dss.husqvarnagroup.net/maintenance-allowlist
readonly API=https://support.iot.sg-lab.dss.husqvarnagroup.net/v1/register
readonly SSH_KEY=/home/root/.ssh/id_dropbear
readonly TLS_CERT=/etc/ssl/certs/client-prod.crt
readonly TLS_KEY=/etc/ssl/private/client-prod.key

if ! gw_id_hash="$(fw_printenv -n gatewayid | tr -d '\n' | openssl sha1 | \
        awk '{print $2}')"; then
    echo "Failed to generate a hash of the gateway ID" >&2
    exit 1
fi

return_code=0
curl -sfI "$ALLOWLIST/$gw_id_hash" >/dev/null || return_code=$?
if [ "$return_code" -eq 22 ]; then  # >= 400
    exit
elif [ "$return_code" -ne 0 ]; then
    echo "Failed to check $ALLOWLIST" >&2
    exit 1
fi

if [ ! -e "$SSH_KEY.pub" ]; then
    mkdir -p "$(dirname "$SSH_KEY")"
    dropbearkey -t rsa -f "$SSH_KEY" | grep '^ssh-rsa ' > "$SSH_KEY.pub"
fi

# TODO: remove `--insecure`
ssh_params=$(curl -sS --data-urlencode "key=$(cat "$SSH_KEY.pub")" \
                  --cert "$TLS_CERT" --key "$TLS_KEY" --insecure "$API")

if [ -z "$ssh_params" ]; then
    echo "Did not receive any SSH tunnel parameters." >&2
    exit 1
fi

tunnel_port=$(echo "$ssh_params" | jq .tunnel_port)
remote_port=$(echo "$ssh_params" | jq .remote_port)
remote_user=$(echo "$ssh_params" | jq -r .remote_user)
remote_host=$(echo "$ssh_params" | jq -r .remote_host)

ssh -NTy -K 60 -o "ExitOnForwardFailure=yes" -R "$tunnel_port:localhost:22" \
    -p "$remote_port" "$remote_user@$remote_host"
