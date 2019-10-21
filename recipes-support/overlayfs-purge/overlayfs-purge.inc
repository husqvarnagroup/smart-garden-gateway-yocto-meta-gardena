# XXX: WORKAROUND FOR RUST COMPILER BUGS
SRC_URI_append_at91sam9x5 = " \
    file://disable-lto.patch \
"
