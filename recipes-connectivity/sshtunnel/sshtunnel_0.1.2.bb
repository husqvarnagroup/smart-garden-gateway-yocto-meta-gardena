DESCRIPTION = "SSH Tunnel"
MAINTAINER = "Gardena GmbH"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
PR = "r1"

inherit allarch systemd

SRC_URI = " \
    file://sshtunnel.service \
    file://sshtunnel-check.service \
    file://sshtunnel-check.timer \
    file://sshtunnel.sh \
    file://sshtunnel-check.sh \
    file://keep.d/sshtunnel \
"

FILES:${PN} = " \
    ${systemd_unitdir}/system/sshtunnel.service \
    ${systemd_unitdir}/system/sshtunnel-check.service \
    ${systemd_unitdir}/system/sshtunnel-check.timer \
    ${sbindir}/sshtunnel \
    ${sbindir}/sshtunnel-check \
    ${base_libdir}/upgrade/keep.d \
"

RDEPENDS:${PN} = " \
    busybox \
    curl \
    dropbear \
    jq \
    openssl \
"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshtunnel.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshtunnel-check.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshtunnel-check.timer ${D}${systemd_unitdir}/system

    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/sshtunnel.sh ${D}${sbindir}/sshtunnel
    install -m 0755 ${WORKDIR}/sshtunnel-check.sh ${D}${sbindir}/sshtunnel-check

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/sshtunnel ${D}${base_libdir}/upgrade/keep.d
}

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    sshtunnel-check.timer \
"
