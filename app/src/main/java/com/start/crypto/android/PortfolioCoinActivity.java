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

import com.start.crypto.android.data.CryptoContract;
import com.start.crypto.android.utils.KeyboardHelper;

import java.util.Locale;

import butterknife.BindView;


public class PortfolioCoinActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    public static final String EXTRA_PORTFOLIO_COIN_ID  = "portfolio_coin_id";
    public static final String EXTRA_COIN_NAME          = "coin_name";
    public static final String EXTRA_PRICE_NOW          = "price_now";
    public static final String EXTRA_ORIGINAL           = "original";
    public static final String EXTRA_PRICE_ORIGINAL     = "price_original";
    public static final String EXTRA_24H_PROFIT         = "24h_profit";

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

    public static void startActivity(Context context,
                                     long portfolioCoinId,
                                     String name,
                                     double priceNow,
                                     double original,
                                     double priceOriginal,
                                     double profit24h) {
        Intent intent = new Intent(context, PortfolioCoinActivity.class);
        intent.putExtra(EXTRA_PORTFOLIO_COIN_ID,    portfolioCoinId);
        intent.putExtra(EXTRA_COIN_NAME,            name);
        intent.putExtra(EXTRA_PRICE_NOW,            priceNow);
        intent.putExtra(EXTRA_ORIGINAL,             original);
        intent.putExtra(EXTRA_PRICE_ORIGINAL,       priceOriginal);
        intent.putExtra(EXTRA_24H_PROFIT,           profit24h);
        context.startActivity(intent);
    }

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_portfolio_coin);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        argPortfolioCoinId = getIntent().getLongExtra(EXTRA_PORTFOLIO_COIN_ID, 0);
        double priceNow         = getIntent().getDoubleExtra(EXTRA_PRICE_NOW, 0);
        double original         = getIntent().getDoubleExtra(EXTRA_ORIGINAL, 0);
        double priceOriginal    = getIntent().getDoubleExtra(EXTRA_PRICE_ORIGINAL, 0);
        double profit24h        = getIntent().getDoubleExtra(EXTRA_24H_PROFIT, 0);
        String coinName         = getIntent().getStringExtra(EXTRA_COIN_NAME);

        setTitle(coinName);

        mAllTimeProfitView.setText(String.format(Locale.US, "%s", KeyboardHelper.format(original * (priceNow - priceOriginal))));
        mSymbolAllTimeProfitView .setText(CreateTransactionActivity.DEFAULT_SYMBOL);
        mAmountView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(original), coinName));
        mBuyPriceView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(priceOriginal), CreateTransactionActivity.DEFAULT_SYMBOL));
        mCurrentView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(priceNow), CreateTransactionActivity.DEFAULT_SYMBOL));
        mTotalCostView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(priceNow * original), CreateTransactionActivity.DEFAULT_SYMBOL));
        mAcqusitionCoastView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(priceOriginal), CreateTransactionActivity.DEFAULT_SYMBOL));
        m24hChangeView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(profit24h), CreateTransactionActivity.DEFAULT_SYMBOL));


        mAdapter = new TransactionsListAdapter(this, null);
        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        getSupportLoaderManager().restartLoader(0, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id != 0) {
            throw new IllegalArgumentException("no id handled!");
        }
        return new CursorLoader(this, CryptoContract.CryptoTransactions.CONTENT_URI, null, CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_COIN_ID + " = " + argPortfolioCoinId, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() != 0) {
            throw new IllegalArgumentException("no id handled!");
        }

        data.moveToFirst();
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() != 0) {
            throw new IllegalArgumentException("no id handled!");
        }
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.portfolio_coin_edit:
                return true;
            case R.id.portfolio_coin_remove:
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
