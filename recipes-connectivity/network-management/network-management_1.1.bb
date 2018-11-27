DESCRIPTION = "Gardena OpenVPN"
MAINTAINER = "Gardena GmbH"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "Proprietary"

LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

RDEPENDS_${PN} += " \
    inotify-tools \
    openvpn \
"

PR = "r0"

S = "${WORKDIR}/"

SRC_URI = " \
    file://network_management.service \
    file://network_management \
"

do_install() {
    install -m 700 -d ${D}${bindir}
    install -m 700 ${S}network_management ${D}${bindir}

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/network_management.service ${D}${systemd_unitdir}/system/
}

inherit allarch systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "network_management.service"
SYSTEMD_AUTO_ENABLE = "disable"
