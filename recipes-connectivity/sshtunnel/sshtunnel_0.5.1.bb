DESCRIPTION = "SSH Tunnel"
MAINTAINER = "Gardena GmbH"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
PR = "r0"

inherit allarch systemd

SRC_URI = " \
    file://sshtunnel.service \
    file://sshtunnel-button.service \
    file://sshtunnel-button.sh \
    file://sshtunnel-shutdown.service \
    file://sshtunnel-shutdown.timer \
    file://sshtunnel.sh \
    file://keep.d/sshtunnel \
"

FILES:${PN} = " \
    ${systemd_unitdir}/system/sshtunnel.service \
    ${systemd_unitdir}/system/sshtunnel-button.service \
    ${systemd_unitdir}/system/sshtunnel-shutdown.service \
    ${systemd_unitdir}/system/sshtunnel-shutdown.timer \
    ${sbindir}/sshtunnel-button \
    ${sbindir}/sshtunnel \
    ${base_libdir}/upgrade/keep.d \
"

RDEPENDS:${PN} = " \
    busybox \
    evtest \
    curl \
    dropbear \
    jq \
    openssl \
"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshtunnel.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshtunnel-button.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshtunnel-shutdown.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshtunnel-shutdown.timer ${D}${systemd_unitdir}/system

    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/sshtunnel.sh ${D}${sbindir}/sshtunnel
    install -m 0755 ${WORKDIR}/sshtunnel-button.sh ${D}${sbindir}/sshtunnel-button

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/sshtunnel ${D}${base_libdir}/upgrade/keep.d
}

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    sshtunnel-button.service \
"
