SUMMARY = "Alljoyn framework and SDK by the Allseen Alliance."
DESCRIPTION = "Alljoyn is an Open Source framework that makes it easy for devices and apps to discover and securely communicate with each other."
HOMEPAGE = "https://www.allseenalliance.org/"
DEPENDS = "openssl libxml2 libcap"
SECTION = "libs"
LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/ISC;md5=f3b90e78ea0cffb20bf5cca7947a896d"

S = "${WORKDIR}/${PN}"
SRC_URI = "git://git.allseenalliance.org/gerrit/core/${PN}.git;protocol=https;branch=RB${PV};destsuffix=${S}/core/${PN} \
          "
SRC_URI += "${@bb.utils.contains("IMAGE_INSTALL", "${PN}-services", 'git://git.allseenalliance.org/gerrit/services/base.git;protocol=https;branch=RB${PV};destsuffix=${S}/services/base', "", d)}"
SRCREV = "${AUTOREV}"

ALLJOYN_BINDINGS = "cpp"
ALLJOYN_SERVICES = "config,controlpanel,notification,onboarding,time,audio"
ALLJOYN_BINDIR = "/opt/${PN}/bin"
ALLJOYN_TSTDIR = "/opt/${PN}/test"

ALLJOYN_BUILD_OPTIONS += "${@bb.utils.contains("IMAGE_INSTALL", "${PN}-core-test", "GTEST_DIR=${STAGING_DIR_HOST}/${prefix}/src/gtest", "", d)}"
ALLJOYN_BUILD_OPTIONS += "${@bb.utils.contains("IMAGE_INSTALL", "${PN}-services", "SERVICES=${ALLJOYN_SERVICES}", "", d)}"

ALLJOYN_BUILD_OPTIONS_NATIVE += "${@bb.utils.contains("IMAGE_INSTALL", "${PN}-core-test", "GTEST_DIR=${STAGING_DIR_NATIVE}/${prefix}/src/gtest", "", d)}"
ALLJOYN_BUILD_OPTIONS_NATIVE += "${@bb.utils.contains("IMAGE_INSTALL", "${PN}-services", "SERVICES=${ALLJOYN_SERVICES}", "", d)}"

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
             ${PN}-bin-dbg \
             ${PN}-bin \
             ${PN}-dbg \
             ${PN} \
             ${PN}-staticdev \
             ${PN}-dev \
             ${PN}-docs \
             ${PN}-core-test-dbg \
             ${PN}-core-test \
           "

do_compile() {
# For _class-target and _class-nativesdk
    export TARGET_CC="${CC}"
    export TARGET_CXX="${CXX}"
    export TARGET_CFLAGS="${CFLAGS}"
    export TARGET_CPPFLAGS="${CPPFLAGS}"
    export TARGET_PATH="${PATH}:${STAGING_BINDIR_NATIVE}/${HOST_SYS}"
    export TARGET_LINKFLAGS="${LDFLAGS}"
    export TARGET_LINK="${CCLD}"
    export TARGET_AR="${AR}"
    export TARGET_RANLIB="${RANLIB}"
    export STAGING_DIR="${STAGING_DIR_TARGET}"
    cd ${S}/core/${PN}
    scons OS=openwrt CPU=openwrt DOCS=html CRYPTO=openssl BINDINGS=${ALLJOYN_BINDINGS} ${ALLJOYN_BUILD_OPTIONS} OE_BASE=/usr WS=off VARIANT=debug
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
    cd ${S}/core/${PN}
    scons DOCS=html CRYPTO=openssl BINDINGS=${ALLJOYN_BINDINGS} ${ALLJOYN_BUILD_OPTIONS_NATIVE} OE_BASE=/usr WS=off VARIANT=debug
}

install_alljoyn_core() {
    install -d ${D}/${libdir}
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/lib/lib${PN}.* ${D}/${libdir}
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/lib/libajrouter.* ${D}/${libdir}
}

