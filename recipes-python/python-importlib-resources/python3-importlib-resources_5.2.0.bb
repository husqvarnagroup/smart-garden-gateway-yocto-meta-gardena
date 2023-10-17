SUMMARY = "Read resources from Python packages"
HOMEPAGE = "https://github.com/python/importlib_resources"
AUTHOR = "Barry Warsaw <barry@python.org>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e81780ac4c0888aaef94a7cb49b55edc"

inherit setuptools3

SRC_URI = "https://files.pythonhosted.org/packages/source/i/importlib-resources/importlib_resources-5.2.0.tar.gz"
SRC_URI[sha256sum] = "22a2c42d8c6a1d30aa8a0e1f57293725bfd5c013d562585e46aff469e0ff78b3"

S = "${WORKDIR}/importlib_resources-5.2.0"

RDEPENDS:${PN} += "python3-zipp"
