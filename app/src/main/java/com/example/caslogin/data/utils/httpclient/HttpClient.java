package com.example.caslogin.data.utils.httpclient;

import com.example.caslogin.data.utils.exceptions.HttpClientException;
import com.example.caslogin.data.utils.exceptions.UnexpectedHTTPStatusCode;

import java.io.IOException;

public interface HttpClient {

    String httpsGet(String url) throws IOException, UnexpectedHTTPStatusCode;

    String httpsPostForm(String url, String postParams) throws IOException, HttpClientException, UnexpectedHTTPStatusCode;

    int getLastStatusCode();

    void destroy();
}
