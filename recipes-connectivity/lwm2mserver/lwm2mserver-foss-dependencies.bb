SUMMARY = "Brave New World LWM2M Server FOSS dependencies"

inherit packagegroup

PR = "r0"

RDEPENDS:${PN} += " \
    python3-aiorun \
    python3-core \
    python3-netifaces \
    python3-pkg-resources \
    python3-pkgutil \
    python3-plistlib \
    python3-pynng \
    python3-typing \
    python3-transitions \
    python3-opentelemetry-api \
    python3-opentelemetry-semantic-conventions \
    python3-cryptography \
    python3-crcmod \
"
