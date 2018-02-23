package com.start.crypto.android.api.model;

import com.google.gson.annotations.SerializedName;

public class Transaction {

    private long id;

    private long portfolioCoinId;
    private long pairId;

    @SerializedName("price_now")
    private double amount;

    @SerializedName("price_original")
    private double price;

    @SerializedName("price_24h")
    private long date;

    @SerializedName("price_7d")
    private String description;

    private double basePrice;

    private double coinPrice;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;


    public Transaction(double amount, double price, long date, String description, double basePrice) {
        this.amount = amount;
        this.price = price;
        this.date = date;
        this.description = description;
        this.basePrice = basePrice;
    }

    public Transaction(long portfolioCoinId, double amount, double price, long date, String description, double basePrice) {
        this.portfolioCoinId = portfolioCoinId;
        this.amount = amount;
        this.price = price;
        this.date = date;
        this.description = description;
        this.basePrice = basePrice;
    }

    public Transaction(long portfolioCoinId, long pairId, double amount, double price, long date, String description, double basePrice, double coinPrice) {
        this.portfolioCoinId = portfolioCoinId;
        this.pairId = pairId;
        this.amount = amount;
        this.price = price;
        this.date = date;
        this.description = description;
        this.basePrice = basePrice;
        this.coinPrice = coinPrice;
    }



    public long getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public long getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }


    public double getPairBasePrice() {
        return basePrice;
    }

    public double getCoinBasePrice() {
        return coinPrice;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public long getPairId() {
        return pairId;
    }

    public long getPortfolioCoinId() {
        return portfolioCoinId;
    }

    public void setPortfolioCoinId(long portfolioCoinId) {
        this.portfolioCoinId = portfolioCoinId;
    }



}
