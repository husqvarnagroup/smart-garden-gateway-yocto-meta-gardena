
SUMMARY = "Universal encoding detector for Python 2 and 3"
HOMEPAGE = "https://github.com/chardet/chardet"
AUTHOR = "Mark Pilgrim <mark@diveintomark.org>"
LICENSE = "LGPL-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a6f89e2100d9b6cdffcea4f398e37343"

SRC_URI = "https://files.pythonhosted.org/packages/ee/2d/9cdc2b527e127b4c9db64b86647d567985940ac3698eeabc7ffaccb4ea61/chardet-4.0.0.tar.gz"
SRC_URI[md5sum] = "bc9a5603d8d0994b2d4cbf255f99e654"
SRC_URI[sha256sum] = "0d6f53a15db4120f2b08c94f11e7d93d2c911ee118b6b30a04ec3ee8310179fa"

S = "${WORKDIR}/chardet-4.0.0"

RDEPENDS_${PN} = ""

inherit setuptools3
