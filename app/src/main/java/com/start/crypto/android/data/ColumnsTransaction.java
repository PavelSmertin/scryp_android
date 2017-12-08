package com.start.crypto.android.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.util.Log;

public class ColumnsTransaction {

    private static final int COLUMN_ID                  = 1;
    private static final int COLUMN_COIN_ID             = 2;
    private static final int COLUMN_PORTFOLIO_ID        = 3;
    private static final int COLUMN_COIN_CORRESPOND_ID  = 4;
    private static final int COLUMN_EXCHANGE_ID         = 5;
    private static final int COLUMN_PORTFOLIO_BALANCE   = 6;
    private static final int COLUMN_AMOUNT              = 7;
    private static final int COLUMN_PRICE               = 8;
    private static final int COLUMN_DATETIME            = 9;

    public static final int CACHE_SIZE         = 50;

    public static class ColumnsMap {
        private final String TAG = "TransClmns.ColumnsMap";
        private final boolean DEBUG = false;

        public int mColumnId;
        public int mColumnCoinId;
        public int mColumnPorfolioId;
        public int mColumnCoinCorrespondId;
        public int mColumnExchangeId;
        public int mColumnPortfolioBalance;
        public int mColumnAmount;
        public int mColumnPrice;
        public int mColumnDatetime;


        public ColumnsMap() {
            mColumnId               = COLUMN_ID;
            mColumnCoinId           = COLUMN_COIN_ID;
            mColumnPorfolioId       = COLUMN_PORTFOLIO_ID;
            mColumnCoinCorrespondId = COLUMN_COIN_CORRESPOND_ID;
            mColumnExchangeId       = COLUMN_EXCHANGE_ID;
            mColumnPortfolioBalance = COLUMN_PORTFOLIO_BALANCE;
            mColumnAmount           = COLUMN_AMOUNT;
            mColumnPrice            = COLUMN_PRICE;
            mColumnDatetime         = COLUMN_DATETIME;
        }

        @SuppressLint("InlinedApi")
        public ColumnsMap(Cursor cursor) {
            // Ignore all 'not found' exceptions since the custom columns
            // may be just a subset of the default columns.
            try {
                mColumnId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoTransactions._ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnCoinId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnCoinId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnCoinCorrespondId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_CORRESPOND_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnExchangeId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoTransactions.COLUMN_NAME_EXCHANGE_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnPortfolioBalance = cursor.getColumnIndexOrThrow(CryptoContract.CryptoTransactions.COLUMN_NAME_PROTFOLIO_BALANCE);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnAmount = cursor.getColumnIndexOrThrow(CryptoContract.CryptoTransactions.COLUMN_NAME_AMOUNT);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnPrice = cursor.getColumnIndexOrThrow(CryptoContract.CryptoTransactions.COLUMN_NAME_PRICE);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnDatetime = cursor.getColumnIndexOrThrow(CryptoContract.CryptoTransactions.COLUMN_NAME_DATETIME);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }
        }
    }
}
