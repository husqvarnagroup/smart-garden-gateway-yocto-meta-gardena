SUMMARY = "Brave New World LWM2M Server FOSS dependencies"

inherit packagegroup

PR = "r2"

RDEPENDS:${PN} += " \
    python3-aiorun \
    python3-core \
    python3-dbus-next \
    python3-dictdiffer \
    python3-importlib-resources \
    python3-netifaces \
    python3-pkgutil \
    python3-plistlib \
    python3-transitions \
    python3-cryptography \
    tzdata \
"
