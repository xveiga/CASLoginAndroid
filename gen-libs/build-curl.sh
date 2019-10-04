#!/bin/bash

CURL_VERSION="7.64.1"

#TARGET_HOSTS=("armeabi-v7a" "arm64-v8a" "x86" "x86_64")
TARGET_HOSTS=("armeabi-v7a" "x86")

MIN_SDK_VERSION=21

HOST_TAG=linux-x86_64
export ANDROID_NDK_HOME=$HOME/Android/android-ndk-r20

BASE_DIR=${PWD}
CURL_SRC_DIR="$BASE_DIR/src/curl-$CURL_VERSION"
CURL_BUILD_DIR="$BASE_DIR/../distribution/curl"
OPENSSL_BUILD_DIR="$BASE_DIR/../distribution/openssl"

export CFLAGS="-Oz -ffunction-sections -fdata-sections -fno-unwind-tables -fno-asynchronous-unwind-tables"
export LDFLAGS="-Wl,-s -Wl,-Bsymbolic -Wl,--gc-sections"

NJOBS=$(getconf _NPROCESSORS_ONLN)

BLACK='\033[0;30m'
DARK_GRAY='\033[1;30m'
RED='\033[0;31m'
LIGHT_RED='\033[1;31m'
GREEN='\033[0;32m'
LIGHT_GREEN='\033[1;32m'
ORANGE='\033[0;33m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
LIGHT_BLUE='\033[1;34m'
PURPLE='\033[0;35m'
LIGHT_PURPLE='\033[1;35m'
CYAN='\033[0;36m'
LIGHT_CYAN='\033[1;36m'
LIGHT_GRAY='\033[0;37m'
WHITE='\033[1;37m'
NC='\033[0m'

export TOOLCHAIN=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/$HOST_TAG
PATH=$TOOLCHAIN/bin:$PATH

echo -e "${YELLOW}Will build cURL for targets: ${TARGET_HOSTS[@]}${NC}"

cd ${CURL_SRC_DIR}
./buildconf

for CURRENT_TARGET in "${TARGET_HOSTS[@]}"; do

    echo -e "${GREEN}Configuring cURL for $CURRENT_TARGET...${NC}"
    case $CURRENT_TARGET in
        armeabi-v7a)
            mkdir -p $CURL_BUILD_DIR/$CURRENT_TARGET

            TARGET_HOST=armv7a-linux-androideabi
            echo -e "${GREEN}-> Setting target host as $TARGET_HOST${NC}"
            export AR=$TOOLCHAIN/bin/arm-linux-androideabi-ar
            export AS=$TOOLCHAIN/bin/arm-linux-androideabi-as
            export CC=$TOOLCHAIN/bin/$TARGET_HOST$MIN_SDK_VERSION-clang
            export CXX=$TOOLCHAIN/bin/$TARGET_HOST$MIN_SDK_VERSION-clang++
            export LD=$TOOLCHAIN/bin/arm-linux-androideabi-ld
            export RANLIB=$TOOLCHAIN/bin/arm-linux-androideabi-ranlib
            export NM=$TOOLCHAIN/bin/arm-linux-androideabi-nm
            export STRIP=$TOOLCHAIN/bin/arm-linux-androideabi-strip
        ;;
        x86)
            mkdir -p $CURL_BUILD_DIR/$CURRENT_TARGET

            TARGET_HOST=i686-linux-android
            echo -e "${GREEN}-> Setting target host as $TARGET_HOST${NC}"
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

    ./configure --host=$TARGET_HOST \
                --target=$TARGET_HOST \
                --prefix=$CURL_BUILD_DIR/$CURRENT_TARGET \
                --with-ssl=$OPENSSL_BUILD_DIR/$CURRENT_TARGET \
                --disable-shared \
                --disable-verbose \
                --disable-manual \
                --disable-crypto-auth \
                --disable-unix-sockets \
                --disable-ares \
                --disable-rtsp \
                --disable-ipv6 \
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
                --disable-pop3 \
                --disable-pop3s \
                --disable-smb \
                --disable-smbs \
                --disable-smtp \
                --disable-smtps \
                --disable-telnet \
                --disable-tftp

    echo -e "${YELLOW}Building cURL for $CURRENT_TARGET build...${NC}"
    make -j$NJOBS
    make install
    make clean
    echo -e "${LIGHT_GREEN}Completed build for ${CURRENT_TARGET}${NC}"

done;

echo -e "${LIGHT_GREEN}Completed builds for targets: ${TARGET_HOSTS[@]}${NC}"

cd ..