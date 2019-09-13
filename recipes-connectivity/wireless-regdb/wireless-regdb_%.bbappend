FILESEXTRAPATHS_prepend_mt7688 := "${THISDIR}/${BPN}:"

SRC_URI_append_mt7688 = " \
    file://0001-Reduce-max-possible-txpower-by-10dBm-for-all-frequen.patch \
"

unset do_compile[noexec]

inherit python3native

DEPENDS_append_mt7688 = " python3-attrs-native"

# to reach max of 20dBm in DE with out gateway, we 
# need to reduce txpower by 10dBm. To do it we need 
# to create our own self signed regulatory.db
do_compile() {
    # unset doesn't work conditionally so we have to do the check here.
    # furthermore, if we define 'do_compile' for mt7688 only,
    # the oe_runmake-based default implementation will be run
    if [ "${MACHINE}" = "gardena-sg-mt7688" ]; then
        oe_runmake maintainer-clean
        python3 ./db2fw.py regulatory.db db.txt
    fi
}

do_install_mt7688() {
    install -m 0644 -D regulatory.db ${D}${nonarch_base_libdir}/firmware/regulatory.db
    if [ -e ${D}${nonarch_base_libdir}/firmware/regulatory.db.p7s ]; then
        rm ${D}${nonarch_base_libdir}/firmware/regulatory.db.p7s
    fi
} 
