package com.start.crypto.android.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PublicPortfolioResponse {

    @SerializedName("portfolio_coins")
    List<PortfolioCoin> portfolioCoins;

    public List<PortfolioCoin> getPortfolioCoins() {
        return portfolioCoins;
    }

}
