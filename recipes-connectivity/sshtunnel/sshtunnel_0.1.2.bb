DESCRIPTION = "SSH Tunnel"
MAINTAINER = "Gardena GmbH"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
PR = "r1"

inherit allarch systemd

SRC_URI = " \
    file://sshtunnel.service \
    file://sshtunnel.sh \
    file://keep.d/sshtunnel \
"

FILES:${PN} = " \
    ${systemd_unitdir}/system/sshtunnel.service \
    ${sbindir}/sshtunnel \
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

    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/sshtunnel.sh ${D}${sbindir}/sshtunnel

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/sshtunnel ${D}${base_libdir}/upgrade/keep.d
}

SYSTEMD_PACKAGES = "${PN}"
