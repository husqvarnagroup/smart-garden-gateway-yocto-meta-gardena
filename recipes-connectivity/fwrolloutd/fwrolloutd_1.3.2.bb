inherit cargo cargo-update-recipe-crates

SRC_URI += "gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-firmware-rollout;protocol=ssh;branch=main;tag=v${PV}"
S = "${WORKDIR}/git"
CARGO_SRC_DIR = ""

LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=0557f9d92cf58f2ccdd50f62f8ac0b28 \
"

SUMMARY = "fwrolloutd"
HOMEPAGE = "git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-firmware-rollout"
LICENSE = "Proprietary"

PR = "r0"

include fwrolloutd-${PV}.inc
include fwrolloutd.inc

# Added by rust-recipe.sh
SRC_URI += " \
    file://THIRDPARTY.toml \
"

LIC_FILES_CHKSUM += " \
    file://../THIRDPARTY.toml;md5=2d2762287b1ad3e5819604af0814847f \
"

require fwrolloutd-crates.inc
