DESCRIPTION = "Asynchronous netlink library"
LICENSE = "GPL-3.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1ebbd3e34237af26da5dc08a4e440464"

inherit setuptools3

SRC_URI += " \
    gitsm://github.com/husqvarnagroup/netlink.git;protocol=https;branch=gardena/main \
"
SRCREV = "6a7122051350374c0db0ef50574f1f131da2a20a"

PR = "r0"
PV = "2022-06-16-${PR}+git${SRCPV}"

S = "${WORKDIR}/git"

RDEPENDS:${PN} += " \
    python3-core \
    python3-asyncio \
"
