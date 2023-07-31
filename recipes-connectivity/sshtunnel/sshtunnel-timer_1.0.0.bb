DESCRIPTION = "SSH Tunnel Timer"
MAINTAINER = "Gardena GmbH"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
PR = "r0"

inherit allarch systemd

SRC_URI = " \
    file://sshtunnel.timer \
"

FILES:${PN} = " \
    ${systemd_unitdir}/system/sshtunnel.timer \
"

RDEPENDS:${PN} = " \
    sshtunnel \
"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshtunnel.timer ${D}${systemd_unitdir}/system
}

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    sshtunnel.timer \
"
