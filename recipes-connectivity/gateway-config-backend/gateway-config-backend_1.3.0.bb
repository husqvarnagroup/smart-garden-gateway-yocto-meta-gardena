inherit cargo cargo-update-recipe-crates
inherit systemd
inherit pkgconfig

SUMMARY = "Backend component for the GARDENA smart Gateway config interface"
HOMEPAGE = "https://github.com/husqvarnagroup/smart-garden-gateway-config-backend"
LICENSE = "GPL-3.0-or-later"

SRC_URI += "git://github.com/husqvarnagroup/smart-garden-gateway-config-backend;protocol=https;branch=main"
S = "${WORKDIR}/git"
CARGO_SRC_DIR = ""

PR = "r0"
SRCREV = "61069d1e8c0b28f21146dd2df998077c451b32b1"
SRCREV_gardenalog = "687e6cfeb79f62735dd47e74a0fa387b7f58c8c6"
SRC_URI += "\
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=gardenalog;subpath=gardenalog;destsuffix=gardenalog \
    file://gateway-config-backend.service \
    file://gateway-config-backend.socket \
    file://THIRDPARTY.toml \
"

SRCREV_FORMAT = "gateway-config-backend"

LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=75d892af193fd5a298f724c4377d8f62 \
    file://../THIRDPARTY.toml;md5=9a7eaec6c665e3dc52849dff4cc017ad \
"

DEPENDS += "openssl dbus"

RDEPENDS:${PN} += "gateway-config-frontend gateway-config-backend-cert"


# BUG: meta-rust doesn't add the include directories to bindgen runs
export BINDGEN_EXTRA_CLANG_ARGS="-I${STAGING_INCDIR}"
export CARGO_FEATURE_STD="1"

export CARGO_PROFILE_RELEASE_OPT_LEVEL="z"
export CARGO_PROFILE_RELEASE_CODEGEN_UNITS="1"

do_install () {
    cargo_do_install

    install -d ${D}${datadir}/gateway-config-interface/www
    ln -s ${sysconfdir}/gateway-config-interface/key.pem ${sysconfdir}/gateway-config-interface/cert.pem ${D}${datadir}/gateway-config-interface

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/gateway-config-backend.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/gateway-config-backend.socket ${D}${systemd_unitdir}/system
}

FILES:${PN} += "\
    ${datadir}/gateway-config-interface \
"

SYSTEMD_SERVICE:${PN} = " \
    gateway-config-backend.service \
    gateway-config-backend.socket \
"

require gateway-config-backend-crates.inc
