package com.start.crypto.android.api.model;

import com.google.gson.annotations.SerializedName;

public class Portfolio {

    @SerializedName("coins_count")
    private int mCoinsCount;

    @SerializedName("user_id")
    private long mUserId;

    @SerializedName("user_name")
    private String mUserName;

    @SerializedName("profit_24h")
    private double mProfit24h;

    @SerializedName("profit_7d")
    private double mProfit7d;

    public int getCoinsCount() {
        return mCoinsCount;
    }

    public long getUserId() {
        return mUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    public double getProfit24h() {
        return mProfit24h;
    }

    public double getProfit7d() {
        return mProfit7d;
    }
}
