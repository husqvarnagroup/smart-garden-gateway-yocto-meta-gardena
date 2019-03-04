LICENSE = "CLOSED"

SRC_URI = "git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-emc-testing-tools.git;protocol=https"
SRCREV = "d07026dd6e69e2c88cc877331a2341084f231158"

PR = "r0"
PV = "1.2+git${SRCPV}"

S = "${WORKDIR}/git"

inherit python3-dir

FILES_${PN} += " \
    ${PYTHON_SITEPACKAGES_DIR}/* \
    ${PYTHON_SITEPACKAGES_DIR}/emc_testing/* \
    ${PYTHON_SITEPACKAGES_DIR}/scripts/gateway_autoconfig.sh \
"

RDEPENDS_emc-tools += " \
    python3-lemonbeat \
    python3-core \
    python3-misc \
    python3-xml \
    python3-threading \
    python3-netclient \
    python3-netserver \
    python3-fcntl \
"

do_install () {
    # scripts
    install -d ${D}${bindir}
    install -m 0755 ${S}/lbtool.py ${D}${bindir}/lbtool
    install -m 0755 ${S}/battery.py ${D}${bindir}/battery
    install -m 0755 ${S}/ic24.py ${D}${bindir}/ic24
    install -m 0755 ${S}/power.py ${D}${bindir}/power
    install -m 0755 ${S}/sensor.py ${D}${bindir}/sensor
    install -m 0755 ${S}/watercontrol.py ${D}${bindir}/watercontrol

    # Lemonbeat library
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}

    # EMC testing module
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}/emc_testing
    install -m 0755 ${S}/emc_testing/__init__.py ${D}${PYTHON_SITEPACKAGES_DIR}/emc_testing/__init__.py
    install -m 0755 ${S}/emc_testing/config.py ${D}${PYTHON_SITEPACKAGES_DIR}/emc_testing/config.py
    install -m 0755 ${S}/emc_testing/error_codes.py ${D}${PYTHON_SITEPACKAGES_DIR}/emc_testing/error_codes.py
    install -m 0755 ${S}/emc_testing/status_level.py ${D}${PYTHON_SITEPACKAGES_DIR}/emc_testing/status_level.py

    # script for initial auto-configuration
    install -m 0755 ${S}/scripts/gateway_autoconfig.sh ${D}${bindir}/emc-tools-autoconfig
}

pkg_postinst_${PN} () {
    # do autoconfig
    emc-tools-autoconfig
}