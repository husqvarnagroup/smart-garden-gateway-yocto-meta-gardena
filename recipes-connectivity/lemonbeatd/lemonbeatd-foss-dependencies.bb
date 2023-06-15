SUMMARY = "Brave New World Lemonbeat Server FOSS dependencies"

inherit packagegroup

PR = "r0"

RDEPENDS:${PN} += " \
    list-lemonbeat-devices \
    openssl \
    openssl-bin \
    openssl-conf \
"
