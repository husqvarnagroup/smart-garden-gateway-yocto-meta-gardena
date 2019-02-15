inherit cargo

SRC_URI += " \
    file://Cargo.lock \
    file://Cargo.toml \
    file://src \
"
S = "${WORKDIR}"

# please note if you have entries that do not begin with crate://
# you must change them to how that package can be fetched
SRC_URI += " \
crate://crates.io/aho-corasick/0.6.9 \
crate://crates.io/cfg-if/0.1.6 \
crate://crates.io/fnv/1.0.6 \
crate://crates.io/globset/0.4.2 \
crate://crates.io/lazy_static/1.2.0 \
crate://crates.io/libc/0.2.48 \
crate://crates.io/log/0.4.6 \
crate://crates.io/memchr/2.1.3 \
crate://crates.io/regex-syntax/0.6.5 \
crate://crates.io/regex/1.1.0 \
crate://crates.io/thread_local/0.3.6 \
crate://crates.io/ucd-util/0.1.3 \
crate://crates.io/utf8-ranges/1.0.2 \
crate://crates.io/xattr/0.2.2 \
"

LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

SUMMARY = "Filesystem overlay cleaner for system upgrades"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "Proprietary"
