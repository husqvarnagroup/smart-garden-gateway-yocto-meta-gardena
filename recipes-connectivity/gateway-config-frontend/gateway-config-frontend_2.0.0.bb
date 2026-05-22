SUMMARY = "Frontend component for the GARDENA smart Gateway config interface"
HOMEPAGE = "https://www.gardena.com/"
LICENSE = "GPL-3.0-or-later"

LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/GPL-3.0-or-later;md5=1c76c4cc354acaac30ed4d5eefea7245 \
"

PR = "r0"

SRC_URI += "https://github.com/husqvarnagroup/smart-garden-gateway-config-frontend/releases/download/v${PV}/sg-gateway-config-frontend-v${PV}.tar.gz"
SRC_URI[sha256sum] = "9fa78a2074d3bb71da65a3c5bd918415f647aff630bcd77c54a7082f90b0b32c"

python do_unpack () {
    """Inspired by the default `do_unpack` task.
       The default task unpacks everything into `WORKDIR`, where other
       Bitbake related files are located. This makes it hard to install
       just the files from the archive to the root filesystem.
       See also:
       https://lists.yoctoproject.org/g/docs/topic/wokrdir_change_transition/106113486
       Note: This custom task might be unnecessary with Yocto Wrynose.
       """
    from pathlib import Path
    src_uri = (d.getVar('SRC_URI') or "").split()
    if not src_uri:
        return

    try:
        fetcher = bb.fetch2.Fetch(src_uri, d)
        fetcher.unpack(d.getVar('UNPACKDIR'))
    except bb.fetch2.BBFetchException as e:
        bb.fatal("Bitbake Fetcher Error: " + repr(e))
}

WWWDIR = "${datadir}/gateway-config-interface/www"

do_install () {
    install -d ${D}${WWWDIR}
    cp -dr ${UNPACKDIR}/* ${D}${WWWDIR}
    find ${D}${WWWDIR} \( -type d \! -perm 0755 -exec chmod 00755 -- '{}' + \) -o \( -type f \! -perm 0644 -exec chmod 00644 -- '{}' + \)
}

FILES:${PN} += "\
    ${WWWDIR} \
    "
