DESCRIPTION = "Tool to migrate a Art. No. 19000 Gateway to the new architecture"
SUMMARY = "A collection of tools to migrate the old gateway (article number 19000) to the new architecture"
LICENSE = "CLOSED"

inherit allarch

RDEPENDS:${PN} = "\
    mtd-utils \
    wpa-supplicant-passphrase \
"

SRCBRANCH ?= "main"

SRC_URI += " \
    git://ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-gateway-migration;protocol=ssh;branch=${SRCBRANCH} \
"

PR = "r0"
PV = "2023-04-17-0+git${SRCPV}"

SRCREV ?= "12bfd1295ce120d9e307575c5ce6d1976bf4fea6"

S = "${WORKDIR}/git"

do_install() {
    install -D -m 0755 ${S}/migration/migration-stage3.sh ${D}${bindir}/${BPN}-stage3
}
