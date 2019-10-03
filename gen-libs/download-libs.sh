#!/bin/sh

CURL_VERSION="7.64.1"
OPENSSL_VERSION="1.0.2g"

BASE_DIR="$PWD"

OPENSSL_SRC_DIR="$BASE_DIR/src/openssl"
CURL_SRC_DIR="$BASE_DIR/src/curl"

create_directories() {
    mkdir -p $BASE_DIR/tar
    mkdir -p $BASE_DIR/src/openssl
    mkdir -p $BASE_DIR/src/curl
}

download_sources() {
    echo "Checking if original sources exist..."
    if [ ! -f "${BASE_DIR}/tar/openssl-$OPENSSL_VERSION.tar.gz" ]; then
        echo "Downloading OpenSSL $OPENSSL_VERSION sources"
        curl -Lo "${BASE_DIR}/tar/openssl-$OPENSSL_VERSION.tar.gz" "https://www.openssl.org/source/openssl-$OPENSSL_VERSION.tar.gz"
    else
        echo "-> OpenSSL $OPENSSL_VERSION found"
    fi

    if [ ! -f "${BASE_DIR}/tar/curl-$CURL_VERSION.tar.gz" ]; then
        echo "Downloading cURL $CURL_VERSION sources"
        curl -Lo "${BASE_DIR}/tar/curl-$CURL_VERSION.tar.gz" "https://curl.haxx.se/download/curl-$CURL_VERSION.tar.gz"
    else
        echo "-> cURL $CURL_VERSION found"
    fi
}

clean_sources() {
    if [ -d {$BASE_DIR}/src ]; then
        rm -rf $BASE_DIR/src
        mkdir -p $BASE_DIR/src/openssl
        mkdir -p $BASE_DIR/src/curl
    fi
}

unpack_sources() {
    echo "Unpacking OpenSSL source..."
    tar xzf "${BASE_DIR}/tar/openssl-$OPENSSL_VERSION.tar.gz" -C "$OPENSSL_SRC_DIR"
    echo "Unpacking cURL source..."
    tar xzf "${BASE_DIR}/tar/curl-$CURL_VERSION.tar.gz" -C "$CURL_SRC_DIR"
}

create_directories
download_sources
clean_sources
unpack_sources
