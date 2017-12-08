package com.start.crypto.android.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.util.Log;

public class ColumnsPortfolio {


    private static final int COLUMN_ID                 = 1;
    private static final int COLUMN_BASE_COIN_ID       = 2;
    private static final int COLUMN_BALANCE            = 3;
    private static final int COLUMN_ORIGINAL           = 4;
    private static final int COLUMN_PRICE_NOW          = 5;
    private static final int COLUMN_PRICE_ORIGINAL     = 6;
    private static final int COLUMN_PRICE_24H          = 7;


    public static final int CACHE_SIZE         = 50;

    public static class ColumnsMap {
        private final String TAG = "PtflClmns.ColumnsMap";
        private final boolean DEBUG = false;

        public int mColumnId;
        public int mColumnBaseCoinId;
        public int mColumnBalance;
        public int mColumnOriginal;
        public int mColumnPriceNow;
        public int mColumnPriceOriginal;
        public int mColumnPrice24h;


        public ColumnsMap() {
            mColumnId               = COLUMN_ID;
            mColumnBaseCoinId       = COLUMN_BASE_COIN_ID;
            mColumnBalance          = COLUMN_BALANCE;
            mColumnOriginal         = COLUMN_ORIGINAL;
            mColumnPriceNow         = COLUMN_PRICE_NOW;
            mColumnPriceOriginal    = COLUMN_PRICE_ORIGINAL;
            mColumnPrice24h         = COLUMN_PRICE_24H;
        }


        @SuppressLint("InlinedApi")
        public ColumnsMap(Cursor cursor) {
            // Ignore all 'not found' exceptions since the custom columns
            // may be just a subset of the default columns.
            try {
                mColumnId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios._ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnBaseCoinId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_BASE_COIN_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }


            try {
                mColumnBalance = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_BALANCE);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnOriginal = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_ORIGINAL);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnPriceNow = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_PRICE_NOW);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnPriceOriginal = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_PRICE_ORIGINAL);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnPrice24h = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_PRICE_24H);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }
        }
    }
}
