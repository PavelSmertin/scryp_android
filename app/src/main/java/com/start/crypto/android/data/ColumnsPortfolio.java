package com.start.crypto.android.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.util.Log;

public class ColumnsPortfolio {


    private static final int COLUMN_ID                  = 1;
    private static final int COLUMN_BASE_COIN_ID        = 2;
    private static final int COLUMN_BALANCE             = 3;
    private static final int COLUMN_ORIGINAL            = 4;
    private static final int COLUMN_PRICE_NOW           = 5;
    private static final int COLUMN_PRICE_ORIGINAL      = 6;
    private static final int COLUMN_PRICE_24H           = 7;

    private static final int COLUMN_COINS_COUNT         = 8;
    private static final int COLUMN_USER_ID             = 9;
    private static final int COLUMN_USERNAME            = 10;
    private static final int COLUMN_PROFIT24H           = 11;
    private static final int COLUMN_PROFIT7D            = 12;
    private static final int COLUMN_REMOVED             = 13;

    private static final int COLUMN_CREATED_AT          = 14;
    private static final int COLUMN_UPDATED_AT          = 15;



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

        public int mColumnCoinsCount;
        public int mColumnUserId;
        public int mColumnUsername;
        public int mColumnProfit24h;
        public int mColumnProfit7d;
        public int mColumnRemoved;

        public int mColumnCreatedAt;
        public int mColumnUpdatedAt;


        public ColumnsMap() {
            mColumnId               = COLUMN_ID;
            mColumnBaseCoinId       = COLUMN_BASE_COIN_ID;
            mColumnBalance          = COLUMN_BALANCE;
            mColumnOriginal         = COLUMN_ORIGINAL;
            mColumnPriceNow         = COLUMN_PRICE_NOW;
            mColumnPriceOriginal    = COLUMN_PRICE_ORIGINAL;
            mColumnPrice24h         = COLUMN_PRICE_24H;

            mColumnCoinsCount       = COLUMN_COINS_COUNT;
            mColumnUserId           = COLUMN_USER_ID;
            mColumnUsername         = COLUMN_USERNAME;
            mColumnProfit24h        = COLUMN_PROFIT24H;
            mColumnProfit7d         = COLUMN_PROFIT7D;
            mColumnRemoved          = COLUMN_REMOVED;

            mColumnCreatedAt        = COLUMN_CREATED_AT;
            mColumnUpdatedAt        = COLUMN_UPDATED_AT;

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

            try {
                mColumnCoinsCount = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_COINS_COUNT);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnUserId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_USER_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnUsername = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_USERNAME);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnProfit24h = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_PROFIT24H);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnProfit7d = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_PROFIT7D);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnRemoved = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_REMOVED);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnCreatedAt = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_CREATED_AT);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnUpdatedAt = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios.COLUMN_NAME_UPDATED_AT);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

        }
    }
}
