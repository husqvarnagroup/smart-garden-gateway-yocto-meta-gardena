inherit setuptools3
require python-cython.inc

RDEPENDS:${PN} += "\
    python3-setuptools \
"

# running build_ext a second time during install fails, because Python
# would then attempt to import cythonized modules built for the target
# architecture.
SETUPTOOLS_INSTALL_ARGS += "--skip-build"

PACKAGESPLITFUNCS =+ "cython_fix_sources"

cython_fix_sources () {
	for f in ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Compiler/FlowControl.c \
		${PKGD}${TARGET_DBGSRC_DIR}/Cython/Compiler/FusedNode.c \
		${PKGD}${TARGET_DBGSRC_DIR}/Cython/Compiler/Scanning.c \
		${PKGD}${TARGET_DBGSRC_DIR}/Cython/Compiler/Visitor.c \
		${PKGD}${TARGET_DBGSRC_DIR}/Cython/Plex/Actions.c \
		${PKGD}${TARGET_DBGSRC_DIR}/Cython/Plex/Scanners.c \
		${PKGD}${TARGET_DBGSRC_DIR}/Cython/Runtime/refnanny.c \
		${PKGD}${TARGET_DBGSRC_DIR}/Cython/Tempita/_tempita.c \
		${PKGD}${libdir}/${PYTHON_DIR}/site-packages/Cython*/SOURCES.txt; do
		if [ -e $f ]; then
			sed -i -e 's#${WORKDIR}/Cython-${PV}#${TARGET_DBGSRC_DIR}#g' $f
		fi
	done
}
