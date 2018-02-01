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

    private static final int COLUMN_CREATED_AT      = 10;
    private static final int COLUMN_UPDATED_AT      = 11;

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
