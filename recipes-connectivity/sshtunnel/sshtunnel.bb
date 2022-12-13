DESCRIPTION = "SSH Tunnel"
MAINTAINER = "Gardena GmbH"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
PR = "r1"

inherit allarch systemd

SRC_URI = " \
    file://sshtunnel.service \
    file://sshtunnel.timer \
    file://sshtunnel.sh \
"

FILES_${PN} = " \
    ${systemd_unitdir}/system/sshtunnel.service \
    ${systemd_unitdir}/system/sshtunnel.timer \
    ${sbindir}/sshtunnel \
"

RDEPENDS_${PN} = " \
    busybox \
    curl \
    dropbear \
    jq \
    openssl \
"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshtunnel.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshtunnel.timer ${D}${systemd_unitdir}/system

    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/sshtunnel.sh ${D}${sbindir}/sshtunnel
}

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = " \
    sshtunnel.timer \
"
