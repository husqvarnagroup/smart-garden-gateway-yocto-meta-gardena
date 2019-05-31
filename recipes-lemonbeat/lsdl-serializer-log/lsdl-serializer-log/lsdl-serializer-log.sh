#!/bin/sh

set -eu -o pipefail

# Enable lsdl-serializer logging only in qa and dev environment
if [ "$SELUXIT_ENV" != "qa" ] && [ "$SELUXIT_ENV" != "dev" ]; then
    exit 0
fi

if ! grep -q /usr/lib/liblsdl-serializer-log.so /etc/ld.so.preload; then
    # Add /usr/lib/liblsdl-serializer-log.so to /etc/ld.so.preload atomically
    if [ -f /etc/ld.so.preload ]; then
        cp /etc/ld.so.preload /etc/ld.so.preload.tmp
    fi
    echo /usr/lib/liblsdl-serializer-log.so >> /etc/ld.so.preload.tmp
    sync
    mv /etc/ld.so.preload.tmp /etc/ld.so.preload
fi
