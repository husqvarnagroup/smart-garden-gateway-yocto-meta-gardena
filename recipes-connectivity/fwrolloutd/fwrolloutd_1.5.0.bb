inherit cargo cargo-update-recipe-crates
inherit pkgconfig
inherit systemd

SUMMARY = "fwrolloutd"
HOMEPAGE = "git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-firmware-rollout"
LICENSE = "Proprietary"

SRC_URI += "gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-firmware-rollout;protocol=ssh;branch=main;tag=v${PV}"
S = "${WORKDIR}/git"
CARGO_SRC_DIR = "fwrolloutd"

LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=0557f9d92cf58f2ccdd50f62f8ac0b28 \
    file://../THIRDPARTY.toml;md5=2d2762287b1ad3e5819604af0814847f \
"

PR = "r1"

SRC_URI += " \
    git://github.com/husqvarnagroup/nng-rs.git;protocol=https;nobranch=1;name=nng;destsuffix=nng-rs \
    gitsm://github.com/husqvarnagroup/nng-rust.git;protocol=https;branch=nng-v1.7.3;name=nng-sys;destsuffix=nng-sys \
"

SRCREV_FORMAT = "fwrolloutd"

SRCREV_nng = "13828ad0f3a8044dd9ab1265c3d5c88b5fcfce1f"
EXTRA_OECARGO_PATHS += "${WORKDIR}/nng-rs"

SRCREV_nng-sys = "96b98092f610c379c60cefd0973427146be0380e"
EXTRA_OECARGO_PATHS += "${WORKDIR}/nng-sys"

DEPENDS += "openssl"
RDEPENDS:${PN} += " \
     ipso-registry \
     openssl \
     openssl-bin openssl-conf \
"

SRC_URI += " \
    file://fwrolloutd.service \
    file://THIRDPARTY.toml \
"

export CARGO_PROFILE_RELEASE_OPT_LEVEL="z"
export CARGO_PROFILE_RELEASE_CODEGEN_UNITS="1"

FILES:${PN} += " \
    ${systemd_unitdir}/system/ \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/fwrolloutd.service ${D}${systemd_unitdir}/system/

    install -d ${D}${sysconfdir}
    install -m 0644 ${S}/fwrolloutd/config.yml ${D}${sysconfdir}/fwrolloutd.yml

    # Ensure required IPSO directories exist
    install -d ${D}${localstatedir}/lib/ipso_definitions/base
    install -d ${D}${localstatedir}/lib/ipso_definitions/fwrolloutd
}

SYSTEMD_SERVICE:${PN} = "fwrolloutd.service"

RUSTFLAGS += "-latomic"

require fwrolloutd-crates.inc
