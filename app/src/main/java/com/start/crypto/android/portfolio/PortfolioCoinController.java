package com.start.crypto.android.portfolio;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.start.crypto.android.BaseController;
import com.start.crypto.android.DialogController;
import com.start.crypto.android.DialogView;
import com.start.crypto.android.R;
import com.start.crypto.android.data.ColumnsCoin;
import com.start.crypto.android.data.ColumnsExchange;
import com.start.crypto.android.data.ColumnsPortfolioCoin;
import com.start.crypto.android.data.CryptoContract;
import com.start.crypto.android.transaction.TransactionAddActivity;
import com.start.crypto.android.transaction.TransactionEditActivity;
import com.start.crypto.android.transaction.TransactionsListAdapter;
import com.start.crypto.android.utils.BundleBuilder;
import com.start.crypto.android.utils.KeyboardHelper;

import java.util.Locale;

import butterknife.BindView;

public class PortfolioCoinController extends BaseController implements LoaderManager.LoaderCallbacks<Cursor>, DialogView {


    private static final int LOADER_PORTFOLIO_COINS = 201;
    private static final int LOADER_TRANSACTIONS = 202;


    @BindView(R.id.value_all_time)              TextView mAllTimeProfitView;
    @BindView(R.id.symbol_all_time)             TextView mSymbolAllTimeProfitView;
    @BindView(R.id.percent_all_time)            TextView mPercentAllTimeProfitView;
    @BindView(R.id.value_amount)                TextView mAmountView;
    @BindView(R.id.value_buy_price)             TextView mBuyPriceView;
    @BindView(R.id.value_current_value)         TextView mCurrentView;
    @BindView(R.id.value_total_coast)           TextView mTotalCostView;
    @BindView(R.id.value_price_)                TextView mPriceView;
    @BindView(R.id.value_24h_change)            TextView m24hChangeView;
    @BindView(R.id.value_exchange)              TextView mExchangeView;
    @BindView(R.id.transactions)                RecyclerView mRecyclerView;

    private TransactionsListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private long argPortfolioCoinId;

    private long mCoinId;
    private long mExchangeId;
    private String mCoinSymbol;
    private long mPortfolioId;
    private String mExchange;



    public PortfolioCoinController(long portfolioId) {
        this(new BundleBuilder(new Bundle())
                .putLong(PortfolioCoinActivity.EXTRA_PORTFOLIO_COIN_ID, portfolioId)
                .build());
    }

    public PortfolioCoinController(Bundle args) {
        super(args);
    }


    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.portfolio_activity_portfolio_coin, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        setHasOptionsMenu(true);



        argPortfolioCoinId      = getArgs().getLong(PortfolioCoinActivity.EXTRA_PORTFOLIO_COIN_ID, 0);

        mAdapter = new TransactionsListAdapter(getActivity(), null);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setNestedScrollingEnabled(false);

