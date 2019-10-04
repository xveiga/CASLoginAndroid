#!/bin/bash

# For version 1.0.2g, view workaround below
#OPENSSL_VERSION="1.0.2t"

# Clang options: -Oz (optimize size smallest possible), -fno-integrated-as (less strict inline asm rules)
#OPENSSL_CONFIGURATION="enable-weak-ssl-ciphers enable-ssl2 enable-ssl3 enable-ssl3-method no-shared no-tests no-deprecated zlib -Oz -fno-integrated-as"
#OPENSSL_CONFIGURATION="enable-weak-ssl-ciphers enable-ssl2 enable-ssl3 enable-ssl3-method -fPIC -Oz -fno-integrated-as"

#TARGET_HOSTS=("armeabi-v7a" "arm64-v8a" "x86" "x86_64")
#TARGET_HOSTS=("armeabi-v7a" "x86")

#MIN_SDK_VERSION=21

#HOST_TAG=linux-x86_64
#export ANDROID_NDK_HOME=$HOME/Android/Sdk/ndk/20.0.5594570

#BASE_DIR="$PWD"
#OPENSSL_SRC_DIR="$BASE_DIR/src/openssl-${OPENSSL_VERSION}"
#OPENSSL_BUILD_DIR="$BASE_DIR/../distribution/openssl"

source config-vars.sh

# Workaround for version 1.0.2 and newer NDK's that only support clang:
# Remove -mandroid gcc-only flags in Configure script.
sed -i 's/-mandroid//g' ${OPENSSL_SRC_DIR}/Configure

export CFLAGS="-Oz -ffunction-sections -fdata-sections -fno-unwind-tables -fno-asynchronous-unwind-tables"
export LDFLAGS="-Wl,-s -Wl,-Bsymbolic -Wl,--gc-sections"

NJOBS=$(getconf _NPROCESSORS_ONLN)

export TOOLCHAIN=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/$HOST_TAG
PATH=$TOOLCHAIN/bin:$PATH

cd $OPENSSL_SRC_DIR

echo -e "${YELLOW}Will build OpenSSL ${OPENSSL_VERSION} for targets: ${TARGET_HOSTS[@]}${NC}"

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
    echo -e "${LIGHT_GREEN}Completed OpenSSL build for ${CURRENT_TARGET}${NC}"

done;

echo -e "${LIGHT_GREEN}Completed OpenSSL ${OPENSSL_VERSION} builds for targets: ${TARGET_HOSTS[@]}${NC}"

cd ..