PR = "r4"

DEPENDS += "openssl"
RDEPENDS_${PN} += "gateway-config-frontend openssl openssl-bin openssl-conf"

# XXX: WORKAROUND FOR RUST COMPILER BUGS
SRC_URI_append_at91sam9x5 = " \
    file://disable-lto.patch \
"

SRC_URI += "\
    file://gateway-config-backend-sslkey.service \
    file://gateway-config-backend.service \
    file://keep.d/gateway-config-backend \
    "

do_install () {
    cargo_do_install

    install -d ${D}${datadir}/gateway-config-interface/www
    install -m 0644 ${S}/www/simple.html ${D}${datadir}/gateway-config-interface/www
    ln -s ${sysconfdir}/gateway-config-interface/key.pem ${sysconfdir}/gateway-config-interface/cert.pem ${D}${datadir}/gateway-config-interface

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/gateway-config-backend-sslkey.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/gateway-config-backend.service ${D}${systemd_unitdir}/system

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/gateway-config-backend ${D}${base_libdir}/upgrade/keep.d
}

FILES_${PN} += "\
    ${datadir}/gateway-config-interface \
    ${systemd_unitdir}/system/gateway-config-backend-sslkey.service \
    ${base_libdir}/upgrade/keep.d/gateway-config-backend \
    "

inherit systemd

SYSTEMD_SERVICE_${PN} = "gateway-config-backend.service"
