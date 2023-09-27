DEPENDS += "openssl"
RCONFLICTS:${PN} += "python3-coap-transport-proxy"

SRC_URI += " \
    file://coap-transport-proxy.service \
"

do_install_append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/coap-transport-proxy.service ${D}${systemd_unitdir}/system
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    coap-transport-proxy.service \
"
