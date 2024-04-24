#!/bin/sh
# shellcheck shell=dash

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
#  * Deleting this file (/usr/bin/sysupgrade) will prevent it from ever running
#    again. A factory reset will be needed.
#
# Credits: Heavily inspired by sysupgrade from OpenWrt (https://github.com/openwrt/openwrt/blob/master/package/base-files/files/sbin/sysupgrade)

readonly OS_RELEASE=/media/rfs/ro/usr/lib/os-release
readonly OS_RELEASE_OLD=/media/rfs/rw/upperdir/etc/os-release.old
readonly LOG_DIR=/media/rfs/rw/upperdir/var/lib/sysupgrade

mkdir -p $LOG_DIR
rm -f $LOG_DIR/sysupgrade.log

if [ ! -f $OS_RELEASE_OLD ]; then
    mkdir -p "$(dirname "$OS_RELEASE_OLD")"
    cp $OS_RELEASE $OS_RELEASE_OLD.tmp
    sync
    mv $OS_RELEASE_OLD.tmp $OS_RELEASE_OLD
    echo "First startup - nothing to do" | tee -a $LOG_DIR/sysupgrade.log
    exit 0
fi

if cmp -s $OS_RELEASE_OLD $OS_RELEASE &&
    [ ! -f /media/rfs/rw/upperdir/usr/lib/os-release ] &&
    [ ! -f /media/rfs/rw/upperdir/etc/os-release ]; then
    echo "System not changed since last startup" | tee -a $LOG_DIR/sysupgrade.log
    exit 0
fi

fw_printenv -n swupdate_done 1>/dev/null 2>&-
# shellcheck disable=SC2181
if [ $? -ne 0 ]; then
    echo "WARNING: System got updated by other means than SWUpdate!" | tee -a $LOG_DIR/sysupgrade.log >&2
else
    fw_setenv swupdate_done
fi

# Keep output for inspection after reboot
/usr/bin/overlayfs-purge -f >$LOG_DIR/overlayfs-purge-stdout.log 2>$LOG_DIR/overlayfs-purge-stderr.log

# The merged directory does not always correctly reflect the fact we just deleted many files in the upperdir.
# Remount the rootfs to "commit" the changes.
mount / -o remount

# TODO: Implement data migration using scripts (SG-10427)
# ...

# Ease debugging: Print the release changes
touch $OS_RELEASE_OLD
echo "System has been upgraded:" | tee -a $LOG_DIR/sysupgrade.log
diff $OS_RELEASE_OLD $OS_RELEASE | tee -a $LOG_DIR/sysupgrade.log

# Prevent this script from running on the next startup
cp $OS_RELEASE $OS_RELEASE_OLD.tmp
sync
mv $OS_RELEASE_OLD.tmp $OS_RELEASE_OLD

sync
