#!/bin/sh
# shellcheck shell=dash
# Unset any custom mDNS name

set -eu -o pipefail

rm -f /etc/homekit.conf
systemctl restart accessory-server
