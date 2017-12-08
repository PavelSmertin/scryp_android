package com.start.crypto.android.api.model;


import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class CoinsResponse {

    @SerializedName("Data")
    private HashMap<String, CoinResponse> data;

    public HashMap<String,CoinResponse> getData() {
        return data;
    }

}
