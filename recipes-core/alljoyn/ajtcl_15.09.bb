SUMMARY = "Alljoyn thin framework and SDK by the Allseen Alliance."
DESCRIPTION = "Alljoyn is an Open Source framework that makes it easy for devices and apps to discover and securely communicate with each other."
HOMEPAGE = "https://www.allseenalliance.org/"
DEPENDS = "openssl libxml2"
SECTION = "libs"
LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/ISC;md5=f3b90e78ea0cffb20bf5cca7947a896d"

S = "${WORKDIR}/alljoyn"
SRC_URI = "git://git.allseenalliance.org/gerrit/core/${PN}.git;protocol=https;branch=RB${PV};destsuffix=${S}/core/${PN} \
           git://git.allseenalliance.org/gerrit/services/base_tcl.git;protocol=https;branch=RB${PV};destsuffix=${S}/services/base_tcl"
SRCREV = "${AUTOREV}"

AJTCL_BINDIR ?= "/opt/ajtcl/bin"

inherit scons

PACKAGES = " \
             ${PN}-services-samples-dbg \
             ${PN}-services-samples \
             ${PN}-services-dbg \
             ${PN}-services \
             ${PN}-services-staticdev \
             ${PN}-services-dev \
             ${PN}-samples-dbg \
             ${PN}-samples \
             ${PN}-dbg \
             ${PN} \
             ${PN}-staticdev \
             ${PN}-dev \
           "

do_compile() {
# For _class-target and _class-nativesdk
    export CROSS_PREFIX="${TARGET_PREFIX}"
    if [ -f ${STAGING_BINDIR_NATIVE}/${MULTIMACH_TARGET_SYS}/${TARGET_PREFIX}gcc ]; then
        export CROSS_PATH="${STAGING_BINDIR_NATIVE}/${MULTIMACH_TARGET_SYS}"
    elif [ -f ${STAGING_BINDIR_NATIVE}/${TARGET_SYS}/${TARGET_PREFIX}gcc ]; then
        export CROSS_PATH="${STAGING_BINDIR_NATIVE}/${TARGET_SYS}"
    else
        bberror "Can't find path to compiler!"
    fi
    export CROSS_CFLAGS="${TARGET_CC_ARCH} ${TOOLCHAIN_OPTIONS} ${CFLAGS}"
    export CROSS_LINKFLAGS="${TARGET_LD_ARCH} ${TOOLCHAIN_OPTIONS} ${LDFLAGS}"
    cd ${S}/core/${PN}
# GTEST_DIR is required because if gtest framework is present, but GTEST_DIR is not, then it triggers a compilation error!
    scons TARG=linux WS=off GTEST_DIR=${STAGING_DIR_HOST}/${prefix}
    cd ${S}/services/base_tcl
    scons TARG=linux WS=off AJ_TCL_ROOT=../../core/ajtcl
    unset CROSS_PREFIX
    unset CROSS_PATH
    unset CROSS_CFLAGS
    unset CROSS_LINKFLAGS
}

do_compile_class-native() {
    cd ${S}/core/${PN}
    scons TARG=linux WS=off
    cd ${S}/services/base_tcl
    scons WS=off AJ_TCL_ROOT=../../core/ajtcl
}

do_install() {
# Install ajtcl core
    install -d ${D}/${libdir} ${D}/${includedir}/${PN}
    install ${S}/core/${PN}/dist/lib/* ${D}/${libdir}
    cp -r ${S}/core/${PN}/dist/include/${PN}/* ${D}/${includedir}/${PN}
# Install ajtcl samples
    install -d ${D}/${AJTCL_BINDIR}
    cp -r ${S}/core/${PN}/dist/bin/* ${D}/${AJTCL_BINDIR}
# Install base_tcl
    install -d ${D}/${libdir} ${D}/${includedir}/${PN}
    install ${S}/services/base_tcl/dist/lib/* ${D}/${libdir}
    cp -r ${S}/services/base_tcl/dist/include/${PN}/* ${D}/${includedir}/${PN}
# Install base_tcl samples
    install -d ${D}/${AJTCL_BINDIR}
    install ${S}/services/base_tcl/dist/bin/* ${D}/${AJTCL_BINDIR}
}

FILES_${PN}-services-samples = " \
                                 ${AJTCL_BINDIR}/NotifConfigSample \
                               "
FILES_${PN}-services-samples-dbg = " \
                                     ${prefix}/src/debug/${PN}/${PV}-${PR}/alljoyn/services/base_tcl/samples/* \
                                     ${AJTCL_BINDIR}/.debug/NotifConfigSample \
                                   "

FILES_${PN}-services = " \
                         ${libdir}/lib${PN}_services.so \
                       "
FILES_${PN}-services-dbg = " \
                             ${prefix}/src/debug/${PN}/${PV}-${PR}/alljoyn/services/base_tcl/src/* \
                             ${prefix}/src/debug/${PN}/${PV}-${PR}/alljoyn/services/base_tcl/dist/* \
                             ${libdir}/.debug/lib${PN}_services.so \
                           "
FILES_${PN}-services-staticdev = " \
                                   ${libdir}/lib${PN}_services.a \
                                 "
FILES_${PN}-services-dev = " \
                             ${includedir}/${PN}/services/* \
                           "

FILES_${PN}-samples-dbg = " \
                            ${prefix}/src/debug/${PN}/${PV}-${PR}/alljoyn/core/${PN}/samples/* \
                            ${AJTCL_BINDIR}/.debug/* \
                          "
FILES_${PN}-samples = " \
                        ${AJTCL_BINDIR}/* \
                      "
FILES_${PN} = " \
                ${libdir}/lib${PN}.so \
              "
FILES_${PN}-dbg = " \
                    ${prefix}/src/debug/${PN}/${PV}-${PR}/alljoyn/core/${PN}/src/* \
                    ${prefix}/src/debug/${PN}/${PV}-${PR}/alljoyn/core/${PN}/dist/include/${PN}/* \
                    ${libdir}/.debug/lib${PN}.so \
                  "
FILES_${PN}-staticdev = " \
                          ${libdir}/lib${PN}.a \
                        "
FILES_${PN}-dev = " \
                    ${includedir}/${PN}/* \
                  "

RDEPENDS_${PN}-samples += "${PN}"
RDEPENDS_${PN}-services += "${PN}"
RDEPENDS_${PN}-services-samples += "${PN} ${PN}-services"

BBCLASSEXTEND = "native nativesdk"
