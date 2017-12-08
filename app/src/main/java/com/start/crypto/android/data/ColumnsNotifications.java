package com.start.crypto.android.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.util.Log;

public class ColumnsNotifications {

    private static final int COLUMN_ID                      = 1;
    private static final int COLUMN_COIN_ID                 = 2;
    private static final int COLUMN_COIN_SYMBOL             = 3;
    private static final int COLUMN_COIN_CORRESPOND_ID      = 4;
    private static final int COLUMN_COIN_CORRESPOND_SYMBOL  = 5;
    private static final int COLUMN_EXCHANGE_ID             = 6;
    private static final int COLUMN_EXCHANGE_NAME           = 7;
    private static final int COLUMN_PRICE_THRESHOLD         = 8;
    private static final int COLUMN_TYPE                    = 9;
    private static final int COLUMN_ACTIVE                  = 10;
    private static final int COLUMN_COMPARE                 = 11;

    public static final int CACHE_SIZE         = 50;

    public static class ColumnsMap {
        private final String TAG = "NtfctnsClmns.ColumnsMap";
        private final boolean DEBUG = false;

        public int mColumnId;
        public int mColumnCoinId;
        public int mColumnCoinSymbol;
        public int mColumnCorrespondId;
        public int mColumnCorrespondSymbol;
        public int mColumnExchangeId;
        public int mColumnExchangeName;
        public int mColumnPriceThreshold;
        public int mColumnType;
        public int mColumnActive;
        public int mColumnCompare;

        public ColumnsMap() {
            mColumnId               = COLUMN_ID;
            mColumnCoinId           = COLUMN_COIN_ID;
            mColumnCoinSymbol       = COLUMN_COIN_SYMBOL;
            mColumnCorrespondId     = COLUMN_COIN_CORRESPOND_ID;
            mColumnCorrespondSymbol = COLUMN_COIN_CORRESPOND_SYMBOL;
            mColumnExchangeId       = COLUMN_EXCHANGE_ID;
            mColumnExchangeName     = COLUMN_EXCHANGE_NAME;
            mColumnPriceThreshold   = COLUMN_PRICE_THRESHOLD;
            mColumnType             = COLUMN_TYPE;
            mColumnActive           = COLUMN_ACTIVE;
            mColumnCompare          = COLUMN_COMPARE;

        }


        @SuppressLint("InlinedApi")
        public ColumnsMap(Cursor cursor) {
            // Ignore all 'not found' exceptions since the custom columns
            // may be just a subset of the default columns.
            try {
                mColumnId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoNotifications._ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnCoinId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoNotifications.COLUMN_NAME_COIN_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnCoinSymbol = cursor.getColumnIndexOrThrow(CryptoContract.CryptoNotifications.COLUMN_NAME_COIN_SYMBOL);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnCorrespondId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoNotifications.COLUMN_NAME_CORRESPOND_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnCorrespondSymbol = cursor.getColumnIndexOrThrow(CryptoContract.CryptoNotifications.COLUMN_NAME_CORRESPOND_SYMBOL);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnExchangeId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoNotifications.COLUMN_NAME_EXCHANGE_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnExchangeName = cursor.getColumnIndexOrThrow(CryptoContract.CryptoNotifications.COLUMN_NAME_EXCHANGE_NAME);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnPriceThreshold = cursor.getColumnIndexOrThrow(CryptoContract.CryptoNotifications.COLUMN_NAME_PRICE_THRESHOLD);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnType = cursor.getColumnIndexOrThrow(CryptoContract.CryptoNotifications.COLUMN_NAME_TYPE);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnActive = cursor.getColumnIndexOrThrow(CryptoContract.CryptoNotifications.COLUMN_NAME_ACTIVE);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnCompare = cursor.getColumnIndexOrThrow(CryptoContract.CryptoNotifications.COLUMN_NAME_COMPARE);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

        }
    }
}
