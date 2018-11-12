LICENSE = "CLOSED"

SRC_URI = "git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-emc-testing-tools.git;protocol=https"
SRCREV = "f505d5e62fcf705359c38693aaef17a587daa04e"

PR = "r0"
PV = "1.0+git${SRCPV}"

S = "${WORKDIR}/git"

inherit python3-dir

FILES_${PN} += " \
    ${PYTHON_SITEPACKAGES_DIR}/emc_testing/* \
"

RDEPENDS_emc-tools += " \
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
    install -m 0755 ${S}/battery.py ${D}${bindir}/battery
    install -m 0755 ${S}/ic24.py ${D}${bindir}/ic24
    install -m 0755 ${S}/power.py ${D}${bindir}/power
    install -m 0755 ${S}/sensor.py ${D}${bindir}/sensor
    install -m 0755 ${S}/watercontrol.py ${D}${bindir}/watercontrol

    # module
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}/emc_testing
    # TODO should we put config.py outside of site-packages? -> probably yes, but not really worth the effort ..
    install -m 0755 ${S}/emc_testing/config.py ${D}${PYTHON_SITEPACKAGES_DIR}/emc_testing/config.py
    install -m 0755 ${S}/emc_testing/error_codes.py ${D}${PYTHON_SITEPACKAGES_DIR}/emc_testing/error_codes.py
    install -m 0755 ${S}/emc_testing/lbtool.py ${D}${PYTHON_SITEPACKAGES_DIR}/emc_testing/lbtool.py
    install -m 0755 ${S}/emc_testing/status_level.py ${D}${PYTHON_SITEPACKAGES_DIR}/emc_testing/status_level.py

    # lsdl-serializer
    install -m 0755 ${S}/bin/mips/lsdl-serializer ${D}${bindir}/lsdl-serializer


    # script for initial auto-configuration
    install -m 0755 ${S}/scripts/gateway_autoconfig.sh ${D}${bindir}/emc-tools-autoconfig

    # symlink for lbtool
    ln -s -r ${D}${PYTHON_SITEPACKAGES_DIR}/emc_testing/lbtool.py ${D}${bindir}/lbtool
}
