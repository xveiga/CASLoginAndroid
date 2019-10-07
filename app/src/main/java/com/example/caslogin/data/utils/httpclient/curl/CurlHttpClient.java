package com.example.caslogin.data.utils.httpclient.curl;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import com.example.caslogin.App;
import com.example.caslogin.BuildConfig;
import com.example.caslogin.R;
import com.example.caslogin.data.utils.exceptions.HttpClientException;
import com.example.caslogin.data.utils.exceptions.UnexpectedHTTPStatusCode;
import com.example.caslogin.data.utils.httpclient.HttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CurlHttpClient implements HttpClient {

	/* Load the native library acting as bridge between native curl library and java code.
	   BuildConfig.NATIVE_LIB_NAME is a constant defined in build.gradle for the app
	 */
	static {
		System.loadLibrary(BuildConfig.NATIVE_LIB_NAME);
	}

	public native void curlInit(byte[] certdata);
	public native void curlCleanup();
	public native String curlGet(String url);
	public native String curlPost(String url, String postfields);
	public native int curlHttpCode();

	private static final String LOG_TAG = "CurlHttpClient";

	public CurlHttpClient() {
		// Load CA certificate store from res/raw/cacert.pem
		/* Resources need to be loaded using the application context.
		   The individual resource ID (integer) needs to be be referenced
		   using the generated "R" class. */
		/* As a workaround for the inability to get the application context from an "inner class",
		   the App class that extends Application was created for this purpose. */

		byte[] caCertData = loadResourceAsByteArray(App.instance.getApplicationContext().getResources(), R.raw.cacert);

		curlInit(caCertData);
	}

	private byte[] loadResourceAsByteArray(Resources r, int resId) {
		InputStream is;
		try {
			is = r.openRawResource(resId);
			int count;
			byte[] data = new byte[4096];
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			while ((count = is.read(data, 0, data.length)) != -1)
				buf.write(data, 0, count);
			return buf.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null; // Probably build-time error, not fixable in runtime, ignore data.
	}

	public String httpsGet(String url) throws UnexpectedHTTPStatusCode, HttpClientException {
		String response = curlGet(url);
		int code;
		if ((code = curlHttpCode()) != 200)
			throw new UnexpectedHTTPStatusCode("Received code " + code);
		else if (code == 0)
			throw new HttpClientException("cURL exception"); //TODO: Get cURL message from c code.
		// Debug code to view redirects instead of handling them automatically with curl
		/*Log.d(LOG_TAG, "GET " + url + ": " + response);
		if (curlHttpCode()/100 == 3) {
			Log.d(LOG_TAG, "Redirect: " + response);
			response = httpsGet(response);
		}*/
		return response;
	}

	public String httpsPostForm(String url, String postParams) throws UnexpectedHTTPStatusCode, HttpClientException {
		String response = curlPost(url, postParams);
		int code;
		if ((code = curlHttpCode()) != 200)
			throw new UnexpectedHTTPStatusCode("Received code " + code);
		else if (code == 0)
			throw new HttpClientException("cURL exception"); //TODO: Get cURL message from c code.
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
