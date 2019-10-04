#!/bin/bash

# Load configuration
source config-vars.sh

export TOOLCHAIN=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/$HOST_TAG
PATH=$TOOLCHAIN/bin:$PATH

echo -e "${YELLOW}Will build cURL ${CURL_VERSION} for targets: ${TARGET_HOSTS[@]}${NC}"

cd ${CURL_SRC_DIR}
./buildconf

for CURRENT_TARGET in "${TARGET_HOSTS[@]}"; do

    echo -e "${GREEN}Configuring cURL for $CURRENT_TARGET...${NC}"
    case $CURRENT_TARGET in
        armeabi-v7a)
            TARGET_HOST=armv7a-linux-androideabi
            export AR=$TOOLCHAIN/bin/arm-linux-androideabi-ar
            export AS=$TOOLCHAIN/bin/arm-linux-androideabi-as
            export CC=$TOOLCHAIN/bin/$TARGET_HOST$MIN_SDK_VERSION-clang
            export CXX=$TOOLCHAIN/bin/$TARGET_HOST$MIN_SDK_VERSION-clang++
            export LD=$TOOLCHAIN/bin/arm-linux-androideabi-ld
            export RANLIB=$TOOLCHAIN/bin/arm-linux-androideabi-ranlib
            export NM=$TOOLCHAIN/bin/arm-linux-androideabi-nm
            export STRIP=$TOOLCHAIN/bin/arm-linux-androideabi-strip
        ;;
        arm64-v8a)
            TARGET_HOST=aarch64-linux-android
            export AR=$TOOLCHAIN/bin/$TARGET_HOST-ar
            export AS=$TOOLCHAIN/bin/$TARGET_HOST-as
            export CC=$TOOLCHAIN/bin/$TARGET_HOST$MIN_SDK_VERSION-clang
            export CXX=$TOOLCHAIN/bin/$TARGET_HOST$MIN_SDK_VERSION-clang++
            export LD=$TOOLCHAIN/bin/$TARGET_HOST-ld
            export RANLIB=$TOOLCHAIN/bin/$TARGET_HOST-ranlib
            export NM=$TOOLCHAIN/bin/$TARGET_HOST-nm
            export STRIP=$TOOLCHAIN/bin/$TARGET_HOST-strip
        ;;
        x86)
            TARGET_HOST=i686-linux-android
            export AR=$TOOLCHAIN/bin/$TARGET_HOST-ar
            export AS=$TOOLCHAIN/bin/$TARGET_HOST-as
            export CC=$TOOLCHAIN/bin/$TARGET_HOST$MIN_SDK_VERSION-clang
            export CXX=$TOOLCHAIN/bin/$TARGET_HOST$MIN_SDK_VERSION-clang++
            export LD=$TOOLCHAIN/bin/$TARGET_HOST-ld
            export RANLIB=$TOOLCHAIN/bin/$TARGET_HOST-ranlib
            export NM=$TOOLCHAIN/bin/$TARGET_HOST-nm
            export STRIP=$TOOLCHAIN/bin/$TARGET_HOST-strip
        ;;
        x86_64)
            TARGET_HOST=x86_64-linux-android
            export AR=$TOOLCHAIN/bin/$TARGET_HOST-ar
            export AS=$TOOLCHAIN/bin/$TARGET_HOST-as
            export CC=$TOOLCHAIN/bin/$TARGET_HOST$MIN_SDK_VERSION-clang
            export CXX=$TOOLCHAIN/bin/$TARGET_HOST$MIN_SDK_VERSION-clang++
            export LD=$TOOLCHAIN/bin/$TARGET_HOST-ld
            export RANLIB=$TOOLCHAIN/bin/$TARGET_HOST-ranlib
            export NM=$TOOLCHAIN/bin/$TARGET_HOST-nm
            export STRIP=$TOOLCHAIN/bin/$TARGET_HOST-strip
        ;;
    esac

    echo -e "${GREEN}-> Target host set as $TARGET_HOST${NC}"
    echo -e "${YELLOW}Will link with OpenSSL from '$OPENSSL_BUILD_DIR/$CURRENT_TARGET${NC}''"

    export CFLAGS
    export CPPFLAGS="${CFLAGS} -I${OPENSSL_BUILD_DIR}/${CURRENT_TARGET}/include/openssl"
    export LDFLAGS="${LDFLAGS} -L${OPENSSL_BUILD_DIR}/${CURRENT_TARGET}/lib"

    # Clear target directories in project for current arch
    echo -e "Cleaning build directory: ${CURL_BUILD_DIR}/${CURRENT_TARGET}"
    rm -rf ${CURL_BUILD_DIR}/${CURRENT_TARGET}
    mkdir -p ${CURL_BUILD_DIR}/${CURRENT_TARGET}

    ./configure --host=${TARGET_HOST} \
                --target=${TARGET_HOST} \
                --prefix=${CURL_BUILD_DIR}/${CURRENT_TARGET} \
                --with-ssl=${OPENSSL_BUILD_DIR}/${CURRENT_TARGET} \
                --disable-shared \
                --disable-verbose \
                --disable-manual \
                --disable-crypto-auth \
                --disable-unix-sockets \
                --disable-ares \
                --disable-rtsp \
                --disable-proxy \
                --disable-versioned-symbols \
                --enable-hidden-symbols \
                --without-libidn \
                --without-librtmp \
                --without-zlib \
                --disable-dict \
                --disable-file \
                --disable-ftp \
                --disable-ftps \
                --disable-gopher \
                --disable-imap \
                --disable-imaps \
                --disable-ldap \
                --disable-pop3 \
                --disable-pop3s \
                --disable-smb \
                --disable-smbs \
                --disable-smtp \
                --disable-smtps \
                --disable-telnet \
                --disable-tftp

    echo -e "${YELLOW}Building cURL for ${CURRENT_TARGET} build...${NC}"
    make -j$NJOBS
    make install
    make clean
    echo -e "${LIGHT_GREEN}Completed cURL build for ${CURRENT_TARGET}${NC}"

done;

echo -e "${LIGHT_GREEN}Completed cURL ${CURL_VERSION} builds for targets: ${TARGET_HOSTS[@]}${NC}"

cd ..