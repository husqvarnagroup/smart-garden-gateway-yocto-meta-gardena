DESCRIPTION = "Tool to migrate a Art. No. 19000 Gateway to the new architecture"
SUMMARY = "A collection of tools to migrate the old gateway (article number 19000) to the new architecture"
LICENSE = "CLOSED"

inherit allarch

RDEPENDS_${PN} = "\
    mtd-utils \
"

SRCBRANCH ?= "master"

SRC_URI += " \
    git://stash.dss.husqvarnagroup.com:7999/sg/gateway-migration.git;protocol=ssh;branch=${SRCBRANCH} \
"

PR = "r0"
PV = "2019-12-08-0+git${SRCPV}"

SRCREV ?= "e9413868662858a388ea640db26399f6de9da459"

S = "${WORKDIR}/git"

do_install() {
    install -D -m 0755 ${S}/stage3/stage3.sh ${D}${bindir}/${BPN}-stage3
}
