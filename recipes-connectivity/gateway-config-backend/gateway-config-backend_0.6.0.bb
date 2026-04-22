inherit cargo cargo-update-recipe-crates
inherit systemd
inherit pkgconfig

SUMMARY = "Backend component for the GARDENA smart Gateway config interface"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "Proprietary"

SRC_URI += "git://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-gateway-config-backend;protocol=ssh;branch=main"
S = "${WORKDIR}/git"
CARGO_SRC_DIR = ""

PR = "r1"
SRCREV = "a7fca9d6f063a250c942834997e94384b5525f98"
SRC_URI += "\
    file://gateway-config-backend.service \
    file://gateway-config-backend.socket \
    file://THIRDPARTY.toml \
"

LIC_FILES_CHKSUM = " \
    file://Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28 \
    file://../THIRDPARTY.toml;md5=86f099f48b5ea1ac0d5179b02c4735fe \
"

DEPENDS += "openssl accessory-server"
RDEPENDS:${PN} += "gateway-config-frontend gateway-config-backend-cert"


# BUG: meta-rust doesn't add the include directories to bindgen runs
export BINDGEN_EXTRA_CLANG_ARGS="-I${STAGING_INCDIR}"
export CARGO_FEATURE_STD="1"

export CARGO_PROFILE_RELEASE_OPT_LEVEL="z"
export CARGO_PROFILE_RELEASE_CODEGEN_UNITS="1"

do_install () {
    cargo_do_install

    install -d ${D}${datadir}/gateway-config-interface/www
    install -m 0644 ${S}/www/simple.html ${D}${datadir}/gateway-config-interface/www
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
