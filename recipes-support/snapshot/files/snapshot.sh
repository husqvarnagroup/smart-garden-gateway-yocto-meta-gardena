#!/bin/ash
# shellcheck shell=dash

gwid=$(/sbin/fw_printenv -n gatewayid)
readonly SNAPSHOT_DIR="/tmp/${gwid}_snapshot"
readonly SNAPSHOT_TAR="${SNAPSHOT_DIR}.tar.gz"

rm -rf "${SNAPSHOT_DIR}"
rm -f "${SNAPSHOT_TAR}"
mkdir "${SNAPSHOT_DIR}"
cd /tmp/ || exit 1

##################################
# GARDENA services state         #
##################################
cp -r /var/lib/lemonbeatd "${SNAPSHOT_DIR}/"
cp -r /var/lib/lwm2mserver "${SNAPSHOT_DIR}/"
cp -r /var/lib/cloudadapter "${SNAPSHOT_DIR}/"

##################################
# Generic base image information #
##################################
mkdir "${SNAPSHOT_DIR}/etc"
cp /etc/os-release "${SNAPSHOT_DIR}/etc/"
cp /etc/os-release.old "${SNAPSHOT_DIR}/etc/"
cp /etc/build "${SNAPSHOT_DIR}/etc/"
cp -r /var/lib/sysupgrade "${SNAPSHOT_DIR}/"
cp /etc/resolv.conf "${SNAPSHOT_DIR}/etc/"

##################################
# Relevant runtime information   #
##################################
mkdir "${SNAPSHOT_DIR}/runtime"
/bin/df -h > "${SNAPSHOT_DIR}/runtime/df"
/usr/sbin/ubinfo -a > "${SNAPSHOT_DIR}/runtime/ubinfo"
/sbin/ip address > "${SNAPSHOT_DIR}/runtime/ip-address"
/sbin/ip route > "${SNAPSHOT_DIR}/runtime/ip-route"
/sbin/ip -6 route > "${SNAPSHOT_DIR}/runtime/ip6-route"
/sbin/ip link > "${SNAPSHOT_DIR}/runtime/ip-link"
/bin/journalctl | /bin/gzip > "${SNAPSHOT_DIR}/runtime/journalctl.gz"
/bin/systemctl > "${SNAPSHOT_DIR}/runtime/systemctl"
/bin/systemctl status > "${SNAPSHOT_DIR}/runtime/systemctl-status"
/sbin/fw_printenv > "${SNAPSHOT_DIR}/runtime/fw_printenv"
/bin/date > "${SNAPSHOT_DIR}/runtime/date"
/usr/bin/uptime > "${SNAPSHOT_DIR}/runtime/uptime"
/bin/mount > "${SNAPSHOT_DIR}/runtime/mount"
/bin/netstat -tulen > "${SNAPSHOT_DIR}/runtime/netstat"
/usr/bin/timedatectl > "${SNAPSHOT_DIR}/runtime/timedatectl"
/bin/dmesg > "${SNAPSHOT_DIR}/runtime/dmesg"
/usr/bin/free > "${SNAPSHOT_DIR}/runtime/free"
/usr/sbin/iw wlan0 station dump > "${SNAPSHOT_DIR}/runtime/iw-wlan0-station-dump"
/usr/sbin/iw dev > "${SNAPSHOT_DIR}/runtime/iw-dev"
/bin/ps > "${SNAPSHOT_DIR}/runtime/ps"
/usr/bin/top -bn1 > "${SNAPSHOT_DIR}/runtime/top"
[ -f /usr/sbin/lsof ] && /usr/sbin/lsof > "${SNAPSHOT_DIR}/runtime/lsof"
if [ -f /sys/devices/virtual/misc/bootcount/bootcount ]; then
    cp /sys/devices/virtual/misc/bootcount/bootcount "${SNAPSHOT_DIR}/runtime/bootcount"
fi
/usr/bin/healthcheck  > "${SNAPSHOT_DIR}/runtime/healthcheck" 2>&1 || true
/bin/networkctl > "${SNAPSHOT_DIR}/runtime/networkctl"
/bin/networkctl status ppp0 > "${SNAPSHOT_DIR}/runtime/networkctl-ppp0"
/usr/bin/systemd-cgtop --batch --iterations=1 --order=memory --cpu=time > "${SNAPSHOT_DIR}/runtime/systemd-cgtop-cpu-time"
/usr/bin/systemd-cgtop --batch --iterations=2 --order=cpu --cpu=percentage --delay=5s > "${SNAPSHOT_DIR}/runtime/systemd-cgtop-cpu-percentage"

##################################
# Systemd units                  #
##################################
for s in \
    accessory-server.service \
    cloudadapter.service \
    environment.service \
    fwrolloutd.service \
    gateway-config-backend.service \
    gateway-config-backend.socket \
    healthcheck.service \
    internet-led.service \
    iptables.service \
    lemonbeatd.service \
    lwm2mserver.service \
    mdns.service \
    openvpn.service \
    power-led.service \
    ppp.service \
    rm-flashing.service \
    rsyslog.service \
    swupdate-check.timer \
    swupdate-progress.service \
    sysupgrade.service \
  ; do systemctl is-failed "${s}" >/dev/null && systemctl status "${s}" > "${SNAPSHOT_DIR}/runtime/systemctl-status-${s}"
done

##################################
# User data                      #
##################################
mkdir "${SNAPSHOT_DIR}/runtime/user"
cp /etc/sysupgrade.conf "${SNAPSHOT_DIR}/runtime/user/"
/usr/bin/opkg list-installed > "${SNAPSHOT_DIR}/runtime/user/opkg-list-installed"

##################################
# tcpdump files                  #
##################################
if [ -d /tmp/tcpdump-sherlock ]; then
    cp -r /tmp/tcpdump-sherlock "${SNAPSHOT_DIR}/runtime/"
fi

##################################
# Create tarball & delete files  #
##################################
tar cfz "${SNAPSHOT_TAR}" "./$(basename "${SNAPSHOT_DIR}")"
rm -rf "${SNAPSHOT_DIR}"
