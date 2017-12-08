package com.start.crypto.android.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.util.Log;

public class ColumnsCoin {

    private static final int COLUMN_ID          = 1;
    private static final int COLUMN_NAME        = 2;
    private static final int COLUMN_SYMBOL      = 3;
    private static final int COLUMN_LOGO        = 4;
    private static final int COLUMN_SORT_ORDER  = 5;


    public static final int CACHE_SIZE          = 50;

    public static class ColumnsMap {
        private final String TAG = "CoinColumns.ColumnsMap";
        private final boolean DEBUG = false;

        public int mColumnId;
        public int mColumnName;
        public int mColumnSymbol;
        public int mColumnLogo;
        public int mColumnSortOrder;


        public ColumnsMap() {
            mColumnId           = COLUMN_ID;
            mColumnName         = COLUMN_NAME;
            mColumnSymbol       = COLUMN_SYMBOL;
            mColumnLogo         = COLUMN_LOGO;
            mColumnSortOrder    = COLUMN_SORT_ORDER;
        }

        @SuppressLint("InlinedApi")
        public ColumnsMap(Cursor cursor) {
            // Ignore all 'not found' exceptions since the custom columns
            // may be just a subset of the default columns.
            try {
                mColumnId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoCoins._ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnName = cursor.getColumnIndexOrThrow(CryptoContract.CryptoCoins.COLUMN_NAME_NAME);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnSymbol = cursor.getColumnIndexOrThrow(CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnLogo = cursor.getColumnIndexOrThrow(CryptoContract.CryptoCoins.COLUMN_NAME_LOGO);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnSortOrder = cursor.getColumnIndexOrThrow(CryptoContract.CryptoCoins.COLUMN_NAME_SORT_ORDER);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

        }
    }
}
