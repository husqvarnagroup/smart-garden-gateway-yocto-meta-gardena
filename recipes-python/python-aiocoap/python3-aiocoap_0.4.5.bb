SUMMARY = "The aiocoap package is an implementation of CoAP, the Constrained Application Protocol."
SECTION = "devel/python"
HOMEPAGE = "https://github.com/chrysn/aiocoap"
AUTHOR = "Christian Amsüss <c.amsuess@energyharvesting.at>"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=afaf767baa20ac524dc12f1071ca493a"

inherit pypi
inherit setuptools3

PYPI_PACKAGE = "aiocoap"
SRC_URI[md5sum] = "03ca0411b7401bfd6e9bdaefe09bd88a"
SRC_URI[sha256sum] = "906c927822185c8acf04fdcc1dd98fd845f31dabf5d5e395c39137dcbd6ba1e3"

