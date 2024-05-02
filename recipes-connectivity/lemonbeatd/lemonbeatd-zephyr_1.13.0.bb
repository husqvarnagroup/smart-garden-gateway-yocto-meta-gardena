inherit cargo
inherit cargo-update-recipe-crates


SRC_URI = "gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-lemonbeat-cargo;protocol=ssh;nobranch=1;branch=gateway3"
SRCREV = "de372f5a5960e6052592b6943139ae3f5f505d67"

S = "${WORKDIR}/git"
CARGO_SRC_DIR = "lemonbeatd"

PR = "r0"

SRC_URI += " \
    git://github.com/husqvarnagroup/nix.git;protocol=https;nobranch=1;name=nix;destsuffix=nix \
    git://github.com/husqvarnagroup/nng-rs.git;protocol=https;nobranch=1;name=nng;destsuffix=nng-rs \
    gitsm://github.com/husqvarnagroup/nng-rust.git;protocol=https;branch=nng-v1.7.3;name=nng-sys;destsuffix=nng-sys \
"

SRCREV_FORMAT = "lemonbeatd"

SRCREV_nix = "0ba2f892186e0d97b192e4d7a5e9ca54bf58cc94"
EXTRA_OECARGO_PATHS += "${WORKDIR}/nix"

SRCREV_nng = "13828ad0f3a8044dd9ab1265c3d5c88b5fcfce1f"
EXTRA_OECARGO_PATHS += "${WORKDIR}/nng-rs"

SRCREV_nng-sys = "96b98092f610c379c60cefd0973427146be0380e"
EXTRA_OECARGO_PATHS += "${WORKDIR}/nng-sys"


LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=0557f9d92cf58f2ccdd50f62f8ac0b28 \
"

SUMMARY = "lemonbeatd"
HOMEPAGE = "https://dev.azure.com/HQV-Gardena/SG-Gateway/_git/sg-lemonbeat-cargo"
LICENSE = "Proprietary"

# includes this file if it exists but does not fail
# this is useful for anything you may want to override from
# what cargo-bitbake generates.
include lemonbeatd-${PV}.inc
include lemonbeatd.inc

# Added by rust-recipe.sh
SRC_URI += " \
    file://THIRDPARTY-lemonbeatd-zephyr.toml \
"


include lemonbeatd-zephyr-crates.inc

LIC_FILES_CHKSUM += "file://../THIRDPARTY-lemonbeatd-zephyr.toml;md5=c0e62d228b2b4637511af1623fc6f44f"
