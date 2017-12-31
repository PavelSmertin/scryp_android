package com.start.crypto.android.api.model;

import com.google.gson.annotations.SerializedName;

public class RestoreResponse {

    @SerializedName("user_id")
    private String userId;

    public String getUserId() {
        return userId;
    }

}
