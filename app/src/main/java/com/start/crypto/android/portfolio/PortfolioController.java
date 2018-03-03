package com.start.crypto.android.portfolio;


import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bluelinelabs.conductor.rxlifecycle2.ControllerEvent;
import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.BaseController;
import com.start.crypto.android.ControllerPageTitle;
import com.start.crypto.android.CryptoApp;
import com.start.crypto.android.R;
import com.start.crypto.android.account.AuthView;
import com.start.crypto.android.account.SigninActivity;
import com.start.crypto.android.account.UserActivity;
import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.start.crypto.android.api.RestClientMinApi;
import com.start.crypto.android.api.model.CoinResponse;
import com.start.crypto.android.api.model.CoinsResponse;
import com.start.crypto.android.api.model.ExchangeResponse;
import com.start.crypto.android.api.model.PriceMultiFullResponse;
import com.start.crypto.android.data.ColumnsCoin;
import com.start.crypto.android.data.ColumnsExchange;
import com.start.crypto.android.data.ColumnsPortfolioCoin;
import com.start.crypto.android.data.CryptoContract;
import com.start.crypto.android.data.DBHelper;
import com.start.crypto.android.sync.SyncPresenter;
import com.start.crypto.android.transaction.TransactionAddActivity;
import com.start.crypto.android.utils.KeyboardHelper;
import com.start.crypto.android.utils.PreferencesHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class PortfolioController extends BaseController implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        ControllerPageTitle {

    private static final String STATE_DIALOG = "state_dialog";

    private static final int LOADER_PORTFOLIO_COINS_ID = 301;

    private static final boolean DUMP_DB = false;

    public static final int REQUEST_USER_ACCOUNT = 301;


    @BindView(R.id.coins_list)                  RecyclerView mRecyclerView;
    @BindView(R.id.add_transaction)             FloatingActionButton addTransactionView;
    @BindView(R.id.pre_insert)                  FloatingActionButton preInsertView;
    @BindView(R.id.app_bar)                     AppBarLayout mAppBarLayout;


    @BindView(R.id.swipe_refresh)               SwipeRefreshLayout mSwipeRefresh;

    @BindView(R.id.portfolio_current_value)         TextView mPortfolioCurrentValue;
    @BindView(R.id.portfolio_current_value_unit)    TextView mPortfolioCurrentValueUnit;
    @BindView(R.id.portfolio_profit_24h)            TextView mPortfolioProfit24h;
    @BindView(R.id.portfolio_profit_24h_unit)       TextView mPortfolioProfit24hUnit;
    @BindView(R.id.portfolio_original_value)        TextView mPortfolioOriginalValue;
    @BindView(R.id.portfolio_original_value_unit)   TextView mPortfolioOriginalValueUnit;
    @BindView(R.id.portfolio_profit_all)            TextView mPortfolioProfitAll;
    @BindView(R.id.portfolio_profit_all_unit)       TextView mPortfolioProfitAllUnit;


    private AlertDialog mAlertDialog;

    private PortfolioCoinsListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private HashMap<String, Long> mCoinsForRefresh = new HashMap<>();
    private ArrayList<String> mExchangesForRefresh = new ArrayList<>();

    private SyncPresenter mSyncPresenter;

    private Socket mSocket;
    private OkHttpClient mClient;

    private AccountManager mAccountManager;

    private PublishSubject<Boolean> mAuthButtonSubject = PublishSubject.create();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    @NonNull @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.portfolio_controller, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        setHasOptionsMenu(true);

        mAppBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if(verticalOffset == 0 && !addTransactionView.isShown()) {
                addTransactionView.show();
            } else if (verticalOffset != 0 && addTransactionView.isShown()){
                addTransactionView.hide();
            }
        });
        mClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        CryptoApp app = (CryptoApp) getActivity().getApplication();
        mSocket = app.getSocket();

        mSyncPresenter = new SyncPresenter(getActivity().getContentResolver());

        mAdapter = new PortfolioCoinsListAdapter(getActivity(), null);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        initLoaderManager();

        long portfolioId = DUMP_DB ? 1 : selectPortfolioId();

        if(DUMP_DB) {
            preInsertView.setVisibility(View.VISIBLE);
        } else {
            preInsertView.setVisibility(View.GONE);
        }
        RxView.clicks(addTransactionView).subscribe(success -> TransactionAddActivity.start(getActivity(), portfolioId));
        RxView.clicks(preInsertView).subscribe(success -> {
            mSwipeRefresh.setRefreshing(true);
            reset();
            mSwipeRefresh.setRefreshing(false);
        });

        mSwipeRefresh.setOnRefreshListener(this);

        mAccountManager = AccountManager.get(getActivity());

