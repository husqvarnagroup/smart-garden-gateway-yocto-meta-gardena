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
    fw_setenv swupdate_done
fi

# Gather a list of all files which must be preserved
find $(sed -ne '/^[[:space:]]*$/d; /^#/d; p' /etc/sysupgrade.conf /lib/upgrade/keep.d/* 2>/dev/null) -type f -o -type l | sort > /tmp/sysupgrade.to-migrate

# Gather a list of all files which are actually different from the ro rootfs
find /media/rfs/rw/upperdir \( -type f -o -type l -o -type c \) | sed 's|/media/rfs/rw/upperdir||g' | sort > /tmp/sysupgrade.changed

# Create a list of files to be deleted
diff /tmp/sysupgrade.to-migrate /tmp/sysupgrade.changed | grep ^+/ | cut -c 2- > /tmp/sysupgrade.to-delete

# Create a list of files NOT to be deleted for debugging purposes
diff /tmp/sysupgrade.to-delete /tmp/sysupgrade.to-migrate | grep ^+/ | cut -c 2- > /tmp/sysupgrade.to-not-delete

# Actually delete the files
while IFS= read -r full_path; do rm -- "/media/rfs/rw/upperdir${full_path}" ; done < /tmp/sysupgrade.to-delete

# TODO: Implement data migration using scripts (SG-10427)
# ...

# Ease debugging: Print the release changes
touch /etc/os-release.old
diff /etc/os-release.old /etc/os-release

# Prevent this script from running on the next startup
cp /etc/os-release /etc/os-release.old

echo "Finished data migration"
