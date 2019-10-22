SUMMARY = "Shim to create manufacturing tools status files"
LICENSE = "CLOSED"

COMPATIBLE_MACHINE = "at91sam9x5"

inherit systemd allarch

SRC_URI += " \
    file://eoltest-check.service \
    file://manufacturing-statusfiles.service \
    file://manufacturing-statusfiles.sh \
    file://keep.d/eoltest \
    file://keep.d/manufacturing-statusfiles \
    "

PV = "2019-10-22"
PR = "r0"

FILES_${PN} += " \
    ${base_libdir}/upgrade/keep.d \
"

do_install () {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/manufacturing-statusfiles.sh ${D}${bindir}/manufacturing-statusfiles

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/eoltest-check.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/manufacturing-statusfiles.service ${D}${systemd_unitdir}/system/

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/eoltest ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/manufacturing-statusfiles ${D}${base_libdir}/upgrade/keep.d
}

SYSTEMD_SERVICE_${PN} += "manufacturing-statusfiles.service"
SYSTEMD_SERVICE_${PN} += "eoltest-check.service"
