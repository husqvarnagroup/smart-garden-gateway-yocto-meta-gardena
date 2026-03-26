DESCRIPTION = "WebSocket Daemon"
HOMEPAGE = "https://github.com/husqvarnagroup/smart-garden-gateway-websocketd"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=1ebbd3e34237af26da5dc08a4e440464 \
"

DEPENDS += "openssl pkgconfig-native"

PR = "r0"
SRCREV = "52bc992c1b3c8f04bd3a674faf32b7ffddfcaee6"
SRCREV_gardenalog = "43e8c7b2784281c3fd37ea0f2e583baad5a018e4"
SRCREV_sg-ipc = "43e8c7b2784281c3fd37ea0f2e583baad5a018e4"
SRCREV_tokioutil = "43e8c7b2784281c3fd37ea0f2e583baad5a018e4"
SRC_URI = " \
    git://github.com/husqvarnagroup/smart-garden-gateway-websocketd.git;protocol=https;branch=main \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=gardenalog;subpath=gardenalog;destsuffix=gardenalog \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=sg-ipc;subpath=sg-ipc;destsuffix=sg-ipc \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=tokioutil;subpath=tokioutil;destsuffix=tokioutil \
    file://websocketd.service \
    file://keep.d/websocketd \
"

SRCREV_FORMAT = "websocketd"

S = "${WORKDIR}/git"

inherit cargo cargo-update-recipe-crates

CARGO_SRC_DIR = ""

RUSTFLAGS += "-latomic"

do_install:append() {
    install -Dm 0644 ${WORKDIR}/websocketd.service ${D}${systemd_unitdir}/system/websocketd.service
    install -Dm 0644 ${WORKDIR}/keep.d/websocketd ${D}${base_libdir}/upgrade/keep.d/websocketd
}

FILES:${PN} += " \
    ${base_libdir}/upgrade/keep.d \
"

RDEPENDS:${PN} += "gateway-config-backend"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = " \
    websocketd.service \
"

SRC_URI += " \
    file://THIRDPARTY.toml \
"

LIC_FILES_CHKSUM += " \
    file://../THIRDPARTY.toml;md5=6a0cdba1bcafb493cbee0bb179cdad97 \
"

include websocketd-crates.inc
