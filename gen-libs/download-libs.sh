#!/bin/bash

source config-vars.sh

create_tar_dir() {
    if [ ! -d {$BASE_DIR}/tar ]; then
        mkdir -p ${BASE_DIR}/tar
    fi
}

#TODO: Checksum of downladed files, redownload if corrupt.

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
    if [ -d {$BASE_SRC_DIR} ]; then
        echo -e "-> Removing old source files..."
        rm -rf ${BASE_SRC_DIR}
    fi
    mkdir -p ${BASE_SRC_DIR}
}

unpack_openssl() {
    echo -e "-> Unpacking OpenSSL source..."
    tar xzf "${BASE_DIR}/tar/openssl-${OPENSSL_VERSION}.tar.gz" -C "${BASE_SRC_DIR}"
}

unpack_curl() {
    echo -e "-> Unpacking cURL source..."
    tar xzf "${BASE_DIR}/tar/curl-${CURL_VERSION}.tar.gz" -C "${BASE_SRC_DIR}"
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
