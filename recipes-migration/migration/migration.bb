DESCRIPTION = "Tool to migrate a Art. No. 19000 Gateway to the new architecture"
SUMMARY = "A collection of tools to migrate the old gateway (article number 19000) to the new architecture"
LICENSE = "CLOSED"

inherit allarch

RDEPENDS_${PN} = "\
    mtd-utils \
"

SRCBRANCH ?= "main"

SRC_URI += " \
    git://ssh.dev.azure.com/v3/HQV-Gardena/SG-Gateway/sg-gateway-migration;protocol=ssh;branch=${SRCBRANCH} \
"

PR = "r0"
PV = "2019-12-14-0+git${SRCPV}"

SRCREV ?= "de0d7b0d932f642252a32181b5e2a16d994fc60e"

S = "${WORKDIR}/git"

do_install() {
    install -D -m 0755 ${S}/stage3/stage3.sh ${D}${bindir}/${BPN}-stage3
}
