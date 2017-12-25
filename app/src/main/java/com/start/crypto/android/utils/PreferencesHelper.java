package com.start.crypto.android.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;


public class PreferencesHelper {

    public static final String NAME_CRYPT = "crypt";
    public static final String KEY_LOGIN = "login";

    private static volatile PreferencesHelper instance;

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

    public void setLogin(String login) {
        Log.d("DEBUG_INFO", "setLogin: " + login);
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LOGIN, login);
        editor.apply();
    }

    public String getLogin() {
        return sharedPreferences.getString(KEY_LOGIN, null);
    }

    public void logout() {
        Log.d("DEBUG_INFO", "logout");

        Editor editor = sharedPreferences.edit();
        editor.remove(KEY_LOGIN);
        editor.apply();
    }

}
