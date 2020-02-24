DESCRIPTION = "Gardena Network Management"
MAINTAINER = "Gardena GmbH"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "MIT"

LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

RDEPENDS_${PN} += " \
    openvpn \
    python3-evdev \
    socat \
    wpa-supplicant-passphrase \
"

PR = "r6"

S = "${WORKDIR}/"

FILES_${PN} += " \
    ${bindir}/ap_button_listener \
    ${systemd_unitdir}/system/ap_button_listener.service \
    ${systemd_unitdir}/system/network_management.service \
"

SRC_URI = " \
    file://ap_button_listener.py \
    file://ap_button_listener.service \
    file://network_management.service \
    file://network_management.sh \
"

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${S}ap_button_listener.py ${D}${bindir}/ap_button_listener
    install -m 755 ${S}network_management.sh ${D}${bindir}/network_management

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/ap_button_listener.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/network_management.service ${D}${systemd_unitdir}/system/
}

inherit allarch systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = " \
    ap_button_listener.service \
    network_management.service \
"
