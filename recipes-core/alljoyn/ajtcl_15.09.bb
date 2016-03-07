SUMMARY = "Alljoyn thin framework and SDK by the Allseen Alliance."
DESCRIPTION = "Alljoyn is an Open Source framework that makes it easy for devices and apps to discover and securely communicate with each other."
HOMEPAGE = "https://www.allseenalliance.org/"
DEPENDS = "openssl libxml2"
SECTION = "libs"
LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/ISC;md5=f3b90e78ea0cffb20bf5cca7947a896d"

S = "${WORKDIR}/alljoyn"
SRC_URI = "git://git.allseenalliance.org/gerrit/core/${PN}.git;protocol=https;branch=RB${PV};destsuffix=${S}/core/${PN} \
          "
SRC_URI += "${@bb.utils.contains("IMAGE_INSTALL", "${PN}-services", 'git://git.allseenalliance.org/gerrit/services/base_tcl.git;protocol=https;branch=RB${PV};destsuffix=${S}/services/base_tcl', "", d)}"
SRCREV = "${AUTOREV}"

AJTCL_BINDIR = "/opt/ajtcl/bin"
AJTCL_TSTDIR = "/opt/ajtcl/test"

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
             ${PN}-core-test-dbg \
             ${PN}-core-test \
           "

do_compile() {
# For _class-target and _class-nativesdk
    export CROSS_PREFIX="${TARGET_PREFIX}"
    export CROSS_PATH="${STAGING_DIR_NATIVE}${prefix_native}/bin/${TUNE_PKGARCH}${HOST_VENDOR}-${HOST_OS}"
    export CROSS_CFLAGS="${CFLAGS}"
    export CROSS_LINKFLAGS="${LDFLAGS}"
    export 
    cd ${S}/core/${PN}
    scons TARG=linux WS=off GTEST_DIR=${STAGING_DIR_HOST}/${prefix}
    if ${@bb.utils.contains("IMAGE_INSTALL", "${PN}-services", "true", "false", d)}; then
        cd ${S}/services/base_tcl
        scons TARG=linux WS=off AJ_TCL_ROOT=../../core/ajtcl
    fi
    unset CROSS_PREFIX
    unset CROSS_PATH
    unset CROSS_CFLAGS
    unset CROSS_LINKFLAGS
}

do_compile_class-native() {
    export 
    cd ${S}/core/${PN}
    scons TARG=linux WS=off GTEST_DIR=${STAGING_DIR_NATIVE}/${prefix}/src/gtest
    if ${@bb.utils.contains("IMAGE_INSTALL", "${PN}-services", "true", "false", d)}; then
        cd ${S}/services/base_tcl
        scons WS=off AJ_TCL_ROOT=../../core/ajtcl
    fi
}

do_install() {
# Install ajtcl core
    install -d ${D}/${libdir} ${D}/${includedir}/${PN}
    install ${S}/core/${PN}/dist/lib/* ${D}/${libdir}
    cp -r ${S}/core/${PN}/dist/include/${PN}/* ${D}/${includedir}/${PN}
# Install ajtcl samples
    if ${@bb.utils.contains("IMAGE_INSTALL", "${PN}-samples", "true", "false", d)}; then
        install -d ${D}/${AJTCL_BINDIR}
        cp -r ${S}/core/${PN}/dist/bin/* ${D}/${AJTCL_BINDIR}
    fi
# Install ajtcl tests
    if ${@bb.utils.contains("IMAGE_INSTALL", "${PN}-core-test", "true", "false", d)}; then
        install -d ${D}/${AJTCL_TSTDIR}
        cp -r ${S}/core/${PN}/dist/test/* ${D}/${AJTCL_TSTDIR}
    fi
# Install base_tcl
    if ${@bb.utils.contains("IMAGE_INSTALL", "${PN}-services", "true", "false", d)}; then
        install -d ${D}/${libdir} ${D}/${includedir}/${PN}
        install ${S}/services/base_tcl/dist/lib/* ${D}/${libdir}
        cp -r ${S}/services/base_tcl/dist/include/${PN}/* ${D}/${includedir}/${PN}
    fi
# Install base_tcl samples
    if ${@bb.utils.contains("IMAGE_INSTALL", "${PN}-services-samples", "true", "false", d)}; then
        install -d ${D}/${AJTCL_BINDIR}
        install ${S}/services/base_tcl/dist/bin/* ${D}/${AJTCL_BINDIR}
    fi
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

FILES_${PN}-core-test = " \
                          ${AJTCL_TSTDIR}/* \
                        "
FILES_${PN}-core-test-dbg = " \
                              ${AJTCL_TSTDIR}/.debug/* \
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
RDEPENDS_${PN}-core-test += "${PN}"
DEPENDS += "${@bb.utils.contains("IMAGE_INSTALL", "${PN}-core-test", "gtest", "", d)}"

BBCLASSEXTEND = "native nativesdk"
