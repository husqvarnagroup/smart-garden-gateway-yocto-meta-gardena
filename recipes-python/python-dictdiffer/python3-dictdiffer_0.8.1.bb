SUMMARY = "Dictdiffer is a library that helps you to diff and patch dictionaries."
SECTION = "devel/python"
HOMEPAGE = "https://github.com/inveniosoftware/dictdiffer"
AUTHOR = "Invenio Collaboration <info@inveniosoftware.org>"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5319f20678c5972ead78588780fbd47a"

inherit python3native
inherit pypi
inherit setuptools3

DEPENDS += "python3-setuptools-scm-native python3-pytest-runner-native"

RDEPENDS:${PN} += " \
    python3-plistlib \
"

PYPI_PACKAGE = "dictdiffer"
SRC_URI[md5sum] = "3185fe683d976282bf6313de14b7c7e9"
SRC_URI[sha256sum] = "1adec0d67cdf6166bda96ae2934ddb5e54433998ceab63c984574d187cc563d2"

