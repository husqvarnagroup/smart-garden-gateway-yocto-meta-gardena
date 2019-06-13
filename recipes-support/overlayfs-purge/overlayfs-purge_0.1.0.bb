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
crate://crates.io/aho-corasick/0.6.10 \
crate://crates.io/autocfg/0.1.2 \
crate://crates.io/backtrace-sys/0.1.28 \
crate://crates.io/backtrace/0.3.14 \
crate://crates.io/bitflags/1.0.4 \
crate://crates.io/cc/1.0.29 \
crate://crates.io/cfg-if/0.1.6 \
crate://crates.io/failure/0.1.5 \
crate://crates.io/failure_derive/0.1.5 \
crate://crates.io/fnv/1.0.6 \
crate://crates.io/globset/0.4.2 \
crate://crates.io/lazy_static/1.2.0 \
crate://crates.io/libc/0.2.48 \
crate://crates.io/log/0.4.6 \
crate://crates.io/memchr/2.2.0 \
crate://crates.io/nix/0.13.0 \
crate://crates.io/proc-macro2/0.4.27 \
crate://crates.io/quote/0.6.11 \
crate://crates.io/regex-syntax/0.6.5 \
crate://crates.io/regex/1.1.0 \
crate://crates.io/rustc-demangle/0.1.13 \
crate://crates.io/syn/0.15.26 \
crate://crates.io/synstructure/0.10.1 \
crate://crates.io/thread_local/0.3.6 \
crate://crates.io/ucd-util/0.1.3 \
crate://crates.io/unicode-xid/0.1.0 \
crate://crates.io/utf8-ranges/1.0.2 \
crate://crates.io/void/1.0.2 \
crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi/0.3.6 \
crate://crates.io/xattr/0.2.2 \
"

LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SUMMARY = "Filesystem overlay cleaner for system upgrades"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "MIT"
