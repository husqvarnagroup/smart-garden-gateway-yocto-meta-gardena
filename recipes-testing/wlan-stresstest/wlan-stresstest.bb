DESCRIPTION = "Reboot until the Wi-Fi connectivity breaks"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PV = "0.2.1"
PR = "r0"

RDEPENDS_${PN} = "systemd"

SRC_URI = "\
    file://wlan-stresstest.service \
    file://wlan-stresstest.sh \
"

S = "${WORKDIR}/"

inherit allarch systemd

do_install() {
    install -D -m 0755 ${WORKDIR}/wlan-stresstest.sh ${D}${bindir}/wlan-stresstest
    install -D -m 0644 ${WORKDIR}/${PN}.service ${D}${systemd_unitdir}/system/${PN}.service
}

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "${PN}.service"
