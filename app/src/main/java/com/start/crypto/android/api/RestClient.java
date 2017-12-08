package com.start.crypto.android.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public enum RestClient {

    INSTANCE;

    private static final String BASE_URL = "https://www.cryptocompare.com/api/data/";
    private final CriptoCompaireService mCriptoCompaireService;

    RestClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .build();
        mCriptoCompaireService = retrofit.create(CriptoCompaireService.class);
    }

    public CriptoCompaireService getClient() {
        return mCriptoCompaireService;
    }

}

