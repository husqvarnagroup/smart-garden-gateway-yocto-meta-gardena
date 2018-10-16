LICENSE = "CLOSED"

SRC_URI = "git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-gateway-swd-tool.git;protocol=https"

PR = "r0"

PV = "1.2+git${SRCPV}"
SRCREV = "0da9ab843b00bfa926940aba7e19ac3c27a4762b"

S = "${WORKDIR}/git"

do_compile () {
	oe_runmake
}

do_install () {
	install -d ${D}${bindir}
	install -m 0755 ${S}/SWDtool ${D}${bindir}/
}
