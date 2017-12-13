package com.start.crypto.android.api.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.start.crypto.android.AuthActivity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class ApiInterceptor implements Interceptor {

    private Context mContext;

    public ApiInterceptor(Context context) {

        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        AccountManager accountManager = AccountManager.get(mContext);
        Account[] accounts = accountManager.getAccountsByType( AuthActivity.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            String token = accountManager.peekAuthToken(accounts[0], AuthActivity.AUTHTOKEN_TYPE_FULL_ACCESS);
            if (token != null) {
                request = request.newBuilder()
                        .addHeader("Authorization", "Bearer " + token)
                        .build();
            }
        }

        Response response = chain.proceed(request);
        return response;
    }
}
