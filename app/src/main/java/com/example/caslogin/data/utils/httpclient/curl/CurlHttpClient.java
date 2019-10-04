package com.example.caslogin.data.utils.httpclient.curl;

import android.util.Log;

import com.example.caslogin.BuildConfig;
import com.example.caslogin.data.utils.exceptions.HttpClientException;
import com.example.caslogin.data.utils.exceptions.UnexpectedHTTPStatusCode;
import com.example.caslogin.data.utils.httpclient.HttpClient;
import com.example.caslogin.data.utils.httpclient.HttpConstants;
import com.example.caslogin.data.utils.httpclient.HttpUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

public class CurlHttpClient implements HttpClient {

	/* Load the native library acting as bridge between native curl library and java code.
	   BuildConfig.NATIVE_LIB_NAME is a constant defined in build.gradle for the app
	 */
	static {
		System.loadLibrary(BuildConfig.NATIVE_LIB_NAME);
	}

	public native void curlInit();
	public native void curlCleanup();
	public native String curlGet(String url);
	public native String curlPost(String url, String postfields);
	public native int curlHttpCode();

	private static final String LOG_TAG = "CurlHttpClient";

	public CurlHttpClient() {
		curlInit();
	}

	public String httpsGet(String url) throws IOException, UnexpectedHTTPStatusCode {
		String response = curlGet(url);
		// Debug code to view redirects instead of handling them automatically with curl
		/*Log.d(LOG_TAG, "GET " + url + ": " + response);
		if (curlHttpCode()/100 == 3) {
			Log.d(LOG_TAG, "Redirect: " + response);
			response = httpsGet(response);
		}*/
		return response;
	}

	public String httpsPostForm(String url, String postParams) throws IOException, HttpClientException, UnexpectedHTTPStatusCode {
		String response = curlPost(url, postParams);
		// Debug code to view redirects instead of handling them automatically with curl
		/*Log.d(LOG_TAG, "POST " + url + ": " + response);
		if (curlHttpCode()/100 == 3) {
			Log.d(LOG_TAG, "Redirect: " + response);
			response = httpsGet(response);
		}*/
		return response;
	}

	public int getLastStatusCode() {
		return curlHttpCode();
	}

	public void destroy() {
		curlCleanup();
	}
}
