package com.start.crypto.android.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    Context mContext;

    public DBHelper(Context context) {
        super(context, CryptoContract.DATABASE_NAME, null, CryptoContract.DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CryptoContract.SQL_CREATE_PORTFOLIOS);
        db.execSQL(CryptoContract.SQL_CREATE_COINS);
        db.execSQL(CryptoContract.SQL_CREATE_TRANSACTIONS);
        db.execSQL(CryptoContract.SQL_CREATE_EXCHANGES);
        db.execSQL(CryptoContract.SQL_CREATE_PORTFOLIO_COINS);
        db.execSQL(CryptoContract.SQL_CREATE_NOTIFICATIONS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(CryptoContract.SQL_DELETE_PORTFOLIOS);
        db.execSQL(CryptoContract.SQL_DELETE_COINS);
        db.execSQL(CryptoContract.SQL_DELETE_TRANSACTIONS);
        db.execSQL(CryptoContract.SQL_DELETE_EXCHANGES);
        db.execSQL(CryptoContract.SQL_DELETE_PORTFOLIO_COINS);
        db.execSQL(CryptoContract.SQL_DELETE_NOTIFICATIONS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }



  }