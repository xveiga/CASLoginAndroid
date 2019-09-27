#include <jni.h>
#include <string>
#include <curl/curl.h>
#include <android/log.h>
#include <cinttypes>

#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, "caslogin-native-lib::", __VA_ARGS__))

using namespace std;

string response;

size_t writeCallback(char* buf, size_t size, size_t nmemb, void* up) {

    for (int c = 0; c<size*nmemb; c++) {
        response.push_back(buf[c]);
    }

    return size*nmemb;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_caslogin_data_LoginDataSource_curlTest(JNIEnv* env, jobject)
{
    CURL *curl;
    CURLcode res;
    char errbuf[CURL_ERROR_SIZE];

    curl_global_init(CURL_GLOBAL_DEFAULT);

    curl = curl_easy_init();
    if(curl) {
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);
        curl_easy_setopt(curl, CURLOPT_ERRORBUFFER, errbuf);
        curl_easy_setopt(curl, CURLOPT_URL, "https://moodle.udc.es/");
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, &writeCallback);

        /* Perform the request, res will get the return code */
        res = curl_easy_perform(curl);
        /* Check for errors */
        if(res != CURLE_OK) {
            if(strlen(errbuf))
                LOGE("curl_easy_perform() failed: %s\n", errbuf);
            else
                LOGE("curl_easy_perform() failed: %s\n", curl_easy_strerror(res));
        }

        /* always cleanup */
        curl_easy_cleanup(curl);
    }

    curl_global_cleanup();

    /*std::string hello = "Hello from JNI!";
    return env->NewStringUTF(hello.c_str());*/
    return env->NewStringUTF(response.c_str());
}

//extern "C" JNIEXPORT jstring JNICALL
//Java_com_example_caslogin_ui_login_LoginActivity_stringFromJNI(
//        JNIEnv *env,
//        jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}

/*extern "C" JNIEXPORT jstring JNICALL
Java_com_example_caslogin_ui_login_LoginActivity_stringFromJNI( JNIEnv* env, jobject thiz )
{
    #if defined(__arm__)
        #if defined(__ARM_ARCH_7A__)
        #if defined(__ARM_NEON__)
          #if defined(__ARM_PCS_VFP)
            #define ABI "armeabi-v7a/NEON (hard-float)"
          #else
            #define ABI "armeabi-v7a/NEON"
          #endif
        #else
          #if defined(__ARM_PCS_VFP)
            #define ABI "armeabi-v7a (hard-float)"
          #else
            #define ABI "armeabi-v7a"
          #endif
        #endif
      #else
       #define ABI "armeabi"
      #endif
    #elif defined(__i386__)
    #define ABI "x86"
    #elif defined(__x86_64__)
        #define ABI "x86_64"
    #elif defined(__mips64)  // mips64el-* toolchain defines __mips__ too
    #define ABI "mips64"
    #elif defined(__mips__)
    #define ABI "mips"
    #elif defined(__aarch64__)
    #define ABI "arm64-v8a"
    #else
    #define ABI "unknown"
    #endif

    std::string hello = "Hello from JNI !  Compiled with ABI " ABI ".";
    return env->NewStringUTF(hello.c_str());
}*/