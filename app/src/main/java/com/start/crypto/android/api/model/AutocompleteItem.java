package com.start.crypto.android.api.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AutocompleteItem implements Parcelable {

    private long id;
    private String symbol;
    private String name;

    public AutocompleteItem(long id, String symbol, String name) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
    }

    public AutocompleteItem(long id, String name) {
        this.id = id;
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

    public static final Parcelable.Creator<AutocompleteItem> CREATOR
            = new Parcelable.Creator<AutocompleteItem>() {
        public AutocompleteItem createFromParcel(Parcel in) {
            return new AutocompleteItem(in);
        }

        public AutocompleteItem[] newArray(int size) {
            return new AutocompleteItem[size];
        }
    };

    private AutocompleteItem(Parcel in) {
        this.id     = in.readLong();
        this.symbol = in.readString();
        this.name   = in.readString();

    }

}
