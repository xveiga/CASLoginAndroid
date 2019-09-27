## Actions taken to add cURL to the project:

### First step: Add basic c++ code support so that we can call a function on Java code and get a string back.

The Android NDK is compatible with two different build systems: CMake and ndk-build (using Android.mk files). CMake is more flexible, supported on multiple platforms and it's the preferred system, ndk-build is Android only and is generally used on legacy projects. We chose CMake.

Create the CMake build script `app/src/main/cpp/CMakeLists.txt`. It will generate a library called `native-lib` from our c++ code.
```
cmake_minimum_required(VERSION 3.4.1)

add_library(
        native-lib
        SHARED
        native-lib.cpp)

# LOG library to allow LOGV/LOGD/LOGI/LOGW/LOGE calls from c++ code. See hello-libs NDK example for more details
find_library(
        log-lib
        log)

# Links both libraries
target_link_libraries(
        native-lib
        ${log-lib})
```

Add to `app/build.gradle` the location of the CMake build script and the version required. 3.10.2 is included on the Android Studio repositories.

```
    externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
            version "3.10.2"
        }
    }
```
By default, Android Studio should compile the libraries for all architectures.
To optimize this behaviour, add the following code to generate a different APK for each architecture, plus an universal one that includes all.
On NDK version 20, `armeabi` was removed, only `armeabi-v7a`, `arm64-v8a`, `x86` and `x86_64` are supported.
```
    // Configures multiple APKs based on ABI.
    splits {
        abi {
            // Enables building multiple APKs per ABI.
            enable true

            // By default all ABIs are included, so use reset() and include to specify that we only
            // want APKs for x86, armeabi-v7a, and mips.
            reset()

            // Specifies a list of ABIs that Gradle should create APKs for.
            include "x86", "x86_64", "armeabi-v7a", "arm64-v8a"

            // Specifies that we want to also generate a universal APK that includes all ABIs.
            universalApk true
        }
    }
```

Add the c++ example code that returns a string `app/src/main/cpp/native-lib.cpp`.
```
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_caslogin_ui_login_LoginActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
```

Finally, to test it, we'll use the LoginActivity.java class to show the string returned from native code on a EditText. Add the following:
```
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
```
At the end of the onCreate, execute the native function:
```
usernameEditText.setText(stringFromJNI());
```

To check that the libraries were included on the APK, on Android Studio, go to `Build -> Analyze APK...`

### Second step: Add precompiled cURL and OpenSSL libraries to project

Following the "hello-libs" NDK example, the compiled libraries will be stored on the `distribution` folder inside the project's main folder.
As they were compiled using android-ndk-r20, the minimum supported API now is 21. This can be reverted back to 15 if an older revision is used.
Now we need to add the libraries to our CMake script `app/src/main/cpp/CMakeLists.txt`. The variable "library_link_mode" just sets the three
libraries to the same value (static or shared). "distribution_DIR" points to the main library directory. For each library, we define the *.a file location.
Then, the native C to Java "bridge library" is defined. Also, the include directories are defined (*.h files), and finally, the linking of the libraries.
The order on which they are defined is important due to their dependencies.

```
set(library_link_mode STATIC)

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

```

If the libraries are set as "SHARED" paste this on `app/build.gradle` (not working due to different folder structure).
Default is STATIC, integrated on native-lib for the app.
```
// Shared libraries
sourceSets {
    main {
        // let gradle pack the shared library into apk
        jniLibs.srcDirs = ['../distribution/curl/lib', '../distribution/openssl/lib']
    }
}
```

## Step 3: Example cURL test code
Now cURL is included on the project and it can be used. We'll modify the `app/src/main/cpp/native-lib.cpp` to test it.
Very important to note that the header of the cpp function is named like the Java class package that it links with.
Example: `extern "C" JNIEXPORT jstring JNICALLJava_com_example_caslogin_ui_login_LoginActivity_stringFromJNI()` links
with the function `public native String stringFromJNI()` from `com.example.caslogin.ui.login.LoginActivity`.

An HTTP GET example for cURL can be found [here](https://curl.haxx.se/libcurl/c/simple.html).

For an HTTPS connection, cURL may give an SSL certificate problem. This can be fixed by downloading
a certificate store and embedding it into the project. [CA certificate store extracted from Mozilla](https://curl.haxx.se/docs/caextract.html)
Another option is to switch off the certificate verification with `curl_easy_setopt(CURL *handle, CURLOPT_SSL_VERIFYPEER, 0L)`.




