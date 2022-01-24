
SUMMARY = "Internationalized Domain Names in Applications (IDNA)"
HOMEPAGE = "https://github.com/kjd/idna"
AUTHOR = "Kim Davies <kim@cynosure.com.au>"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=239668a7c6066d9e0c5382e9c8c6c0e1"

SRC_URI = "https://files.pythonhosted.org/packages/62/08/e3fc7c8161090f742f504f40b1bccbfc544d4a4e09eb774bf40aafce5436/idna-3.3.tar.gz"
SRC_URI[md5sum] = "5856306eac5f25db8249e37a4c6ee3e7"
SRC_URI[sha256sum] = "9d643ff0a55b762d5cdb124b8eaa99c66322e2157b69160bc32796e824360e6d"

S = "${WORKDIR}/idna-3.3"

RDEPENDS_${PN} = ""

inherit setuptools3
