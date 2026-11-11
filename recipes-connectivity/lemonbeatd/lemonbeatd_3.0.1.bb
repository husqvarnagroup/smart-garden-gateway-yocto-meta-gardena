inherit cargo
inherit cargo-update-recipe-crates

# Named, so that SRCREV_lemonbeatd applies. A plain SRCREV would clash with the rev=
# values of the unnamed entries below.
SRCREV_lemonbeatd = "a4fa97a9b04ec5d517d093a9f28128f0ae0e9320"
SRC_URI = "gitsm://github.com/husqvarnagroup/smart-garden-gateway-lemonbeatd;protocol=https;branch=main;name=lemonbeatd"

S = "${WORKDIR}/git"

# The crate is at the repository root.
CARGO_SRC_DIR = ""

PR = "r2"


SRC_URI += " \
    git://github.com/husqvarnagroup/nix.git;protocol=https;nobranch=1;name=nix;destsuffix=nix \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=gardenalog;subpath=gardenalog;destsuffix=gardenalog \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lsdl;subpath=lsdl;destsuffix=lsdl \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lwm2m;subpath=lwm2m;destsuffix=lwm2m \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lwm2m-types;subpath=lwm2m-types;destsuffix=lwm2m-types \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=rpc-mpsc;subpath=rpc-mpsc;destsuffix=rpc-mpsc \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lsdl-sys;subpath=lsdl-sys;destsuffix=lsdl-sys \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lwm2m-objgen;subpath=lwm2m-objgen;destsuffix=lwm2m-objgen \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=sg-ipc;subpath=sg-ipc;destsuffix=sg-ipc \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=systemd-async;subpath=systemd-async;destsuffix=systemd-async \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=tokio-task-rpc;subpath=tokio-task-rpc;destsuffix=tokio-task-rpc \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=tokio-task-rpc-util;subpath=tokio-task-rpc-util;destsuffix=tokio-task-rpc-util \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=tokioutil;subpath=tokioutil;destsuffix=tokioutil \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=xsd2rust;subpath=xsd2rust;destsuffix=xsd2rust \
"

# Former submodules of the main repository. These entries have `rev=` instead of `name=` (unnamed entries). `cargo_common.bbclass` adds a cargo [patch]
# entry for each named entry, and cargo fails on these because they are not crates.
SRC_URI += " \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;rev=15298a2b49cc1f905d064ea931255026984407c5;subpath=third_party/lsdl-specification-w3c;destsuffix=third_party/lsdl-specification-w3c \
    git://github.com/OpenMobileAlliance/lwm2m-registry.git;protocol=https;nobranch=1;rev=7572ada63973156e21ce3634e274161b7d7b8e31;destsuffix=third_party/lwm2m-registry \
    git://github.com/husqvarnagroup/smart-garden-ipso-registry.git;protocol=https;nobranch=1;rev=d44b3dc672e2374dc9a22705f23abde840c576e7;destsuffix=third_party/bnw-ipso-registry \
    git://github.com/husqvarnagroup/smart-garden-lemonbeat-serializer.git;protocol=https;nobranch=1;rev=7555186a76daa3775e7372fa55b6641a6f79b9b3;destsuffix=third_party/lsdl-serializer \
"

SRCREV_FORMAT = "lemonbeatd"

SRCREV_nix = "0ba2f892186e0d97b192e4d7a5e9ca54bf58cc94"
SRCREV_gardenalog = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lsdl = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lwm2m = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lwm2m-types = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_rpc-mpsc = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lsdl-sys = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lwm2m-objgen = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_sg-ipc = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_systemd-async = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_tokio-task-rpc = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_tokio-task-rpc-util = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_tokioutil = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_xsd2rust = "15298a2b49cc1f905d064ea931255026984407c5"
EXTRA_OECARGO_PATHS += "${WORKDIR}/nix"

SUMMARY = "lemonbeatd"
HOMEPAGE = "https://dev.azure.com/HQV-Gardena/SG-Gateway/_git/sg-lemonbeat-cargo"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=75d892af193fd5a298f724c4377d8f62"

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
    file://../THIRDPARTY.toml;md5=0c6e936d627ccf799bf6db6f828cad24 \
"

require lemonbeatd-crates.inc
