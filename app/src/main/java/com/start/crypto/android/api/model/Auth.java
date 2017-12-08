package com.start.crypto.android.api.model;

public class Auth {
    private AuthFileds auth;

    public Auth(String email, String password) {
        auth = new AuthFileds(email, password);
    }
}
