SUMMARY = "Device setup and testing during manufacturing"
LICENSE = "CLOSED"

DEPENDS = "python3-native"

COMPATIBLE_MACHINE = "mt7688"

inherit systemd allarch python3-dir python3native

SRC_URI = "file://bootstrap.py \
           file://cpms_client.py \
           file://cpms_config.py \
           file://eoltest-check.service \
           file://eoltest-check.sh \
           file://eoltest-run.sh \
           file://eoltest.py \
           file://eoltest.service \
           file://errorhandler.py \
           file://fct-tool.py \
           file://homekit-setup \
           file://homekit-setup.service \
           file://homekit-tool.py \
           file://ipr-setup \
           file://ipr-tool.py \
           file://ipr.service \
           file://keep.d/eoltest \
           file://keep.d/manufacturing-statusfiles \
           file://manufacturing-statusfiles.service \
           file://manufacturing-statusfiles.sh \
           file://radio_module_test.py \
           file://selftest-check \
           file://selftest.py \
           file://selftest.service \
           file://testing.py \
           file://util.py \
"

PR = "r0"

PV = "2023-08-17"
PE = "1"

FILES:${PN} += " \
    ${PYTHON_SITEPACKAGES_DIR}/bootstrap.py \
    ${PYTHON_SITEPACKAGES_DIR}/util.py \
    ${PYTHON_SITEPACKAGES_DIR}/testing.py \
    ${PYTHON_SITEPACKAGES_DIR}/cpms_client.py \
    ${PYTHON_SITEPACKAGES_DIR}/cpms_config.py \
    ${PYTHON_SITEPACKAGES_DIR}/radio_module_test.py \
    ${base_libdir}/upgrade/keep.d \
"

RDEPENDS:${PN} += " \
    fct-tool \
    python3-core \
    python3-datetime \
    python3-json \
    python3-threading \
    python3-unittest \
"

# additional python modules (not part of core python3 packages)
RDEPENDS:${PN} += " \
    python3-crcmod \
    python3-pyserial \
"

do_install () {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/eoltest-check.sh ${D}${bindir}/eoltest-check
    install -m 0755 ${WORKDIR}/eoltest-run.sh ${D}${bindir}/eoltest-run
    install -m 0755 ${WORKDIR}/eoltest.py ${D}${bindir}/eoltest
    install -m 0755 ${WORKDIR}/errorhandler.py ${D}${bindir}/cpms-errorhandler
    install -m 0755 ${WORKDIR}/fct-tool.py ${D}${bindir}/fct-tool
    install -m 0755 ${WORKDIR}/homekit-setup ${D}${bindir}
    install -m 0755 ${WORKDIR}/homekit-tool.py ${D}${bindir}/homekit-tool
    install -m 0755 ${WORKDIR}/ipr-setup ${D}${bindir}
    install -m 0755 ${WORKDIR}/ipr-tool.py ${D}${bindir}/ipr-tool
    install -m 0755 ${WORKDIR}/manufacturing-statusfiles.sh ${D}${bindir}/manufacturing-statusfiles
    install -m 0755 ${WORKDIR}/selftest-check ${D}${bindir}
    install -m 0755 ${WORKDIR}/selftest.py ${D}${bindir}/selftest

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/eoltest-check.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/eoltest.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/homekit-setup.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/ipr.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/manufacturing-statusfiles.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/selftest.service ${D}${systemd_unitdir}/system

    install -d 0755 ${D}${PYTHON_SITEPACKAGES_DIR}
    install -m 0755 ${WORKDIR}/bootstrap.py ${D}${PYTHON_SITEPACKAGES_DIR}
    install -m 0755 ${WORKDIR}/cpms_client.py ${D}${PYTHON_SITEPACKAGES_DIR}
    install -m 0755 ${WORKDIR}/cpms_config.py ${D}${PYTHON_SITEPACKAGES_DIR}
    install -m 0755 ${WORKDIR}/radio_module_test.py ${D}${PYTHON_SITEPACKAGES_DIR}
    install -m 0755 ${WORKDIR}/testing.py ${D}${PYTHON_SITEPACKAGES_DIR}
    install -m 0755 ${WORKDIR}/util.py ${D}${PYTHON_SITEPACKAGES_DIR}

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/eoltest ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/manufacturing-statusfiles ${D}${base_libdir}/upgrade/keep.d
}

pkg_postinst:${PN} () {
    cd $D${PYTHON_SITEPACKAGES_DIR} && python3 -m compileall .
}

SYSTEMD_SERVICE:${PN} += "manufacturing-statusfiles.service"
SYSTEMD_SERVICE:${PN} += "ipr.service"
SYSTEMD_SERVICE:${PN} += "selftest.service"
SYSTEMD_SERVICE:${PN} += "eoltest-check.service"
SYSTEMD_SERVICE:${PN} += "eoltest.service"
SYSTEMD_SERVICE:${PN} += "homekit-setup.service"

PACKAGES =+ "fct-tool"
PROVIDES =+ "fct-tool"

FILES:fct-tool += "${bindir}/fct-tool"

RDEPENDS:fct-tool += " \
    iw \
    openocd \
    python3-core \
    python3-datetime \
    python3-evdev \
    systemd \
"

RCONFLICTS:fct-tool += " \
    procps \
"
