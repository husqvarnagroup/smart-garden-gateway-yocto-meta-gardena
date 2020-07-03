DESCRIPTION = "Store traffic locally to allow post mortem analysis"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

RDEPENDS_${PN} = "tcpdump"

PV = "0.3"
PR = "r1"

SRC_URI = "\
    file://${BPN}-ppp0.service \
    file://${BPN}-vpn0.service \
"

S = "${WORKDIR}/"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/${BPN}-ppp0.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/${BPN}-vpn0.service ${D}${systemd_unitdir}/system
}

INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_PACKAGE_STRIP = "1"

inherit systemd allarch
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = " \
    ${BPN}-ppp0.service \
    ${BPN}-vpn0.service \
"
