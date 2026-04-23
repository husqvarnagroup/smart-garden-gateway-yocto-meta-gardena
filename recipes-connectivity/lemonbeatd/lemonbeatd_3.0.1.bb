inherit cargo
inherit cargo-update-recipe-crates

SRCREV = "99a011813b84d7ee4afc34948298afc4e2757708"
SRC_URI = "gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-lemonbeat-cargo;protocol=ssh;branch=main"

S = "${UNPACKDIR}/git"
CARGO_SRC_DIR = "lemonbeatd"

PR = "r1"

SRC_URI += " \
    git://github.com/husqvarnagroup/nix.git;protocol=https;nobranch=1;name=nix;destsuffix=nix \
"

SRCREV_FORMAT = "lemonbeatd"

SRCREV_nix = "0ba2f892186e0d97b192e4d7a5e9ca54bf58cc94"
EXTRA_OECARGO_PATHS += "${WORKDIR}/nix"

SUMMARY = "lemonbeatd"
HOMEPAGE = "https://dev.azure.com/HQV-Gardena/SG-Gateway/_git/sg-lemonbeat-cargo"
LICENSE = "Proprietary & BSD-3-Clause & MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0557f9d92cf58f2ccdd50f62f8ac0b28 \
                    file://LICENSE.rust-bindgen;md5=0b9a98cb3dcdefcceb145324693fda9b \
                    file://LICENSE.tokio;md5=249f61f40bd9437e2426970dd454e313"

# includes this file if it exists but does not fail
# this is useful for anything you may want to override from
# what cargo-bitbake generates.
include lemonbeatd-${PV}.inc
include lemonbeatd.inc

# Added by rust-recipe.sh
SRC_URI += " \
    file://THIRDPARTY.toml \
"

LIC_FILES_CHKSUM += " \
    file://../THIRDPARTY.toml;md5=8b7039b09e8b2bd231d4666c04db2f5d \
"

require lemonbeatd-crates.inc
