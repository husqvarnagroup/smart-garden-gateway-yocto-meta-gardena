inherit cargo cargo-update-recipe-crates

SRC_URI += "git://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-gateway-config-backend;protocol=ssh;branch=main;tag=v${PV}"
S = "${WORKDIR}/git"
CARGO_SRC_DIR = ""

PR = "r6"


SRC_URI += " \
    file://THIRDPARTY.toml \
"

LIC_FILES_CHKSUM = " \
    file://Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28 \
    file://../THIRDPARTY.toml;md5=7d98e0e4ff40b65ef7ceaf804572beca \
"

SUMMARY = "Backend component for the GARDENA smart Gateway config interface"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "Proprietary"

# includes this file if it exists but does not fail
# this is useful for anything you may want to override from
# what cargo-bitbake generates.
include gateway-config-backend-${PV}.inc
include gateway-config-backend.inc
require gateway-config-backend-crates.inc

