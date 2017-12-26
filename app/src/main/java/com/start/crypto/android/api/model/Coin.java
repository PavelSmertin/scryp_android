package com.start.crypto.android.api.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Coin implements Parcelable {

    private long id;
    private String symbol;
    private String name;

    public Coin(long id, String symbol, String name) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.symbol);
        dest.writeString(this.name);
    }

    public static final Parcelable.Creator<Coin> CREATOR
            = new Parcelable.Creator<Coin>() {
        public Coin createFromParcel(Parcel in) {
            return new Coin(in);
        }

        public Coin[] newArray(int size) {
            return new Coin[size];
        }
    };

    private Coin(Parcel in) {
        this.id     = in.readLong();
        this.symbol = in.readString();
        this.name   = in.readString();

    }

}
