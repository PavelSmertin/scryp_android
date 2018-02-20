package com.start.crypto.android;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.start.crypto.android.api.model.PortfolioCoin;
import com.start.crypto.android.api.model.Transaction;
import com.start.crypto.android.data.CryptoContract;


public class TransactionPresenterSell extends TransactionPresenterBase {

    private long mPortfolioPairId;

    public TransactionPresenterSell(ContentResolver mContentResolver) {
        super(mContentResolver);
    }

    @Override
    public void updatePortfolioByTransaction(PortfolioCoin portfolioCoin, Transaction transaction) {
        mPortfolioCoin = portfolioCoin;
        mTransaction = transaction;
        if(getPortfolioCoinOriginal() == 0) {
            removeCoin();
        } else {
            updateCoin();
        }
        mPortfolioPairId = insertPair(new PortfolioCoin(portfolioCoin.getPortfolioId(), mTransaction.getPairId(), portfolioCoin.getExchangeId()));
        insertTransaction();
    }

    @Override
    protected double getPortfolioCoinOriginal() {
        return mPortfolioCoin.getOriginal() - mTransaction.getAmount();
    }

    @Override
    protected double getPortfolioCoinPrice() {
        return mPortfolioCoin.getPriceOriginal();
    }

    @Override
    protected ContentValues createTransactionValues() {
        if(mTransaction.getPairId() <= 0 || mPortfolioPairId <= 0) {
            throw new IllegalStateException("transaction is not correct");
        }
        ContentValues values = super.createTransactionValues();
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_CORRESPOND_ID, mTransaction.getPairId());
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_PAIR_ID, mPortfolioPairId);
        return values;
    }

    private long insertPair(PortfolioCoin portfolioCoin) {
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID, portfolioCoin.getCoinId());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID, portfolioCoin.getPortfolioId());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID, portfolioCoin.getExchangeId());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL, mTransaction.getPrice());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_24H, mTransaction.getBasePrice());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW, mTransaction.getBasePrice());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, mTransaction.getAmount() * mTransaction.getCoinPrice() / mTransaction.getPrice());
        Uri uri = mContentResolver.insert(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values);

        return selectId(uri);
    }


    private  void removeCoin() {
        mContentResolver.delete(
                CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                CryptoContract.CryptoPortfolioCoins._ID + "=" + mTransaction.getPortfolioCoinId(),
                null
        );
        mContentResolver.delete(
                CryptoContract.CryptoTransactions.CONTENT_URI,
                CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_COIN_ID + "=" + mTransaction.getPortfolioCoinId(),
                null
        );
        mContentResolver.delete(
                CryptoContract.CryptoNotifications.CONTENT_URI,
                CryptoContract.CryptoNotifications.COLUMN_NAME_COIN_ID + "=" + mPortfolioCoin.getCoinId(),
                null
        );
    }
}
