SUMMARY = "Device setup and testing during manufacturing"
LICENSE = "CLOSED"

inherit systemd allarch python3-dir

SRC_URI = "git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-gateway-manufacturing-scripts.git;protocol=https \
           file://export-gpios.service \
           file://ipr-setup \
           file://ipr.service \
           file://selftest-check \
           file://selftest.service \
           file://fctcheck \
           file://fctcheck.service \
           file://homekit-setup \
           file://homekit-setup.service \
           "

PR = "r0"

PV = "1.6+git${SRCPV}"
SRCREV = "97a25f33ff1ad5ad18a2b6475c87bb49bbf66896"

S = "${WORKDIR}/git"

FILES_${PN} += " \
    ${PYTHON_SITEPACKAGES_DIR}/bootstrap.py \
    ${PYTHON_SITEPACKAGES_DIR}/util.py \
    ${PYTHON_SITEPACKAGES_DIR}/cpms_client.py \
    ${PYTHON_SITEPACKAGES_DIR}/cpms_config.py \
"

RDEPENDS_${PN} += " \
    python3-core \
    python3-datetime \
    python3-json \
    python3-threading \
    python3-unittest \
    fct-tool \
"

do_install () {
	install -d ${D}${bindir}
	install -m 0755 ${S}/selftest.py ${D}${bindir}/selftest
	install -m 0755 ${S}/fct-tool.py ${D}${bindir}/fct-tool
	install -m 0755 ${S}/ipr-tool.py ${D}${bindir}/ipr-tool
	install -m 0755 ${S}/homekit-tool.py ${D}${bindir}/homekit-tool
	install -m 0755 ${WORKDIR}/ipr-setup ${D}${bindir}
	install -m 0755 ${WORKDIR}/homekit-setup ${D}${bindir}
	install -m 0755 ${WORKDIR}/selftest-check ${D}${bindir}
	install -m 0755 ${WORKDIR}/fctcheck ${D}${bindir}

	install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/export-gpios.service ${D}${systemd_unitdir}/system/
	install -m 0644 ${WORKDIR}/ipr.service ${D}${systemd_unitdir}/system/
	install -m 0644 ${WORKDIR}/selftest.service ${D}${systemd_unitdir}/system/
	install -m 0644 ${WORKDIR}/fctcheck.service ${D}${systemd_unitdir}/system/
	install -m 0644 ${WORKDIR}/homekit-setup.service ${D}${systemd_unitdir}/system/

	install -d 0755 ${D}${PYTHON_SITEPACKAGES_DIR}
	install -m 0755 ${S}/util.py ${D}${PYTHON_SITEPACKAGES_DIR}/
	install -m 0755 ${S}/bootstrap.py ${D}${PYTHON_SITEPACKAGES_DIR}/
	install -m 0755 ${S}/cpms_client.py ${D}${PYTHON_SITEPACKAGES_DIR}/
	install -m 0755 ${S}/cpms_config.py ${D}${PYTHON_SITEPACKAGES_DIR}/
}

SYSTEMD_SERVICE_${PN} += "export-gpios.service"
SYSTEMD_SERVICE_${PN} += "ipr.service"
SYSTEMD_SERVICE_${PN} += "selftest.service"
SYSTEMD_SERVICE_${PN} += "fctcheck.service"
SYSTEMD_SERVICE_${PN} += "homekit-setup.service"

PACKAGES =+ "fct-tool"
PROVIDES =+ "fct-tool"

FILES_fct-tool += "${bindir}/fct-tool"

RDEPENDS_fct-tool += " \
    python3-core \
    python3-datetime \
"
