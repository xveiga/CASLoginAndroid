package com.example.caslogin.data.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HttpConstants {

    // Default charset for HttpClient and URL encoding
    public static final Charset HTTP_ENCODING = StandardCharsets.UTF_8;

    // In case of SSL Handshake errors, how many times to retry before giving up.
    public static final int SSL_EXCEPTION_RETRY_MAX_COUNT = 5;
}
