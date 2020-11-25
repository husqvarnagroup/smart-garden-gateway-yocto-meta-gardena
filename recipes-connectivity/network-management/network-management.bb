DESCRIPTION = "Gardena Network Management"
MAINTAINER = "Gardena GmbH"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "MIT"

LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

RDEPENDS_${PN} += " \
    openvpn \
    socat \
    wpa-supplicant-passphrase \
"

DEPENDS += " \
    libevdev \
"

PV = "1.3.5"
PR = "r0"

S = "${WORKDIR}/"

FILES_${PN} += " \
    ${bindir}/ap_button_listener \
    ${systemd_unitdir}/system/ap_button_listener.service \
    ${systemd_unitdir}/system/network_management.service \
"

SRC_URI = " \
    file://ap_button_listener.c \
    file://ap_button_listener.service \
    file://network_management.service \
    file://network_management.sh \
"

do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} \
        ${WORKDIR}/ap_button_listener.c \
        $(pkg-config --libs --cflags libevdev) \
        -o ap_button_listener -Wall -Wextra -Wpedantic -Werror
}

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${WORKDIR}/ap_button_listener ${D}${bindir}/
    install -m 755 ${S}network_management.sh ${D}${bindir}/network_management

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/ap_button_listener.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/network_management.service ${D}${systemd_unitdir}/system/
}

inherit systemd pkgconfig
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = " \
    ap_button_listener.service \
    network_management.service \
"
