inherit cargo cargo-update-recipe-crates
inherit pkgconfig
inherit systemd

SUMMARY = "fwrolloutd"
HOMEPAGE = "git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-firmware-rollout"
LICENSE = "Proprietary"

# Named, so that SRCREV_fwrolloutd applies. A plain SRCREV would clash with the rev=
# values of the unnamed entries below.
SRCREV_fwrolloutd = "179a57c85d0c40328fb27336f127cf3f0cb4f5d6"
SRCREV_gardenalog = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lwm2m = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lwm2m-types = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lwm2m-objgen = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_sg-ipc = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_tokioutil = "15298a2b49cc1f905d064ea931255026984407c5"
SRC_URI = " \
    gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-firmware-rollout;protocol=ssh;branch=main;name=fwrolloutd \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=gardenalog;subpath=gardenalog;destsuffix=gardenalog \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lwm2m;subpath=lwm2m;destsuffix=lwm2m \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lwm2m-types;subpath=lwm2m-types;destsuffix=lwm2m-types \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lwm2m-objgen;subpath=lwm2m-objgen;destsuffix=lwm2m-objgen \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=sg-ipc;subpath=sg-ipc;destsuffix=sg-ipc \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=tokioutil;subpath=tokioutil;destsuffix=tokioutil \
"

# Third-party repos which are not crates. Use `rev=` instead of `name=` (unnamed entries), as otherwise
# `cargo_common.bbclass` will add Cargo patch entries. This would fail on these because they are not crates.
SRC_URI += " \
    git://github.com/husqvarnagroup/smart-garden-ipso-registry.git;protocol=https;nobranch=1;rev=d44b3dc672e2374dc9a22705f23abde840c576e7;destsuffix=third_party/bnw-ipso-registry \
    git://github.com/OpenMobileAlliance/lwm2m-registry.git;protocol=https;nobranch=1;rev=7572ada63973156e21ce3634e274161b7d7b8e31;destsuffix=third_party/lwm2m-registry \
"

S = "${WORKDIR}/git"
CARGO_SRC_DIR = "fwrolloutd"

LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=0557f9d92cf58f2ccdd50f62f8ac0b28 \
    file://../THIRDPARTY.toml;md5=2bb77560e3698ee50662acc7ba4bab20 \
"

PR = "r0"

SRCREV_FORMAT = "fwrolloutd"


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
