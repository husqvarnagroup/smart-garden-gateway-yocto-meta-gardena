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
SRC_URI[md5sum] = "524b353b969300d4dc6aa6720c953657"
SRC_URI[sha256sum] = "17bacf5fbfe613ccf1b6d512bd766e6b21fb798822a133aa86098b8ac9997578"

