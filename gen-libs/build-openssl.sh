#!/bin/bash

# For version 1.0.2g, view workaround below
OPENSSL_VERSION="1.0.2g"

# Clang options: -Oz (optimize size smallest possible), -fno-integrated-as (less strict inline asm rules)
#OPENSSL_CONFIGURATION="enable-weak-ssl-ciphers enable-ssl2 enable-ssl3 enable-ssl3-method no-shared no-tests no-deprecated zlib -Oz -fno-integrated-as"
OPENSSL_CONFIGURATION="enable-weak-ssl-ciphers enable-ssl2 enable-ssl3 enable-ssl3-method -fPIC -Oz -fno-integrated-as"

#TARGET_HOSTS=("armeabi-v7a" "arm64-v8a" "x86" "x86_64")
TARGET_HOSTS=("armeabi-v7a" "x86")

MIN_SDK_VERSION=21

HOST_TAG=linux-x86_64
export ANDROID_NDK_HOME=$HOME/Android/Sdk/ndk/20.0.5594570

BASE_DIR="$PWD"
OPENSSL_SRC_DIR="$BASE_DIR/src/openssl-${OPENSSL_VERSION}"
OPENSSL_BUILD_DIR="$BASE_DIR/../distribution/openssl"

# Workaround for version 1.0.2g and newer NDK's that only use clang:
# Remove -mandroid flags in Configure script. Not supported by clang.
sed -i 's/-mandroid//g' ${OPENSSL_SRC_DIR}/Configure

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

cd $OPENSSL_SRC_DIR

echo -e "${YELLOW}Will build OpenSSL for targets: ${TARGET_HOSTS[@]}${NC}"

for CURRENT_TARGET in "${TARGET_HOSTS[@]}"; do

    echo -e "${GREEN}Configuring OpenSSL for $CURRENT_TARGET...${NC}"
    case $CURRENT_TARGET in
        armeabi-v7a)
            mkdir -p $OPENSSL_BUILD_DIR/$CURRENT_TARGET

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

            # Configured as generic android due to asm clang errors
            ./Configure android $OPENSSL_CONFIGURATION \
            -DANDROID -D__ANDROID_API__=$MIN_SDK_VERSION \
            -DANDROID_ABI=armeabi-v7a \
            --prefix=$OPENSSL_BUILD_DIR/$CURRENT_TARGET
        ;;
        x86)
            mkdir -p $OPENSSL_BUILD_DIR/$CURRENT_TARGET

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

            ./Configure android-x86 $OPENSSL_CONFIGURATION \
            -DANDROID -D__ANDROID_API__=$MIN_SDK_VERSION \
            -DANDROID_ABI=$CURRENT_TARGET \
            --prefix=$OPENSSL_BUILD_DIR/$CURRENT_TARGET
        ;;
    esac

    echo -e "${YELLOW}Building OpenSSL for $CURRENT_TARGET build...${NC}"
    make clean
    make depend # Rebuild dependencies to prevent incompatible target problems
    make -j$NJOBS
    make install_sw
    echo -e "${LIGHT_GREEN}Completed build for ${CURRENT_TARGET}${NC}"

done;

echo -e "${LIGHT_GREEN}Completed builds for targets: ${TARGET_HOSTS[@]}${NC}"

cd ..