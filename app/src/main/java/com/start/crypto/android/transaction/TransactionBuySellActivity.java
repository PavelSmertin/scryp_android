package com.start.crypto.android.transaction;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.widget.Button;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.R;
import com.start.crypto.android.api.model.AutocompleteItem;
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
                            getPrice(),
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
                            getPrice(),
                            mDate,
                            mDescription,
                            mBasePrice,
                            mCoinPrice
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
        data.moveToFirst();
        ColumnsPortfolioCoin.ColumnsMap columnsPortfolioCoinMap = new ColumnsPortfolioCoin.ColumnsMap(data);
        mPortfolioCoinOriginal = data.getDouble(columnsPortfolioCoinMap.mColumnOriginal);
        mPortfolioCoinPriceOriginal = data.getDouble(columnsPortfolioCoinMap.mColumnPriceOriginal);
        mAmountMax = mPortfolioCoinOriginal;

        ColumnsCoin.ColumnsMap columnsCoinMap = new ColumnsCoin.ColumnsMap(data);
        setCoin(new AutocompleteItem(
                data.getLong(columnsPortfolioCoinMap.mColumnCoinId),
                data.getString(columnsCoinMap.mColumnSymbol),
                data.getString(columnsCoinMap.mColumnName))
        );

        mCoinComplete.setEnabled(false);

        if(mPortfolioCoinOriginal < 0.5D) {
            mAmountView.setText(String.format(Locale.US, "%.08f", mPortfolioCoinOriginal));
        } else if(mPortfolioCoinOriginal < 1D){
            mAmountView.setText(String.format(Locale.US, "%.05f", mPortfolioCoinOriginal));
        } else {
            mAmountView.setText(String.format(Locale.US, "%.02f", mPortfolioCoinOriginal));
        }

    }

    @Override
    protected void setCoin(AutocompleteItem coin) {
        super.setCoin(coin);
        mBuyTransactionButton.setText(String.format(Locale.getDefault(), getString(R.string.transaction_buy_button), coin.getSymbol()));
        mSellTransactionButton.setText(String.format(Locale.getDefault(), getString(R.string.transaction_sell_button), coin.getSymbol()));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected int getScrollBottom() {
        return mBuyTransactionButton.getBottom();
    }
}
