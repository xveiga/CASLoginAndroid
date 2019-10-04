#!/bin/sh

# Configuration variables for automatic cURL and OpenSSL build script.
# Load inside script by executing 'source configuration'

# Android NDK root location
ANDROID_NDK_HOME=$HOME/Android/Sdk/ndk/20.0.5594570

# Toolchain SDK version used. Use the lowest you want to support.
MIN_SDK_VERSION=21

# Host llvm toolchain used. MacOS: darwin-x86_64, Linux: linux-x86_64
HOST_TAG=linux-x86_64

# Target ABI's to compile the libraries for. SDK versions lower than 21 do not
# support targeting 64 bit architectures.
TARGET_HOSTS=("armeabi-v7a" "x86")

# Versions of the libraries used. Please double check that the cURL version
# supports the same OpenSSL interface version, otherwise linking will fail.
CURL_VERSION="7.46.0"
OPENSSL_VERSION="1.0.2t"

# Additional parameters for the OpenSSL Configure script
OPENSSL_CONFIGURATION="enable-weak-ssl-ciphers enable-ssl2 enable-ssl3 enable-ssl3-method -fPIC -Oz -fno-integrated-as"

# Additional compiler and linker flags
CFLAGS="-Oz -ffunction-sections -fdata-sections -fno-unwind-tables -fno-asynchronous-unwind-tables"
LDFLAGS="-Wl,-s -Wl,-Bsymbolic -Wl,--gc-sections"

# Directory structure
BASE_DIR="$( cd "$(dirname "$0")" ; pwd -P )" # Script location directory

# Where library sources will be stored
BASE_SRC_DIR="${BASE_DIR}/src"
OPENSSL_SRC_DIR="${BASE_DIR}/src/openssl-${OPENSSL_VERSION}"
CURL_SRC_DIR="${BASE_DIR}/src/curl-$CURL_VERSION"

# Built librariies location
CURL_BUILD_DIR="${BASE_DIR}/../distribution/curl"
OPENSSL_BUILD_DIR="${BASE_DIR}/../distribution/openssl"

# Maximum concurrent jobs while building.
# By default, same as the number of processors.
NJOBS=$(getconf _NPROCESSORS_ONLN)

# Colors
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