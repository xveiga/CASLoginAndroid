#!/bin/bash

# Notes for version 1.0.2g:
# Remove -mandroid flags in src/openssl/openssl-1.0.2g/Configure, lines 472 to 475. Not supported by NDK's clang.

OPENSSL_VERSION="1.0.2g"
OPENSSL_CONFIGURATION="no-shared enable-weak-ssl-ciphers enable-ssl2 enable-ssl3 no-comp no-hw no-engine no-shared no-tests no-deprecated zlib -Os"
#TARGET_HOSTS=("armeabi-v7a" "arm64-v8a" "x86" "x86_64")
TARGET_HOSTS=("x86" "armeabi-v7a")
MIN_SDK_VERSION=16
HOST_TAG=linux-x86_64
export ANDROID_NDK_HOME=$HOME/Android/android-ndk-r20

#export CFLAGS="-Os -ffunction-sections -fdata-sections -fno-unwind-tables -fno-asynchronous-unwind-tables"
#export LDFLAGS="-Wl,-s -Wl,-Bsymbolic -Wl,--gc-sections"

BASE_DIR="$PWD"
OPENSSL_SRC_DIR="$BASE_DIR/src/openssl/openssl-"
OPENSSL_BUILD_DIR="$BASE_DIR/../distribution/openssl"

NJOBS=$(getconf _NPROCESSORS_ONLN)

export TOOLCHAIN=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/$HOST_TAG
PATH=$TOOLCHAIN/bin:$PATH

cd $BASE_DIR/src/openssl/openssl-$OPENSSL_VERSION

for CURRENT_TARGET in "${TARGET_HOSTS[@]}"; do

    echo "-> Configuring OpenSSL for $CURRENT_TARGET..."
    case $CURRENT_TARGET in
        armeabi-v7a)
            mkdir -p $OPENSSL_BUILD_DIR/$CURRENT_TARGET

            TARGET_HOST=armv7a-linux-androideabi
            echo "Setting target host as $CURRENT_TARGET"
            export AR=$TOOLCHAIN/bin/arm-linux-androideabi-ar
            export AS=$TOOLCHAIN/bin/arm-linux-androideabi-as
            export CC=$TOOLCHAIN/bin/$TARGET_HOST$MIN_SDK_VERSION-clang
            export CXX=$TOOLCHAIN/bin/$TARGET_HOST$MIN_SDK_VERSION-clang++
            export LD=$TOOLCHAIN/bin/arm-linux-androideabi-ld
            export RANLIB=$TOOLCHAIN/bin/arm-linux-androideabi-ranlib
            export NM=$TOOLCHAIN/bin/arm-linux-androideabi-nm
            export STRIP=$TOOLCHAIN/bin/arm-linux-androideabi-strip

            ./Configure android-armv7 $OPENSSL_CONFIGURATION \
            -DANDROID -D__ANDROID_API__=$MIN_SDK_VERSION \
            --prefix=$OPENSSL_BUILD_DIR/$CURRENT_TARGET
        ;;
        x86)
            mkdir -p $OPENSSL_BUILD_DIR/$CURRENT_TARGET

            TARGET_HOST=i686-linux-android
            echo "Setting target host as $TARGET_HOST"
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
            --prefix=$OPENSSL_BUILD_DIR/$CURRENT_TARGET
        ;;
    esac

    echo "Building OpenSSL for $CURRENT_TARGET build..."
    make -j$NJOBS

done

cd ..