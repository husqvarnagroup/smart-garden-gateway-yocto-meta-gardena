inherit setuptools3
require python-cython.inc

RDEPENDS:${PN} += "\
    python3-setuptools \
"

# running build_ext a second time during install fails, because Python
# would then attempt to import cythonized modules built for the target
# architecture.
SETUPTOOLS_INSTALL_ARGS += "--skip-build"

do_install:append() {
    # rename scripts that would conflict with the Python 2 build of Cython
    mv ${D}${bindir}/cython ${D}${bindir}/cython3
    mv ${D}${bindir}/cythonize ${D}${bindir}/cythonize3
    mv ${D}${bindir}/cygdb ${D}${bindir}/cygdb3
}

PACKAGESPLITFUNCS =+ "cython_fix_sources"

cython_fix_sources () {
    for f in ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Compiler/Code.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Compiler/FlowControl.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Compiler/FusedNode.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Compiler/Parsing.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Compiler/Scanning.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Compiler/Visitor.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Plex/Actions.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Plex/DFA.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Plex/Machines.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Plex/Scanners.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Plex/Transitions.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Runtime/refnanny.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Tempita/_tempita.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/AsyncGen.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/Buffer.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/Builtins.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/CMath.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/CommonStructures.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/Complex.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/Coroutine.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/CythonFunction.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/Dataclasses.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/Embed.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/Exceptions.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/ExtensionTypes.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/FunctionArguments.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/ImportExport.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/MemoryView_C.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/ModuleSetupCode.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/NumpyImportArray.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/ObjectHandling.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/Optimize.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/Overflow.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/Printing.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/Profile.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/StringTools.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/TestUtilityLoader.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/TypeConversion.c \
             ${PKGD}${TARGET_DBGSRC_DIR}/Cython/Utility/UFuncs_C.c \
             ${PKGD}${libdir}/${PYTHON_DIR}/site-packages/Cython*/SOURCES.txt; do
        if [ -e $f ]; then
            sed -i -e 's#${WORKDIR}/Cython-${PV}#${TARGET_DBGSRC_DIR}#g' $f
        fi
    done
}

