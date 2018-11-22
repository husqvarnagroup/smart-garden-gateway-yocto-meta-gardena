#!/bin/sh

set -u

# Check upgrade flag to figure out if upgrade logic needs to run
#
# Warning: Appart from SWUpdate, other reasons may cause the currently running
#          system to be different from the previously one.
#          E.g. U-Boot falling back an old version on the second bootslot.
fw_printenv -n swupdate_done 1>/dev/null 2>&-
if [ $? -ne 0 ]; then
    echo "System was not upgraded by SWUpdate"
    exit 0
fi

# TODO: Implement actual data migration (SG-10427)

# Erase the upgrade flag
fw_setenv swupdate_done
if [ $? -ne 0 ]; then
    echo "Failed to erase the swupdate_done flag in U-Boot"
    exit 1
fi

echo "Finished data migration"
