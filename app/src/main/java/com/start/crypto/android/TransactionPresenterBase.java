package com.start.crypto.android;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.start.crypto.android.api.model.PortfolioCoin;
import com.start.crypto.android.api.model.Transaction;
import com.start.crypto.android.data.CryptoContract;

import java.util.Calendar;

abstract public class TransactionPresenterBase {

    protected ContentResolver mContentResolver;

    protected PortfolioCoin mPortfolioCoin;
    protected Transaction mTransaction;

    public TransactionPresenterBase(ContentResolver mContentResolver) {
        this.mContentResolver = mContentResolver;
    }

    abstract public void updatePortfolioByTransaction(PortfolioCoin portfolioCoin, Transaction transaction);

    protected void insertTransaction() {
        mContentResolver.insert(CryptoContract.CryptoTransactions.CONTENT_URI, createTransactionValues());
    }

    protected ContentValues createTransactionValues() {
        if(
                mTransaction.getPortfolioCoinId() <= 0 ||
                mPortfolioCoin.getCoinId() <= 0 ||
                mPortfolioCoin.getPortfolioId() <= 0 ||
                mPortfolioCoin.getExchangeId() <= 0
                ){
            throw new IllegalStateException("transaction is not correct");
        }
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_COIN_ID, mTransaction.getPortfolioCoinId());
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_ID,           mPortfolioCoin.getCoinId());
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_ID,      mPortfolioCoin.getPortfolioId());
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_EXCHANGE_ID,       mPortfolioCoin.getExchangeId());
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_AMOUNT,            mTransaction.getAmount());
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PRICE,             mTransaction.getPrice());
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_DATETIME,          mTransaction.getDate());
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_DESCRIPTION,       mTransaction.getDescription());

        return values;
    }

    protected long insertCoin() {
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID,         mPortfolioCoin.getCoinId());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID,    mPortfolioCoin.getPortfolioId());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID,     mPortfolioCoin.getExchangeId());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL,        getPortfolioCoinOriginal());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_24H,       getPortfolioCoinPrice());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW,       getPortfolioCoinPrice());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL,  getPortfolioCoinPrice());
        Uri uri = mContentResolver.insert(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values);

        return selectId(uri);
    }

    protected void updateCoin() {
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID,     mPortfolioCoin.getExchangeId());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL,        getPortfolioCoinOriginal());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL,  getPortfolioCoinPrice());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_UPDATED_AT,      Calendar.getInstance().getTimeInMillis());
        mContentResolver.update(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values, CryptoContract.CryptoPortfolioCoins._ID + " = " + mTransaction.getPortfolioCoinId(), null);
    }

    protected long selectId(Uri uri) {
        Cursor cursor = mContentResolver.query(uri, null, null, null, null);
        if(cursor != null) {
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                int itemColumnIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
                return cursor.getLong(itemColumnIndex);
            }
            cursor.close();
        }
        throw new IllegalStateException("illegal uri");
    }


    abstract protected double getPortfolioCoinOriginal();
    abstract protected double getPortfolioCoinPrice();

}
