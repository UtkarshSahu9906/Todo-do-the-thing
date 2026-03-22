package com.utkarsh.todo;

import android.app.Application;
import com.google.android.gms.ads.MobileAds;

public class TodoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize AdMob SDK once at app start
        MobileAds.initialize(this, initializationStatus -> {});
    }
}
