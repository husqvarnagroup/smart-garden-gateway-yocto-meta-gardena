#!/bin/sh

set -ue

# Check upgrade flag to figure out if upgrade logic needs to run
#
# Warning: Apart from SWUpdate, other reasons may cause the currently running
#          system to be different from the previously one.
#          E.g. U-Boot falling back an old version on the second bootslot.
#
# Known limitations:
#  * This script does not (yet) delete any directories
#  * For debugging purposes, the intermediate files in /tmp are not deleted
fw_printenv -n swupdate_done 1>/dev/null 2>&-
if [ $? -ne 0 ]; then
    echo "System was not upgraded by SWUpdate"
    exit 0
fi

# Gather a list of all files which must be preserved
find $(sed -ne '/^[[:space:]]*$/d; /^#/d; p' /etc/sysupgrade.conf /lib/upgrade/keep.d/* 2>/dev/null) -type f -o -type l | sort > /tmp/sysupgrade.to-migrate

# Gather a list of all files which are actually different from the ro rootfs
find /media/rfs/rw/upperdir \( -type f -o -type l -o -type c \) | sed 's|/media/rfs/rw/upperdir||g' | sort > /tmp/sysupgrade.changed

# Create a list of files to be deleted
diff /tmp/sysupgrade.to-migrate /tmp/sysupgrade.changed | grep ^+/ | cut -c 2- > /tmp/sysupgrade.to-delete
diff /tmp/sysupgrade.to-migrate /tmp/sysupgrade.changed | grep -v ^+/ | cut -c 2- | grep ^/ > /tmp/sysupgrade.to-keep

# Actually delete the files
while IFS= read -r full_path; do rm -- "/media/rfs/rw/upperdir${full_path}" ; done < /tmp/sysupgrade.to-delete

# TODO: Implement data migration using scripts (SG-10427)
# ...

# Erase the upgrade flag
fw_setenv swupdate_done
if [ $? -ne 0 ]; then
    echo "Failed to erase the swupdate_done flag in U-Boot"
    exit 1
fi

echo "Finished data migration"
