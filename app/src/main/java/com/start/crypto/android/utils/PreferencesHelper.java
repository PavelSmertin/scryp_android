package com.start.crypto.android.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;
import java.util.Map;

public class PreferencesHelper {

    public static final String NAME_CRYPT = "crypt";
    public static final String KEY_AUTH_TOKEN = "authToken";
    public static final String KEY_AUTH_STATE = "authState";
    public static final String KEY_USER = "user";
    public static final String KEY_LOGIN = "login";


    public static final int WAITING_INIT_EMAIL_REQUEST = 7;
    public static final String INIT_EMAIL_LAST_REQUEST = "init_email_last_request";

    private static volatile PreferencesHelper instance;

    private static Map<String, String> systemErrorMessages = new HashMap<>();

    private Context context;
    private SharedPreferences sharedPreferences;

    public static PreferencesHelper createInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesHelper(context);
        }
        return instance;
    }

    public static PreferencesHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Call createInstance(Context context)");
        }
        return instance;
    }

    public PreferencesHelper(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(NAME_CRYPT, Context.MODE_PRIVATE);
    }

    public void setAuthToken(String token) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }

    public boolean isAuth() {
        return getAuthToken() != null;
    }

    public void setLogin(String login) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LOGIN, login);
        editor.apply();
    }

    public String getLogin() {
        return sharedPreferences.getString(KEY_LOGIN, null);
    }

    public void logout() {
        Editor editor = sharedPreferences.edit();
        editor.remove(KEY_AUTH_TOKEN);
        editor.remove(KEY_AUTH_STATE);
        editor.remove(KEY_USER);
        editor.apply();
    }

}
