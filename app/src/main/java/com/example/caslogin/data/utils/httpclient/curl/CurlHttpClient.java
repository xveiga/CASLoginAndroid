package com.example.caslogin.data.utils.httpclient.curl;

import com.example.caslogin.data.utils.exceptions.HttpClientException;
import com.example.caslogin.data.utils.exceptions.URLEncodingException;
import com.example.caslogin.data.utils.exceptions.UnexpectedHTTPStatusCode;
import com.example.caslogin.data.utils.httpclient.HttpClient;
import com.example.caslogin.data.utils.httpclient.java.HttpConstants;
import com.example.caslogin.data.utils.httpclient.java.TLSSocketFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class CurlHttpClient implements HttpClient {

	private static final String LOG_TAG = "CurlHttpClient";

	private String USER_AGENT = "Mozilla/5.0";
	private String ACCEPT = "text/html";
	private String ACCEPT_LANGUAGE = "es-ES";
	private String REFERRER = null;
	private String HOST = null;
	private Charset encoding = HttpConstants.HTTP_ENCODING;

	private boolean gzipSupport = false; // Encoding support switch.
	private boolean referrerActive = false; // POST referrer header sending switch.
	private boolean hostActive = false; // POST host header sending switch.

	private int lastStatusCode = -1;

	public CurlHttpClient() {

	}

	public String httpsGet(String url) throws IOException, UnexpectedHTTPStatusCode {

		URL obj = new URL(url); // transform String to URL

		//lastStatusCode = conn.getResponseCode();

		if (lastStatusCode == 200) { // Code 200 means SUCCESS

		} else {
			throw new UnexpectedHTTPStatusCode("Server replied with code " + lastStatusCode); // Throw exception if server doesn't reply with OK
		}

		return null; // return HTTP response
	}

	public String httpsPostForm(String url, char[] postParams) throws IOException, HttpClientException, UnexpectedHTTPStatusCode {

		URL obj = new URL(url); // transform String to URL

		//lastStatusCode =  conn.getResponseCode(); // Execute request
		switch (lastStatusCode) {
			case 200: // OK

			break;
			case 302: // Redirect, return new location.
				String location = "";
				if (location.isEmpty())
					throw new HttpClientException("\"Location\" header was not found for HTTP code 302");
				return location;
			default:
				throw new UnexpectedHTTPStatusCode("Server replied with code " + lastStatusCode);
		}
		return null;
	}

	public int getLastStatusCode() {
		return lastStatusCode;
	}
}
