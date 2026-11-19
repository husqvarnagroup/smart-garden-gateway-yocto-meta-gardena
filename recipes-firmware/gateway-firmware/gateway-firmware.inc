SUMMARY = "Firmware for the radio module (RM)"
LICENSE = "Proprietary & Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

inherit systemd allarch

# Note: lb_radio_gateway.sh is a re-implementation in shell of the original
# client: gateway-firmware-zephyr-3.6.2/lb_radio_gateway.py is the unmodified
# copy of the file 'api_client' from the directory 
# 'application/lb_radio_gateway/' in the sg-zephyr-firmware repository.
# This way there is one less dependency to Python. Possibly this also reduces
# the memory footprint on the gateway.
# The original Python script is not installed anymore to the target.
#
# While not installed on the gateway, the file zephyr.elf is stored in this
# repository to ensure we have something to feed GDB with even once CI dropped
# the corresponding build artifacts.
SRC_URI = "\
    file://zephyr.bin \
    file://zephyr.elf \
    file://rm-flashing.service \
    file://rm-flashing.sh \
    file://rm-flashtool.sh \
    file://keep.d/rm-flashing \
    file://lb_radio_gateway.sh \
"

INC_PR = "r11"

PE = "1"

RDEPENDS:${PN} = " \
    reset-rm \
    socat \
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
    install -m 0755 ${WORKDIR}/rm-flashtool.sh ${D}${bindir}/rm-flashtool
    install -m 0755 ${WORKDIR}/lb_radio_gateway.sh ${D}${bindir}/lb_radio_gateway

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/rm-flashing.service ${D}${systemd_unitdir}/system

    install -d ${D}${base_libdir}/upgrade/keep.d
    install -m 0644 ${WORKDIR}/keep.d/rm-flashing ${D}${base_libdir}/upgrade/keep.d

    install -d ${D}${sysconfdir}
    echo ${PV} > ${D}${sysconfdir}/rm-firmware-version.latest
}

# On QEMU, the radio module cannot be flashed. Marking the shipped firmware
# version as installed makes rm-flashing.service skip flashing. Otherwise, it
# would fail and ppp.service, which requires it, would not start.
do_install:append:qemuarmv5 () {
    echo ${PV} > ${D}${sysconfdir}/rm-firmware-version
}

FILES:${PN}:append:qemuarmv5 = " ${sysconfdir}/rm-firmware-version"

FILES:${PN} += "\
    ${datadir}/gardena/firmware/${PV}/gateway.bin \
    ${datadir}/gardena/firmware/current \
    ${bindir}/rm-flashing \
    ${systemd_unitdir}/system/rm-flashing.service \
    ${sysconfdir}/rm-firmware-version.latest \
    ${base_libdir}/upgrade/keep.d/rm-flashing \
"

SYSTEMD_SERVICE:${PN} += "rm-flashing.service"
