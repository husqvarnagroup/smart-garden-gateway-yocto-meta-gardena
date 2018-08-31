LICENSE = "CLOSED"

SRC_URI = "git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-low-cost-gateway-manufacturing-scripts.git;protocol=https"

PR = "r1"

PV = "1.0+git${SRCPV}"
SRCREV = "3695bd3eff8361b5426f64e24e9d20bba2a02974"

S = "${WORKDIR}/git"

FILES_${PN} += " \
    ${libdir}/python3.5/site-packages/cpms_client.py \
"

RDEPENDS_manufacturing-tools += " \
    python3-core \
    python3-datetime \
    python3-json \
    python3-threading \
    python3-unittest \
"

do_install () {
	install -d ${D}${bindir}
	install -m 0755 ${S}/selftest.py ${D}${bindir}/selftest
	install -m 0755 ${S}/fct-tool.py ${D}${bindir}/fct-tool

	install -d 0755 ${D}${libdir}/python3.5/site-packages
	install -m 0755 ${S}/cpms_client.py ${D}${libdir}/python3.5/site-packages/
}

