DESCRIPTION = "SSH Tunnel Check"
MAINTAINER = "Gardena GmbH"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
PR = "r0"

inherit allarch systemd

SRC_URI = " \
    file://sshtunnel-check.service \
    file://sshtunnel-check.timer \
    file://sshtunnel-check.sh \
"

FILES:${PN} = " \
    ${systemd_unitdir}/system/sshtunnel-check.service \
    ${systemd_unitdir}/system/sshtunnel-check.timer \
    ${sbindir}/sshtunnel-check \
"

RDEPENDS:${PN} = " \
    busybox \
    sshtunnel \
"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshtunnel-check.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshtunnel-check.timer ${D}${systemd_unitdir}/system

    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/sshtunnel-check.sh ${D}${sbindir}/sshtunnel-check
}

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    sshtunnel-check.timer \
"
