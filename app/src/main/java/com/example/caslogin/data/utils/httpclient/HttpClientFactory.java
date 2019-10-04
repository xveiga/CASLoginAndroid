package com.example.caslogin.data.utils.httpclient;

public class HttpClientFactory {

    private final static String CLASS_NAME = "com.example.caslogin.data.utils.httpclient.java.NativeHttpClient.class";
    private static HttpClient instance = null;

    private HttpClientFactory() {}

    public static HttpClient getInstance() {
        try {
            Class serviceClass = Class.forName(CLASS_NAME);
            return (HttpClient) serviceClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public synchronized static HttpClient getSharedInstance() {
        if (instance == null) {
            instance = getInstance();
        }
        return instance;
    }

}

