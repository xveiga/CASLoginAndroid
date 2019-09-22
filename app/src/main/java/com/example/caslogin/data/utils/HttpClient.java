package com.example.caslogin.data.utils;

import android.util.Log;

import com.example.caslogin.data.utils.exceptions.HttpClientException;
import com.example.caslogin.data.utils.exceptions.URLEncodingException;
import com.example.caslogin.data.utils.exceptions.UnexpectedHTTPStatusCode;

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

public class HttpClient {

	/*
	 * DISCLAIMER: This HTTP client implementation DOES NOT check which cookies belong
	 * to which domain. Therefore, when connecting to a server, it sends ALL saved cookies,
	 * for any domain. Please use different instances to handle different websites.
	 */

	private static final String LOG_TAG = "HttpClient";
	private static final String ACCEPT_ENCODING = "gzip"; // Fixed value for gzip support.

	private String USER_AGENT = "Mozilla/5.0";
	private String ACCEPT = "text/html";
	private String ACCEPT_LANGUAGE = "es-ES";
	private String REFERRER = null;
	private String HOST = null;
	private Charset encoding = HttpConstants.HTTP_ENCODING;

	private boolean gzipSupport = false; // Encoding support switch.
	private boolean referrerActive = false; // POST referrer header sending switch.
	private boolean hostActive = false; // POST host header sending switch.

	private HttpsURLConnection conn;
	private SSLSocketFactory sf;
	private List<String> cookies;
	private int lastStatusCode = -1;

	public HttpClient() {
		cookies = new ArrayList<>();
		sf = new TLSSocketFactory();
	}

	/*
	 * Sends a HTTPS GET request to an URL and gets the response back.
	 */

