DESCRIPTION = "Tool to migrate a Art. No. 19000 Gateway to the new architecture"
SUMMARY = "A collection of tools to migrate the old gateway (article number 19000) to the new architecture"
LICENSE = "CLOSED"

inherit allarch

RDEPENDS_${PN} = "\
    mtd-utils \
"

SRCBRANCH ?= "stage3"

SRC_URI += " \
    git://stash.dss.husqvarnagroup.com:7999/sg/gateway-migration.git;protocol=ssh;branch=${SRCBRANCH} \
"

PR = "r1"
PV = "2019-11-29-3+git${SRCPV}"

SRCREV ?= "e4accc304a612f0f89564b68593bda86c3c376fc"

S = "${WORKDIR}/git"

do_install() {
    install -D -m 0755 ${S}/stage3/stage3.sh ${D}${bindir}/${BPN}-stage3
}
