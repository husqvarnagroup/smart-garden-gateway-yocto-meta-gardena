#!/bin/sh
# shellcheck shell=dash

set -eu -o pipefail

echo Started
curl -sS http://speedtest.tele2.net/10MB.zip -o /dev/null
echo Finished
