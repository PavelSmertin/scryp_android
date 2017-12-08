package com.start.crypto.android.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.util.Log;

public class ColumnsExchange {

    private static final int COLUMN_ID          = 1;
    private static final int COLUMN_NAME        = 2;
    private static final int COLUMN_EXTERNAL_ID = 3;
    private static final int COLUMN_API_URL     = 4;

    public static final int CACHE_SIZE          = 50;

    public static class ColumnsMap {
        private final String TAG = "XchngColumns.ColumnsMap";
        private final boolean DEBUG = false;

        public int mColumnId;
        public int mColumnName;
        public int mColumnExternalId;
        public int mColumnApiUrl;


        public ColumnsMap() {
            mColumnId           = COLUMN_ID;
            mColumnName         = COLUMN_NAME;
            mColumnExternalId   = COLUMN_EXTERNAL_ID;
            mColumnApiUrl       = COLUMN_API_URL;
        }

        @SuppressLint("InlinedApi")
        public ColumnsMap(Cursor cursor) {
            // Ignore all 'not found' exceptions since the custom columns
            // may be just a subset of the default columns.
            try {
                mColumnId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoExchanges._ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnName = cursor.getColumnIndexOrThrow(CryptoContract.CryptoExchanges.COLUMN_NAME_NAME);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnExternalId = cursor.getColumnIndexOrThrow(CryptoContract.CryptoExchanges.COLUMN_NAME_EXTERNAL_ID);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

            try {
                mColumnApiUrl = cursor.getColumnIndexOrThrow(CryptoContract.CryptoExchanges.COLUMN_NAME_API_URL);
            } catch (IllegalArgumentException e) {
                if (DEBUG) Log.w(TAG, e.getMessage());
            }

        }
    }
}
