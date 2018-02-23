package com.start.crypto.android.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;


public class PriceMultiFullResponse {

    @SerializedName("RAW")
    private HashMap<String, HashMap<String, RawCoin>> raw;

    @SerializedName("DISPLAY")
    private HashMap<String, HashMap<String, DisplayCoin>> display;

    public HashMap<String, HashMap<String, RawCoin>> getRaw() {
        return raw;
    }

    public HashMap<String, HashMap<String, DisplayCoin>> getDisplay() {
        return display;
    }

    public class RawCoin {

        @SerializedName("TYPE")
        private int type;

        @SerializedName("FLAGS")
        private String flags;

        @SerializedName("FROMSYMBOL")
        private String fromSymbol;

        @SerializedName("TOSYMBOL")
        private String toSymbol;

        @SerializedName("MARKET")
        private String market;

        @SerializedName("PRICE")
        private double price;

        @SerializedName("LASTUPDATE")
        private long lastUpdate;

        @SerializedName("LASTVOLUME")
        private double lastVolume;

        @SerializedName("LASTVOLUMETO")
        private double lastVolumeTo;

        @SerializedName("LASTTRADEID")
        private String lastRadeId;

        @SerializedName("VOLUMEDAY")
        private double volumeDay;

        @SerializedName("VOLUMEDAYTO")
        private double volumeDayTo;

        @SerializedName("VOLUME24HOUR")
        private double volume24Hour;

        @SerializedName("VOLUME24HOURTO")
        private double volume24HourTo;

        @SerializedName("OPENDAY")
        private double openDay;

        @SerializedName("HIGHDAY")
        private double hightDay;

        @SerializedName("LOWDAY")
        private double lowDay;

        @SerializedName("OPEN24HOUR")
        private double open24Hour;

        @SerializedName("HIGH24HOUR")
        private double hight24Hour;

        @SerializedName("LOW24HOUR")
        private double low24Hour;

        @SerializedName("LASTMARKET")

        private String lastMarket;

        @SerializedName("CHANGE24HOUR")
        private double change24Hour;

        @SerializedName("CHANGEPCT24HOUR")
        private double changePct24Hour;

        @SerializedName("CHANGEDAY")
        private double changeDay;

        @SerializedName("CHANGEPCTDAY")
        private double changePctDay;

        @SerializedName("SUPPLY")
        private double supply;

        @SerializedName("MKTCAP")
        private double MktCap;

        @SerializedName("TOTALVOLUME24H")
        private double totalVolume24h;

        @SerializedName("TOTALVOLUME24HTO")
        private double totalVolume24hTo;

        public String getFromSymbol() {
            return fromSymbol;
        }

        public String getToSymbol() {
            return toSymbol;
        }

        public String getMarket() {
            return market;
        }

        public double getPrice() {
            return price;
        }

        public long getLastUpdate() {
            return lastUpdate;
        }

        public double getChange24Hour() {
            return change24Hour;
        }

        public double getChangePct24Hour() {
            return changePct24Hour;
        }


    }

    public class DisplayCoin {

        @SerializedName("FROMSYMBOL")
        private String fromSymbol;

        @SerializedName("TOSYMBOL")
        private String toSymbol;

        @SerializedName("MARKET")
        private String market;

        @SerializedName("PRICE")
        private String price;

        @SerializedName("LASTUPDATE")
        private String lastUpdate;

        @SerializedName("LASTVOLUME")
        private String lastVolume;

        @SerializedName("LASTVOLUMETO")
        private String lastVolumeTo;

        @SerializedName("LASTTRADEID")
        private String lastRadeId;

        @SerializedName("VOLUMEDAY")
        private String volumeDay;

        @SerializedName("VOLUMEDAYTO")
        private String volumeDayTo;

        @SerializedName("VOLUME24HOUR")
        private String volume24Hour;

        @SerializedName("VOLUME24HOURTO")
        private String volume24HourTo;

        @SerializedName("OPENDAY")
        private String openDay;

        @SerializedName("HIGHDAY")
        private String hightDay;

        @SerializedName("LOWDAY")
        private String lowDay;

        @SerializedName("OPEN24HOUR")
        private String open24Hour;

        @SerializedName("HIGH24HOUR")
        private String hight24Hour;

        @SerializedName("LOW24HOUR")
        private String low24Hour;

        @SerializedName("LASTMARKET")
        private String lastMarket;

        @SerializedName("CHANGE24HOUR")
        private String change24Hour;

        @SerializedName("CHANGEPCT24HOUR")
        private String changePct24Hour;

        @SerializedName("CHANGEDAY")
        private String changeDay;

        @SerializedName("CHANGEPCTDAY")
        private String changePctDay;

        @SerializedName("SUPPLY")
        private String supply;

        @SerializedName("MKTCAP")
        private String MktCap;

        @SerializedName("TOTALVOLUME24H")
        private String totalVolume24h;

        @SerializedName("TOTALVOLUME24HTO")
        private String totalVolume24hTo;
    }
}
