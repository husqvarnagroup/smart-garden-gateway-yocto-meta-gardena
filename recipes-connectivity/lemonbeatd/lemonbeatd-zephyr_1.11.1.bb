inherit cargo
inherit cargo-update-recipe-crates


SRC_URI = "gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-lemonbeat-cargo;protocol=ssh;nobranch=1;branch=gateway3"
SRCREV = "87b8a2755ce7c508ef20b911fb0c42c1e453909a"

S = "${WORKDIR}/git"
CARGO_SRC_DIR = "lemonbeatd"

PR = "r0"

SRC_URI += " \
    git://github.com/husqvarnagroup/nix.git;protocol=https;nobranch=1;name=nix;destsuffix=nix \
"

SRCREV_FORMAT .= "_nix"
SRCREV_nix = "0ba2f892186e0d97b192e4d7a5e9ca54bf58cc94"
EXTRA_OECARGO_PATHS += "${WORKDIR}/nix"

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

LIC_FILES_CHKSUM += "file://../THIRDPARTY-lemonbeatd-zephyr.toml;md5=6ac57bd16c8efe32078d01aa9ef10203"
