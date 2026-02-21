DESCRIPTION = "SiM3U flash programmer — bit-bangs SWD over /dev/mem mmap GPIO \
to erase, program and verify firmware on SiLabs SiM3U Cortex-M targets."
SUMMARY = "SiM3U GPIO SWD flash programmer"
MAINTAINER = "Gardena GmbH"
HOMEPAGE = "https://github.com/husqvarnagroup/sim3u-flasher"

LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3d26203303a722dedc6bf909d95ba815"

PV = "0.1.0"
PR = "r0"

SRC_URI = "git://github.com/husqvarnagroup/sim3u-flasher.git;protocol=https;branch=gardena/eb/wip"
SRCREV = "${AUTOREV}"

do_compile() {
    oe_runmake CC="${CC}" CFLAGS="${CFLAGS}" LDFLAGS="${LDFLAGS}"
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/sim3u-flasher ${D}${bindir}/sim3u-flasher
}
