package com.start.crypto.android.transaction;

import android.content.ContentResolver;

import com.start.crypto.android.api.model.PortfolioCoin;
import com.start.crypto.android.api.model.Transaction;

public class TransactionPresenterBuy extends TransactionPresenterBase {

    public TransactionPresenterBuy(ContentResolver mContentResolver) {
        super(mContentResolver);
    }

    @Override
    public void updatePortfolioByTransaction(PortfolioCoin portfolioCoin, Transaction transaction) {
        mPortfolioCoin = portfolioCoin;
        mTransaction = transaction;
        updateCoin();

        insertTransaction();
    }

    @Override
    protected double getPortfolioCoinOriginal() {
        return mPortfolioCoin.getOriginal() + mTransaction.getAmount();
    }

    @Override
    protected double getPortfolioCoinPrice() {
        double totalOriginal = mPortfolioCoin.getTotalOriginal(); // in base currency
        double transactionSum = mTransaction.getAmount() * mTransaction.getPrice() * mTransaction.getPairBasePrice(); // in base currency
        return (totalOriginal + transactionSum) / (mPortfolioCoin.getOriginal() + mTransaction.getAmount());
    }

}
