#!/bin/sh
# Change the mDNS name as advertised by the accessory server

set -eu -o pipefail

mdns_name=$1

cat > /etc/homekit.conf << EOF
[config]
mdns_name = $mdns_name
EOF

systemctl restart accessory-server@prod
