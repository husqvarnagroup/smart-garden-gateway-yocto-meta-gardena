
SUMMARY = "Python package for providing Mozilla's CA Bundle."
HOMEPAGE = "https://certifiio.readthedocs.io/en/latest/"
AUTHOR = "Kenneth Reitz <me@kennethreitz.com>"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=67da0714c3f9471067b729eca6c9fbe8"

SRC_URI = "https://files.pythonhosted.org/packages/6c/ae/d26450834f0acc9e3d1f74508da6df1551ceab6c2ce0766a593362d6d57f/certifi-2021.10.8.tar.gz"
SRC_URI[md5sum] = "880ed9e5d04aff8f46f5ff82a3a3e395"
SRC_URI[sha256sum] = "78884e7c1d4b00ce3cea67b44566851c4343c120abd683433ce934a68ea58872"

S = "${WORKDIR}/certifi-2021.10.8"

RDEPENDS_${PN} = ""

inherit setuptools3
