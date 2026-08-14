inherit cargo
inherit cargo-update-recipe-crates

SRCREV = "0bc5331e8b049acd2c4ee3de7e881d12c18427e5"
SRC_URI = "gitsm://git@ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-lemonbeat-cargo;protocol=ssh;branch=eb/move"

PR = "r2"

SRCREV_gardenalog = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lsdl = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lsdl-sys = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lwm2m = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lwm2m-objgen = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lwm2m-types = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_rpc-mpsc = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_sg-ipc = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_systemd-async = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_tokio-task-rpc = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_tokio-task-rpc-util = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_tokioutil = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_xsd2rust = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_lsdl-serializer = "7555186a76daa3775e7372fa55b6641a6f79b9b3"
SRCREV_lsdl-specification-w3c = "15298a2b49cc1f905d064ea931255026984407c5"
SRCREV_bnw-ipso-registry = "d44b3dc672e2374dc9a22705f23abde840c576e7"
SRCREV_lwm2m-registry = "7572ada63973156e21ce3634e274161b7d7b8e31"

SRC_URI += " \
    git://github.com/husqvarnagroup/nix.git;protocol=https;nobranch=1;name=nix;destsuffix=nix \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=gardenalog;subpath=gardenalog;destsuffix=gardenalog \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lsdl-sys;subpath=lsdl-sys;destsuffix=lsdl-sys \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lsdl;subpath=lsdl;destsuffix=lsdl \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lwm2m-objgen;subpath=lwm2m-objgen;destsuffix=lwm2m-objgen \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lwm2m-types;subpath=lwm2m-types;destsuffix=lwm2m-types \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lwm2m;subpath=lwm2m;destsuffix=lwm2m \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=rpc-mpsc;subpath=rpc-mpsc;destsuffix=rpc-mpsc \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=sg-ipc;subpath=sg-ipc;destsuffix=sg-ipc \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=systemd-async;subpath=systemd-async;destsuffix=systemd-async \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=tokio-task-rpc-util;subpath=tokio-task-rpc-util;destsuffix=tokio-task-rpc-util \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=tokio-task-rpc;subpath=tokio-task-rpc;destsuffix=tokio-task-rpc \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=tokioutil;subpath=tokioutil;destsuffix=tokioutil \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=xsd2rust;subpath=xsd2rust;destsuffix=xsd2rust \
    git://github.com/husqvarnagroup/smart-garden-lemonbeat-serializer.git;protocol=https;nobranch=1;name=lsdl-serializer;destsuffix=third_party/lsdl-serializer \
    git://github.com/husqvarnagroup/smart-garden-gateway-crates.git;protocol=https;nobranch=1;name=lsdl-specification-w3c;subpath=third_party/lsdl-specification-w3c;destsuffix=third_party/lsdl-specification-w3c \
    git://github.com/husqvarnagroup/smart-garden-ipso-registry.git;protocol=https;nobranch=1;name=bnw-ipso-registry;destsuffix=third_party/bnw-ipso-registry \
    git://github.com/OpenMobileAlliance/lwm2m-registry.git;protocol=https;nobranch=1;name=lwm2m-registry;destsuffix=third_party/lwm2m-registry \
"

# Remove patch entries for third-party repos which are not crates or else the build will fail with:
#   failed to read `.../third_party/bnw-ipso-registry/Cargo.toml`
# This workaround is necessary as we need to use the `name` fetcher parameter to be able to pass a
# revision different from `SRCREV`.
python lemonbeatd_fixup_cargo_config() {
    import re
    cargo_config = os.path.join(d.getVar("CARGO_HOME"), "config.toml")
    with open(cargo_config) as f:
        content = f.read()
    content = re.sub(
        r'\n\[patch\."https://github\.com/husqvarnagroup/smart-garden-ipso-registry\.git"\][^\[]*',
        '',
        content,
    )
    content = re.sub(
        r'\n\[patch\."https://github\.com/husqvarnagroup/smart-garden-lemonbeat-serializer\.git"\][^\[]*',
        '',
        content,
    )
    content = re.sub(
        r'\n\[patch\."https://github\.com/OpenMobileAlliance/lwm2m-registry\.git"\][^\[]*',
        '',
        content,
    )
    content = re.sub(
        r'\nlsdl-specification-w3c = \{[^\n]*\}',
        '',
        content,
    )
    with open(cargo_config, 'w') as f:
        f.write(content)
}
do_configure[postfuncs] += "lemonbeatd_fixup_cargo_config"

SRCREV_FORMAT = "lemonbeatd"

SRCREV_nix = "0ba2f892186e0d97b192e4d7a5e9ca54bf58cc94"
EXTRA_OECARGO_PATHS += "${UNPACKDIR}/nix"

SUMMARY = "lemonbeatd"
HOMEPAGE = "https://dev.azure.com/HQV-Gardena/SG-Gateway/_git/sg-lemonbeat-cargo"
LICENSE = "Proprietary & BSD-3-Clause & MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

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
    file://../THIRDPARTY.toml;md5=939b1b87bf79ad531b0776c926641998 \
"

require lemonbeatd-crates.inc
