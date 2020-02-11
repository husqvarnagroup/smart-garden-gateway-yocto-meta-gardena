#!/bin/sh
#
# This script extracts the versions of the installed U-Boot binary in order to
# allow SWUpdate to determine if it needs to update it.

u_boot_version="unknown"

if ! u_boot_version="$(strings /dev/mtd0 | grep "U-Boot 20" | awk '{print $2}')"; then
    echo "Failed to extract the U-Boot version from /dev/mtd0!" >&2
fi

echo "u-boot ${u_boot_version}" > /tmp/sw-versions

# Quit if /etc/sw-versions is already up-to-date
if cmp /tmp/sw-versions /etc/sw-versions; then
  rm /tmp/sw-versions
  exit
fi

# Note: This code does not need to be power cuts save as it runs during on
# every startup anyway.
mv /tmp/sw-versions /etc/sw-versions
