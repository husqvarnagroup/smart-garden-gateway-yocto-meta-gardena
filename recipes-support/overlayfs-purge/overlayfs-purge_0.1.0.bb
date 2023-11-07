inherit cargo
inherit cargo-update-recipe-crates

SRC_URI += " \
    file://Cargo.lock \
    file://Cargo.toml \
    file://LICENSE-MIT \
    file://src \
"
S = "${WORKDIR}"
CARGO_SRC_DIR=""
export CARGO_PROFILE_RELEASE_OPT_LEVEL="z"
export CARGO_PROFILE_RELEASE_CODEGEN_UNITS="1"
PV:append = ""

PR = "r1"

# FIXME: update generateme with the real MD5 of the license file
LIC_FILES_CHKSUM=" \
file://LICENSE-MIT;md5=a3e3fd141148f23107ef1b2019ff1ff6 \
"

SUMMARY = "Filesystem overlay cleaner for system upgrades"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "MIT"

# includes this file if it exists but does not fail
# this is useful for anything you may want to override from
# what cargo-bitbake generates.
include overlayfs-purge-${PV}.inc
include overlayfs-purge.inc

require overlayfs-purge-crates.inc
