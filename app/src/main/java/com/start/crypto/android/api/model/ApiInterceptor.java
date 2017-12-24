package com.start.crypto.android.api.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.start.crypto.android.AuthActivity;
import com.start.crypto.android.utils.PreferencesHelper;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class ApiInterceptor implements Interceptor {

    private Context mContext;
    private PreferencesHelper mPreferencesHelper;

    public ApiInterceptor(Context context) {
        mPreferencesHelper = PreferencesHelper.createInstance(context);
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        String token = getAuthToken();
        if (token != null) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }

        return chain.proceed(request);
    }

    private String getAuthToken() {
        if(mPreferencesHelper.getLogin() != null) {
            return null;
        }

        AccountManager accountManager = AccountManager.get(mContext);

        Account[] accounts = accountManager.getAccountsByType(AuthActivity.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            Account currentAccount = getStoredAccount(accounts);
            if(currentAccount == null) {
                return null;
            }
            return accountManager.peekAuthToken(currentAccount, AuthActivity.AUTHTOKEN_TYPE_FULL_ACCESS);
        }
        return null;
    }

    private Account getStoredAccount(Account[] accounts) {
        for(Account a : accounts) {
            if(a.name.equalsIgnoreCase(mPreferencesHelper.getLogin())) {
                return a;
            }
        }
        return null;
    }


}
