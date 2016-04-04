SUMMARY = "Alljoyn framework and SDK by the Allseen Alliance."
DESCRIPTION = "Alljoyn is an Open Source framework that makes it easy for devices and apps to discover and securely communicate with each other."
HOMEPAGE = "https://www.allseenalliance.org/"
DEPENDS = "openssl libxml2 libcap"
SECTION = "libs"
LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/ISC;md5=f3b90e78ea0cffb20bf5cca7947a896d"

S = "${WORKDIR}/git"
SRC_URI = "git://git.allseenalliance.org/gerrit/core/alljoyn.git;protocol=https;branch=master;destsuffix=git/core/alljoyn"
SRCREV = "efa50d3e2352002969dd96c3f5e0ec72f8249544"

PV = "master+git${SRCPV}"

ALLJOYN_BINDINGS ?= "cpp"
ALLJOYN_TSTDIR ?= "/opt/alljoyn/test"

ALLJOYN_BUILD_OPTIONS += "GTEST_DIR=${STAGING_DIR_HOST}/${prefix}/src/gtest"

ALLJOYN_BUILD_OPTIONS_NATIVE += "GTEST_DIR=${STAGING_DIR_NATIVE}/${prefix}/src/gtest"

inherit scons

PACKAGES = " \
             ${PN}-dbg \
             ${PN} \
           "

do_compile() {
# For _class-target and _class-nativesdk
    export TARGET_CC="${CC}"
    export TARGET_CXX="${CXX}"
    export TARGET_CFLAGS="${CFLAGS}"
    export TARGET_CPPFLAGS="${CPPFLAGS}"
#    export TARGET_PATH="${PATH}:${STAGING_BINDIR_NATIVE}/${HOST_SYS}"
# Path to toolchain already in the path!
    export TARGET_PATH="${PATH}"
    export TARGET_LINKFLAGS="${LDFLAGS}"
    export TARGET_LINK="${CCLD}"
    export TARGET_AR="${AR}"
    export TARGET_RANLIB="${RANLIB}"
    export STAGING_DIR="${STAGING_DIR_TARGET}"
    cd ${S}/core/alljoyn
    scons OS=openwrt CPU=openwrt CRYPTO=openssl BINDINGS=${ALLJOYN_BINDINGS} ${ALLJOYN_BUILD_OPTIONS} OE_BASE=/usr WS=off VARIANT=debug
    unset TARGET_CC
    unset TARGET_CXX
    unset TARGET_CFLAGS
    unset TARGET_CPPFLAGS
    unset TARGET_PATH
    unset TARGET_LINKFLAGS
    unset TARGET_LINK
    unset TARGET_AR
    unset TARGET_RANLIB
    unset STAGING_DIR
}

do_compile_class-native() {
    cd ${S}/core/alljoyn
    scons CRYPTO=openssl BINDINGS=${ALLJOYN_BINDINGS} ${ALLJOYN_BUILD_OPTIONS_NATIVE} OE_BASE=/usr WS=off VARIANT=debug
}

do_install() {
    install -d ${D}/${ALLJOYN_TSTDIR}
    cp -r ${S}/core/alljoyn/build/openwrt/openwrt/debug/test/cpp/bin/* ${D}/${ALLJOYN_TSTDIR}
}

FILES_${PN} = " \
                ${ALLJOYN_TSTDIR}/* \
              "
FILES_${PN}-dbg = " \
                    ${prefix}/src/debug/${PN}/${PV}-${PR}/alljoyn/core/alljoyn/common \
                    ${prefix}/src/debug/${PN}/${PV}-${PR}/alljoyn/core/alljoyn/build \
                    ${prefix}/src/debug/${PN}/${PV}-${PR}/alljoyn/core/alljoyn/alljoyn_core/src \
                    ${prefix}/src/debug/${PN}/${PV}-${PR}/alljoyn/core/alljoyn/alljoyn_core/router \
                    ${prefix}/src/debug/${PN}/${PV}-${PR}/alljoyn/core/alljoyn/alljoyn_core/unit_test \
                    ${prefix}/src/debug/${PN}/${PV}-${PR}/alljoyn/core/alljoyn/services/about \
                    ${ALLJOYN_TSTDIR}/.debug/* \
                  "

RDEPENDS_${PN} += "alljoyn alljoyn-services bash"
DEPENDS += "gtest"

BBCLASSEXTEND = "native nativesdk"