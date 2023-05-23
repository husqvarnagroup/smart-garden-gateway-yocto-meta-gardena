
SUMMARY = "Function decoration for backoff and retry"
HOMEPAGE = "https://github.com/litl/backoff"
AUTHOR = "Bob Green <rgreen@aquent.com>"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=11aa62344867e52ff1061aeb075eaa42"

SRC_URI = "https://files.pythonhosted.org/packages/27/d2/9d2d0f0d6bbe17628b031040b1dadaee616286267e660ad5286a5ed657da/backoff-1.11.1.tar.gz"
SRC_URI[md5sum] = "dfb75f2b2fc54b7c603b54d06a71e996"
SRC_URI[sha256sum] = "ccb962a2378418c667b3c979b504fdeb7d9e0d29c0579e3b13b86467177728cb"

S = "${WORKDIR}/backoff-1.11.1"

RDEPENDS:${PN} = ""

inherit setuptools3