        initLoaderManager();


    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == LOADER_PORTFOLIO_COINS) {
            return getPortfolioCoinsLoader();
        }

        if (id == LOADER_TRANSACTIONS) {
            return getTransactionsLoader();
        }
        throw new IllegalArgumentException("no id handled!");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (loader.getId() == LOADER_PORTFOLIO_COINS) {
            if(data == null || data.getCount() == 0) {
                return;
            }
            data.moveToFirst();
            ColumnsPortfolioCoin.ColumnsMap columnsMap      = new ColumnsPortfolioCoin.ColumnsMap(data);
            ColumnsCoin.ColumnsMap columnsCoinMap           = new ColumnsCoin.ColumnsMap(data);
            ColumnsExchange.ColumnsMap columnsExchangeMap   = new ColumnsExchange.ColumnsMap(data);

            mCoinId = data.getLong(columnsMap.mColumnCoinId);
            mExchangeId = data.getLong(columnsMap.mColumnExchangeId);
            mCoinSymbol = data.getString(columnsCoinMap.mColumnSymbol);
            mExchange   = data.getString(columnsExchangeMap.mColumnName);
            mPortfolioId = data.getLong(columnsMap.mColumnPortfolioId);

            double original             = data.getDouble(columnsMap.mColumnOriginal);
            double priceOriginal        = data.getDouble(columnsMap.mColumnPriceOriginal);
            double priceNow             = data.getDouble(columnsMap.mColumnPriceNow);
            double price24h             = data.getDouble(columnsMap.mColumnPrice24h);
            double profit24h            = (priceNow - price24h) * 100 / price24h;
            double profitAll            = original * (priceNow - priceOriginal);
            double profitAllPercent = 0;
            if(priceOriginal > 0) {
                profitAllPercent     = (priceNow - priceOriginal) * 100 / priceOriginal;
            }

            getActivity().setTitle(mCoinSymbol);

            mAllTimeProfitView.setText(String.format(Locale.US, "%s", KeyboardHelper.format(profitAll)));
            mSymbolAllTimeProfitView .setText(TransactionAddActivity.DEFAULT_SYMBOL);
            if(priceOriginal > 0) {
                mPercentAllTimeProfitView.setText(String.format(Locale.US, "(%.2f%%)", profitAllPercent));
            }

            if (profitAll < 0) {
                mAllTimeProfitView.setTextColor(getResources().getColor(R.color.colorDownValue));
                mAllTimeProfitView.setTextColor(getResources().getColor(R.color.colorDownValue));
                mPercentAllTimeProfitView.setTextColor(getResources().getColor(R.color.colorDownValue));
                mPercentAllTimeProfitView.setTextColor(getResources().getColor(R.color.colorDownValue));
            } else {
                mAllTimeProfitView.setTextColor(getResources().getColor(R.color.colorUpValue));
                mAllTimeProfitView.setTextColor(getResources().getColor(R.color.colorUpValue));
                mPercentAllTimeProfitView.setTextColor(getResources().getColor(R.color.colorUpValue));
                mPercentAllTimeProfitView.setTextColor(getResources().getColor(R.color.colorUpValue));
            }

            mAmountView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(original), mCoinSymbol));
            mPriceView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(priceNow), TransactionAddActivity.DEFAULT_SYMBOL));
            m24hChangeView.setText(String.format(Locale.US, "%.2f%%", profit24h));

            if (profit24h < 0) {
                m24hChangeView.setTextColor(getResources().getColor(R.color.colorDownValue));
                m24hChangeView.setTextColor(getResources().getColor(R.color.colorDownValue));
            } else {
                m24hChangeView.setTextColor(getResources().getColor(R.color.colorUpValue));
                m24hChangeView.setTextColor(getResources().getColor(R.color.colorUpValue));
            }

            mCurrentView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(priceNow * original), TransactionAddActivity.DEFAULT_SYMBOL));

            mBuyPriceView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(priceOriginal), TransactionAddActivity.DEFAULT_SYMBOL));
            mTotalCostView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.format(priceOriginal * original), TransactionAddActivity.DEFAULT_SYMBOL));

            mExchangeView.setText(mExchange);

            return;
        }

        if (loader.getId() == LOADER_TRANSACTIONS) {
            mAdapter.changeCursor(data);
            return;
        }

        throw new IllegalArgumentException("no id handled!");

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_PORTFOLIO_COINS) {
            return;
        }
        if (loader.getId() == LOADER_TRANSACTIONS) {
            if (mAdapter != null) {
                mAdapter.changeCursor(null);
            }
            return;
        }

    }


    private Loader<Cursor> getPortfolioCoinsLoader() {
        return new CursorLoader(
                getActivity(),
                CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                null,
                CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins._ID + " = " + argPortfolioCoinId,
                null,
                null
        );
    }
    private Loader<Cursor> getTransactionsLoader() {
        return new CursorLoader(
                getActivity(),
                CryptoContract.CryptoTransactions.CONTENT_URI,
                null,
                CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_COIN_ID + " = " + argPortfolioCoinId,
                null,
                null
        );
    }

    private void initLoaderManager() {

        ((AppCompatActivity)getActivity()).getSupportLoaderManager().restartLoader(LOADER_PORTFOLIO_COINS, null, this);
        ((AppCompatActivity)getActivity()).getSupportLoaderManager().restartLoader(LOADER_TRANSACTIONS, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.portfolio_coin_edit:
                TransactionEditActivity.start(
                        getActivity(),
                        mPortfolioId,
                        argPortfolioCoinId,
                        mExchangeId
                );
                return true;
            case R.id.portfolio_coin_remove:
                DialogController dialogController = new DialogController(getResources().getString(R.string.all_clear_text), "");
                dialogController.setTargetController(this);

                ControllerChangeHandler pushHandler = new FadeChangeHandler(false);
                ControllerChangeHandler popHandler = new FadeChangeHandler();
                getRouter().pushController(RouterTransaction.with(dialogController)
                        .pushChangeHandler(pushHandler)
                        .popChangeHandler(popHandler)
                );

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.portfolio_coin, menu);
    }

    @Override
    public void onOk() {
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_REMOVED, true);

        getActivity().getContentResolver().update(
                CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                values,
                CryptoContract.CryptoPortfolioCoins._ID + "=" + argPortfolioCoinId,
                null
        );

        getActivity().finish();
    }

    @Override
    public void onCancel() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((AppCompatActivity)getActivity()).getSupportLoaderManager().destroyLoader(LOADER_PORTFOLIO_COINS);
        ((AppCompatActivity)getActivity()).getSupportLoaderManager().destroyLoader(LOADER_TRANSACTIONS);
    }
}