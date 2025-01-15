SUMMARY = "coap-transport-proxy"
HOMEPAGE = "https://dev.azure.com/HQV-Gardena/SG-Gateway/_git/sg-coap-transport-proxy"
LICENSE = "CLOSED"

inherit cargo cargo-update-recipe-crates
inherit pkgconfig
inherit systemd

SRCREV = "6063a1439af24dab3cc33649a0492f71964b0511"
SRC_URI += "gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-coap-transport-proxy;protocol=ssh;branch=main"
S = "${WORKDIR}/git"
CARGO_SRC_DIR = ""

PR = "r1"

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

LIC_FILES_CHKSUM += "file://../THIRDPARTY.toml;md5=3d821b412230a84eef2cb336893d9bd8"

SRCREV_FORMAT .= "_coap-client"
SRCREV_coap-client = "eb8dc5bb6012cf91e38d87efe033267e01f9d47d"
EXTRA_OECARGO_PATHS += "${WORKDIR}/coap-client"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/coap-transport-proxy.service ${D}${systemd_unitdir}/system
}

require coap-transport-proxy-crates.inc
