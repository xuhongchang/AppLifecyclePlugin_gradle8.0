package com.example.myapplication;

import android.app.Application;

import com.hm.lifecycle.api.ApplicationLifecycleManager;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //
        ApplicationLifecycleManager.init();
        ApplicationLifecycleManager.onCreate(this);
    }
}
