package com.start.crypto.android.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.util.Log;

public class ColumnsPortfolioCoin {

    private static final int COLUMN_ID              = 1;
    private static final int COLUMN_USER_ID         = 2;
    private static final int COLUMN_PORTFOLIO_ID    = 3;
    private static final int COLUMN_COIN_ID         = 4;
    private static final int COLUMN_EXCHANGE_ID     = 5;
    private static final int COLUMN_ORIGINAL        = 6;
    private static final int COLUMN_PRICE_NOW       = 7;
    private static final int COLUMN_PRICE_ORIGINAL  = 8;
    private static final int COLUMN_PRICE_24H       = 9;
    private static final int COLUMN_CHANGE_24H      = 10;
    private static final int COLUMN_CHANGE_PCT_24H  = 11;
    private static final int COLUMN_REMOVED         = 12;

    private static final int COLUMN_CREATED_AT      = 13;
    private static final int COLUMN_UPDATED_AT      = 14;

    public static final int CACHE_SIZE          = 50;

    public static class ColumnsMap {
        private final String TAG = "PtflCnClmns.ColumnsMap";
        private final boolean DEBUG = false;

        public int mColumnId;
        public int mColumnUserId;
        public int mColumnPortfolioId;
        public int mColumnCoinId;
        public int mColumnExchangeId;
        public int mColumnOriginal;
        public int mColumnPriceNow;
        public int mColumnPriceOriginal;
        public int mColumnPrice24h;
        public int mColumnChange24h;
        public int mColumnChangePct24h;
        public int mColumnRemoved;

        public int mColumnCreatedAt;
        public int mColumnUpdatedAt;


        public ColumnsMap() {
            mColumnId               = COLUMN_ID;
            mColumnUserId           = COLUMN_USER_ID;
            mColumnPortfolioId      = COLUMN_PORTFOLIO_ID;
            mColumnCoinId           = COLUMN_COIN_ID;
            mColumnExchangeId       = COLUMN_EXCHANGE_ID;
            mColumnOriginal         = COLUMN_ORIGINAL;
            mColumnPriceNow         = COLUMN_PRICE_NOW;
            mColumnPriceOriginal    = COLUMN_PRICE_ORIGINAL;
            mColumnPrice24h         = COLUMN_PRICE_24H;
            mColumnChange24h        = COLUMN_CHANGE_24H;
            mColumnChangePct24h     = COLUMN_CHANGE_PCT_24H;
            mColumnRemoved          = COLUMN_REMOVED;

            mColumnCreatedAt        = COLUMN_CREATED_AT;
            mColumnUpdatedAt        = COLUMN_UPDATED_AT;
        }

        @SuppressLint("InlinedApi")
        public ColumnsMap(Cursor cursor) {
            // Ignore all 'not found' exceptions since the custom columns
            // may be just a subset of the default columns.
            try {
                mColumnId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins._ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnUserId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_USER_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnPortfolioId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnCoinId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnExchangeId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnOriginal = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnPriceNow = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnPriceOriginal = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnPrice24h = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_24H);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnChange24h = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_CHANGE_24H);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnChangePct24h = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_CHANGE_PCT_24H);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnRemoved = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_REMOVED);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnCreatedAt = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_CREATED_AT);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnUpdatedAt = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_UPDATED_AT);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }


        }
    }
}
