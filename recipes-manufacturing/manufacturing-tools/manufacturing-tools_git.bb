SUMMARY = "Device setup and testing during manufacturing"
LICENSE = "CLOSED"

DEPENDS = "python3-native"

inherit systemd allarch python3-dir python3native

SRC_URI = "git://stash.dss.husqvarnagroup.com/scm/sg/smart-garden-gateway-manufacturing-scripts.git;protocol=https \
           file://ipr-setup \
           file://ipr.service \
           file://selftest-check \
           file://selftest.service \
           file://fctcheck \
           file://fctcheck.service \
           file://eoltest-check.sh \
           file://eoltest-check.service \
           file://eoltest-run.sh \
           file://eoltest.service \
           file://homekit-setup \
           file://homekit-setup.service \
           file://keep.d/fctcheck \
           "

PR = "r1"

PV = "20190827+git${SRCPV}"
SRCREV = "a1017f6bc518b4c000ac0c5e1a596339caa0e0f4"

S = "${WORKDIR}/git"

FILES_${PN} += " \
    ${PYTHON_SITEPACKAGES_DIR}/bootstrap.py \
    ${PYTHON_SITEPACKAGES_DIR}/util.py \
    ${PYTHON_SITEPACKAGES_DIR}/testing.py \
    ${PYTHON_SITEPACKAGES_DIR}/cpms_client.py \
    ${PYTHON_SITEPACKAGES_DIR}/cpms_config.py \
    ${base_libdir}/upgrade/keep.d \
"

RDEPENDS_${PN} += " \
    fct-tool \
    python3-core \
    python3-datetime \
    python3-json \
    python3-threading \
    python3-unittest \
"

do_install () {
    install -d ${D}${bindir}
    install -m 0755 ${S}/selftest.py ${D}${bindir}/selftest
    install -m 0755 ${S}/eoltest.py ${D}${bindir}/eoltest
    install -m 0755 ${S}/fct-tool.py ${D}${bindir}/fct-tool
    install -m 0755 ${S}/ipr-tool.py ${D}${bindir}/ipr-tool
    install -m 0755 ${S}/homekit-tool.py ${D}${bindir}/homekit-tool
    install -m 0755 ${WORKDIR}/ipr-setup ${D}${bindir}
    install -m 0755 ${WORKDIR}/homekit-setup ${D}${bindir}
    install -m 0755 ${WORKDIR}/selftest-check ${D}${bindir}
    install -m 0755 ${WORKDIR}/fctcheck ${D}${bindir}
    install -m 0755 ${WORKDIR}/eoltest-check.sh ${D}${bindir}/eoltest-check
    install -m 0755 ${WORKDIR}/eoltest-run.sh ${D}${bindir}/eoltest-run

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/ipr.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/selftest.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/fctcheck.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/eoltest-check.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/eoltest.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/homekit-setup.service ${D}${systemd_unitdir}/system/

    install -d 0755 ${D}${PYTHON_SITEPACKAGES_DIR}
    install -m 0755 ${S}/util.py ${D}${PYTHON_SITEPACKAGES_DIR}/
    install -m 0755 ${S}/testing.py ${D}${PYTHON_SITEPACKAGES_DIR}/
    install -m 0755 ${S}/bootstrap.py ${D}${PYTHON_SITEPACKAGES_DIR}/
    install -m 0755 ${S}/cpms_client.py ${D}${PYTHON_SITEPACKAGES_DIR}/
    install -m 0755 ${S}/cpms_config.py ${D}${PYTHON_SITEPACKAGES_DIR}/

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/fctcheck ${D}${base_libdir}/upgrade/keep.d
}

pkg_postinst_${PN} () {
    cd $D${PYTHON_SITEPACKAGES_DIR} && python3 -m compileall .
}

SYSTEMD_SERVICE_${PN} += "ipr.service"
SYSTEMD_SERVICE_${PN} += "selftest.service"
SYSTEMD_SERVICE_${PN} += "fctcheck.service"
SYSTEMD_SERVICE_${PN} += "eoltest-check.service"
SYSTEMD_SERVICE_${PN} += "eoltest.service"
SYSTEMD_SERVICE_${PN} += "homekit-setup.service"

PACKAGES =+ "fct-tool"
PROVIDES =+ "fct-tool"

FILES_fct-tool += "${bindir}/fct-tool"

RDEPENDS_fct-tool += " \
    iw \
    openocd \
    python3-core \
    python3-datetime \
    python3-evdev \
    systemd \
"

RCONFLICTS_fct-tool += " \
    procps \
"
