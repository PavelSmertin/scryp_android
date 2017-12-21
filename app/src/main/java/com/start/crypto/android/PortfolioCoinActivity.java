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
import android.widget.TextView;

import com.start.crypto.android.data.CryptoContract;

import java.util.Locale;

import butterknife.BindView;


public class PortfolioCoinActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>  {


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

    private long mCoinId;

    public static void startActivity(Context context,
                                     long coinId,
                                     String name,
                                     double priceNow,
                                     double original,
                                     double priceOriginal,
                                     double profit24h) {
        Intent intent = new Intent(context, PortfolioCoinActivity.class);
        intent.putExtra("COIN_ID", coinId);
        intent.putExtra("COIN_NAME", name);
        intent.putExtra("PRICE_NOW", priceNow);
        intent.putExtra("ORIGINAL", original);
        intent.putExtra("PRICE_ORIGINAL", priceOriginal);
        intent.putExtra("24H_PROFIT", profit24h);
        context.startActivity(intent);
    }

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_portfolio_coin);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String symbol = "USD";

        mCoinId             = getIntent().getLongExtra("COIN_ID", 0);
        double priceNow         = getIntent().getDoubleExtra("PRICE_NOW", 0);
        double original         = getIntent().getDoubleExtra("ORIGINAL", 0);
        double priceOriginal    = getIntent().getDoubleExtra("PRICE_ORIGINAL", 0);
        double profit24h        = getIntent().getDoubleExtra("24H_PROFIT", 0);
        String coinName         = getIntent().getStringExtra("COIN_NAME");

        setTitle(coinName);

        mAllTimeProfitView.setText(String.format(Locale.US, "%.2f", original * (priceNow - priceOriginal)));
        mSymbolAllTimeProfitView .setText(symbol);
        mAmountView.setText(String.format(Locale.US, "%f %s", original, coinName));
        mBuyPriceView.setText(String.format(Locale.US, "%.2f %s", priceOriginal, symbol));
        mCurrentView.setText(String.format(Locale.US, "%.2f %s", priceNow, symbol));
        mTotalCostView.setText(String.format(Locale.US, "%.2f %s", priceNow * original, symbol));
        mAcqusitionCoastView.setText(String.format(Locale.US, "%.2f %s", priceOriginal, symbol));
        m24hChangeView.setText(String.format(Locale.US, "%.2f %s", profit24h, symbol));


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
        return new CursorLoader(this, CryptoContract.CryptoTransactions.CONTENT_URI, null, CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_ID + "=" + mCoinId, null, null);
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


}
