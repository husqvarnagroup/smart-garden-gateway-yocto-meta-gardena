#!/bin/ash
#
# Copyright (c) 2024 GARDENA GmbH
#
# SPDX-License-Identifier: MIT
# shellcheck shell=dash
set -eu -o pipefail

opkg update

grep -v "^ *#" < /etc/devpkgs.conf | while read -r pkg; do
    if [ -z "$(opkg list-installed "$pkg" 2>/dev/null)" ]; then
        opkg install "$pkg" || (echo "Failed to install: $pkg" >&2)
    fi
done
