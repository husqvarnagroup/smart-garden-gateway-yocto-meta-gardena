DEPENDS += "openssl"

SRC_URI += "\
            file://cert.pem \
            file://key.pem \
            file://gateway-config-interface.service \
            "

do_install () {
    cargo_do_install

    install -d ${D}/usr/share/gateway-config-interface
    install -m 0644 ${S}/index.html ${D}/usr/share/gateway-config-interface
    install -m 0644 ${WORKDIR}/cert.pem ${WORKDIR}/key.pem ${D}/usr/share/gateway-config-interface

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/gateway-config-interface.service ${D}${systemd_unitdir}/system
}

inherit systemd

SYSTEMD_SERVICE_${PN} = "gateway-config-interface.service"
