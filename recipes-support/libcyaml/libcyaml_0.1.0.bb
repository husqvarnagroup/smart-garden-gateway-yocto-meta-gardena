SUMMARY = "LibCYAML: Schema-based YAML parsing and serialisation"
DESCRIPTION = "LibCYAML is a C library for reading and writing structured YAML documents. \
It is written in ISO C11 and licensed under the ISC licence."
HOMEPAGE = "https://github.com/tlsa/libcyaml"
SECTION = "libs/devel"

LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://LICENSE;md5=52a1707594d3c6694292db3dd1a7f960"

DEPENDS = "libyaml"
RDEPENDS_${PN} = "libyaml"

PR = "r1"

SRC_URI = "git://github.com/tlsa/libcyaml.git;protocol=https"

# Last commit before tag v0.1.0 which is also in master
SRCREV = "8918b1ea2bf8cb4980c7b8c83b57bae3b362698c"

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
