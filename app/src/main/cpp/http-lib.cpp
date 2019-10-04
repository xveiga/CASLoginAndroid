#include <jni.h>
#include <string>
#include <curl/curl.h>
#include <android/log.h>
#include <cinttypes>

#define LIB_LOG_NAME "http-lib::"

#define LOGV(...) \
    ((void)__android_log_print(ANDROID_LOG_VERBOSE, LIB_LOG_NAME, __VA_ARGS__))

#define LOGD(...) \
    ((void)__android_log_print(ANDROID_LOG_DEBUG, LIB_LOG_NAME, __VA_ARGS__))

#define LOGI(...) \
    ((void)__android_log_print(ANDROID_LOG_INFO, LIB_LOG_NAME, __VA_ARGS__))

#define LOGW(...) \
    ((void)__android_log_print(ANDROID_LOG_WARNING, LIB_LOG_NAME, __VA_ARGS__))

#define LOGE(...) \
    ((void)__android_log_print(ANDROID_LOG_ERROR, LIB_LOG_NAME, __VA_ARGS__))

using namespace std;

string response;

CURL *curl;
CURLcode lastCode;
long lastHttpCode = 0;
char errbuf[CURL_ERROR_SIZE];

size_t writeCallback(char *buf, size_t size, size_t nmemb, void *up) {

    for (int c = 0; c < size * nmemb; c++) {
        response.push_back(buf[c]);
    }

    return size * nmemb;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_caslogin_data_utils_httpclient_curl_CurlHttpClient_curlInit(JNIEnv *env, jobject obj) {
    curl_global_init(CURL_GLOBAL_DEFAULT);
    curl = curl_easy_init();
    curl_easy_setopt(curl, CURLOPT_COOKIESESSION, 1L);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_caslogin_data_utils_httpclient_curl_CurlHttpClient_curlCleanup(JNIEnv *env, jobject obj) {
    curl_easy_cleanup(curl);
    curl_global_cleanup();
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_example_caslogin_data_utils_httpclient_curl_CurlHttpClient_curlHttpCode(JNIEnv *env, jobject obj) {
    return lastHttpCode;
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_example_caslogin_data_utils_httpclient_curl_CurlHttpClient_curlGet(JNIEnv *env, jobject obj, jstring jurl) {
    const char *url = env->GetStringUTFChars(jurl, NULL);
    if (curl) {
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L); //TODO: Import certificates
        curl_easy_setopt(curl, CURLOPT_ERRORBUFFER, errbuf);
        curl_easy_setopt(curl, CURLOPT_URL, url);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, &writeCallback);
        curl_easy_setopt(curl, CURLOPT_COOKIEFILE, "");

        /* Perform the request, res will get the return code */
        response.clear();
        lastCode = curl_easy_perform(curl);
        curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &lastHttpCode);
        LOGD("CURLINFO_RESPONSE_CODE: %d", lastHttpCode);
        /* Check for errors */
        if (lastCode != CURLE_OK) {
            if (strlen(errbuf))
                LOGE("curl_easy_perform() failed: %s\n", errbuf);
            else
                LOGE("curl_easy_perform() failed: %s\n", curl_easy_strerror(lastCode));
        } /*else {
            if (lastHttpCode/100 == 3) {
                char *redirecturl;
                curl_easy_getinfo(curl, CURLINFO_REDIRECT_URL, &redirecturl);
                LOGE("redirect: %s\n", redirecturl);
                return env->NewStringUTF(redirecturl);
            }
        }*/
    }
    return env->NewStringUTF(response.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_caslogin_data_utils_httpclient_curl_CurlHttpClient_curlPost(JNIEnv *env,
        jobject obj, jstring jurl, jstring jpostfields) {
    const char *url = env->GetStringUTFChars(jurl, NULL);
    const char *postfields = env->GetStringUTFChars(jpostfields, NULL);
    if (curl) {
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L); //TODO: Import certificates
        curl_easy_setopt(curl, CURLOPT_ERRORBUFFER, errbuf);
        curl_easy_setopt(curl, CURLOPT_URL, url);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, postfields);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, &writeCallback);
        curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
        LOGD("postfields: %s\n", postfields);

        /* Perform the request, res will get the return code */
        response.clear();
        lastCode = curl_easy_perform(curl);
        curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &lastHttpCode);
        LOGD("CURLINFO_RESPONSE_CODE: %d", lastHttpCode);
        /* Check for errors */
        if (lastCode != CURLE_OK) {
            if (strlen(errbuf))
                LOGE("curl_easy_perform() failed: %s\n", errbuf);
            else
                LOGE("curl_easy_perform() failed: %s\n", curl_easy_strerror(lastCode));
        } /*else {
            if ((lastHttpCode/100) == 3) {
                char *redirecturl;
                curl_easy_getinfo(curl, CURLINFO_REDIRECT_URL, &redirecturl);
                LOGE("redirect: %s\n", redirecturl);
                return env->NewStringUTF(redirecturl);
            }
        }*/
    }
    return env->NewStringUTF(response.c_str());
}