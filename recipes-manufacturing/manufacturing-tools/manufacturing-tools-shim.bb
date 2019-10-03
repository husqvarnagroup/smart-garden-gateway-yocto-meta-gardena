SUMMARY = "Shim to create manufacturing tools status files"
LICENSE = "CLOSED"

COMPATIBLE_MACHINE = "at91sam9x5"

inherit systemd allarch

SRC_URI += " \
    file://fctcheck.service \
    file://eoltest-check.service \
    file://keep.d/fctcheck \
    file://keep.d/eoltest \
    "

PV = "2019-10-03"
PR = "r0"

FILES_${PN} += " \
    ${base_libdir}/upgrade/keep.d \
"

do_install () {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/fctcheck.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/eoltest-check.service ${D}${systemd_unitdir}/system/

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/fctcheck ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/eoltest ${D}${base_libdir}/upgrade/keep.d
}

SYSTEMD_SERVICE_${PN} += "fctcheck.service"
SYSTEMD_SERVICE_${PN} += "eoltest-check.service"
