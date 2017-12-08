package com.start.crypto.android.api.model;


import com.google.gson.annotations.SerializedName;

public class CoinResponse {

    @SerializedName("Id")
    private long id;

    @SerializedName("ImageUrl")
    private String imageUrl;

    @SerializedName("Name")
    private String name;

    @SerializedName("CoinName")
    private String coinName;

    @SerializedName("FullName")
    private String fullName;

    @SerializedName("SortOrder")
    private long sortOrder;

    public long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getCoinName() {
        return coinName;
    }

    public String getFullName() {
        return fullName;
    }

    public long getSortOrder() {
        return sortOrder;
    }


}
