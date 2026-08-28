DESCRIPTION = "Creates a dummy GARDENA U-Boot environment for development"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PR = "r0"

DEPENDS += " \
    openssl-native \
    u-boot-mkenvimage-native \
    util-linux-native \
"

RDEPENDS:${PN} += " \
    libubootenv-bin \
"

# The environment holds the machine name, so it cannot be shared between machines
PACKAGE_ARCH = "${MACHINE_ARCH}"

# Has to match the environment size in fw_env.config
UBOOT_ENV_SIZE ?= "0x10000"

do_compile() {
    dummy_addr() {
        openssl rand -hex 5 | sed -e 's/../:&/g' -e 's/^/02/'
    }

    gatewayid=$(uuidgen)

    openssl req -x509 -newkey rsa:2048 -noenc -sha256 -days 36500 \
        -subj "/CN=$gatewayid" \
        -keyout ${B}/x509_key.pem -out ${B}/x509_crt.pem

    x509_crt=$(sed -z -e 's/\n/%/g' -e 's/%$//' ${B}/x509_crt.pem)
    x509_key=$(sed -z -e 's/\n/%/g' -e 's/%$//' ${B}/x509_key.pem)

    cat >${B}/fw_env.txt <<EOF
board_name=${MACHINE}
gateway_hardware_revision=0
gatewayid=$gatewayid
bootslot=0
sgtin=303400000000000000000000
linuxmoduleid=$(uuidgen)
linuxmodulehqvid=0000000000
linuxmoduleunielecid=0000000000
ethaddr=$(dummy_addr)
wifiaddr=$(dummy_addr)
radiomoduleid=$(uuidgen)
rmaddr=$(dummy_addr)
ipr_setup_done=1
fct_finalized=1
self_test_passed=1
hk_setup_done=1
eol_test_passed=1
dev_debug_allow_local_ssh=1
x509_crt=$x509_crt
x509_key=$x509_key
EOF

    uboot-mkenvimage -p 0x00 -s ${UBOOT_ENV_SIZE} -o ${B}/uboot.env ${B}/fw_env.txt
}

do_install() {
    install -d ${D}${sysconfdir}
    install -m 0644 ${B}/uboot.env ${D}${sysconfdir}/uboot.env
}
