package com.start.crypto.android.api;

import com.start.crypto.android.api.model.ApiInterceptor;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public enum RestClientMainApi {

    INSTANCE;
    private static final String BASE_URL = "http://cards.ff.ru/";
    private  MainApiService mMainApiService;
    private ApiInterceptor mMainApiInterceptor;

    RestClientMainApi() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(mMainApiInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .build();
        mMainApiService = retrofit.create(MainApiService.class);
    }

    public MainApiService getClient() {
        return mMainApiService;
    }

    public void setApiInterceptor(ApiInterceptor interceptor) {
        mMainApiInterceptor = interceptor;
    }
}

