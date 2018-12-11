MAJOR = "${@'${PV}'.split('.')[0]}"
MINOR = "${@'${PV}'.split('.')[1]}"
PATCH = "${@'${PV}'.split('.')[2]}"

PR_append = ".0"

do_install_append() {
    # Additional link needed by shadoway
    ln -sf libcryptopp.so.${PV} ${D}${libdir}/libcryptopp.so.${MAJOR}.${MINOR}
}
