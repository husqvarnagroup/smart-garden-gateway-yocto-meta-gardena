inherit cargo cargo-update-recipe-crates

# If this is git based prefer versioned ones if they exist
# DEFAULT_PREFERENCE = "-1"

# how to get coap-transport-proxy could be as easy as but default to a git checkout:
# SRC_URI += "crate://crates.io/coap-transport-proxy/0.5.1"
SRC_URI += "gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-coap-transport-proxy;protocol=ssh;nobranch=1;branch=main;tag=v${PV}"
S = "${WORKDIR}/git"
CARGO_SRC_DIR = ""

PR = "r0"


# please note if you have entries that do not begin with crate://
# you must change them to how that package can be fetched
SRC_URI += " \
    git://github.com/husqvarnagroup/rust-coap-client.git;protocol=https;nobranch=1;name=coap-client;destsuffix=coap-client \
    file://THIRDPARTY.toml \
"

LIC_FILES_CHKSUM += "file://../THIRDPARTY.toml;md5=041b93a136f7aff40102d91ec56402e0"

SRCREV_FORMAT .= "_coap-client"
SRCREV_coap-client = "8803d017fba4f1fe61b518448b02b6e474a6a6a1"
EXTRA_OECARGO_PATHS += "${WORKDIR}/coap-client"

# FIXME: update generateme with the real MD5 of the license file
LIC_FILES_CHKSUM = " \
    "

SUMMARY = "coap-transport-proxy"
HOMEPAGE = "https://dev.azure.com/HQV-Gardena/SG-Gateway/_git/sg-coap-transport-proxy"
LICENSE = "CLOSED"

# includes this file if it exists but does not fail
# this is useful for anything you may want to override from
# what cargo-bitbake generates.
include coap-transport-proxy-${PV}.inc
include coap-transport-proxy.inc
require coap-transport-proxy-crates.inc
