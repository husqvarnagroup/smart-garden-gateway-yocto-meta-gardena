SUMMARY = "coap-transport-proxy"
HOMEPAGE = "https://dev.azure.com/HQV-Gardena/SG-Gateway/_git/sg-coap-transport-proxy"
LICENSE = "CLOSED"

inherit cargo cargo-update-recipe-crates
inherit pkgconfig
inherit systemd

SRC_URI += "gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-coap-transport-proxy;protocol=ssh;nobranch=1;branch=main;tag=v${PV}"
S = "${WORKDIR}/git"
CARGO_SRC_DIR = ""

PR = "r0"

SRC_URI += " \
    git://github.com/husqvarnagroup/rust-coap-client.git;protocol=https;nobranch=1;name=coap-client;destsuffix=coap-client \
    file://THIRDPARTY.toml \
    file://coap-transport-proxy.service \
"

DEPENDS += "openssl"
RCONFLICTS:${PN} += "python3-coap-transport-proxy"

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    coap-transport-proxy.service \
"

LIC_FILES_CHKSUM += "file://../THIRDPARTY.toml;md5=041b93a136f7aff40102d91ec56402e0"

SRCREV_FORMAT .= "_coap-client"
SRCREV_coap-client = "8803d017fba4f1fe61b518448b02b6e474a6a6a1"
EXTRA_OECARGO_PATHS += "${WORKDIR}/coap-client"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/coap-transport-proxy.service ${D}${systemd_unitdir}/system
}

require coap-transport-proxy-crates.inc
