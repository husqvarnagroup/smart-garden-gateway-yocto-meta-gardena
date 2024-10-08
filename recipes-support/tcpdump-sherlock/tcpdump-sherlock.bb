DESCRIPTION = "Store traffic locally to allow post mortem analysis"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "0.5"
PR = "r0"

SRC_URI = "\
    file://${BPN}-ppp0.service \
    file://99-${BPN}-ppp0.rules \
"

S = "${WORKDIR}"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/${BPN}-ppp0.service ${D}${systemd_unitdir}/system

    install -d ${D}${nonarch_base_libdir}/udev/rules.d
    install -m 0644 ${WORKDIR}/99-${BPN}-ppp0.rules ${D}${nonarch_base_libdir}/udev/rules.d/
}

INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_PACKAGE_STRIP = "1"

RDEPENDS:${PN}-ppp0 = "tcpdump"

FILES:${PN}-ppp0 = " \
    ${nonarch_base_libdir}/udev/rules.d \
    ${systemd_unitdir}/system/${BPN}-ppp0.service \
"

PACKAGES = "${PN}-ppp0"

inherit systemd allarch
SYSTEMD_PACKAGES = "${PN}-ppp0"

SYSTEMD_SERVICE:${PN}-ppp0 = " \
    ${BPN}-ppp0.service \
"
SYSTEMD_AUTO_ENABLE:${PN}-ppp0 = "enable"