install_alljoyn_core_dev() {
    install -d ${D}/${includedir}/${PN} ${D}/${includedir}/qcc ${D}/${includedir}/qcc/posix
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/inc/${PN}/*.h ${D}/${includedir}/${PN}
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/inc/qcc/*.h ${D}/${includedir}/qcc
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/inc/qcc/posix/*.h ${D}/${includedir}/qcc/posix
}

install_alljoyn_core_docs() {
    install -d ${D}/${docdir}/${PN}
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/docs/* ${D}/${docdir}/${PN}
}

install_alljoyn_core_bin() {
    install -d ${D}/${ALLJOYN_BINDIR}
    find ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/bin/ -maxdepth 1 -type f -exec install "{}" ${D}/${ALLJOYN_BINDIR} \;
}

install_alljoyn_core_samples() {
    install -d ${D}/${ALLJOYN_BINDIR}
    for i in ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/bin/samples/*
    do
        if [ "${i}" != "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/bin/samples/AboutClient" -a \
             "${i}" != "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/bin/samples/AboutClient_legacy" -a \
             "${i}" != "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/bin/samples/AboutService" -a \
             "${i}" != "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/bin/samples/AboutService_legacy" ]; then
            install ${i} ${D}/${ALLJOYN_BINDIR}
        fi
    done
}

install_alljoyn_core_test() {
    install -d ${D}/${ALLJOYN_TSTDIR}
    cp -r ${S}/core/${PN}/build/openwrt/openwrt/debug/test/cpp/bin/* ${D}/${ALLJOYN_TSTDIR}
}

install_alljoyn_services() {
# About service is always present
    install -d ${D}/${libdir} ${D}/${includedir}/${PN}/about
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/lib/lib${PN}_about.* ${D}/${libdir}
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/inc/${PN}/about/*.h ${D}/${includedir}/${PN}/about
# Install other services
    for i in `find ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/ -maxdepth 1 -type d`; do
        if [ "${i}" != "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp" -a \
             "${i}" != "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/java" -a \
             "${i}" != "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/c" -a \
             "${i}" != "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/js" ]; then
            if [ -d ${i}/inc ]; then
                install -d ${D}/${includedir}/${PN}/
                cp -r ${i}/inc/${PN}/* ${D}/${includedir}/${PN}/
            fi
            if [ -d ${i}/lib ]; then
                install -d ${D}/${libdir}
                install ${i}/lib/* ${D}/${libdir}
            fi
        fi
    done
}

install_alljoyn_services_samples() {
    install -d ${D}/${ALLJOYN_BINDIR}
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/bin/samples/AboutClient* ${D}/${ALLJOYN_BINDIR}
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/bin/samples/AboutService* ${D}/${ALLJOYN_BINDIR}
# Install other services
    for i in `find ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/ -maxdepth 1 -type d`; do
        if [ "${i}" != "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp" -a \
             "${i}" != "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/java" -a \
             "${i}" != "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/c" -a \
             "${i}" != "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/js" ]; then
            if [ -d ${i}/bin ]; then
                install -d ${D}/${ALLJOYN_BINDIR}
                install ${i}/bin/* ${D}/${ALLJOYN_BINDIR}
            fi
        fi
    done
}

do_install() {
    if ${@bb.utils.contains('IMAGE_INSTALL', '${PN}', 'true', 'false', d)}; then
        install_alljoyn_core
    fi

    if ${@bb.utils.contains('IMAGE_INSTALL', '${PN}-dev', 'true', 'false', d)}; then
        install_alljoyn_core_dev
    fi

    if ${@bb.utils.contains("IMAGE_INSTALL", "${PN}-docs", "true", "false", d)}; then
        install_alljoyn_core_docs
    fi

    if ${@bb.utils.contains("IMAGE_INSTALL", "${PN}-bin", "true", "false", d)}; then
        install_alljoyn_core_bin
    fi

    if ${@bb.utils.contains("IMAGE_INSTALL", "${PN}-samples", "true", "false", d)}; then
        install_alljoyn_core_samples
    fi

    if ${@bb.utils.contains("IMAGE_INSTALL", "${PN}-core-test", "true", "false", d)}; then
        install_alljoyn_core_test
    fi

    if ${@bb.utils.contains("IMAGE_INSTALL", "${PN}-services", "true", "false", d)}; then
        install_alljoyn_services
    fi

    if ${@bb.utils.contains("IMAGE_INSTALL", "${PN}-services-samples", "true", "false", d)}; then
        install_alljoyn_services_samples
    fi
}

FILES_${PN} = " \
                ${libdir}/lib${PN}.so \
              "
FILES_${PN}-dbg = " \
                    ${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}/core/${PN}/common \
                    ${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}/core/${PN}/build \
                    ${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}/core/${PN}/${PN}_core/src \
                    ${libdir}/.debug/lib${PN}.so \
                  "
FILES_${PN}-staticdev = " \
                          ${libdir}/lib${PN}.a \
                          ${libdir}/libajrouter.a \
                        "
FILES_${PN}-dev = " \
                    ${includedir}/${PN}/* \
                    ${includedir}/qcc/* \
                  "
FILES_${PN}-docs = " \
                     ${docdir}/* \
                   "
FILES_${PN}-bin = " \
                    ${ALLJOYN_BINDIR}/* \
                  "
FILES_${PN}-bin-dbg = " \
                        ${ALLJOYN_BINDIR}/.debug/* \
                      "
FILES_${PN}-samples = " \
                        ${ALLJOYN_BINDIR}/AboutListener \
                        ${ALLJOYN_BINDIR}/DeskTopSharedKSClient1 \
                        ${ALLJOYN_BINDIR}/DeskTopSharedKSClient2 \
                        ${ALLJOYN_BINDIR}/DeskTopSharedKSService \
                        ${ALLJOYN_BINDIR}/SampleCertificateUtility \
                        ${ALLJOYN_BINDIR}/SampleClientECDHE \
                        ${ALLJOYN_BINDIR}/sample_rule_app \
                        ${ALLJOYN_BINDIR}/SampleServiceECDHE \
                        ${ALLJOYN_BINDIR}/SecureDoorConsumer \
                        ${ALLJOYN_BINDIR}/SecureDoorProvider \
                      "
FILES_${PN}-samples-dbg = " \
                            ${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}/core/${PN}/${PN}_core/samples \
                            ${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}/core/${PN}/${PN}_core/router \
                            ${ALLJOYN_BINDIR}/.debug/AboutListener \
                            ${ALLJOYN_BINDIR}/.debug/DeskTopSharedKSClient1 \
                            ${ALLJOYN_BINDIR}/.debug/DeskTopSharedKSClient2 \
                            ${ALLJOYN_BINDIR}/.debug/DeskTopSharedKSService \
                            ${ALLJOYN_BINDIR}/.debug/SampleCertificateUtility \
                            ${ALLJOYN_BINDIR}/.debug/SampleClientECDHE \
                            ${ALLJOYN_BINDIR}/.debug/sample_rule_app \
                            ${ALLJOYN_BINDIR}/.debug/SampleServiceECDHE \
                            ${ALLJOYN_BINDIR}/.debug/SecureDoorConsumer \
                            ${ALLJOYN_BINDIR}/.debug/SecureDoorProvider \
                          "

FILES_${PN}-core-test = " \
                          ${ALLJOYN_TSTDIR}/* \
                        "
FILES_${PN}-core-test-dbg = " \
                              ${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}/core/${PN}/${PN}_core/test \
                              ${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}/core/${PN}/${PN}_core/unit_test \
                              ${ALLJOYN_TSTDIR}/.debug/* \
                            "

FILES_${PN}-services = " \
                         ${libdir}/lib${PN}_about.so \
                         ${libdir}/lib${PN}_config.so \
                         ${libdir}/lib${PN}_controlpanel.so \
                         ${libdir}/lib${PN}_notification.so \
                         ${libdir}/lib${PN}_onboarding.so \
                         ${libdir}/lib${PN}_services_common.so \
                       "
FILES_${PN}-services-dbg = " \
                             ${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}/core/${PN}/common \
                             ${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}/core/${PN}/services \
                             ${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}/services \
                             ${libdir}/.debug/lib${PN}_about.so \
                             ${libdir}/.debug/lib${PN}_config.so \
                             ${libdir}/.debug/lib${PN}_controlpanel.so \
                             ${libdir}/.debug/lib${PN}_notification.so \
                             ${libdir}/.debug/lib${PN}_onboarding.so \
                             ${libdir}/.debug/lib${PN}_services_common.so \
                           "
FILES_${PN}-services-staticdev = " \
                                   ${libdir}/lib${PN}_about.a \
                                   ${libdir}/lib${PN}_config.a \
                                   ${libdir}/lib${PN}_controlpanel.a \
                                   ${libdir}/lib${PN}_notification.a \
                                   ${libdir}/lib${PN}_onboarding.a \
                                   ${libdir}/lib${PN}_services_common.a \
                                 "
FILES_${PN}-services-dev = " \
                             ${includedir}/${PN}/about/* \
                             ${includedir}/${PN}/config/* \
                             ${includedir}/${PN}/controlpanel/* \
                             ${includedir}/${PN}/notification/* \
                             ${includedir}/${PN}/onboarding/* \
                             ${includedir}/${PN}/services_common/* \
                           "
FILES_${PN}-services-samples = " \
                                 ${ALLJOYN_BINDIR}/AboutClient \
                                 ${ALLJOYN_BINDIR}/AboutClient_legacy \
                                 ${ALLJOYN_BINDIR}/AboutService \
                                 ${ALLJOYN_BINDIR}/AboutService_legacy \
                                 ${ALLJOYN_BINDIR}/ConfigClient \
                                 ${ALLJOYN_BINDIR}/ConfigService \
                                 ${ALLJOYN_BINDIR}/ConfigService.conf \
                                 ${ALLJOYN_BINDIR}/FactoryConfigService.conf \
                                 ${ALLJOYN_BINDIR}/ControlPanelController \
                                 ${ALLJOYN_BINDIR}/ControlPanelProducer \
                                 ${ALLJOYN_BINDIR}/ControlPanelSample \
                                 ${ALLJOYN_BINDIR}/ConsumerService \
                                 ${ALLJOYN_BINDIR}/ProducerBasic \
                                 ${ALLJOYN_BINDIR}/ProducerService \
                                 ${ALLJOYN_BINDIR}/TestService \
                                 ${ALLJOYN_BINDIR}/OnboardingClient \
                                 ${ALLJOYN_BINDIR}/onboarding-daemon \
                                 ${ALLJOYN_BINDIR}/OnboardingService.conf \
                                 ${ALLJOYN_BINDIR}/FactoryOnboardingService.conf \
                                 ${ALLJOYN_BINDIR}/wifi_scan_results \
                                 ${ALLJOYN_BINDIR}/ACServerSample \
                                 ${ALLJOYN_BINDIR}/ACServerSample.conf \
                                 ${ALLJOYN_BINDIR}/FactoryACServerSample.conf \
                               "
FILES_${PN}-services-samples-dbg = " \
                                     ${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}/core/${PN}/common \
                                     ${prefix}/src/debug/${PN}/${PV}-${PR}/${PN}/core/${PN}/services \
                                     ${ALLJOYN_BINDIR}/.debug/AboutClient \
                                     ${ALLJOYN_BINDIR}/.debug/AboutClient_legacy \
                                     ${ALLJOYN_BINDIR}/.debug/AboutService \
                                     ${ALLJOYN_BINDIR}/.debug/AboutService_legacy \
                                     ${ALLJOYN_BINDIR}/.debug/ConfigClient \
                                     ${ALLJOYN_BINDIR}/.debug/ConfigService \
                                     ${ALLJOYN_BINDIR}/.debug/ControlPanelController \
                                     ${ALLJOYN_BINDIR}/.debug/ControlPanelProducer \
                                     ${ALLJOYN_BINDIR}/.debug/ControlPanelSample \
                                     ${ALLJOYN_BINDIR}/.debug/ConsumerService \
                                     ${ALLJOYN_BINDIR}/.debug/ProducerBasic \
                                     ${ALLJOYN_BINDIR}/.debug/ProducerService \
                                     ${ALLJOYN_BINDIR}/.debug/TestService \
                                     ${ALLJOYN_BINDIR}/.debug/OnboardingClient \
                                     ${ALLJOYN_BINDIR}/.debug/onboarding-daemon \
                                     ${ALLJOYN_BINDIR}/.debug/ACServerSample \
                                   "

RDEPENDS_${PN}-dev += "${PN}-staticdev"
RDEPENDS_${PN}-bin += "${PN}"
RDEPENDS_${PN}-samples += "${PN}"
RDEPENDS_${PN}-services += "${PN}"
RDEPENDS_${PN}-services-dev += "${PN}-services-staticdev"
RDEPENDS_${PN}-services-samples += "${PN} ${PN}-services"
RDEPENDS_${PN}-core-test += "${PN}"
DEPENDS += "${@bb.utils.contains("IMAGE_INSTALL", "${PN}-core-test", "gtest", "", d)}"

BBCLASSEXTEND = "native nativesdk"
