SUMMARY = "LibCYAML: Schema-based YAML parsing and serialisation"
DESCRIPTION = "LibCYAML is a C library for reading and writing structured YAML documents. \
It is written in ISO C11 and licensed under the ISC licence."
HOMEPAGE = "https://github.com/tlsa/libcyaml"
SECTION = "libs/devel"

LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://LICENSE;md5=52a1707594d3c6694292db3dd1a7f960"

DEPENDS = "libyaml"
RDEPENDS:${PN} = "libyaml"

PR = "r1"

SRC_URI = "git://github.com/tlsa/libcyaml.git;protocol=https;nobranch=1;tag=v${PV}"

S = "${WORKDIR}/git"

do_compile() {
        oe_runmake
}

do_install() {
        oe_runmake install \
                DESTDIR="${D}" \
                PREFIX="${prefix}"
}

BBCLASSEXTEND = "native nativesdk"
