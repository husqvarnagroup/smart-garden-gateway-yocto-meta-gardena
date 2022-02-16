PR = "r3"

DEPENDS += "openssl accessory-server"
RDEPENDS_${PN} += "gateway-config-frontend openssl openssl-bin openssl-conf"

SRC_URI += "\
    file://gateway-config-backend-sslkey.service \
    file://gateway-config-backend.service \
    file://gateway-config-backend.socket \
    file://keep.d/gateway-config-backend \
    "

# BUG: meta-rust doesn't add the include directories to bindgen runs
export BINDGEN_EXTRA_CLANG_ARGS="-I${STAGING_INCDIR}"
export CARGO_FEATURE_STD="1"

do_install () {
    cargo_do_install

    install -d ${D}${datadir}/gateway-config-interface/www
    install -m 0644 ${S}/www/simple.html ${D}${datadir}/gateway-config-interface/www
    ln -s ${sysconfdir}/gateway-config-interface/key.pem ${sysconfdir}/gateway-config-interface/cert.pem ${D}${datadir}/gateway-config-interface

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/gateway-config-backend-sslkey.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/gateway-config-backend.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/gateway-config-backend.socket ${D}${systemd_unitdir}/system

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/gateway-config-backend ${D}${base_libdir}/upgrade/keep.d
}

FILES_${PN} += "\
    ${datadir}/gateway-config-interface \
    ${systemd_unitdir}/system/gateway-config-backend-sslkey.service \
    ${base_libdir}/upgrade/keep.d/gateway-config-backend \
    "

inherit systemd

SYSTEMD_SERVICE_${PN} = " \
    gateway-config-backend.service \
    gateway-config-backend.socket \
"
