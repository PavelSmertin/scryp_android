package com.start.crypto.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Button;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.api.model.Coin;
import com.start.crypto.android.api.model.PortfolioCoin;
import com.start.crypto.android.api.model.Transaction;
import com.start.crypto.android.data.ColumnsCoin;
import com.start.crypto.android.data.ColumnsPortfolioCoin;

import java.util.Locale;

import butterknife.BindView;


public class TransactionBuySellActivity extends TransactionEditActivity {


    @BindView(R.id.buy_transaction) Button mBuyTransactionButton;
    @BindView(R.id.sell_transaction) Button mSellTransactionButton;

    private TransactionPresenterBuy     mPresenterBuy;
    private TransactionPresenterSell    mPresenterSell;

    private double mPortfolioCoinOriginal;
    private double mPortfolioCoinPriceOriginal;
    private double mAmountMax;



    public static void start(Context context, long portfolioId, long portfolioCoinId, long exchangeId) {
        Intent starter = new Intent(context, TransactionBuySellActivity.class);
        starter.putExtra(EXTRA_PORTFOLIO_ID, portfolioId);
        starter.putExtra(EXTRA_PORTFOLIO_COIN_ID, portfolioCoinId);
        starter.putExtra(EXTRA_EXCHANGE_ID, exchangeId);
        context.startActivity(starter);
    }

    @Override
    protected void setupLayout() {
        setContentView(R.layout.transaction_activity_buy_sell);
    }

    @Override
    protected void bindActions() {
        mPresenterBuy = new TransactionPresenterBuy(getContentResolver());
        mPresenterSell = new TransactionPresenterSell(getContentResolver());

        RxView.clicks(mBuyTransactionButton).subscribe(v -> {
            mBuyTransactionButton.setEnabled(false);
            mSellTransactionButton.setEnabled(false);

            double price = mPrice;
            if(!mPriceSwitch.isChecked() && mAmount > 0) {
                price = mPrice / mAmount;
            }
            mPresenterBuy.updatePortfolioByTransaction(
                    new PortfolioCoin(
                            argPortfolioId,
                            mCoinId,
                            argExchangeId,
                            mPortfolioCoinOriginal,
                            mPortfolioCoinPriceOriginal
                    ),
                    new Transaction(
                            argPortfolioCoinId,
                            mAmount,
                            price,
                            mDate,
                            mDescription,
                            mBasePrice
                    )
            );

            finish();
        });

        RxView.clicks(mSellTransactionButton).subscribe(v -> {
            if(!(mAmount > 0 && (mAmount <= mAmountMax || mAmountMax <= 0))) {
                Toast.makeText(getBaseContext(), String.format(Locale.US, getString(R.string.transaction_amount_error), mAmountMax), Toast.LENGTH_SHORT).show();
                return;
            }

            double price = mPrice;
            if(!mPriceSwitch.isChecked() && mAmount > 0) {
                price = mPrice / mAmount;
            }

            mBuyTransactionButton.setEnabled(false);
            mSellTransactionButton.setEnabled(false);
            mPresenterSell.updatePortfolioByTransaction(
                    new PortfolioCoin(
                            argPortfolioId,
                            mCoinId,
                            argExchangeId,
                            mPortfolioCoinOriginal,
                            mPortfolioCoinPriceOriginal
                    ),
                    new Transaction(
                            argPortfolioCoinId,
                            mCurrenteyId,
                            mAmount,
                            price,
                            mDate,
                            mDescription,
                            mBasePrice
                    )
            );

            finish();
        });
    }

    @Override
    protected void onValid(boolean res) {
        mBuyTransactionButton.setEnabled(res);
        mSellTransactionButton.setEnabled(res);
    }

    @Override
    protected void onPortofolioCoinLoaded(Cursor data) {
        data.moveToNext();
        ColumnsPortfolioCoin.ColumnsMap columnsMap = new ColumnsPortfolioCoin.ColumnsMap(data);
        mPortfolioCoinOriginal = data.getDouble(columnsMap.mColumnOriginal);
        mPortfolioCoinPriceOriginal = data.getDouble(columnsMap.mColumnPriceOriginal);
        mAmountMax = mPortfolioCoinOriginal;

        ColumnsCoin.ColumnsMap columnsCoinMap = new ColumnsCoin.ColumnsMap(data);
        setCoin(new Coin(
                data.getLong(columnsCoinMap.mColumnId),
                data.getString(columnsCoinMap.mColumnSymbol),
                data.getString(columnsCoinMap.mColumnName))
        );

        mCoinComplete.setEnabled(false);
    }


}
