# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# If set to SHARED, add this to the app gradle scripts to pack the required libraries on the apk.
#// Shared libraries
# sourceSets {
#    main {
#        // let gradle pack the shared library into apk
#        jniLibs.srcDirs = ['../distribution/curl', '../distribution/openssl']
#    }
# }
set(library_link_mode STATIC)

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.4.1)

# Set compiled library directory
set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution)

## LIBSSL

# Set it as static
add_library(lib_ssl ${library_link_mode} IMPORTED)
# Set location depending on target ABI
set_target_properties(lib_ssl PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/openssl/${ANDROID_ABI}/lib/libssl.a)

## LIBCRYPTO

# Set it as shared/static
add_library(lib_crypto ${library_link_mode} IMPORTED)
# Set location depending on target ABI
set_target_properties(lib_crypto PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/openssl/${ANDROID_ABI}/lib/libcrypto.a)

## LIBCURL

# Set it as shared/static
add_library(lib_curl ${library_link_mode} IMPORTED)
# Set location depending on target ABI
set_target_properties(lib_curl PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/curl/${ANDROID_ABI}/lib/libcurl.a)

# For JNI interface, "C to Java" bridge code:

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Set include directories
target_include_directories(native-lib PRIVATE
        ${distribution_DIR}/openssl/${ANDROID_ABI}/include
        ${distribution_DIR}/curl/${ANDROID_ABI}/include)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}

        # Add OpenSSL and cURL
        lib_curl # libcurl depends on libssl
        lib_ssl  # libssl depends on libcrypto
        lib_crypto)