#!/bin/sh

rm -rf /tmp/snapshot
rm -f /tmp/snapshot.tgz
mkdir /tmp/snapshot
cd /tmp/ || exit 1

##################################
# Shadoway stuff                 #
##################################
cp -r /var/lib/shadoway /tmp/snapshot/
cp -r /var/run/log/shadoway/ /tmp/snapshot/shadoway/shadoway-volatile-logs
if [ -f /var/run/shadoway/led_status ]; then
  cp /var/run/shadoway/led_status /tmp/snapshot/shadoway/shadoway-led_status
else
  echo 'led_status not (yet) available' > /tmp/snapshot/shadoway/shadoway-led_status
fi
cp /etc/seluxit_env /tmp/snapshot/shadoway/
# remove password & network key from snapshot for real customers
# shellcheck disable=SC1091
. /etc/seluxit_env
if [ "$SELUXIT_ENV" != "qa" ] && [ "$SELUXIT_ENV" != "dev" ]; then
    sed -i '/"network_key"\|"password"/d' /tmp/snapshot/shadoway/work/Gateway.json
    rm -f /tmp/snapshot/shadoway/work/Network_management/Network_key.json
fi

##################################
# Generic base image information #
##################################
mkdir /tmp/snapshot/etc/
cp /etc/os-release /tmp/snapshot/etc/
cp /etc/os-release.old /tmp/snapshot/etc/
cp /etc/build /tmp/snapshot/etc/
cp -r /var/lib/sysupgrade /tmp/snapshot/
cp /etc/resolv.conf /tmp/snapshot/etc/

##################################
# Relevant runtime information   #
##################################
mkdir /tmp/snapshot/runtime
/bin/df -h > /tmp/snapshot/runtime/df
/usr/sbin/ubinfo -a > /tmp/snapshot/runtime/ubinfo
/sbin/ip address > /tmp/snapshot/runtime/ip-address
/sbin/ip route > /tmp/snapshot/runtime/ip-route
/sbin/ip -6 route > /tmp/snapshot/runtime/ip6-route
/sbin/ip link > /tmp/snapshot/runtime/ip-link
/bin/journalctl --no-pager > /tmp/snapshot/runtime/jouralctl
/bin/systemctl > /tmp/snapshot/runtime/systemctl
/bin/systemctl status > /tmp/snapshot/runtime/systemctl-status
/sbin/fw_printenv > /tmp/snapshot/runtime/fw_printenv
/bin/date > /tmp/snapshot/runtime/date
/usr/bin/uptime > /tmp/snapshot/runtime/uptime
/bin/mount > /tmp/snapshot/runtime/mount
/bin/netstat -tulen > /tmp/snapshot/runtime/netstat
/usr/bin/timedatectl > /tmp/snapshot/runtime/timedatectl
/bin/dmesg > /tmp/snapshot/runtime/dmesg
/usr/bin/free > /tmp/snapshot/runtime/free
/usr/sbin/iw wlan0 station dump > /tmp/snapshot/runtime/iw-wlan0-station-dump
/usr/sbin/iw dev > /tmp/snapshot/runtime/iw-dev
/bin/ps > /tmp/snapshot/runtime/ps
/usr/bin/top -bn1 > /tmp/snapshot/runtime/top

##################################
# User data                      #
##################################
mkdir /tmp/snapshot/runtime/user/
cp /etc/sysupgrade.conf /tmp/snapshot/runtime/user/
/usr/bin/opkg list-installed > /tmp/snapshot/runtime/user/opkg-list-installed

##################################
# tcpdump files                  #
##################################
if [ -d /tmp/tcpdump-sherlock ]; then
    cp -r /tmp/tcpdump-sherlock /tmp/snapshot/runtime/
fi

##################################
# Create tarball & delete files  #
##################################
tar cfz "$(/sbin/fw_printenv -n gatewayid)_snapshot.tar.gz" ./snapshot
rm -rf /tmp/snapshot
