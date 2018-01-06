package com.start.crypto.android.api.model;

import com.google.gson.annotations.SerializedName;

public class PortfolioCoin {

    private long id;

    @SerializedName("user_id")
    private long userId;

    @SerializedName("portfolio_id")
    private long portfolioId;

    @SerializedName("coin_id")
    private long coinId;

    @SerializedName("exchange_id")
    private long exchangeId;

    @SerializedName("original")
    private double original;

    @SerializedName("price_now")
    private double priceNow;

    @SerializedName("price_original")
    private double priceOriginal;

    @SerializedName("price_24h")
    private double price24h;

    @SerializedName("price_7d")
    private double price7d;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("name")
    private String exchangeName;

    private String symbol;


    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public long getPortfolioId() {
        return portfolioId;
    }

    public long getCoinId() {
        return coinId;
    }

    public long getExchangeId() {
        return exchangeId;
    }

    public double getOriginal() {
        return original;
    }

    public double getPriceNow() {
        return priceNow;
    }

    public double getPriceOriginal() {
        return priceOriginal;
    }

    public double getPrice24h() {
        return price24h;
    }

    public double getPrice7d() {
        return price7d;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getSymbol() {
        return symbol;
    }



}
