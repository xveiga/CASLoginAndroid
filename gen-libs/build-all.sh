#!/bin/sh

echo "Removing stale source files..."
rm -rf src

echo "Removing old binaries..."
rm -rf ../distribution/*

echo "Resetting dependencies..."
./download-libs.sh

echo "Building OpenSSL..."
./build-openssl.sh

echo "Building cURL..."
./build-curl.sh