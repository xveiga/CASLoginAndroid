#!/bin/bash

# Load configuration
source config-vars.sh

export CFLAGS
export LDFLAGS

export TOOLCHAIN=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/$HOST_TAG
PATH=$TOOLCHAIN/bin:$PATH

cd $OPENSSL_SRC_DIR

echo -e "${YELLOW}Will build OpenSSL ${OPENSSL_VERSION} for targets: ${TARGET_HOSTS[@]}${NC}"

# Workaround for version 1.0.2 and newer NDK's that only support clang:
# Remove -mandroid gcc flags in Configure script.
echo -e "${GREEN}->Apply workaround for new NDK's: Remove -mandroid flag to compile with clang.${NC}"
sed -i 's/-mandroid//g' ${OPENSSL_SRC_DIR}/Configure

for CURRENT_TARGET in "${TARGET_HOSTS[@]}"; do

    echo -e "${GREEN}Configuring OpenSSL for $CURRENT_TARGET...${NC}"
    case $CURRENT_TARGET in
        armeabi-v7a)
            TARGET_HOST=armv7a-linux-androideabi
            TARGET_PRESET=android
            #TARGET_PRESET=android-armv7
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
            TARGET_PRESET=android
            #TARGET_PRESET=android64-aarch64
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
            TARGET_PRESET=android
            #TARGET_PRESET=android-x86
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
            TARGET_PRESET=android
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
    echo -e "${GREEN}-> OpenSSL target set as $TARGET_PRESET${NC}"

    ./Configure $TARGET_PRESET $OPENSSL_CONFIGURATION \
        -DANDROID -D__ANDROID_API__=$MIN_SDK_VERSION \
        -DANDROID_ABI=$CURRENT_TARGET \
        --prefix=$OPENSSL_BUILD_DIR/$CURRENT_TARGET

    # Clear target directories in project for current arch
    echo -e "Cleaning build directory: ${OPENSSL_BUILD_DIR}/${CURRENT_TARGET}"
    rm -rf $OPENSSL_BUILD_DIR/$CURRENT_TARGET
    mkdir -p $OPENSSL_BUILD_DIR/$CURRENT_TARGET

    echo -e "${YELLOW}Building OpenSSL for $CURRENT_TARGET build...${NC}"
    make clean
    make depend -j${NJOBS} # Rebuild dependencies to prevent incompatible target problems
    make -j${NJOBS}
    make install_sw
    echo -e "${LIGHT_GREEN}Completed OpenSSL build for ${CURRENT_TARGET}${NC}"

done;

echo -e "${LIGHT_GREEN}Completed OpenSSL ${OPENSSL_VERSION} builds for targets: ${TARGET_HOSTS[@]}${NC}"

cd ..