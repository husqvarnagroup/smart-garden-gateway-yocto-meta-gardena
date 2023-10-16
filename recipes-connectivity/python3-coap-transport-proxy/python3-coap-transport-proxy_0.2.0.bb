DESCRIPTION = "Proxy server allowing clients to open TCP sockets with optional TLS encryption using the CoAP protocol"
MAINTAINER = "Gardena GmbH"
LICENSE = "CLOSED"

PYPI_SRC_URI = " \
    git://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Embedded/sg-coap-transport-proxy-python;protocol=ssh;branch=main;tag=v${PV} \
"

SRC_URI += " \
    file://coap-transport-proxy.service \
"

PR = "r2"

RDEPENDS:${PN} += " \
    python3-core \
    python3-aiocoap \
"

RCONFLICTS:${PN} += "coap-transport-proxy"

inherit pypi setuptools3 allarch

S = "${WORKDIR}/git"

do_install_append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/coap-transport-proxy.service ${D}${systemd_unitdir}/system
}

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    coap-transport-proxy.service \
"
