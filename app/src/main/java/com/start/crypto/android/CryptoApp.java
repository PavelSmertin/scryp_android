package com.start.crypto.android;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.start.crypto.android.utils.PreferencesHelper;

public class CryptoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        PreferencesHelper.createInstance(this);
    }
}
