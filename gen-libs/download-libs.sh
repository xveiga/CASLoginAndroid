#!/bin/bash

CURL_VERSION="7.46.0"
OPENSSL_VERSION="1.0.2g"

BASE_DIR="$PWD"

OPENSSL_SRC_DIR="$BASE_DIR/src"
CURL_SRC_DIR="$BASE_DIR/src"

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

create_tar_dir() {
    if [ ! -d {$BASE_DIR}/tar ]; then
        mkdir -p ${BASE_DIR}/tar
    fi
}

download_openssl() {
    if [ ! -f "${BASE_DIR}/tar/openssl-${OPENSSL_VERSION}.tar.gz" ]; then
        echo -e "${ORANGE}-> Downloading OpenSSL ${OPENSSL_VERSION} sources${NC}"
        curl -Lo "${BASE_DIR}/tar/openssl-${OPENSSL_VERSION}.tar.gz" "https://www.openssl.org/source/openssl-${OPENSSL_VERSION}.tar.gz"
    else
        echo -e "${GREEN}-> OpenSSL $OPENSSL_VERSION archive found${NC}"
    fi
}

download_curl() {
    if [ ! -f "${BASE_DIR}/tar/curl-${CURL_VERSION}.tar.gz" ]; then
        echo -e "${ORANGE}-> Downloading cURL ${CURL_VERSION} sources${NC}"
        curl -Lo "${BASE_DIR}/tar/curl-${CURL_VERSION}.tar.gz" "https://curl.haxx.se/download/curl-${CURL_VERSION}.tar.gz"
    else
        echo -e "${GREEN}-> cURL ${CURL_VERSION} archive found${NC}"
    fi
}

clean_sources() {
    if [ -d {$BASE_DIR}/src ]; then
        rm -rf ${BASE_DIR}/src
    fi
    mkdir -p ${BASE_DIR}/src
}

unpack_openssl() {
    echo -e "-> Unpacking OpenSSL source..."
    tar xzf "${BASE_DIR}/tar/openssl-${OPENSSL_VERSION}.tar.gz" -C "${OPENSSL_SRC_DIR}"
}

unpack_curl() {
    echo -e "-> Unpacking cURL source..."
    tar xzf "${BASE_DIR}/tar/curl-${CURL_VERSION}.tar.gz" -C "${CURL_SRC_DIR}"
}

# Main

echo -e "Checking if original sources exist..."

create_tar_dir
clean_sources

download_openssl
unpack_openssl

download_curl
unpack_curl

echo -e "${LIGHT_CYAN}Done. Run './build-openssl.sh' next.${NC}"
