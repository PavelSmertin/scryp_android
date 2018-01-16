package com.start.crypto.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.api.model.PortfolioCoin;
import com.start.crypto.android.api.model.Transaction;
import com.start.crypto.android.data.ColumnsPortfolioCoin;
import com.start.crypto.android.data.CryptoContract;

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
            mTransactionButton.setEnabled(false);

            double price = mPrice;
            if(!mPriceSwitch.isChecked() && mAmount > 0) {
                price = mPrice / mAmount;
            }
            mPresenter.updatePortfolioByTransaction(
                    new PortfolioCoin(argPortfolioId, mCoinId, argExchangeId, mPortfolioCoinOriginal),
                    new Transaction(argPortfolioCoinId, mAmount, price, mDate, mDescription, mBasePrice)
            );
            finish();
        });
    }


    @Override
    protected void initLoaderManager() {
        getSupportLoaderManager().restartLoader(CryptoContract.LOADER_PORTFOLIO_COINS, null, this);
        getSupportLoaderManager().restartLoader(CryptoContract.LOADER_COINS, null, this);
        getSupportLoaderManager().restartLoader(CryptoContract.LOADER_EXCHANGES, null, this);
    }


    @Override
    protected void onPortofolioCoinLoaded(Cursor data) {
        data.moveToNext();
        ColumnsPortfolioCoin.ColumnsMap columnsMap = new ColumnsPortfolioCoin.ColumnsMap(data);
        mPortfolioCoinOriginal = data.getDouble(columnsMap.mColumnOriginal);
        mAmountView.setText(String.format(Locale.US, "%.02f", mPortfolioCoinOriginal));
    }
}
