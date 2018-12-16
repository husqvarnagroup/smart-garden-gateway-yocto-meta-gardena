DESCRIPTION = "Gardena OpenVPN"
MAINTAINER = "Gardena GmbH"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "Proprietary"

LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

RDEPENDS_${PN} += " \
    inotify-tools \
    netcat-openbsd \
    openvpn \
    swconfig \
"

PR = "r3"

S = "${WORKDIR}/"

FILES_${PN} += " \
    ${systemd_unitdir}/system/network_management@.service \
"

SRC_URI = " \
    file://network_management@.service \
    file://network_management.sh \
"

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${S}network_management.sh ${D}${bindir}/network_management

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/network_management@.service ${D}${systemd_unitdir}/system/
}

inherit allarch systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "network_management@prod.service"
SYSTEMD_AUTO_ENABLE = "disable"
