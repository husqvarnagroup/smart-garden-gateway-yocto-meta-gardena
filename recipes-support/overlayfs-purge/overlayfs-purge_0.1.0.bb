SUMMARY = "Filesystem overlay cleaner for system upgrades"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "MIT"

LIC_FILES_CHKSUM=" \
file://LICENSE-MIT;md5=a3e3fd141148f23107ef1b2019ff1ff6 \
"
S = "${WORKDIR}"

PR = "r1"

inherit cargo cargo-update-recipe-crates

SRC_URI += " \
    file://Cargo.lock \
    file://Cargo.toml \
    file://LICENSE-MIT \
    file://src \
"

require overlayfs-purge-crates.inc
