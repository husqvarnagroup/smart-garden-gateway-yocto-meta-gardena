SUMMARY = "TLS certificate generator for GARDENA smart Gateway config interface"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PR = "r0"

SRC_URI = "\
    file://gateway-config-backend-sslkey.service \
    file://keep.d/gateway-config-backend \
"

S = "${WORKDIR}"

inherit systemd

SYSTEMD_SERVICE:${PN} = "gateway-config-backend-sslkey.service"

do_install () {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/gateway-config-backend-sslkey.service ${D}${systemd_unitdir}/system

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/gateway-config-backend ${D}${base_libdir}/upgrade/keep.d
}

FILES:${PN} += "\
    ${systemd_unitdir}/system/gateway-config-backend-sslkey.service \
    ${base_libdir}/upgrade/keep.d/gateway-config-backend \
"

RDEPENDS:${PN} += " \
    openssl \
    openssl-bin \
    openssl-conf \
"
