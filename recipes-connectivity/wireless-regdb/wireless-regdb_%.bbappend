FILESEXTRAPATHS_prepend := "${THISDIR}/${BPN}:"

SRC_URI += " \
    file://0001-Reduce-max-possible-txpower-by-10dBm-for-all-frequen.patch \
"

unset do_compile[noexec]

# to reach max of 20dBm in DE with out gateway, we 
# need to reduce txpower by 10dBm. To do it we need 
# to create our own self signed regulatory.db
do_compile() {
    oe_runmake regulatory.db
}

do_install() {
    install -m 0644 -D regulatory.db ${D}${nonarch_base_libdir}/firmware/regulatory.db
    if [ -e ${D}${nonarch_base_libdir}/firmware/regulatory.db.p7s ]; then
        rm ${D}${nonarch_base_libdir}/firmware/regulatory.db.p7s
    fi
} 