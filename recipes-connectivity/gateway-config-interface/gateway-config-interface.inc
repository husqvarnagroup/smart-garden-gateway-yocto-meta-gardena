PR = "r0"

DEPENDS += "openssl"
RDEPENDS_${PN} += "openssl openssl-bin openssl-conf"

SRC_URI += "\
    file://gateway-config-interface-sslkey.service \
    file://gateway-config-interface.service \
    file://keep.d/gateway-config-interface \
    "

do_install () {
    cargo_do_install

    install -d ${D}/usr/share/gateway-config-interface/www
    install -m 0644 ${S}/www/simple.html ${D}/usr/share/gateway-config-interface/www
    ln -s /etc/gateway-config-interface/key.pem /etc/gateway-config-interface/cert.pem ${D}/usr/share/gateway-config-interface

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/gateway-config-interface-sslkey.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/gateway-config-interface.service ${D}${systemd_unitdir}/system

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/gateway-config-interface ${D}${base_libdir}/upgrade/keep.d
}

FILES_${PN} += "\
    ${systemd_unitdir}/system/gateway-config-interface-sslkey.service \
    ${base_libdir}/upgrade/keep.d/gateway-config-interface \
    "

inherit systemd

SYSTEMD_SERVICE_${PN} = "gateway-config-interface.service"
