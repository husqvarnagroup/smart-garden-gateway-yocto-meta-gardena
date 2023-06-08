SUMMARY = "Gateway config interface backend FOSS dependencies"

inherit packagegroup

PR = "r0"

RDEPENDS:${PN} += " \
    openssl \
    openssl-bin \
    openssl-conf \
"
