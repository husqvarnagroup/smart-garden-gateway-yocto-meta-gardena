SUMMARY = "Frontend component for the GARDENA smart Gateway config interface"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "Proprietary"

LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28 \
    file://third-party-licenses.txt;md5=1b70a7e2103115a9c6133426c88df0eb \
    "

PR = "r0"

SRC_URI += "git://stash.dss.husqvarnagroup.com/scm/sg/gateway-config-frontend.git;protocol=https;branch=build"
SRCREV = "4982c6dc9b3c5dec805c9f42de45c767373db93e"
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
