#!/bin/sh

set -eu -o pipefail

echo Started
dd if=/dev/zero bs=10M count=1 2>/dev/null | curl -sS --upload-file - http://speedtest.tele2.net/upload.php -o /dev/null
echo Finished
