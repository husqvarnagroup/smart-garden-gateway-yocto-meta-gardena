DESCRIPTION = "Development and audit tools for Apple HomeKit"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

PR = "r1"

SRC_URI = " \
    file://changeMdnsName.sh \
    file://forgetWifiCredentials.sh \
    file://resetHomeKitPairings.sh \
    file://resetMdnsName.sh \
    file://stopSleepStartWifi.sh \
"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/changeMdnsName.sh ${D}${bindir}/changeMdnsName
    install -m 0755 ${WORKDIR}/forgetWifiCredentials.sh ${D}${bindir}/forgetWifiCredentials
    install -m 0755 ${WORKDIR}/resetHomeKitPairings.sh ${D}${bindir}/resetHomeKitPairings
    install -m 0755 ${WORKDIR}/resetMdnsName.sh ${D}${bindir}/resetMdnsName
    install -m 0755 ${WORKDIR}/stopSleepStartWifi.sh ${D}${bindir}/stopSleepStartWifi
}
