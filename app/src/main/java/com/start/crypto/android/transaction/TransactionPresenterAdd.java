package com.start.crypto.android.transaction;

import android.content.ContentResolver;

import com.start.crypto.android.api.model.PortfolioCoin;
import com.start.crypto.android.api.model.Transaction;

public class TransactionPresenterAdd extends TransactionPresenterBase {

    public TransactionPresenterAdd(ContentResolver mContentResolver) {
        super(mContentResolver);
    }

    public void updatePortfolioByTransaction(PortfolioCoin portfolioCoin, Transaction transaction) {
        mPortfolioCoin = portfolioCoin;
        mTransaction = transaction;
        long portfolioCoinId = insertCoin();
        mTransaction.setPortfolioCoinId(portfolioCoinId);

        insertTransaction();
    }

    @Override
    protected double getPortfolioCoinOriginal() {
        return mTransaction.getAmount();
    }

    @Override
    protected double getPortfolioCoinPrice() {
        return mTransaction.getPrice() * mTransaction.getPairBasePrice(); // in base currency
    }

}
