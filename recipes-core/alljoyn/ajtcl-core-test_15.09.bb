SUMMARY = "Alljoyn thin framework and SDK by the Allseen Alliance."
DESCRIPTION = "Alljoyn is an Open Source framework that makes it easy for devices and apps to discover and securely communicate with each other."
HOMEPAGE = "https://www.allseenalliance.org/"
DEPENDS = "openssl libxml2"
SECTION = "libs"
LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/ISC;md5=f3b90e78ea0cffb20bf5cca7947a896d"

S = "${WORKDIR}/alljoyn"
SRC_URI = "git://git.allseenalliance.org/gerrit/core/ajtcl.git;protocol=https;branch=RB${PV};destsuffix=${S}/core/ajtcl"
SRCREV = "${AUTOREV}"

AJTCL_TSTDIR ?= "/opt/ajtcl/test"

inherit scons

PACKAGES = " \
             ${PN}-dbg \
             ${PN} \
           "

do_compile() {
# For _class-target and _class-nativesdk
    export CROSS_PREFIX="${TARGET_PREFIX}"
    export CROSS_PATH="${STAGING_BINDIR_NATIVE}/${HOST_SYS}"
    CROSS_CFLAGS=`echo ${CC} | sed -e "s/${CROSS_PREFIX}gcc[ ]*//"`
    export CROSS_CFLAGS="${CROSS_CFLAGS} ${CFLAGS}"
    CROSS_LINKFLAGS=`echo ${LD} | sed -e "s/${CROSS_PREFIX}ld[ ]*//"`
    export CROSS_LINKFLAGS="${CROSS_LINKFLAGS} ${LDFLAGS}"
    cd ${S}/core/ajtcl
    scons TARG=linux WS=off GTEST_DIR=${STAGING_DIR_HOST}/${prefix}
    unset CROSS_PREFIX
    unset CROSS_PATH
    unset CROSS_CFLAGS
    unset CROSS_LINKFLAGS
}

do_compile_class-native() {
    cd ${S}/core/ajtcl
    scons TARG=linux WS=off GTEST_DIR=${STAGING_DIR_NATIVE}/${prefix}/src/gtest
}

do_install() {
# Install ajtcl tests
    install -d ${D}/${AJTCL_TSTDIR}
    cp -r ${S}/core/ajtcl/dist/test/* ${D}/${AJTCL_TSTDIR}
}

FILES_${PN} = " \
                ${AJTCL_TSTDIR}/* \
              "
FILES_${PN}-dbg = " \
                    ${AJTCL_TSTDIR}/.debug/* \
                  "

RDEPENDS_${PN} += "ajtcl"
DEPENDS += "gtest"

BBCLASSEXTEND = "native nativesdk"
