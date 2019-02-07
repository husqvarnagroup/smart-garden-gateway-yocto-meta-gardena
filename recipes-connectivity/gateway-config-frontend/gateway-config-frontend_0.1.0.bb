SUMMARY = "Frontend component for the GARDENA smart Gateway config interface"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "Proprietary"

LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28 \
    file://third-party-licenses.txt;md5=2f8fdc4b51b68e73c3501d1856688848 \
    "

PR = "r0"

SRC_URI += "git://stash.dss.husqvarnagroup.com/scm/sg/gateway-config-frontend.git;protocol=https;branch=build"
SRCREV = "31a4adc267459c5c983156e355825a6a27ad8200"
S = "${WORKDIR}/git"

WWWDIR = "${datadir}/gateway-config-interface/www"

do_install () {
    install -d ${D}${WWWDIR}
    cp -dr ${S}/* ${D}${WWWDIR}
    rm ${D}${WWWDIR}/third-party-licenses.txt
    find ${D}${WWWDIR} \( -type d \! -perm 0755 -exec chmod 00755 -- '{}' + \) -o \( -type f \! -perm 0644 -exec chmod 00644 -- '{}' + \)
}

FILES_${PN} += "\
    ${WWWDIR} \
    "
