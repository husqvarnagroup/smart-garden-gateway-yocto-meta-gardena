#!/bin/sh
# Reset all HomeKit pairings

set -eu -o pipefail

systemctl stop accessory-server
rm -rf /var/lib/HomeKitStore
systemctl start accessory-server
