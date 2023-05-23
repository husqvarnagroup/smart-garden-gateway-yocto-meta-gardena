SUMMARY = "List current devices on the gateway from local file storage"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit allarch

SRC_URI = " \
    file://list_devices.sh \
"

PV = "1.0.1"

RDEPENDS:${PN} = "busybox"

FILES:${PN} += " \
    ${bindir}/list-lemonbeat-devices \
"

do_install () {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/list_devices.sh ${D}${bindir}/list-lemonbeat-devices
}
