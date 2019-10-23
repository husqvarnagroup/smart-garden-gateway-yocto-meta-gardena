SUMMARY = "Prebuilt Binary Package for U-Boot"
DESCRIPTION = "Used by e.g. SWUpdate to avoid unnecessary flashing operation in the field"
PROVIDES = "virtual/bootloader"
LICENSE = "GPLv2+"

LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0;md5=801f80980d171dd6425610833a22dbe6"

INC_PR = "r2"

inherit deploy

UBOOT_SUFFIX ??= "bin"
UBOOT_BINARY ?= "u-boot.${UBOOT_SUFFIX}"
UBOOT_IMAGE ?= "u-boot-${MACHINE}-${PV}-${PR}.${UBOOT_SUFFIX}"
UBOOT_SYMLINK ?= "u-boot-${MACHINE}.${UBOOT_SUFFIX}"

# Additional environment variables or a script can be installed alongside
# u-boot to be used automatically on boot. Machine configurations needing one
# of these files should include it in the SRC_URI and set the UBOOT_ENV
# parameter.
UBOOT_ENV_SUFFIX ?= "txt"
UBOOT_ENV ?= ""
UBOOT_ENV_BINARY ?= "${UBOOT_ENV}.${UBOOT_ENV_SUFFIX}"
UBOOT_ENV_IMAGE ?= "${UBOOT_ENV}-${MACHINE}-${PV}-${PR}.${UBOOT_ENV_SUFFIX}"
UBOOT_ENV_SYMLINK ?= "${UBOOT_ENV}-${MACHINE}.${UBOOT_ENV_SUFFIX}"

FILESEXTRAPATHS_prepend := "${THISDIR}/files/${MACHINE}:"

SRC_URI = " \
    file://${UBOOT_BINARY} \
    file://fw_env.config \
"

FILES_${PN} = "${sysconfdir}"

do_deploy () {
    install -d ${DEPLOYDIR}
    install -m 644 ${WORKDIR}/${UBOOT_BINARY} ${DEPLOYDIR}/${UBOOT_IMAGE}
    cd ${DEPLOYDIR}
    rm -f ${UBOOT_SYMLINK} ${UBOOT_BINARY}
    ln -sf ${UBOOT_IMAGE} ${UBOOT_SYMLINK}
    ln -sf ${UBOOT_IMAGE} ${UBOOT_BINARY}

    if [ -n "${UBOOT_ENV}" ]
    then
        install -m 644 ${WORKDIR}/${UBOOT_ENV_BINARY} ${DEPLOYDIR}/${UBOOT_ENV_IMAGE}
        rm -f ${DEPLOYDIR}/${UBOOT_ENV_BINARY} ${DEPLOYDIR}/${UBOOT_ENV_SYMLINK}
        ln -sf ${UBOOT_ENV_IMAGE} ${DEPLOYDIR}/${UBOOT_ENV_BINARY}
        ln -sf ${UBOOT_ENV_IMAGE} ${DEPLOYDIR}/${UBOOT_ENV_SYMLINK}
    fi
}

addtask deploy before do_build after do_compile
