package com.start.crypto.android.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Portfolio {

    @SerializedName("portfolio_id")
    private int id;

    @SerializedName("coins_count")
    private int coinsCount;

    @SerializedName("user_id")
    private long userId;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("profit_24h")
    private double profit24h;

    @SerializedName("profit_7d")
    private double profit7d;

    @SerializedName("portfolio_coins")
    List<PortfolioCoin> portfolioCoins;

    @SerializedName("transactions")
    List<Transaction> transactions;


    public long getId() {
        return id;
    }

    public int getCoinsCount() {
        return coinsCount;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public double getProfit24h() {
        return profit24h;
    }

    public double getProfit7d() {
        return profit7d;
    }

    public List<PortfolioCoin> getPortfolioCoins() {
        return portfolioCoins;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
