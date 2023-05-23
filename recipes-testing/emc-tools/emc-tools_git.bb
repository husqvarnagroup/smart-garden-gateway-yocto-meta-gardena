LICENSE = "CLOSED"

SRC_URI = "git://ssh.dev.azure.com/v3/HQV-Gardena/SG-Embedded/sg-emc-testing-tools;protocol=ssh;branch=main"
SRCREV = "5dd841ca75c9a79d885a1f86e0c63157e0e4152e"

PR = "r3"
PV = "2019-12-19+git${SRCPV}"

S = "${WORKDIR}/git"

inherit python3-dir

FILES:${PN} += " \
    ${PYTHON_SITEPACKAGES_DIR}/* \
    ${PYTHON_SITEPACKAGES_DIR}/emc_testing/* \
    ${PYTHON_SITEPACKAGES_DIR}/scripts/gateway_autoconfig.sh \
"

RDEPENDS:emc-tools += " \
    python3-core \
    python3-fcntl \
    python3-json \
    python3-lemonbeat \
    python3-misc \
    python3-netclient \
    python3-netserver \
    python3-threading \
    python3-xml \
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
    sed -i 's|/usr/lib/python3.7/site-packages|${PYTHON_SITEPACKAGES_DIR}|g' ${D}${bindir}/emc-tools-autoconfig
}

pkg_postinst:${PN} () {
    # do autoconfig
    emc-tools-autoconfig
}
