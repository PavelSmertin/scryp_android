package com.start.crypto.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.start.crypto.android.data.ColumnsCoin;
import com.start.crypto.android.data.ColumnsPortfolioCoin;
import com.start.crypto.android.data.CryptoContract;
import com.start.crypto.android.utils.KeyboardHelper;

import java.util.Locale;

import butterknife.BindView;


public class PortfolioCoinActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    public static final String EXTRA_PORTFOLIO_COIN_ID  = "portfolio_coin_id";

    @BindView(R.id.value_all_time)              TextView mAllTimeProfitView;
    @BindView(R.id.symbol_all_time)             TextView mSymbolAllTimeProfitView;
    @BindView(R.id.value_amount)                TextView mAmountView;
    @BindView(R.id.value_buy_price)             TextView mBuyPriceView;
    @BindView(R.id.value_current_value)         TextView mCurrentView;
    @BindView(R.id.value_total_coast)           TextView mTotalCostView;
    @BindView(R.id.value_acqusition_coast)      TextView mAcqusitionCoastView;
    @BindView(R.id.value_24h_change)            TextView m24hChangeView;

    @BindView(R.id.transactions)                RecyclerView mRecyclerView;

    private TransactionsListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private long argPortfolioCoinId;

    private long mCoinId;
    private long mExchangeId;
    private String mCoinSymbol;
    private long mPortfolioId;


    public static void start(Context context, long portfolioCoinId) {
        Intent intent = new Intent(context, PortfolioCoinActivity.class);
        intent.putExtra(EXTRA_PORTFOLIO_COIN_ID,    portfolioCoinId);
        context.startActivity(intent);
    }

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_portfolio_coin);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        argPortfolioCoinId      = getIntent().getLongExtra(EXTRA_PORTFOLIO_COIN_ID, 0);

        mAdapter = new TransactionsListAdapter(this, null);
        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        initLoaderManager();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == CryptoContract.LOADER_PORTFOLIO_COINS) {
            return getPortfolioCoinsLoader();
        }

        if (id == CryptoContract.LOADER_TRANSACTIONS) {
            return getTransactionsLoader();
        }
        throw new IllegalArgumentException("no id handled!");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (loader.getId() == CryptoContract.LOADER_PORTFOLIO_COINS) {
            if(data == null || data.getCount() == 0) {
                return;
            }
            data.moveToNext();
            ColumnsPortfolioCoin.ColumnsMap columnsMap = new ColumnsPortfolioCoin.ColumnsMap(data);
            ColumnsCoin.ColumnsMap columnsCoinMap = new ColumnsCoin.ColumnsMap(data);

            mCoinId = data.getLong(columnsMap.mColumnCoinId);
            mExchangeId = data.getLong(columnsMap.mColumnExchangeId);
            mCoinSymbol = data.getString(columnsCoinMap.mColumnSymbol);
            mPortfolioId = data.getLong(columnsMap.mColumnPortfolioId);

            double original = data.getDouble(columnsMap.mColumnOriginal);
            double priceOriginal = data.getDouble(columnsMap.mColumnPriceOriginal);
            double priceNow = data.getDouble(columnsMap.mColumnPriceNow);
            double price24h = data.getDouble(columnsMap.mColumnPrice24h);
            double profit24h = original * (priceNow - price24h);

            setTitle(mCoinSymbol);

            mAllTimeProfitView.setText(String.format(Locale.US, "%s", KeyboardHelper.format(original * (priceNow - priceOriginal))));
            mSymbolAllTimeProfitView .setText(CreateTransactionActivity.DEFAULT_SYMBOL);
            mAmountView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(original), mCoinSymbol));
            mBuyPriceView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(priceOriginal), CreateTransactionActivity.DEFAULT_SYMBOL));
            mCurrentView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(priceNow), CreateTransactionActivity.DEFAULT_SYMBOL));
            mTotalCostView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(priceNow * original), CreateTransactionActivity.DEFAULT_SYMBOL));
            mAcqusitionCoastView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(priceOriginal * original), CreateTransactionActivity.DEFAULT_SYMBOL));
            m24hChangeView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(profit24h), CreateTransactionActivity.DEFAULT_SYMBOL));

            return;
        }

        if (loader.getId() == CryptoContract.LOADER_TRANSACTIONS) {
            mAdapter.changeCursor(data);
            return;
        }

        throw new IllegalArgumentException("no id handled!");

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == CryptoContract.LOADER_PORTFOLIO_COINS) {
            return;
        }
        if (loader.getId() == CryptoContract.LOADER_TRANSACTIONS) {
            if (mAdapter != null) {
                mAdapter.changeCursor(null);
            }
            return;
        }

    }

    private Loader<Cursor> getPortfolioCoinsLoader() {
        return new CursorLoader(
                this,
                CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                null,
                CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins._ID + " = " + argPortfolioCoinId,
                null,
                null
        );
    }
    private Loader<Cursor> getTransactionsLoader() {
        return new CursorLoader(
                this,
                CryptoContract.CryptoTransactions.CONTENT_URI,
                null,
                CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_COIN_ID + " = " + argPortfolioCoinId,
                null,
                null
        );
    }

    private void initLoaderManager() {
        getSupportLoaderManager().restartLoader(CryptoContract.LOADER_PORTFOLIO_COINS, null, this);
        getSupportLoaderManager().restartLoader(CryptoContract.LOADER_TRANSACTIONS, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.portfolio_coin_edit:
                CreateTransactionActivity.start(
                        this,
                        mPortfolioId,
                        argPortfolioCoinId,
                        mCoinId,
                        mCoinSymbol,
                        mExchangeId,
                        TransactionType.EDIT);
                return true;
            case R.id.portfolio_coin_remove:
                getContentResolver().delete(
                        CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                        CryptoContract.CryptoPortfolioCoins._ID + "=" + argPortfolioCoinId,
                        null
                );
                getContentResolver().delete(
                        CryptoContract.CryptoTransactions.CONTENT_URI,
                        CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_COIN_ID + "=" + argPortfolioCoinId,
                        null
                );
                getContentResolver().delete(
                        CryptoContract.CryptoNotifications.CONTENT_URI,
                        CryptoContract.CryptoNotifications.COLUMN_NAME_COIN_ID + "=" + mCoinId,
                        null
                );
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.portfolio_coin, menu);
        return true;
    }


}
