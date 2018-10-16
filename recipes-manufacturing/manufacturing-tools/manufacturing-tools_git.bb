SUMMARY = "Device setup and testing during manufacturing"
LICENSE = "CLOSED"

inherit systemd allarch

SRC_URI = "git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-low-cost-gateway-manufacturing-scripts.git;protocol=https \
           file://export-gpios.service \
           file://ipr.service \
           file://selftest-check \
           file://selftest.service \
           "

PR = "r0"

PV = "1.0+git${SRCPV}"
SRCREV = "b629151d5e3e811f5df4367e0393b21009d6bd9f"

S = "${WORKDIR}/git"

FILES_${PN} += " \
    ${libdir}/python3.5/site-packages/cpms_client.py \
    ${libdir}/python3.5/site-packages/cpms_config.py \
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
	install -m 0755 ${S}/ipr-tool.py ${D}${bindir}/ipr-tool
	install -m 0755 ${WORKDIR}/selftest-check ${D}${bindir}

	install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/export-gpios.service ${D}${systemd_unitdir}/system/
	install -m 0644 ${WORKDIR}/ipr.service ${D}${systemd_unitdir}/system/
	install -m 0644 ${WORKDIR}/selftest.service ${D}${systemd_unitdir}/system/

	install -d 0755 ${D}${libdir}/python3.5/site-packages
	install -m 0755 ${S}/cpms_client.py ${D}${libdir}/python3.5/site-packages/
	install -m 0755 ${S}/cpms_config.py ${D}${libdir}/python3.5/site-packages/
}

SYSTEMD_SERVICE_${PN} += "export-gpios.service"
SYSTEMD_SERVICE_${PN} += "ipr.service"
SYSTEMD_SERVICE_${PN} += "selftest.service"
