#!/bin/sh

set -u

# Clean up and migrate files on system update.
#
# Notes: In the field, the only expected way to upgrade the system is SWUpdate,
#        which will always set the swupdate_done flag in the U-Boot environment.
#        During development however, we will do updates by other means.
#        therefore, the variable swupdate_done is used only for validation, not
#        or the actual decision for whether we want to run or not.
#          E.g. U-Boot falling back an old version on the second bootslot.
#
# Known limitations:
#  * This script does not (yet) delete any directories
#  * For debugging purposes, the intermediate files in /tmp are not deleted
#  * Deleting this file (/usr/bin/sysupgrade) will prevent it from ever running
#    again. A factory reset will be needed.
#
# Credits: Heavily inspired by sysupgrade from OpenWrt (https://github.com/openwrt/openwrt/blob/master/package/base-files/files/sbin/sysupgrade)

if [ ! -f /etc/os-release.old ]; then
    cp /etc/os-release /etc/os-release.old
    echo "First startup - nothing to do"
    exit 0;
fi

if cmp -s /etc/os-release.old /etc/os-release; then
    echo "System not changed since last startup"
    exit 0;
fi

fw_printenv -n swupdate_done 1>/dev/null 2>&-
if [ $? -ne 0 ]; then
    echo "WARNING: System got updated by other means than SWUpdate!" >&2
else
    fw_setenv swupdate_done
fi

# Keep output for inspection after reboot
/usr/bin/overlayfs-purge -f >/tmp/overlayfs-purge-stdout.log 2>/tmp/overlayfs-purge-stderr.log

# The merged directory does not always correctly reflect the fact we just deleted many files in the upperdir.
# Remount the rootfs to "commit" the changes.
mount / -o remount

# TODO: Implement data migration using scripts (SG-10427)
# ...

# Ease debugging: Print the release changes
touch /etc/os-release.old
diff /etc/os-release.old /etc/os-release

# Prevent this script from running on the next startup
cp /etc/os-release /etc/os-release.old

# Keep files for inspection after reboot
mv /tmp/overlayfs-purge-*.log /var/lib/sysupgrade

sync

echo "Finished data migration, restarting"
reboot
