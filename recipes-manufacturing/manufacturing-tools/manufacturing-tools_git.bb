SUMMARY = "Device setup and testing during manufacturing"
LICENSE = "CLOSED"

DEPENDS = "python3-native"

COMPATIBLE_MACHINE = "mt7688"

inherit systemd allarch python3-dir python3native

SRC_URI = "git://stash.dss.husqvarnagroup.com:7999/sg/smart-garden-gateway-manufacturing-scripts.git;protocol=ssh \
           file://manufacturing-statusfiles.service \
           file://manufacturing-statusfiles.sh \
           file://ipr-setup \
           file://ipr.service \
           file://selftest-check \
           file://selftest.service \
           file://eoltest-check.sh \
           file://eoltest-check.service \
           file://eoltest-run.sh \
           file://eoltest.service \
           file://homekit-setup \
           file://homekit-setup.service \
           file://keep.d/eoltest \
           file://keep.d/manufacturing-statusfiles \
           "

PR = "r0"

PV = "20200212+git${SRCPV}"
SRCREV = "b05de6a7d8008787f1d7f60ee4c28e1fff2d366c"

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

# additional python modules (not part of core python3 packages)
RDEPENDS_${PN} += " \
    python3-crcmod \
    python3-pyserial \
"

do_install () {
    install -d ${D}${bindir}
    install -m 0755 ${S}/selftest.py ${D}${bindir}/selftest
    install -m 0755 ${S}/eoltest.py ${D}${bindir}/eoltest
    install -m 0755 ${S}/fct-tool.py ${D}${bindir}/fct-tool
    install -m 0755 ${S}/ipr-tool.py ${D}${bindir}/ipr-tool
    install -m 0755 ${S}/homekit-tool.py ${D}${bindir}/homekit-tool
    install -m 0755 ${WORKDIR}/manufacturing-statusfiles.sh ${D}${bindir}/manufacturing-statusfiles
    install -m 0755 ${WORKDIR}/ipr-setup ${D}${bindir}
    install -m 0755 ${WORKDIR}/homekit-setup ${D}${bindir}
    install -m 0755 ${WORKDIR}/selftest-check ${D}${bindir}
    install -m 0755 ${WORKDIR}/eoltest-check.sh ${D}${bindir}/eoltest-check
    install -m 0755 ${WORKDIR}/eoltest-run.sh ${D}${bindir}/eoltest-run

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/manufacturing-statusfiles.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/ipr.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/selftest.service ${D}${systemd_unitdir}/system/
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
    install -m 0644 ${WORKDIR}/keep.d/eoltest ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/manufacturing-statusfiles ${D}${base_libdir}/upgrade/keep.d
}

pkg_postinst_${PN} () {
    cd $D${PYTHON_SITEPACKAGES_DIR} && python3 -m compileall .
}

SYSTEMD_SERVICE_${PN} += "manufacturing-statusfiles.service"
SYSTEMD_SERVICE_${PN} += "ipr.service"
SYSTEMD_SERVICE_${PN} += "selftest.service"
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