	public String httpsGet(String url) throws IOException, UnexpectedHTTPStatusCode {

		URL obj = new URL(url); // transform String to URL

		conn = (HttpsURLConnection) obj.openConnection();  // Prepare connection to that URL
		conn.setSSLSocketFactory(sf); // To be able to select ssl protocols

		// ## HEADERS ##
		conn.setRequestMethod("GET"); // Set request mode to GET
		conn.setUseCaches(false); // Best turned off to avoid problems (can make proxies cache this type of
									// requests and cause undesired behaviour)
		conn.setRequestProperty("User-Agent", USER_AGENT); // Set user-agent
		conn.setRequestProperty("Accept", ACCEPT); // Set what we are expecting to receive/support
		//conn.setRequestProperty("Connection", "keep-alive");
		//conn.setRequestProperty("Accept-Language", ACCEPT_LANGUAGE); // Set language list
		if (gzipSupport) { // If gzip support is enabled, send a header to indicate so to the server.
			conn.setRequestProperty("Accept-Encoding", ACCEPT_ENCODING);
		}
		if (cookies != null) { // If we have cookies stored, add them to the header.
			for (String cookie : cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]); // Cookie attributes are separated with a
																			// ";".
			} // Cookie header format: Set-Cookie: <cookie-name>=<cookie-value>;
				// attribute1=value; attribute2. We only need the first part ([0]);
		}

		// ## RESPONSE ##
		String responseString = null; // String containing response data.

		// When this is executed, the request is sent. Get response code back.
		//lastStatusCode = sslHandshakeFailureWorkaround(conn);
		lastStatusCode = conn.getResponseCode();

		if (lastStatusCode == 200) { // Code 200 means SUCCESS
			InputStream connInputStream; // Input stream for connection.
			
			if (gzipSupport && "gzip".equals(conn.getContentEncoding())) { // If the response is gzipped,
				connInputStream = new GZIPInputStream(conn.getInputStream()); // unzip InputStream.
			} else { // If it is a plain text response,
				connInputStream = conn.getInputStream(); // pass the stream to the BufferedReader.
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(connInputStream, StandardCharsets.UTF_8)); // Connection Buffer
			String inputLine; // Temporary variable
			StringBuffer response = new StringBuffer(); // String Buffer

			while ((inputLine = in.readLine()) != null) { // When there's data available,
				response.append(inputLine); // write data to the String Buffer
			}
			in.close(); // Destroy connection buffer
			responseString = response.toString(); // pass String Buffer contents to a String

			//cookies = conn.getHeaderFields().get("Set-Cookie");
			checkCookies(conn.getHeaderFields().get("Set-Cookie")); // Get the header
			// containing new cookies, don't store duplicates, and keep the old ones.
		} else {
			throw new UnexpectedHTTPStatusCode("Server replied with code " + lastStatusCode); // Throw exception if server doesn't reply with OK
		}
		
		//conn.disconnect();
		return responseString; // return HTTP response
	}

	/*
	 * Sends a HTTPS POST request to an URL and gets the response back.
	 */
	public String httpsPostForm(String url, char[] postParams) throws IOException, HttpClientException, UnexpectedHTTPStatusCode {

		URL obj = new URL(url); // transform String to URL

		// Retries connection in case of SSL Handshake failure. Read method below for details.
		//conn = sslHandshakeFailureWorkaround(obj);
		conn = (HttpsURLConnection) obj.openConnection(); // Prepare connection to that URL
		conn.setSSLSocketFactory(sf); // To be able to select ssl protocols
		// ## HEADERS ##

		conn.setRequestMethod("POST"); // Set request mode to POST
		conn.setUseCaches(false); // Best turned off to avoid problems (can make proxies cache this type of
									// requests and cause undesired behaviour)
		conn.setRequestProperty("User-Agent", USER_AGENT); // Set user-agent
		conn.setRequestProperty("Accept", ACCEPT); // Set what we are expecting to receive/support
		//conn.setRequestProperty("Accept-Language", ACCEPT_LANGUAGE); // Set language list
		if (hostActive) { // If host is enabled, send the corresponding header. May be required in some
							// proxy configurations.
			conn.setRequestProperty("Host", HOST);
		}
		//conn.setRequestProperty("Connection", "keep-alive");
		if (referrerActive) { // If referrer is enabled, send the corresponding header. May be required in
								// some pages.
			conn.setRequestProperty("Referer", REFERRER);
		}
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // Set content type to http form
																						// response.
		conn.setRequestProperty("Content-Length", Integer.toString(postParams.length)); // Length of post request
		if (cookies != null) { // If we have cookies stored, add them to the header.
			for (String cookie : cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]); // Cookie attributes are separated with a
																			// ";".
			} // Cookie header format: Set-Cookie: <cookie-name>=<cookie-value>;
				// attribute1=value; attribute2. We only need the first part ([0]);
		}
		conn.setDoOutput(true); // Enable outputStream for POST request (Allows to transmit data in body instead
								// of header only).
		conn.setDoInput(true); // Enable inputStream for POST request.

		// ## RESPONSE ##

		String responseString = null; // String containing response data.

		// TODO: Figure out gzip transport for POST requests.
		DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream()); // Get connection outputStream and buffer it.
		byte[] b = charsToBytes(postParams);
		outputStream.write(b); // Write POST request to buffer.
		outputStream.flush(); // Flush buffer.
		outputStream.close(); // Close data stream.

		//cookies = conn.getHeaderFields().get("Set-Cookie");
		checkCookies(conn.getHeaderFields().get("Set-Cookie")); // Get the header containing new cookies, don't store duplicates, and keep the old ones.

		//lastStatusCode = sslHandshakeFailureWorkaround(conn);
		lastStatusCode =  conn.getResponseCode(); // Execute request
		switch (lastStatusCode) {
			case 200: // OK
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)); // Connection Input

				String inputLine; 
				StringBuffer response = new StringBuffer();
				
				while ((inputLine = in.readLine()) != null) { // When there's data available,
				response.append(inputLine); // write data to the String Buffer
				}
				in.close(); // Destroy connection buffer
				responseString = response.toString(); // pass String Buffer contents to a String
			break;
			case 302: // Redirect, return new location.
				List<String> location = conn.getHeaderFields().get("Location");
				if (location.size() < 1)
					throw new HttpClientException("\"Location\" header was not found for HTTP code 302");
				return location.get(0);
			default:
				throw new UnexpectedHTTPStatusCode("Server replied with code " + lastStatusCode);
		}
		//conn.disconnect();
		return responseString;
	}

	/*
	 * ## encodeParameters ## -This method gets two arrays of parameters and its
	 * values, and encodes them on URL-ready format. Exceptions:
	 * -UnsupportedEncodingException
	 */

	public String encodeParameters(String parameterName[], String parameterValue[]) throws UnsupportedEncodingException, URLEncodingException {
		StringBuilder result = null;
		if (parameterName.length == parameterValue.length) {
			result = new StringBuilder();
			for (int i = 0; i < parameterName.length; i++) {
				String param = parameterName[i] + "=" + URLEncoder.encode(parameterValue[i], encoding.name());
				if (result.length() == 0) {
					result.append(param);
				} else {
					result.append("&" + param);
				}
			}
		} else {
			throw new URLEncodingException(
					"Encoding Error: Arrays parameterName and parameterValue are not of the same length.");
		}

		return result.toString();
	}

	private void checkCookies(List<String> newCookies) {
		//System.out.println("old     " + cookies);
		//System.out.println("new     " + newCookies);
		if (newCookies != null) {
			boolean exists = false;
			for (String newCookie : newCookies) {
				for (String oldCookie : cookies) {
					if (oldCookie.split("=")[0].contains(newCookie.split("=")[0])) {
						oldCookie = newCookie;
						exists = true;
						break;
					}
				}
				if (!exists) {
					cookies.add(newCookie);
				}
			}
		}
		//System.out.println("old+new " + cookies);
	}
	
	public byte[] charsToBytes(char[] c) {
		// Encode characters to a new ByteBuffer (CharBuffer.wrap does not copy the array)
		ByteBuffer buf = encoding.encode(CharBuffer.wrap(c));
		// Copy the contents to a byte array (similar to memcpy in c)
		byte[] b = Arrays.copyOfRange(buf.array(), buf.position(), buf.limit());
		Arrays.fill(buf.array(), (byte) 0); // Overwrite buffer array to protect passwords
		// Remember to overwrite char array if not needed after this function!
		return b;
	}

	/*  WORKAROUND for SSL handshake aborted by server: Retry until it works. Probably a server configuration issue.
    See https://stackoverflow.com/questions/30538640/javax-net-ssl-sslexception-read-error-ssl-0x9524b800-i-o-error-during-system?lq=1
    Also, ssl-test it: https://www.ssllabs.com/ssltest/analyze.html?d=cas.udc.es */
	/*private int sslHandshakeFailureWorkaround(HttpURLConnection conn) throws IOException {
		int count = HttpConstants.SSL_EXCEPTION_RETRY_MAX_COUNT;
		SSLException e = null;
		while (count > 0) {
			try {
				return conn.getResponseCode();
			} catch (SSLException ex) {
				e = ex;
				count--;
				Log.e(LOG_TAG, "SSL exception (retries remaining: " + count + "): " + e.getMessage());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		throw e;
	}*/

	public int getLastStatusCode() {
		return lastStatusCode;
	}

	public boolean getReferrerActive() {
		return referrerActive;
	}

	public void setReferrerActive(boolean enabled) {
		referrerActive = enabled;
	}

	public String getReferrer() {
		return REFERRER;
	}

	public void setReferrer(String referrer) {
		REFERRER = referrer;
	}

	public boolean getHostActive() {
		return hostActive;
	}

	public void setHostActive(boolean enabled) {
		hostActive = enabled;
	}

	public String getHost() {
		return HOST;
	}

	public void setHost(String host) {
		HOST = host;
	}

	public boolean getGzipSupport() {
		return gzipSupport;
	}

	public void setGzipSupport(boolean enabled) {
		gzipSupport = enabled;
	}

	public String getUserAgent() {
		return USER_AGENT;
	}

	public void setUserAgent(String userAgent) {
		USER_AGENT = userAgent;
	}

	public String getAcceptHeader() {
		return ACCEPT;
	}

	public void setAcceptHeader(String acceptHeader) {
		ACCEPT = acceptHeader;
	}

	public String getAcceptLanguage() {
		return ACCEPT_LANGUAGE;
	}

	public void setAcceptLanguage(String acceptLanguage) {
		ACCEPT_LANGUAGE = acceptLanguage;
	}

	public Charset getEncoding() {
		return encoding;
	}

	public void setEncoding(Charset encoding) {
		this.encoding = encoding;
	}
	
	public void clearCookies() {
		cookies.clear();
	}
	
	public List<String> getCookies() {
		return cookies;
	}
	
	public void addCookie(String cookie) {
		cookies.add(cookie);
	}
	
	public void removeCookie(int index) {
		cookies.remove(index);
	}
}
