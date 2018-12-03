FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
    file://0001-jtag-sysfsgpio-clean-up-swd-gpios.patch \
    file://gardena_radio.cfg \
    file://gardena_nrf52.cfg \
"

# We are using upstream and most patches in the base .bb are already upstreamed.
SRC_URI_remove = "file://0001-Add-fallthrough-comments.patch \
                  file://0002-Workaround-new-warnings-generated-by-GCC-7.patch\
                  file://0003-armv7a-Add-missing-break-to-fix-fallthrough-warning.patch \
                  file://0005-command-Move-the-fall-through-comment-to-right-scope.patch"

SRCREV="d0be1630dc080b0c881830fa28bf2ccfe7850bb8"

do_install_append () {
    install -m 0644 ${WORKDIR}/gardena_radio.cfg ${D}${datadir}/openocd/scripts/board/
    install -m 0644 ${WORKDIR}/gardena_nrf52.cfg ${D}${datadir}/openocd/scripts/board/
}
