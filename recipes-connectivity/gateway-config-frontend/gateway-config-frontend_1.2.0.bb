SUMMARY = "Frontend component for the GARDENA smart Gateway config interface"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "Proprietary"

LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28 \
    file://third-party-licenses.txt;md5=05b4c6864c181049f0a08b49242f97e7 \
    "

PR = "r1"

SRC_URI += "git://ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-gateway-config-frontend;protocol=ssh;branch=build"
SRCREV = "5126abb259f79ae7504dc5f9b45806a489ed42eb"
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
