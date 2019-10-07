#include <jni.h>
#include <android/log.h>
#include <string>
#include <cinttypes>
#include <curl/curl.h>
#include <openssl/err.h>
#include <openssl/ssl.h>

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

static const char *pCACertFile = "res/raw/cacert.pem";

CURL *curl;
CURLcode lastCode;
string response;
long lastHttpCode = 0;
char errbuf[CURL_ERROR_SIZE];
char *ca_data;
size_t ca_data_sz;

/*
 * Implementation inspired on the following cURL examples:
 * https://curl.haxx.se/libcurl/c/https.html
 * https://curl.haxx.se/libcurl/c/http-post.html
 * https://curl.haxx.se/libcurl/c/cacertinmem.html
 */

// Allows importing a CA cert file using Java code
static CURLcode sslctx_function(CURL *curl, void *sslctx, void *parm)
{
    CURLcode rv = CURLE_ABORTED_BY_CALLBACK;

    BIO *cbio = BIO_new_mem_buf(ca_data, ca_data_sz);
    X509_STORE  *cts = SSL_CTX_get_cert_store((SSL_CTX *)sslctx);
    int i;
    STACK_OF(X509_INFO) *inf;
    (void)curl;
    (void)parm;

    if(!cts || !cbio) {
        return rv;
    }

    inf = PEM_X509_INFO_read_bio(cbio, NULL, NULL, NULL);

    if(!inf) {
        BIO_free(cbio);
        return rv;
    }

    for(i = 0; i < sk_X509_INFO_num(inf); i++) {
        X509_INFO *itmp = sk_X509_INFO_value(inf, i);
        if(itmp->x509) {
            X509_STORE_add_cert(cts, itmp->x509);
        }
        if(itmp->crl) {
            X509_STORE_add_crl(cts, itmp->crl);
        }
    }

    sk_X509_INFO_pop_free(inf, X509_INFO_free);
    BIO_free(cbio);

    rv = CURLE_OK;
    return rv;
}

size_t writeCallback(char *buf, size_t size, size_t nmemb, void *up) {

    for (int c = 0; c < size * nmemb; c++) {
        response.push_back(buf[c]);
    }

    return size * nmemb;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_caslogin_data_utils_httpclient_curl_CurlHttpClient_curlInit(JNIEnv *env, jobject obj, jbyteArray jca_data) {
    curl_global_init(CURL_GLOBAL_DEFAULT);
    curl = curl_easy_init();
    curl_easy_setopt(curl, CURLOPT_COOKIESESSION, 1L);
    curl_easy_setopt(curl, CURLOPT_CAINFO, NULL);
    curl_easy_setopt(curl, CURLOPT_CAPATH, NULL);
    curl_easy_setopt(curl, CURLOPT_SSLCERTTYPE, "PEM");
    curl_easy_setopt(curl, CURLOPT_SSL_CTX_FUNCTION, sslctx_function);
    curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 1L);
    curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 1L);
    curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
    curl_easy_setopt(curl, CURLOPT_ERRORBUFFER, errbuf);
    curl_easy_setopt(curl, CURLOPT_COOKIEFILE, "");
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, &writeCallback);

    // Copy certificate data
    if (jca_data != NULL) {
        jbyte *buf_ptr = env->GetByteArrayElements(jca_data, NULL);
        ca_data_sz = (size_t) env->GetArrayLength(jca_data);
        ca_data = (char *) (malloc(ca_data_sz));
        memcpy(ca_data, (char *) buf_ptr, ca_data_sz);

        /* Release memory. With JNI_ABORT the JVM will not try to copy
           the contents back (as we won't modify them).
           See https://developer.android.com/training/articles/perf-jni for details. */
        env->ReleaseByteArrayElements(jca_data, buf_ptr, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_caslogin_data_utils_httpclient_curl_CurlHttpClient_curlCleanup(JNIEnv *env, jobject obj) {
    curl_easy_cleanup(curl);
    curl_global_cleanup();
    if (ca_data != NULL)
        free(ca_data); // Release CA certs memory
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_example_caslogin_data_utils_httpclient_curl_CurlHttpClient_curlHttpCode(JNIEnv *env, jobject obj) {
    return lastHttpCode;
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_example_caslogin_data_utils_httpclient_curl_CurlHttpClient_curlGet(JNIEnv *env, jobject obj, jstring jurl) {
    const char *url = env->GetStringUTFChars(jurl, NULL);

    response.clear();

    if (curl) {
        curl_easy_setopt(curl, CURLOPT_URL, url);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, NULL);
        /* Perform the request, res will get the return code */
        lastCode = curl_easy_perform(curl);
        curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &lastHttpCode);
        LOGD("CURLINFO_RESPONSE_CODE: %ld", lastHttpCode);
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
    env->ReleaseStringUTFChars(jurl, url);
    return env->NewStringUTF(response.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_caslogin_data_utils_httpclient_curl_CurlHttpClient_curlPost(JNIEnv *env,
        jobject obj, jstring jurl, jstring jpostfields) {
    const char *url = env->GetStringUTFChars(jurl, NULL);
    const char *postfields = env->GetStringUTFChars(jpostfields, NULL);

    response.clear();

    if (curl) {
        curl_easy_setopt(curl, CURLOPT_URL, url);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, postfields);
        LOGD("postfields: %s\n", postfields);

        /* Perform the request, res will get the return code */
        lastCode = curl_easy_perform(curl);
        curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &lastHttpCode);
        LOGD("CURLINFO_RESPONSE_CODE: %ld", lastHttpCode);
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
    env->ReleaseStringUTFChars(jurl, url);
    env->ReleaseStringUTFChars(jpostfields, postfields);
    return env->NewStringUTF(response.c_str());
}