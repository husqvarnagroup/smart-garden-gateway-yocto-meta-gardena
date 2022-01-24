
SUMMARY = "Python @deprecated decorator to deprecate old python classes, functions or methods."
HOMEPAGE = "https://github.com/tantale/deprecated"
AUTHOR = "Laurent LAPORTE <tantale.solutions@gmail.com>"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.rst;md5=44288e26f4896bdab14072d4fa35ff01"

SRC_URI = "https://files.pythonhosted.org/packages/c8/d1/e412abc2a358a6b9334250629565fe12697ca1cdee4826239eddf944ddd0/Deprecated-1.2.13.tar.gz"
SRC_URI[md5sum] = "2b7a15b559af0b9b499737d70e171b4b"
SRC_URI[sha256sum] = "43ac5335da90c31c24ba028af536a91d41d53f9e6901ddb021bcc572ce44e38d"

S = "${WORKDIR}/Deprecated-1.2.13"

RDEPENDS_${PN} = "python3-wrapt"

inherit setuptools3
