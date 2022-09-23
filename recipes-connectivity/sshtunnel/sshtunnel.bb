DESCRIPTION = "SSH Tunnel"
MAINTAINER = "Gardena GmbH"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
PR = "r0"

inherit allarch systemd

SRC_URI = " \
    file://sshtunnel.service \
    file://sshtunnel.sh \
"

RDEPENDS_${PN} = " \
    busybox \
    jq \
"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/sshtunnel.service ${D}${systemd_unitdir}/system

    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/sshtunnel.sh ${D}${sbindir}/sshtunnel
}

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = " \
    sshtunnel.service \
"
