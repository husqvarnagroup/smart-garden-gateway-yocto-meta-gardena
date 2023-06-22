#!/bin/ash
#
# Copyright (c) 2023 GARDENA GmbH
#
# SPDX-License-Identifier: MIT
# shellcheck shell=dash
set -eu -o pipefail

if ! pkgs="$(fw_printenv -n dev_extra_pkgs 2>/dev/null)"; then
    exit 0
fi

opkg update

for pkg in $pkgs; do
    if [ -z "$(opkg list-installed "$pkg" 2>/dev/null)" ]; then
        opkg install "$pkg" || (echo "Failed to install: $pkg" >&2)
    fi
done
