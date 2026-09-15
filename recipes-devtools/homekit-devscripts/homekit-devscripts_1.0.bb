DESCRIPTION = "Development and audit tools for Apple HomeKit"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PR = "r4"

SRC_URI = " \
    file://changeMdnsName.sh \
    file://forgetWifiCredentials.sh \
    file://resetHomeKitPairings.sh \
    file://resetMdnsName.sh \
    file://stopSleepStartWifi.sh \
"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/changeMdnsName.sh ${D}${bindir}/changeMdnsName
    install -m 0755 ${UNPACKDIR}/forgetWifiCredentials.sh ${D}${bindir}/forgetWifiCredentials
    install -m 0755 ${UNPACKDIR}/resetHomeKitPairings.sh ${D}${bindir}/resetHomeKitPairings
    install -m 0755 ${UNPACKDIR}/resetMdnsName.sh ${D}${bindir}/resetMdnsName
    install -m 0755 ${UNPACKDIR}/stopSleepStartWifi.sh ${D}${bindir}/stopSleepStartWifi
}
