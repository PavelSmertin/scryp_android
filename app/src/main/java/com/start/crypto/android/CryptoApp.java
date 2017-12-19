package com.start.crypto.android;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.start.crypto.android.utils.PreferencesHelper;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class CryptoApp extends Application {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("https://streamer.cryptocompare.com/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        // Create an InitializerBuilder
        Stetho.InitializerBuilder initializerBuilder =
                Stetho.newInitializerBuilder(this);

        // Enable Chrome DevTools
        initializerBuilder.enableWebKitInspector(
                Stetho.defaultInspectorModulesProvider(this)
        );

        // Enable command line interface
        initializerBuilder.enableDumpapp(
                Stetho.defaultDumperPluginsProvider(this)
        );

        // Use the InitializerBuilder to generate an Initializer
        Stetho.Initializer initializer = initializerBuilder.build();

        // Initialize Stetho with the Initializer
        Stetho.initialize(initializer);

        PreferencesHelper.createInstance(this);
    }
}
