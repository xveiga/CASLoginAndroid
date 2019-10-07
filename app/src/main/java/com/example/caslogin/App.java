package com.example.caslogin;

import android.app.Application;
import android.content.Context;

public class App extends android.app.Application {
    public static android.app.Application instance;

    @Override public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
