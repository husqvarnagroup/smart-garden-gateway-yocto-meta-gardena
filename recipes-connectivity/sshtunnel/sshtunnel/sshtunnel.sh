#!/bin/ash
#
# Copyright (c) 2022 GARDENA GmbH
#
# SPDX-License-Identifier: MIT
# shellcheck shell=dash
set -eu -o pipefail

readonly API=https://maintenance-access.iot.sg.dss.husqvarnagroup.net/v1/register
readonly SSH_KEY=/home/root/.ssh/id_dropbear
readonly TLS_CERT=/etc/ssl/certs/client-prod.crt
readonly TLS_KEY=/etc/ssl/private/client-prod.key

if [ ! -s "$SSH_KEY" ] || [ ! -s "$SSH_KEY.pub" ]; then
    mkdir -p "$(dirname "$SSH_KEY")"
    dropbearkey -t rsa -f "$SSH_KEY.tmp" | grep "^ssh-rsa " > "$SSH_KEY.pub.tmp"
    sync
    rm -f "$SSH_KEY" "$SSH_KEY.pub"
    mv "$SSH_KEY.tmp" "$SSH_KEY"
    mv "$SSH_KEY.pub.tmp" "$SSH_KEY.pub"
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

systemd-notify --ready

ssh -NTy -K 60 -o "ExitOnForwardFailure=yes" -R "$tunnel_port:localhost:22" \
    -p "$remote_port" "$remote_user@$remote_host"
