#!/bin/sh
# Reset all HomeKit pairings

set -eu -o pipefail

systemctl stop accessory-server@prod
rm -rf /var/lib/HomeKitStore
systemctl start accessory-server@prod
