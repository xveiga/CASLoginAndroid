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
To specify the architectures the libraries should be compiled for, add the following:
On NDK version 20, `armeabi` was removed, only `armeabi-v7a`, `arm64-v8a`, `x86` and `x86_64` are supported.
```
    flavorDimensions 'cpuArch'
    productFlavors {
        arm7 {
            dimension 'cpuArch'
            ndk {
                abiFilter 'armeabi-v7a'
            }
        }
        arm8 {
            dimension 'cpuArch'
            ndk {
                abiFilters 'arm64-v8a'
            }
        }
        x86 {
            dimension 'cpuArch'
            ndk {
                abiFilter 'x86'
            }
        }
        x86_64 {
            dimension 'cpuArch'
            ndk {
                abiFilter 'x86_64'
            }
        }
        universal {
            dimension 'cpuArch'
            // include all default ABIs. with NDK-r16,  it is:
            //   armeabi-v7a, arm64-v8a, x86, x86_64
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




