SUMMARY = "Filesystem overlay cleaner for system upgrades"
HOMEPAGE = "https://github.com/husqvarnagroup/overlayfs-purge"
LICENSE = "MIT"
LIC_FILES_CHKSUM = " \
    file://LICENSE-MIT;md5=a3e3fd141148f23107ef1b2019ff1ff6 \
"

PR = "r0"
SRCREV = "a5e3582df0b4ebfd2fc723f3ee9de6d189faa77a"
SRC_URI = " \
    git://github.com/husqvarnagroup/overlayfs-purge.git;protocol=https;branch=main \
"

S = "${WORKDIR}/git"

inherit cargo cargo-update-recipe-crates

SRC_URI += " \
    file://THIRDPARTY.toml \
"

LIC_FILES_CHKSUM = " \
    file://../THIRDPARTY.toml;md5=cb6b061b1993ff5f6262177b56fa557c \
"

require overlayfs-purge-crates.inc
