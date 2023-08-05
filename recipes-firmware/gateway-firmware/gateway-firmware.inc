SUMMARY = "Firmware for the radio module (RM)"
LICENSE = "CLOSED"

inherit systemd allarch

SRC_URI = "\
    file://zephyr.bin \
    file://rm-flashing.service \
    file://rm-flashing.sh \
    file://rm-flashtool.py \
    file://keep.d/rm-flashing \
"

INC_PR = "r1"

RDEPENDS:${PN} = " \
    python3-io \
    reset-rm \
"

do_install () {
    install -d ${D}${datadir}/gardena/firmware/${PV}
    install -m 0644 ${WORKDIR}/zephyr.bin ${D}${datadir}/gardena/firmware/${PV}/gateway.bin

    cd ${D}${datadir}/gardena/firmware/
    rm -f current
    ln -s ${PV} current

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/rm-flashing.sh ${D}${bindir}/rm-flashing
    install -m 0755 ${WORKDIR}/rm-flashtool.py ${D}${bindir}/rm-flashtool

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/rm-flashing.service ${D}${systemd_unitdir}/system

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/rm-flashing ${D}${base_libdir}/upgrade/keep.d

    install -d ${D}${sysconfdir}
    echo ${PV} > ${D}${sysconfdir}/rm-firmware-version.latest
}

FILES:${PN} += "\
    ${datadir}/gardena/firmware/${PV}/gateway.bin \
    ${datadir}/gardena/firmware/current \
    ${bindir}/rm-flashing \
    ${systemd_unitdir}/system/rm-flashing.service \
    ${sysconfdir}/rm-firmware-version.latest \
    ${base_libdir}/upgrade/keep.d/rm-flashing \
"

SYSTEMD_SERVICE:${PN} += "rm-flashing.service"

RPROVIDES_${PN} += "virtual/gardena-gateway-firmware"
