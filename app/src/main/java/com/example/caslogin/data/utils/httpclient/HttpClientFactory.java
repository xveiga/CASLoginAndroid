package com.example.caslogin.data.utils.httpclient;

import com.example.caslogin.BuildConfig;

public class HttpClientFactory {

    // BuildConfig.HTTP_CLIENT_FACTORY_CLASS is a constant defined in build.gradle for the app
    private final static String CLASS_NAME = BuildConfig.HTTP_CLIENT_FACTORY_CLASS;
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

