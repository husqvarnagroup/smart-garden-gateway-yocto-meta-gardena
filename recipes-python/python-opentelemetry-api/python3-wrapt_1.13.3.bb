
SUMMARY = "Module for decorators, wrappers and monkey patching."
HOMEPAGE = "https://github.com/GrahamDumpleton/wrapt"
AUTHOR = "Graham Dumpleton <Graham.Dumpleton@gmail.com>"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=fdfc019b57affbe1d7a32e3d34e83db4"

SRC_URI = "https://files.pythonhosted.org/packages/eb/f6/d81ccf43ac2a3c80ddb6647653ac8b53ce2d65796029369923be06b815b8/wrapt-1.13.3.tar.gz"
SRC_URI[md5sum] = "50efce974cc8a0d39fd274d74eb0fd1e"
SRC_URI[sha256sum] = "1fea9cd438686e6682271d36f3481a9f3636195578bab9ca3382e2f5f01fc185"

S = "${WORKDIR}/wrapt-1.13.3"

RDEPENDS:${PN} = ""

inherit setuptools3
