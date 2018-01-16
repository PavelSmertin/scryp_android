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
        updateCoin();
        mPortfolioPairId = insertPair(new PortfolioCoin(portfolioCoin.getPortfolioId(), mTransaction.getPairId(), portfolioCoin.getExchangeId()));
        insertTransaction();
    }

    @Override
    protected double getPortfolioCoinOriginal() {
        return mPortfolioCoin.getOriginal() - mTransaction.getAmount();
    }

    @Override
    protected double getPortfolioCoinPrice() {
        double totalOriginal = mPortfolioCoin.getTotalOriginal();  // in base currency
        double transactionSum = mTransaction.getAmount() * mTransaction.getPrice() * mTransaction.getBasePrice();             // in base currency
        if(mPortfolioCoin.getOriginal() - mTransaction.getAmount() <= 0) {
            return mPortfolioCoin.getPriceOriginal();
        }
        return (totalOriginal - transactionSum) / (mPortfolioCoin.getOriginal() - mTransaction.getAmount());
    }

    @Override
    protected ContentValues createTransactionValues() {
        ContentValues values = super.createTransactionValues();
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_CORRESPOND_ID, mTransaction.getPairId());
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_CURRENTEY_ID, mPortfolioPairId);
        return values;
    }

    private long insertPair(PortfolioCoin portfolioCoin) {
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID, portfolioCoin.getCoinId());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID, portfolioCoin.getPortfolioId());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID, portfolioCoin.getExchangeId());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL, mTransaction.getAmount() * mTransaction.getPrice());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_24H, mTransaction.getBasePrice());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW, mTransaction.getBasePrice());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, mTransaction.getBasePrice());
        Uri uri = mContentResolver.insert(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values);

        return selectId(uri);
    }

}