//        Account[] accounts = mAccountManager.getAccountsByType( SigninActivity.ACCOUNT_TYPE);
//        if (accounts.length != 0) {
//            String token = mAccountManager.peekAuthToken(accounts[0], SigninActivity.AUTHTOKEN_TYPE_FULL_ACCESS);
//            if (token != null && PreferencesHelper.getInstance().getLogin() != null) {
//                preInsertView.setVisibility(View.GONE);
//            }
//        }

        compositeDisposable.add(
                mAuthButtonSubject.observeOn(AndroidSchedulers.mainThread())
                        .subscribe(res -> {
                            resetMenu();
                        })
        );

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_MESSAGE, onDisconnect);
        mSocket.on("SubAdd", onSubAdd);
        mSocket.on("message", onSubAdd);

        mSocket.io().on(Manager.EVENT_TRANSPORT, args -> {
            Transport transport = (Transport)args[0];

            transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("DEBUG_INFO", "EVENT_REQUEST_HEADERS");
//                        @SuppressWarnings("unchecked")
//                        Map<String, List<String>> headers = (Map<String, List<String>>)args[0];
//                        // modify request headers
//                        headers.put("Cookie", Arrays.asList("foo=1;"));
                }
            });
            transport.on(Transport.EVENT_PACKET, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("DEBUG_INFO", "EVENT_PACKET");
//                        @SuppressWarnings("unchecked")
//                        Map<String, List<String>> headers = (Map<String, List<String>>)args[0];
//                        // modify request headers
//                        headers.put("Cookie", Arrays.asList("foo=1;"));
                }
            });
            transport.on(Transport.EVENT_CLOSE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("DEBUG_INFO", "EVENT_CLOSE");
//                        @SuppressWarnings("unchecked")
//                        Map<String, List<String>> headers = (Map<String, List<String>>)args[0];
//                        // modify request headers
//                        headers.put("Cookie", Arrays.asList("foo=1;"));
                }
            });
            transport.on(Transport.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("DEBUG_INFO", "EVENT_ERROR");
//                        @SuppressWarnings("unchecked")
//                        Map<String, List<String>> headers = (Map<String, List<String>>)args[0];
//                        // modify request headers
//                        headers.put("Cookie", Arrays.asList("foo=1;"));
                }
            });
            transport.on(Transport.EVENT_DRAIN, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("DEBUG_INFO", "EVENT_DRAIN");
//                        @SuppressWarnings("unchecked")
//                        Map<String, List<String>> headers = (Map<String, List<String>>)args[0];
//                        // modify request headers
//                        headers.put("Cookie", Arrays.asList("foo=1;"));
                }
            });
            transport.on(Transport.EVENT_RESPONSE_HEADERS, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("DEBUG_INFO", "EVENT_RESPONSE_HEADERS");
//                        @SuppressWarnings("unchecked")
//                        Map<String, List<String>> headers = (Map<String, List<String>>)args[0];
//                        // access response headers
//                        String cookie = headers.get("Set-Cookie").get(0);
                }
            });
        });

        //mSocket.connect();

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean showDialog = savedInstanceState.getBoolean(STATE_DIALOG);
        if (showDialog) {
            showAccountPicker(SigninActivity.AUTHTOKEN_TYPE_FULL_ACCESS);
        }
    }

    @Override
    protected void onActivityResumed(@NonNull Activity activity) {
        super.onActivityResumed(activity);
        mSwipeRefresh.setRefreshing(true);
        refreshPrices();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == LOADER_PORTFOLIO_COINS_ID) {
            return new CursorLoader(
                    getActivity(),
                    CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                    null,
                    CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_REMOVED + " != 1 ",
                    null,
                    CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_CREATED_AT + " ASC"
            );
        }
        throw new IllegalArgumentException("no id handled!");

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() != LOADER_PORTFOLIO_COINS_ID) {
            throw new IllegalArgumentException("no id handled!");
        }

        calculatePortfolioValues(data);
        if(PreferencesHelper.getInstance().getLogin() != null) {
            mSyncPresenter.triggerRefresh(PreferencesHelper.getInstance().getLogin());
        }

        data.moveToFirst();
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() != LOADER_PORTFOLIO_COINS_ID) {
            throw new IllegalArgumentException("no id handled!");
        }
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();

        ((AppCompatActivity)getActivity()).getSupportLoaderManager().destroyLoader(LOADER_PORTFOLIO_COINS_ID);

        mSocket.disconnect();
        mSocket.off("SubAdd", onSubAdd);

    }

    @Override
    public void onRefresh() {
        refreshPrices();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            dumpDB();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            outState.putBoolean(STATE_DIALOG, true);
        }
    }

    @Override
    public String getPageTitle(Context context) {
        return context.getString(R.string.title_activity_main);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_USER_ACCOUNT && resultCode == Activity.RESULT_OK) {
            logout();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_edit:
                Intent intent = new Intent(getActivity(), UserActivity.class);
                startActivityForResult(intent, PortfolioController.REQUEST_USER_ACCOUNT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(PreferencesHelper.getInstance().getLogin() == null) {
            return;
        }
        inflater.inflate(R.menu.main, menu);
    }

    private void initLoaderManager() {
        ((AppCompatActivity) getActivity()).getSupportLoaderManager().initLoader(LOADER_PORTFOLIO_COINS_ID, null, this);
    }

    private void resetMenu() {
        getActivity().invalidateOptionsMenu();
    }

    // Вызывается при изменении локальных данных(обновление цен, добавление удаление монеты и т.д.)
    // Расчитывает параметры портфолио
    private void calculatePortfolioValues(Cursor data) {

        ColumnsPortfolioCoin.ColumnsMap columnsMap = new ColumnsPortfolioCoin.ColumnsMap(data);

        double valueAll = 0;
        double profit24h = 0;
        double valueHoldings = 0;

        mCoinsForRefresh = new HashMap<>();
        mExchangesForRefresh = new ArrayList<>();

        if (data != null && data.getCount() > 0) {
            while (data.moveToNext()) {
                double original = data.getDouble(columnsMap.mColumnOriginal);
                double priceOriginal = data.getDouble(columnsMap.mColumnPriceOriginal);
                double priceNow = data.getDouble(columnsMap.mColumnPriceNow);
                double change24h = data.getDouble(columnsMap.mColumnChange24h);

                if (Double.isInfinite(priceOriginal) || Double.isNaN(priceOriginal)) {
                    priceOriginal = 0;
                }

                if (Double.isInfinite(priceNow) || Double.isNaN(priceNow)) {
                    priceNow = 0;
                }

                if (Double.isInfinite(change24h) || Double.isNaN(change24h)) {
                    change24h = 0;
                }

                valueAll += original * priceOriginal;
                profit24h += original * change24h;
                valueHoldings += original * priceNow;
            }

            if (Double.isInfinite(valueHoldings) || Double.isInfinite(profit24h) || Double.isInfinite(valueAll)) {
                Crashlytics.logException(new Exception(String.format("Illegal values valueHoldings: %s, profit24h: %s, valueAll: %s, coins count: %s",
                        valueHoldings,
                        profit24h,
                        valueAll,
                        data.getCount())));
                return;
            }
        }

        double value24h = valueHoldings - profit24h;
        double profitAll = valueHoldings - valueAll;

        double profit24hPercent = 0;
        double profitAllPercent = 0;

        if(value24h > 0) {
            profit24hPercent = (valueHoldings - value24h) * 100 / value24h;
        }

        if(valueAll > 0) {
            profitAllPercent = (valueHoldings - valueAll) * 100 / valueAll;
        }

        mPortfolioCurrentValue.setText(KeyboardHelper.cutForHeader(valueHoldings));
        mPortfolioCurrentValueUnit.setText(TransactionAddActivity.DEFAULT_SYMBOL_ICON);
        mPortfolioProfit24h.setText(KeyboardHelper.cutForHeader(profit24h));
        mPortfolioProfit24hUnit.setText(String.format(Locale.US, "%s (%.2f%%)", TransactionAddActivity.DEFAULT_SYMBOL_ICON, profit24hPercent));
        mPortfolioOriginalValue.setText(KeyboardHelper.cutForHeader(valueAll));
        mPortfolioOriginalValueUnit.setText(TransactionAddActivity.DEFAULT_SYMBOL_ICON);
        mPortfolioProfitAll.setText(KeyboardHelper.cutForHeader(profitAll));
        mPortfolioProfitAllUnit.setText(String.format(Locale.US, "%s (%.2f%%)", TransactionAddActivity.DEFAULT_SYMBOL_ICON, profitAllPercent));

        if (profit24h < 0) {
            mPortfolioProfit24h.setTextColor(getResources().getColor(R.color.colorDownValue));
            mPortfolioProfit24hUnit.setTextColor(getResources().getColor(R.color.colorDownValue));
        } else {
            mPortfolioProfit24h.setTextColor(getResources().getColor(R.color.colorUpValue));
            mPortfolioProfit24hUnit.setTextColor(getResources().getColor(R.color.colorUpValue));
        }

        if (profitAll < 0) {
            mPortfolioProfitAll.setTextColor(getResources().getColor(R.color.colorDownValue));
            mPortfolioProfitAllUnit.setTextColor(getResources().getColor(R.color.colorDownValue));
        } else {
            mPortfolioProfitAll.setTextColor(getResources().getColor(R.color.colorUpValue));
            mPortfolioProfitAllUnit.setTextColor(getResources().getColor(R.color.colorUpValue));
        }
    }

    private void collectForRefresh(Cursor data) {

        ColumnsPortfolioCoin.ColumnsMap columnsMap = new ColumnsPortfolioCoin.ColumnsMap(data);
        ColumnsCoin.ColumnsMap columnsCoinsMap = new ColumnsCoin.ColumnsMap(data);
        ColumnsExchange.ColumnsMap columnsExchangeMap = new ColumnsExchange.ColumnsMap(data);

        mCoinsForRefresh = new HashMap<>();
        mExchangesForRefresh = new ArrayList<>();

        String symbol;
        String exchange;

        if (data != null && data.getCount() > 0) {
            while (data.moveToNext()) {
                symbol = data.getString(columnsCoinsMap.mColumnSymbol);
                long coinId = data.getLong(columnsMap.mColumnCoinId);
                if (!mCoinsForRefresh.containsKey(symbol)) {
                    mCoinsForRefresh.put(symbol, coinId);
                }
                exchange = data.getString(columnsExchangeMap.mColumnName);
                if (!mExchangesForRefresh.contains(exchange)) {
                    mExchangesForRefresh.add(exchange);
                }
            }
        }
    }

    //region prices
    private void refreshPrices() {

        Cursor cursor = getActivity().getContentResolver().query(
                CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                null,
                CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_REMOVED + " != 1 ",
                null,
                CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_CREATED_AT + " ASC"
        );

        if(cursor != null) {
            collectForRefresh(cursor);
            cursor.close();
        }

        if(mCoinsForRefresh.size() == 0) {
            mSwipeRefresh.setRefreshing(false);
            return;
        }

        RestClientMinApi.INSTANCE.getClient().priceMultiFull(implode(mCoinsForRefresh), TransactionAddActivity.DEFAULT_SYMBOL, null)
                .compose(bindUntilEvent(ControllerEvent.DETACH))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if(mSwipeRefresh != null ) {
                                mSwipeRefresh.setRefreshing(false);
                            }
                            updatePrices(response);
                        },
                        e -> {
                            if (mSwipeRefresh != null) {
                                mSwipeRefresh.setRefreshing(false);
                            }
                        });

    }

    private boolean updatePrices(PriceMultiFullResponse response) {
        HashMap<String, HashMap<String, PriceMultiFullResponse.RawCoin>> prices = response.getRaw();
        for (Map.Entry<String, HashMap<String, PriceMultiFullResponse.RawCoin>> rawCoin : prices.entrySet()) {
            for (Map.Entry<String, PriceMultiFullResponse.RawCoin> currency : rawCoin.getValue().entrySet()) {
                ContentValues values = new ContentValues();
                values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW, currency.getValue().getPrice());
                values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_CHANGE_24H, currency.getValue().getChange24Hour());
                values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_CHANGE_PCT_24H, currency.getValue().getChangePct24Hour());
                if (getActivity() != null) {
                    getActivity().getContentResolver().update(
                            CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values,
                            CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID + " = " + mCoinsForRefresh.get(rawCoin.getKey()),
                            null
                    );
                }
            }
        }
        return true;
    }
    //endregion

    //region initial db
    private long selectPortfolioId() {

        long portfolioId = -1;

        Cursor cursor = getActivity().getContentResolver().query(CryptoContract.CryptoCoins.CONTENT_URI, CryptoContract.CryptoCoins.DEFAULT_PROJECTION, null, null, null);

        if(cursor != null) {
            if(cursor.getCount() == 0) {
                importDB();
            }
            cursor.close();
        }

        cursor = getActivity().getContentResolver().query(CryptoContract.CryptoPortfolios.CONTENT_URI, null, null, null, null);
        if(cursor != null) {
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                int itemColumnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios._ID);
                portfolioId = cursor.getLong(itemColumnIndex);
            }
            cursor.close();
            if(portfolioId > 0) {
                return portfolioId;
            }

        }
        throw new IllegalStateException("illegal portfolio id");
    }

    private void reset() {

        if(isStoragePermissionGranted()) {
            dumpDB();
        }

        getActivity().getContentResolver().delete(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, null, null);
        mAdapter.changeCursor(null);

        SQLiteDatabase db = new DBHelper(getActivity()).getWritableDatabase();

        db.execSQL(CryptoContract.SQL_DELETE_PORTFOLIOS);
        db.execSQL(CryptoContract.SQL_DELETE_COINS);
        db.execSQL(CryptoContract.SQL_DELETE_TRANSACTIONS);
        db.execSQL(CryptoContract.SQL_DELETE_EXCHANGES);
        db.execSQL(CryptoContract.SQL_DELETE_PORTFOLIO_COINS);
        db.execSQL(CryptoContract.SQL_DELETE_NOTIFICATIONS);

        db.execSQL(CryptoContract.SQL_CREATE_PORTFOLIOS);
        db.execSQL(CryptoContract.SQL_CREATE_COINS);
        db.execSQL(CryptoContract.SQL_CREATE_TRANSACTIONS);
        db.execSQL(CryptoContract.SQL_CREATE_EXCHANGES);
        db.execSQL(CryptoContract.SQL_CREATE_PORTFOLIO_COINS);
        db.execSQL(CryptoContract.SQL_CREATE_NOTIFICATIONS);

        insert();
    }

    private void insert() {

        // Coins
        String coinsJson = loadCoinsFromRaw();
        Type collectionCoinType = new TypeToken<HashMap<String, CoinResponse>>() {}.getType();
        HashMap<String, CoinResponse> coins = new Gson().fromJson(coinsJson, collectionCoinType);
        refreshCoins(coins);

        // Exchanges
        String exchangesJson = loadExchangesFromRaw();
        Type collectionExchangeType = new TypeToken<List<ExchangeResponse>>() {}.getType();
        List<ExchangeResponse> exchanges = new Gson().fromJson(exchangesJson, collectionExchangeType);
        refreshExchanges(exchanges);

        // Default Portfolio
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_BASE_COIN_ID, TransactionAddActivity.DEFAULT_COIN_ID);
        getActivity().getContentResolver().insert(CryptoContract.CryptoPortfolios.CONTENT_URI, values);
    }


    private void refreshCoins(CoinsResponse coinsResponse) {
        HashMap<String, CoinResponse> coins = coinsResponse.getData();
        for (Map.Entry<String, CoinResponse> coin : coins.entrySet()) {
            ContentValues values = new ContentValues();
            values.put(CryptoContract.CryptoCoins.COLUMN_NAME_NAME, coin.getValue().getFullName());
            values.put(CryptoContract.CryptoCoins.COLUMN_NAME_LOGO, coin.getValue().getImageUrl());
            values.put(CryptoContract.CryptoCoins.COLUMN_NAME_SORT_ORDER, coin.getValue().getSortOrder());
            values.put(CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL, coin.getKey());
            getActivity().getContentResolver().insert(CryptoContract.CryptoCoins.CONTENT_URI, values);
        }

    }


    private void refreshCoins(HashMap<String, CoinResponse> coins) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (Map.Entry<String, CoinResponse> coin : coins.entrySet()) {
            operations.add(ContentProviderOperation.newInsert(CryptoContract.CryptoCoins.CONTENT_URI)
                    .withValue(CryptoContract.CryptoCoins._ID,                      coin.getValue().getId())
                    .withValue(CryptoContract.CryptoCoins.COLUMN_NAME_NAME,         coin.getValue().getFullName())
                    .withValue(CryptoContract.CryptoCoins.COLUMN_NAME_LOGO,         coin.getValue().getImageUrl())
                    .withValue(CryptoContract.CryptoCoins.COLUMN_NAME_SORT_ORDER,   coin.getValue().getSortOrder())
                    .withValue(CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL,       coin.getKey())
                    .withYieldAllowed(true)
                    .build());
        }

        try {
            getActivity().getContentResolver().applyBatch(CryptoContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }

        operations.clear();
    }

    private void refreshExchanges(List<ExchangeResponse> exchanges) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (ExchangeResponse exchange : exchanges) {
            operations.add(ContentProviderOperation.newInsert(CryptoContract.CryptoExchanges.CONTENT_URI)
                    .withValue(CryptoContract.CryptoExchanges._ID, exchange.getId())
                    .withValue(CryptoContract.CryptoExchanges.COLUMN_NAME_NAME, exchange.getName())
                    .withYieldAllowed(true)
                    .build());
        }

        try {
            getActivity().getContentResolver().applyBatch(CryptoContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }

        operations.clear();
    }

    private String implode(HashMap<String, Long> map) {

        Set<String> list = map.keySet();
        StringBuilder builder = new StringBuilder();

        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String el = iterator.next();
            builder.append(el);
            if(iterator.hasNext()) {
                builder.append(",");
            }
        }

        return builder.toString();
    }

    private String loadCoinsFromRaw() {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try (InputStream is = getResources().openRawResource(R.raw.coins)) {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return writer.toString();

    }

    private String loadExchangesFromRaw() {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try (InputStream is = getResources().openRawResource(R.raw.exchanges)) {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return writer.toString();

    }

    private void importDB() {
        byte[] buffer = new byte[1024];
        OutputStream myOutput;
        int length;
        InputStream myInput;
        try {
            myInput = getActivity().getAssets().open(CryptoContract.DATABASE_NAME);
            myOutput = new FileOutputStream(getActivity().getDatabasePath(CryptoContract.DATABASE_NAME));
            while((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.close();
            myOutput.flush();
            myInput.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void dumpDB() {
        File sd = Environment.getExternalStorageDirectory();

        if (isExternalStorageWritable()) {
            File currentDB = getActivity().getDatabasePath(CryptoContract.DATABASE_NAME);
            File backupDB = new File(sd, Calendar.getInstance().getTimeInMillis() + "_" + CryptoContract.DATABASE_NAME);
            try {
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            return true;
        }
    }
    //endregion

    //region account
    private void addNewAccount(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(accountType, authTokenType, null, null, getActivity(),
                future1 -> {
                    try {
                        Bundle bnd = future1.getResult();
                        final String accountName = bnd.getString(AccountManager.KEY_ACCOUNT_NAME);
                        PreferencesHelper.getInstance().setLogin(accountName);
                        mAuthButtonSubject.onNext(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }, null);
    }

    private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(accountType, authTokenType, null, getActivity(), null, null,
                future1 -> {
                    try {
                        Bundle bnd = future1.getResult();
                        final String accountName = bnd.getString(AccountManager.KEY_ACCOUNT_NAME);
                        PreferencesHelper.getInstance().setLogin(accountName);
                        mAuthButtonSubject.onNext(false);

                        compositeDisposable.add(
                                MainServiceGenerator.createService(MainApiService.class, getActivity()).syncDownload()
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                response -> mSyncPresenter.restorePortfolio(response.string()),
                                                error -> {
                                                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                        )
                        );

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                , null);
    }

    private void showAccountPicker(final String authTokenType) {
        final Account availableAccounts[] = mAccountManager.getAccountsByType(SigninActivity.ACCOUNT_TYPE);

        if (availableAccounts.length == 0) {
            Toast.makeText(getActivity(), "No accounts", Toast.LENGTH_SHORT).show();
        } else {
            String name[] = new String[availableAccounts.length];
            for (int i = 0; i < availableAccounts.length; i++) {
                name[i] = availableAccounts[i].name;
            }

            // Account picker
            mAlertDialog = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                    .setTitle("Pick Account")
                    .setAdapter(
                            new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_list_item_1, name),
                            (dialog, which) -> getExistingAccountAuthToken(availableAccounts[which], authTokenType))
                    .create();
            mAlertDialog.show();
        }
    }

    private void getExistingAccountAuthToken(Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, getActivity(), null, null);

        new Thread(() -> {
            try {
                Bundle bnd = future.getResult();

                final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                if(authtoken == null) {
                    Crashlytics.log("authefication failed");
                }
                PreferencesHelper.getInstance().setLogin(account.name);
                mAuthButtonSubject.onNext(false);

            } catch (Exception e) {
                Crashlytics.logException(new Exception(e.getMessage()));
            }
        }).start();
    }

    private void logout() {
        PreferencesHelper.getInstance().logout();
        Account[] accounts = mAccountManager.getAccountsByType(SigninActivity.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            for(Account account : accounts) {
                mAccountManager.invalidateAuthToken(account.type, null);
            }
        }

        mSyncPresenter.clearPortfolio(getActivity());

        AuthView target = (AuthView)getTargetController();
        target.onLogout();

    }

    private void invalidateAuthToken(final Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, getActivity(), null,null);

        new Thread(() -> {
            Bundle bnd = null;
            try {
                bnd = future.getResult();
            } catch (OperationCanceledException e) {
                Crashlytics.log(e.getMessage());
            } catch (IOException e) {
                Crashlytics.log(e.getMessage());
            } catch (AuthenticatorException e) {
                Crashlytics.log(e.getMessage());
            }
            final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
            mAccountManager.invalidateAuthToken(account.type, authtoken);

        }).start();
    }
    //endregion

    //region socket
    private void start() {

        Request request = new Request.Builder().url("wss://streamer.cryptocompare.com").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = mClient.newWebSocket(request, listener);

        mClient.dispatcher().executorService().shutdown();
    }

    private Emitter.Listener onConnect = args -> {
        Log.d("DEBUG_INFO", "connect");
    };

    private Emitter.Listener onDisconnect = args -> {
        Log.d("DEBUG_INFO", "EVENT_MESSAGE");
    };

    private Emitter.Listener onSubAdd = args -> {
        Log.d("DEBUG_INFO", "onSubAdd");
//        JSONObject data = (JSONObject) args[0];
//        int numUsers;
//        try {
//            numUsers = data.getInt("numUsers");
//        } catch (JSONException e) {
//            return;
//        }
    };


    private class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            Log.d("DEBUG_INFO", "onOpen");

            //webSocket.send("0~Poloniex~BTC~USD");
            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            Log.d("DEBUG_INFO", "onMessage");
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
            Log.d("DEBUG_INFO", "onMessage2");
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
            Log.d("DEBUG_INFO", "onClosing " + reason);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            Log.d("DEBUG_INFO", "onClosed " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            Log.d("DEBUG_INFO", "onFailure " + t.getMessage());

        }
    }

//    public Account createAccount() {
//        // Create the account type and default account
//        Account account = new Account(ACCOUNT, ACCOUNT_TYPE);
//        // Get an instance of the Android account manager
//        AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
//        /*
//         * Add the account and account type, no password or user data
//         * If successful, return the Account object, otherwise report an error.
//         */
//        if (accountManager.addAccountExplicitly(account, null, null)) {
//            /*
//             * If you don't set android:syncable="true" in
//             * in your <provider> element in the manifest,
//             * then call context.setIsSyncable(account, AUTHORITY, 1)
//             * here.
//             */
//        } else {
//            /*
//             * The account exists or some other error occurred. Log this, report it,
//             * or handle it internally.
//             */
//        }
//        return account;
//    }


//    class StethoWebSocketListener extends WebSocketListener {
//        NetworkEventReporter mNetworkEventReporter = NetworkEventReporterImpl.get();
//
//        @Override
//        public void onMessage(WebSocket webSocket, String text) {
//            super.onMessage(webSocket, text);
//            Log.d("DEBUG_INFO", "onMessage");
//            mNetworkEventReporter.webSocketFrameReceived();
//        }
//
//        @Override
//        public void onMessage(WebSocket webSocket, ByteString bytes) {
//            super.onMessage(webSocket, bytes);
//            Log.d("DEBUG_INFO", "onMessage2");
//            mNetworkEventReporter.webSocketFrameReceived();
//        }
//    }
    //endregion

}
