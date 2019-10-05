# Project setup and configuration

### Download project
Do a `git clone <url>` on the directory of your choice or download the zip file.

### Import project into Android Studio

Go to `File > New > Import Project` and select the base directory of the project,
containing the root `build.gradle` file.

A gradle sync will try to run. If you do not have the Android NDK configured an
error will show up. You can automatically download and install the NDK using
the SDK manager (`Tools > SDK Manager`, if you do not find check on your toolbar
for a 3D style box with a blue downfacing arrow). Go to the tab `SDK Tools`,
check the box for `NDK (Side by side)` and click apply. Wait for the
installation to finish.

### Building Native Dependencies (OpenSSL and cURL)

There are automated scripts that will automatically build OpenSSL and cURL
for the project.

To install the build system dependencies on Ubuntu-based systems, run:
`sudo apt-get update && sudo apt-get install -y curl make`

For other systems, manually ensure `curl` and `make` are installed.

Then, you must configure the NDK location. Open `gen-libs/config-vars.sh`
and modify the `ANDROID_NDK_HOME` variable to point to the root folder of the
NDK. Usually `$HOME/Android/Sdk/ndk/<ndk-revision-number>`.

Finally, to build the dependencies, execute the `gen-libs/build-all.sh` script.

### Building project on Android Studio

Once the libraries are built successfully, the whole process should be
automatically handled from now on by Android Studio and Gradle. Clicking build
should compile the native C code that bridges Java with the cURL library and
include it as a shared library on the final apk.

The project is configured to build a separate apk for each supported
architecture (`x86`, `x86_64`, `armeabi-v7a`, `arm64-v8a`) and an universal one for all.
