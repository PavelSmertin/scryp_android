package com.start.crypto.android.api;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public enum RestClientMinApi {

    INSTANCE;
    private static final String BASE_URL = "https://min-api.cryptocompare.com/data/";
    private final CriptoCompaireMinApiService mCriptoCompaireMinApiService;

    RestClientMinApi() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .addInterceptor(logging)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .build();
        mCriptoCompaireMinApiService = retrofit.create(CriptoCompaireMinApiService.class);
    }

    public CriptoCompaireMinApiService getClient() {
        return mCriptoCompaireMinApiService;
    }

}

