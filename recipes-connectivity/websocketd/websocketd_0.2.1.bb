DESCRIPTION = "WebSocket Daemon"
HOMEPAGE = "https://github.com/husqvarnagroup/smart-garden-gateway-websocketd"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=1ebbd3e34237af26da5dc08a4e440464 \
"

DEPENDS += "openssl pkgconfig-native mdns"

PR = "r0"
SRCREV = "081dd89ad029d1173aa5ab8619184b2cb6ad3cec"
SRCREV_gardenalog = "687e6cfeb79f62735dd47e74a0fa387b7f58c8c6"
SRCREV_sg-ipc = "687e6cfeb79f62735dd47e74a0fa387b7f58c8c6"
SRCREV_tokioutil = "687e6cfeb79f62735dd47e74a0fa387b7f58c8c6"
SRCREV_astro-dnssd = "cffb3a70725e52ed7cc684f114a0586ec5dea7b6"
SRC_URI = " \
    git://github.com/husqvarnagroup/smart-garden-gateway-websocketd.git;protocol=https;branch=main \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=gardenalog;subpath=gardenalog;destsuffix=gardenalog \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=sg-ipc;subpath=sg-ipc;destsuffix=sg-ipc \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=tokioutil;subpath=tokioutil;destsuffix=tokioutil \
    git://github.com/AstroHQ/astro-dnssd.git;protocol=https;nobranch=1;name=astro-dnssd;destsuffix=astro-dnssd \
    file://websocketd.service \
    file://keep.d/websocketd \
"

SRCREV_FORMAT = "websocketd"


inherit cargo cargo-update-recipe-crates

CARGO_SRC_DIR = ""

RUSTFLAGS += "-latomic"

do_install:append() {
    install -Dm 0644 ${UNPACKDIR}/websocketd.service ${D}${systemd_unitdir}/system/websocketd.service
    install -Dm 0644 ${UNPACKDIR}/keep.d/websocketd ${D}${base_libdir}/upgrade/keep.d/websocketd
}

FILES:${PN} += " \
    ${base_libdir}/upgrade/keep.d \
"

RDEPENDS:${PN} += "gateway-config-backend-cert"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    websocketd.service \
"

SRC_URI += " \
    file://THIRDPARTY.toml \
"

LIC_FILES_CHKSUM += " \
    file://../THIRDPARTY.toml;md5=802475b37f4b7bc2b8ab41bd69e52d8f \
"

include websocketd-crates.inc
