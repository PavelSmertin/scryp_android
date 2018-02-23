package com.start.crypto.android.api.model;

import android.database.Cursor;

import com.google.gson.annotations.SerializedName;
import com.start.crypto.android.data.ColumnsCoin;
import com.start.crypto.android.data.ColumnsPortfolioCoin;


/* RESPONSE from Main server */
public class PortfolioCoinResponse {

    @SerializedName("portfolio_coin_id")
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

    @SerializedName("exchange")
    private String exchangeName;

    private String symbol;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("change_24h")
    private double change24h;

    @SerializedName("change_percent_24h")
    private double changePercent24h;


    private PortfolioCoinResponse(long portfolioId,
                                  long portfolioCoinId,
                                  long exchangeId,
                                  String coinSymbol,
                                  double original,
                                  double priceNow,
                                  double change24h,
                                  double changePercent24h,
                                  double priceOriginal) {

        this.portfolioId        = portfolioId;
        this.id                 = portfolioCoinId;
        this.exchangeId         = exchangeId;
        this.symbol             = coinSymbol;
        this.original           = original;
        this.priceNow           = priceNow;
        this.change24h          = change24h;
        this.changePercent24h   = changePercent24h;
        this.priceOriginal      = priceOriginal;

    }


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
        if(!Double.isNaN(priceNow) && !Double.isInfinite(priceNow)) {
            return priceNow;
        }
        return 0D;
    }

    public double getPriceOriginal() {
        return priceOriginal;
    }

    public double getTotalOriginal() {
        return original * priceOriginal;
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

    public double getChange24h() {
        if(!Double.isNaN(change24h) && !Double.isInfinite(change24h)) {
            return change24h;
        }
        return 0D;
    }

    public double getChangePercent24h() {
        if(!Double.isNaN(changePercent24h) && !Double.isInfinite(changePercent24h)) {
            return changePercent24h;
        }
        return 0D;
    }

    public void setPriceNow(double priceNow) {
        this.priceNow = priceNow;
    }

    public void setPrice24h(double price24h) {
        this.price24h = price24h;
    }

    public void setPrice7d(double price7d) {
        this.price7d = price7d;
    }

    public static PortfolioCoinResponse fromCursor(Cursor cursor) {

        ColumnsPortfolioCoin.ColumnsMap columnsMap      = new ColumnsPortfolioCoin.ColumnsMap(cursor);
        ColumnsCoin.ColumnsMap columnsCoinsMap          = new ColumnsCoin.ColumnsMap(cursor);

        return new PortfolioCoinResponse(
                cursor.getLong(columnsMap.mColumnPortfolioId),
                cursor.getLong(columnsMap.mColumnId),
                cursor.getLong(columnsMap.mColumnExchangeId),
                cursor.getString(columnsCoinsMap.mColumnSymbol),
                cursor.getDouble(columnsMap.mColumnOriginal),
                cursor.getDouble(columnsMap.mColumnPriceNow),
                cursor.getDouble(columnsMap.mColumnChange24h),
                cursor.getDouble(columnsMap.mColumnChangePct24h),
                cursor.getDouble(columnsMap.mColumnPriceOriginal)
        );


    }


    public double getValue() {
        double value = original * priceNow;
        if(!Double.isNaN(value) && !Double.isInfinite(value)) {
            return value;
        }
        return 0D;
    }

    public double getChangePercentAll() {
        double changePercentAll = 0;
        if(priceOriginal > 0) {
            changePercentAll = (priceNow - priceOriginal) * 100 / priceOriginal;
        }

        if(!Double.isNaN(changePercentAll) && !Double.isInfinite(changePercentAll)) {
            return changePercentAll;
        }

        return 0D;
    }

    public double getChangeAll() {
        double changeAll = 0;
        if(priceNow > 0) {
            changeAll = (priceNow - priceOriginal) * original;
        }

        if(!Double.isNaN(changeAll) && !Double.isInfinite(changeAll)) {
            return changeAll;
        }

        return 0D;
    }
}
