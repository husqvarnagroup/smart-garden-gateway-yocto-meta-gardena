SUMMARY = "Firmware for the radio module (RM)"
LICENSE = "Proprietary & Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

inherit systemd allarch

# Note: lb_radio_gateway.py is an unmodified copy of the file 'api_client'
# from the directory 'application/lb_radio_gateway/' in the
# sg-zephyr-firmware repository. As long as the API version remains at
# one, the client is backwards-compatible and the latest version can
# always be used. Changes to this file should be done only in the
# sg-zephyr-firmware repository.
#
# While not installed on the gateway, the file zephyr.elf is stored in this
# repository to ensure we have something to feed GDB with even once CI dropped
# the corresponding build artifacts.
SRC_URI = "\
    file://zephyr.bin \
    file://zephyr.elf \
    file://rm-flashing.service \
    file://rm-flashing.sh \
    file://rm-flashtool.py \
    file://keep.d/rm-flashing \
    file://lb_radio_gateway.py \
"

INC_PR = "r7"

PE = "1"

RDEPENDS:${PN} = " \
    python3-io \
    reset-rm \
"

do_install () {
    strings ${WORKDIR}/zephyr.bin | grep --quiet ^${PV}\$ || bbfatal 'Version mismatch in zephyr.bin'
    strings ${WORKDIR}/zephyr.elf | grep --quiet ^${PV}\$ || bbfatal 'Version mismatch in zephyr.elf'

    install -d ${D}${datadir}/gardena/firmware/${PV}
    install -m 0644 ${WORKDIR}/zephyr.bin ${D}${datadir}/gardena/firmware/${PV}/gateway.bin

    cd ${D}${datadir}/gardena/firmware/
    rm -f current
    ln -s ${PV} current

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/rm-flashing.sh ${D}${bindir}/rm-flashing
    install -m 0755 ${WORKDIR}/rm-flashtool.py ${D}${bindir}/rm-flashtool
    install -m 0755 ${WORKDIR}/lb_radio_gateway.py ${D}${bindir}/lb_radio_gateway

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

RPROVIDES:${PN} += "virtual/gardena-gateway-firmware"
