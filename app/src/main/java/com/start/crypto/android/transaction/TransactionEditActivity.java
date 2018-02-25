package com.start.crypto.android.transaction;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.R;
import com.start.crypto.android.api.model.AutocompleteItem;
import com.start.crypto.android.api.model.PortfolioCoin;
import com.start.crypto.android.api.model.Transaction;
import com.start.crypto.android.data.ColumnsCoin;
import com.start.crypto.android.data.ColumnsPortfolioCoin;
import com.start.crypto.android.utils.KeyboardHelper;

import java.util.Locale;

public class TransactionEditActivity extends TransactionAddActivity {

    private TransactionPresenterEdit mPresenter;

    private double mPortfolioCoinOriginal;

    public static void start(Context context, long portfolioId, long portfolioCoinId, long exchangeId) {
        Intent starter = new Intent(context, TransactionEditActivity.class);
        starter.putExtra(EXTRA_PORTFOLIO_ID, portfolioId);
        starter.putExtra(EXTRA_PORTFOLIO_COIN_ID, portfolioCoinId);
        starter.putExtra(EXTRA_EXCHANGE_ID, exchangeId);
        context.startActivity(starter);
    }

    @Override
    protected void setupLayout() {
        setContentView(R.layout.transaction_activity_edit);
    }

    protected void bindActions() {
        mPresenter = new TransactionPresenterEdit(getContentResolver());

        if(mTransactionButton == null) {
            return;
        }
        RxView.clicks(mTransactionButton).subscribe(v -> {
            createTransaction();
        });
    }

    @Override
    protected void createTransaction() {
        mTransactionButton.setEnabled(false);
        finish();
        mPresenter.updatePortfolioByTransaction(
                new PortfolioCoin(argPortfolioId, mCoinId, argExchangeId, mPortfolioCoinOriginal),
                new Transaction(argPortfolioCoinId, mAmount, getPrice(), mDate, mDescription, mBasePrice)
        );
    }

    @Override
    protected void initLoaderManager() {
        getSupportLoaderManager().restartLoader(LOADER_PORTFOLIO_COINS, null, this);
        getSupportLoaderManager().restartLoader(LOADER_COINS, null, this);
        getSupportLoaderManager().restartLoader(LOADER_EXCHANGES, null, this);
    }


    @Override
    protected void onPortofolioCoinLoaded(Cursor data) {
        data.moveToFirst();
        ColumnsPortfolioCoin.ColumnsMap columnsPortfolioCoinMap = new ColumnsPortfolioCoin.ColumnsMap(data);
        mPortfolioCoinOriginal      = data.getDouble(columnsPortfolioCoinMap.mColumnOriginal);

        // init price
        double portfolioCoinPriceOriginal = data.getDouble(columnsPortfolioCoinMap.mColumnPriceOriginal);
        mPricePerCoin = portfolioCoinPriceOriginal;
        mPriceInTotal = portfolioCoinPriceOriginal * mPortfolioCoinOriginal;
        if(mPriceSwitch.isChecked()) {
            mPriceView.setText(KeyboardHelper.format(mPricePerCoin));
        } else {
            mPriceView.setText(KeyboardHelper.format(mPriceInTotal));
        }


        // init amount
        if(mPortfolioCoinOriginal < 0.5D) {
            mAmountView.setText(String.format(Locale.US, "%.08f", mPortfolioCoinOriginal));
        } else if(mPortfolioCoinOriginal < 1D){
            mAmountView.setText(String.format(Locale.US, "%.05f", mPortfolioCoinOriginal));
        } else {
            mAmountView.setText(String.format(Locale.US, "%.02f", mPortfolioCoinOriginal));
        }

        // init coin
        ColumnsCoin.ColumnsMap columnsCoinMap = new ColumnsCoin.ColumnsMap(data);
        setCoin(new AutocompleteItem(
                data.getLong(columnsPortfolioCoinMap.mColumnCoinId),
                data.getString(columnsCoinMap.mColumnSymbol),
                data.getString(columnsCoinMap.mColumnName))
        );
        mCoinComplete.setEnabled(false);

    }

    @Override
    protected int getScrollBottom() {
        return mTransactionButton.getBottom();
    }
}
