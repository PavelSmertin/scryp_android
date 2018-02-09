package com.start.crypto.android.api.model;

import com.google.gson.annotations.SerializedName;
import com.start.crypto.android.api.RestClientMainApi;

public class User {

    String avatar;

    @SerializedName("first_name")
    String firstName;

    @SerializedName("last_name")
    String lastName;


    public String getAvatar() {
        if(avatar == null) {
            return null;
        }
        return RestClientMainApi.BASE_URL + avatar;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
