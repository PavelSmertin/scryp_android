package com.start.crypto.android.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.util.Log;

public class ColumnsPortfolioCoin {

    private static final int COLUMN_ID              = 1;
    private static final int COLUMN_PORTFOLIO_ID    = 2;
    private static final int COLUMN_COIN_ID         = 3;
    private static final int COLUMN_EXCHANGE_ID     = 4;
    private static final int COLUMN_ORIGINAL        = 5;
    private static final int COLUMN_PRICE_NOW       = 6;
    private static final int COLUMN_PRICE_ORIGINAL  = 7;
    private static final int COLUMN_PRICE_24H       = 8;

    private static final int COLUMN_CREATED_AT      = 9;
    private static final int COLUMN_UPDATED_AT      = 10;

    public static final int CACHE_SIZE          = 50;

    public static class ColumnsMap {
        private final String TAG = "PtflCnClmns.ColumnsMap";
        private final boolean DEBUG = false;

        public int mColumnId;
        public int mPortfolioId;
        public int mCoinId;
        public int mExchangeId;
        public int mOriginal;
        public int mColumnPriceNow;
        public int mColumnPriceOriginal;
        public int mColumnPrice24h;
        public int mColumnCreatedAt;
        public int mColumnUpdatedAt;


        public ColumnsMap() {
            mColumnId               = COLUMN_ID;
            mPortfolioId            = COLUMN_PORTFOLIO_ID;
            mCoinId                 = COLUMN_COIN_ID;
            mExchangeId             = COLUMN_EXCHANGE_ID;
            mOriginal               = COLUMN_ORIGINAL;
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
                mPortfolioId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mCoinId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mExchangeId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mOriginal = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL);
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
