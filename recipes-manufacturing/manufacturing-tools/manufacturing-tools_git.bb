LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

SRC_URI = "git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-low-cost-gateway-manufacturing-scripts.git;protocol=https"

PR = "r0"

PV = "1.0+git${SRCPV}"
SRCREV = "3695bd3eff8361b5426f64e24e9d20bba2a02974"

S = "${WORKDIR}/git"

FILES_${PN} += " \
    /usr/* \
"

RDEPENDS_manufacturing-tools += " \
    python3-core \
    python3-datetime \
    python3-json \
    python3-threading \
    python3-unittest \
"

do_install () {
	install -d 0755 ${D}/usr/bin
	install -m 0755 ${S}/selftest.py ${D}/usr/bin/selftest
	install -m 0755 ${S}/fct-tool.py ${D}/usr/bin/fct-tool
	install -d 0755 ${D}/usr/lib/python3.5/site-packages
	install -m 0755 ${S}/cpms_client.py ${D}/usr/lib/python3.5/site-packages/
}

