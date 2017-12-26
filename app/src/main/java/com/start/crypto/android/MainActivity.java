package com.start.crypto.android;


import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.start.crypto.android.api.RestClientMinApi;
import com.start.crypto.android.api.model.Coin;
import com.start.crypto.android.api.model.CoinResponse;
import com.start.crypto.android.api.model.CoinsResponse;
import com.start.crypto.android.data.ColumnsCoin;
import com.start.crypto.android.data.ColumnsExchange;
import com.start.crypto.android.data.ColumnsPortfolioCoin;
import com.start.crypto.android.data.CryptoContract;
import com.start.crypto.android.data.DBHelper;
import com.start.crypto.android.sync.SyncAdapter;
import com.start.crypto.android.utils.KeyboardHelper;
import com.start.crypto.android.utils.PreferencesHelper;
import com.trello.rxlifecycle2.android.ActivityEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_DIALOG = "state_dialog";

    @BindView(R.id.coins_list)                  RecyclerView mRecyclerView;
    @BindView(R.id.add_transaction)             FloatingActionButton addTransactionView;
    @BindView(R.id.pre_insert)                  FloatingActionButton preInsertView;

    @BindView(R.id.swipe_refresh)               SwipeRefreshLayout mSwipeRefresh;

    @BindView(R.id.navigation)                  BottomNavigationView mNavigation;

    @BindView(R.id.portfolio_current_value)         TextView mPortfolioCurrentValue;
    @BindView(R.id.portfolio_current_value_unit)    TextView mPortfolioCurrentValueUnit;
    @BindView(R.id.portfolio_profit_24h)            TextView mPortfolioProfit24h;
    @BindView(R.id.portfolio_profit_24h_unit)       TextView mPortfolioProfit24hUnit;
    @BindView(R.id.portfolio_original_value)        TextView mPortfolioOriginalValue;
    @BindView(R.id.portfolio_original_value_unit)   TextView mPortfolioOriginalValueUnit;
    @BindView(R.id.portfolio_profit_all)            TextView mPortfolioProfitAll;
    @BindView(R.id.portfolio_profit_all_unit)       TextView mPortfolioProfitAllUnit;

    private long mPortfolioId;

    private AlertDialog mAlertDialog;

    private PortfolioCoinsListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private HashMap<String, Long> mCoins = new HashMap<>();
    private ArrayList<String> mExchanges = new ArrayList<>();

    private HashMap<String, Double> mPieData = new HashMap<>();

    private ArrayList<ContentProviderOperation> mOperations = new ArrayList<>();

    private Socket mSocket;
    private OkHttpClient mClient;

    private AccountManager mAccountManager;

    private PublishSubject<Boolean> mAuthButtonSubject = PublishSubject.create();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        startActivity(new Intent(this, PortfoliosActivity.class));
                        return true;
                    case R.id.navigation_dashboard:
                        Intent intent = new Intent(this, PieActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        intent.putExtra(PieActivity.EXTRA_PIE_DATA, mPieData);
                        startActivity(intent);
                        return true;
                    case R.id.navigation_notifications:
                        startActivity(new Intent(this, NotificationsActivity.class));
                        return true;
                }
                return false;
            };

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_test);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        CryptoApp app = (CryptoApp) getApplication();
        mSocket = app.getSocket();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        toolbar.setTitle(null);


        mAdapter = new PortfolioCoinsListAdapter(this, null);
        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && addTransactionView.isShown()) {
                    addTransactionView.hide();
                    preInsertView.hide();
                } else if(dy < 0 && !addTransactionView.isShown()){
                    addTransactionView.show();
                    preInsertView.show();
                }
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);

        RxView.clicks(addTransactionView).subscribe(success -> AutocompleteListActivity.start(this));
        RxView.clicks(preInsertView).subscribe(success ->
//                {
//                    mSwipeRefresh.setRefreshing(true);
//                    reset();
//                    mSwipeRefresh.setRefreshing(false);
//                }
                {
                    if(PreferencesHelper.getInstance().getLogin() == null) {
                        getTokenForAccountCreateIfNeeded(AuthActivity.ACCOUNT_TYPE, AuthActivity.AUTHTOKEN_TYPE_FULL_ACCESS);
                    }
                }
        );

        if(PreferencesHelper.getInstance().getLogin() != null) {
            preInsertView.setVisibility(View.GONE);
        }


        Cursor cursor = getContentResolver().query(CryptoContract.CryptoCoins.CONTENT_URI, CryptoContract.CryptoCoins.DEFAULT_PROJECTION, null, null, null);

        if(cursor != null) {
            if(cursor.getCount() == 0) {
                importDB();
            }
            cursor.close();
        }

        cursor = getContentResolver().query(CryptoContract.CryptoPortfolios.CONTENT_URI, null, null, null, null);
        if(cursor != null) {
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                int itemColumnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolios._ID);
                mPortfolioId = cursor.getLong(itemColumnIndex);
            } else {
                throw new IllegalStateException("illegal portfolio id");
            }
            cursor.close();
        }

        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mSwipeRefresh.setOnRefreshListener(this);

        mAccountManager = AccountManager.get(this);

//        Account[] accounts = mAccountManager.getAccountsByType( AuthActivity.ACCOUNT_TYPE);
//        if (accounts.length != 0) {
//            String token = mAccountManager.peekAuthToken(accounts[0], AuthActivity.AUTHTOKEN_TYPE_FULL_ACCESS);
//            if (token != null && PreferencesHelper.getInstance().getLogin() != null) {
//                preInsertView.setVisibility(View.GONE);
//            }
//        }

        if (savedInstanceState != null) {
            boolean showDialog = savedInstanceState.getBoolean(STATE_DIALOG);
            if (showDialog) {
                showAccountPicker(AuthActivity.AUTHTOKEN_TYPE_FULL_ACCESS);
            }
        }

        compositeDisposable.add(
                mAuthButtonSubject.observeOn(AndroidSchedulers.mainThread())
                        .subscribe(res -> {
                            if(!res) {
                                preInsertView.setVisibility(View.GONE);
                            } else {
                                preInsertView.setVisibility(View.VISIBLE);
                            }
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
    protected void onResume() {
        super.onResume();
        mSwipeRefresh.post(() -> mSwipeRefresh.setRefreshing(true));
        refreshPrices();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id != 0) {
            throw new IllegalArgumentException("no id handled!");
        }
        return new CursorLoader(this, CryptoContract.CryptoPortfolioCoins.CONTENT_URI, null, null, null, CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_CREATED_AT + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() != 0) {
            throw new IllegalArgumentException("no id handled!");
        }

        calculatePortfolioValues(data);

        data.moveToFirst();
        mAdapter.changeCursor(data);


    }

    private void calculatePortfolioValues(Cursor data) {

        ColumnsPortfolioCoin.ColumnsMap columnsMap = new ColumnsPortfolioCoin.ColumnsMap(data);
        ColumnsCoin.ColumnsMap columnsCoinsMap = new ColumnsCoin.ColumnsMap(data);
        ColumnsExchange.ColumnsMap columnsExchangeMap = new ColumnsExchange.ColumnsMap(data);

        double valueAll = 0;
        double value24h = 0;
        double valueHoldings = 0;

        mCoins = new HashMap<>();
        mExchanges = new ArrayList<>();


        String symbol;
        String exchange;
        mPieData = new HashMap<>();

        while (data.moveToNext()) {
            double original = data.getDouble(columnsMap.mOriginal);
            double priceOriginal = data.getDouble(columnsMap.mColumnPriceOriginal);
            double priceNow = data.getDouble(columnsMap.mColumnPriceNow);
            double price24h = data.getDouble(columnsMap.mColumnPrice24h);

            valueAll += original * priceOriginal;
            value24h += original * price24h;
            valueHoldings += original * priceNow;

            symbol = data.getString(columnsCoinsMap.mColumnSymbol);
            long coinId = data.getLong(columnsMap.mCoinId);
            if (!mCoins.containsKey(symbol)) {
                mCoins.put(symbol, coinId);
            }

            exchange = data.getString(columnsExchangeMap.mColumnName);
            if (!mExchanges.contains(exchange)) {
                mExchanges.add(exchange);
            }

            mPieData.put(data.getString(columnsCoinsMap.mColumnSymbol), original * priceNow);
        }

        if (Double.isInfinite(valueHoldings)) {
            return;
        }

        double profit24h = valueHoldings - value24h;
        double profitAll = valueHoldings - valueAll;

        double profit24hPercent = (valueHoldings - value24h) / valueHoldings;
        double profitAllPercent = (valueHoldings - valueAll) / valueHoldings;


        mPortfolioCurrentValue.setText(KeyboardHelper.cut(valueHoldings));
        mPortfolioCurrentValueUnit.setText(CreateTransactionActivity.DEFAULT_SYMBOL);
        mPortfolioProfit24h.setText(KeyboardHelper.cut(profit24h));
        mPortfolioProfit24hUnit.setText(String.format(Locale.US, "%s (%s%%)", CreateTransactionActivity.DEFAULT_SYMBOL, Math.round(profit24hPercent)));
        mPortfolioOriginalValue.setText(KeyboardHelper.cut(valueAll));
        mPortfolioOriginalValueUnit.setText(CreateTransactionActivity.DEFAULT_SYMBOL);
        mPortfolioProfitAll.setText(KeyboardHelper.cut(profitAll));
        mPortfolioProfitAllUnit.setText(String.format(Locale.US, "%s (%.2f%%)", CreateTransactionActivity.DEFAULT_SYMBOL, profitAllPercent));

        if (profit24h < 0) {
            mPortfolioProfit24h.setTextColor(getResources().getColor(R.color.colorDownValue));
            mPortfolioProfit24hUnit.setTextColor(getResources().getColor(R.color.colorDownValue));
        }

        if (profitAll < 0) {
            mPortfolioProfitAll.setTextColor(getResources().getColor(R.color.colorDownValue));
            mPortfolioProfitAllUnit.setTextColor(getResources().getColor(R.color.colorDownValue));
        }


        // push to server
        if (PreferencesHelper.getInstance().getLogin() != null && data.getCount() > 0 && profit24h > 0 && profitAll > 0) {
            compositeDisposable.add(
                    MainServiceGenerator.createService(MainApiService.class, this).pushPortfolio(PreferencesHelper.getInstance().getLogin(), data.getCount(), profit24h, profitAll)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    response -> {
                                    },
                                    error -> {
                                        logout();
                                    }
                            )
            );
        }

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
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();

        mSocket.disconnect();
        mSocket.off("SubAdd", onSubAdd);

    }

    private void insert() {

        // Coins
//        mSwipeRefresh.setRefreshing(true);
//        RestClientMinApi.INSTANCE.getClient().coins()
//                .compose(bindUntilEvent(ActivityEvent.PAUSE))
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.computation())
//                .doOnNext(this::refreshCoins)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                        response -> {
//                            mSwipeRefresh.setRefreshing(false);
//                        },
//                        error -> {
//                            mSwipeRefresh.setRefreshing(false);
//                        }
//                );

        String coinsJson = loadCoinsFromRaw();
        Type collectionType = new TypeToken<HashMap<String, CoinResponse>>() {}.getType();
        HashMap<String, CoinResponse> coins = new Gson().fromJson(coinsJson, collectionType);
        refreshCoins(coins);

        // Exchanges
        ContentValues values = new ContentValues(1);
        values.put(CryptoContract.CryptoExchanges.COLUMN_NAME_NAME, CreateTransactionActivity.DEFAULT_EXCHANGE);
        getContentResolver().insert(CryptoContract.CryptoExchanges.CONTENT_URI, values);

        Resources res = getResources();
        String[] exchanges = res.getStringArray(R.array.exchanges);

        for (String exchange : exchanges) {
            values = new ContentValues(1);
            values.put(CryptoContract.CryptoExchanges.COLUMN_NAME_NAME, exchange);
            getContentResolver().insert(CryptoContract.CryptoExchanges.CONTENT_URI, values);
        }

        // Portfolio
        values = new ContentValues();
        values.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_BASE_COIN_ID, 1);
        getContentResolver().insert(CryptoContract.CryptoPortfolios.CONTENT_URI, values);
    }

    public void reset() {

        if(isStoragePermissionGranted()) {
            dumpDB();
        }

        getContentResolver().delete(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, null, null);
        mAdapter.changeCursor(null);

        SQLiteDatabase db = new DBHelper(this).getWritableDatabase();

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

    @Override
    public void onRefresh() {
        refreshPrices();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == AutocompleteListActivity.REQUEST_COIN && resultCode == RESULT_OK) {
            Coin coin = data.getParcelableExtra(AutocompleteListActivity.EXTRA_COIN);
            CreateTransactionActivity.start(this, mPortfolioId, coin.getId(), coin.getSymbol());
        }
    }

    private void refreshPrices() {


        RestClientMinApi.INSTANCE.getClient().prices(CreateTransactionActivity.DEFAULT_SYMBOL, implode(mCoins), null)
                .compose(bindUntilEvent(ActivityEvent.PAUSE))

                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            writePrices(response);
                            mSwipeRefresh.setRefreshing(false);
                            triggerRefresh();
                        },
                        error -> {
                            mSwipeRefresh.setRefreshing(false);
                        }
                );

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);


        RestClientMinApi.INSTANCE.getClient().pricesHistorical(CreateTransactionActivity.DEFAULT_SYMBOL, implode(mCoins), Long.toString(cal.getTimeInMillis()), null)
                .compose(bindUntilEvent(ActivityEvent.PAUSE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            write24hPrices(response.get(CreateTransactionActivity.DEFAULT_SYMBOL));
                            mSwipeRefresh.setRefreshing(false);
                        },
                        error -> {
                            mSwipeRefresh.setRefreshing(false);
                        }
                );
    }

    private void writePrices(HashMap<String, Double> prices) {
        String message = "0~Poloniex~BTC~USD";

        JSONObject obj = new JSONObject();
        try {
            obj.put("subs", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit("SubAdd", obj);

        //start();

        for (Map.Entry<String, Double> currency : prices.entrySet()) {
            ContentValues values = new ContentValues();
            values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW, 1/currency.getValue());
            getContentResolver().update(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values, CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID + " = " + mCoins.get(currency.getKey()), null);
        }

    }

    private void write24hPrices(HashMap<String, Double> prices) {
        for (Map.Entry<String, Double> currency : prices.entrySet()) {
            ContentValues values = new ContentValues();
            values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_24H, 1/currency.getValue());
            getContentResolver().update(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values, CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID + " = " + mCoins.get(currency.getKey()), null);
        }

    }

    private void refreshCoins(CoinsResponse coinsResponse) {
        HashMap<String, CoinResponse> coins = coinsResponse.getData();
        for (Map.Entry<String, CoinResponse> coin : coins.entrySet()) {
            ContentValues values = new ContentValues();
            values.put(CryptoContract.CryptoCoins.COLUMN_NAME_NAME, coin.getValue().getFullName());
            values.put(CryptoContract.CryptoCoins.COLUMN_NAME_LOGO, coin.getValue().getImageUrl());
            values.put(CryptoContract.CryptoCoins.COLUMN_NAME_SORT_ORDER, coin.getValue().getSortOrder());
            values.put(CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL, coin.getKey());
            getContentResolver().insert(CryptoContract.CryptoCoins.CONTENT_URI, values);
        }

    }

    private void refreshCoins(HashMap<String, CoinResponse> coins) {

        for (Map.Entry<String, CoinResponse> coin : coins.entrySet()) {
            mOperations.add(ContentProviderOperation.newInsert(CryptoContract.CryptoCoins.CONTENT_URI)
                    .withValue(CryptoContract.CryptoCoins.COLUMN_NAME_NAME,         coin.getValue().getFullName())
                    .withValue(CryptoContract.CryptoCoins.COLUMN_NAME_LOGO,         coin.getValue().getImageUrl())
                    .withValue(CryptoContract.CryptoCoins.COLUMN_NAME_SORT_ORDER,   coin.getValue().getSortOrder())
                    .withValue(CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL,       coin.getKey())
                    .withYieldAllowed(true)
                    .build());
        }

        try {
            getContentResolver().applyBatch(CryptoContract.AUTHORITY, mOperations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }


        mOperations.clear();
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

    public String loadCoinsFromRaw() {
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

    private void importDB() {
        byte[] buffer = new byte[1024];
        OutputStream myOutput;
        int length;
        InputStream myInput;
        try {
            myInput = getAssets().open(CryptoContract.DATABASE_NAME);
            myOutput = new FileOutputStream(getDatabasePath(CryptoContract.DATABASE_NAME));
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

    public void dumpDB() {
        File sd = Environment.getExternalStorageDirectory();

        if (isExternalStorageWritable()) {
            File currentDB = getDatabasePath(CryptoContract.DATABASE_NAME);
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

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            dumpDB();
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            return true;
        }
    }






    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            outState.putBoolean(STATE_DIALOG, true);
        }
    }

    private void addNewAccount(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(accountType, authTokenType, null, null, this,
                future1 -> {
                    try {
                        Bundle bnd = future1.getResult();
                        Toast.makeText(getBaseContext(), "Account was created", Toast.LENGTH_SHORT).show();
                        final String accountName = bnd.getString(AccountManager.KEY_ACCOUNT_NAME);
                        PreferencesHelper.getInstance().setLogin(accountName);
                        mAuthButtonSubject.onNext(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }, null);
    }

    private void invalidateAuthToken(final Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null,null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();

                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    mAccountManager.invalidateAuthToken(account.type, authtoken);
                    showMessage(account.name + " invalidated");
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }).start();
    }

    private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(accountType, authTokenType, null, this, null, null,
                future1 -> {
                    try {
                        Bundle bnd = future1.getResult();
                        final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                        final String accountName = bnd.getString(AccountManager.KEY_ACCOUNT_NAME);
                        Toast.makeText(getBaseContext(), (authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL", Toast.LENGTH_SHORT).show();
                        PreferencesHelper.getInstance().setLogin(accountName);
                        mAuthButtonSubject.onNext(false);


                        compositeDisposable.add(
                                MainServiceGenerator.createService(MainApiService.class, this).syncDownload()
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                succ -> doSync(succ.string()),
                                                error -> {
                                                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                        )
                        );

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                , null);
    }

    private void doSync(String response) {
        clearDb();
        saveJsonCollections(response);
    }

    private void getExistingAccountAuthToken(Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();

                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    showMessage((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL");
                    PreferencesHelper.getInstance().setLogin(account.name);
                    mAuthButtonSubject.onNext(false);

                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }).start();
    }

    private void showAccountPicker(final String authTokenType) {
        final Account availableAccounts[] = mAccountManager.getAccountsByType(AuthActivity.ACCOUNT_TYPE);

        if (availableAccounts.length == 0) {
            Toast.makeText(this, "No accounts", Toast.LENGTH_SHORT).show();
        } else {
            String name[] = new String[availableAccounts.length];
            for (int i = 0; i < availableAccounts.length; i++) {
                name[i] = availableAccounts[i].name;
            }

            // Account picker
            mAlertDialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                    .setTitle("Pick Account")
                    .setAdapter(
                            new ArrayAdapter<>(getBaseContext(),
                            android.R.layout.simple_list_item_1, name),
                            (dialog, which) -> getExistingAccountAuthToken(availableAccounts[which], authTokenType))
                    .create();
            mAlertDialog.show();
        }
    }

    private void logout() {
        PreferencesHelper.getInstance().logout();
        Account[] accounts = mAccountManager.getAccountsByType(AuthActivity.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            for(Account a : accounts) {
                invalidateAuthToken(a, AuthActivity.AUTHTOKEN_TYPE_FULL_ACCESS);
            }
        }
        preInsertView.setVisibility(View.VISIBLE);
    }

    private void triggerRefresh() {
        if(PreferencesHelper.getInstance().getLogin() == null) {
            return;
        }
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                new Account(PreferencesHelper.getInstance().getLogin(), AuthActivity.ACCOUNT_TYPE),
                CryptoContract.AUTHORITY,
                b);
    }

    private void showMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void clearDb() {
        getContentResolver().delete(CryptoContract.CryptoPortfolios.CONTENT_URI, null, null);
        getContentResolver().delete(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, null, null);
        getContentResolver().delete(CryptoContract.CryptoTransactions.CONTENT_URI, null, null);
        getContentResolver().delete(CryptoContract.CryptoNotifications.CONTENT_URI, null, null);
    }

    private void saveJsonCollections(String response) {
        try {
            Log.d("DEBUG_INFO", "   collection: " + SyncAdapter.COLLECTION_PORTFOLIOS);
            JSONArray jsonPortfolios = (new JSONObject(response)).getJSONArray(SyncAdapter.COLLECTION_PORTFOLIOS);
            saveJsonToDatabase(CryptoContract.CryptoPortfolios.CONTENT_URI, jsonPortfolios, CryptoContract.CryptoPortfolios.DEFAULT_PROJECTION);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Log.d("DEBUG_INFO", "   collection: " + SyncAdapter.COLLECTION_PORTFOLIO_COINS);
            JSONArray jsonPortfolioCoins = (new JSONObject(response)).getJSONArray(SyncAdapter.COLLECTION_PORTFOLIO_COINS);
            saveJsonToDatabase(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, jsonPortfolioCoins, CryptoContract.CryptoPortfolioCoins.DEFAULT_PROJECTION_SIMPLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Log.d("DEBUG_INFO", "   collection: " + SyncAdapter.COLLECTION_TRANSACTIONS);
            JSONArray jsonTransactions = (new JSONObject(response)).getJSONArray(SyncAdapter.COLLECTION_TRANSACTIONS);
            saveJsonToDatabase(CryptoContract.CryptoTransactions.CONTENT_URI, jsonTransactions, CryptoContract.CryptoTransactions.DEFAULT_PROJECTION);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Log.d("DEBUG_INFO", "   collection: " + SyncAdapter.COLLECTION_NOTIFICATIONS);
            JSONArray jsonNotifications = (new JSONObject(response)).getJSONArray(SyncAdapter.COLLECTION_NOTIFICATIONS);
            saveJsonToDatabase(CryptoContract.CryptoNotifications.CONTENT_URI, jsonNotifications, CryptoContract.CryptoNotifications.DEFAULT_PROJECTION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveJsonToDatabase(Uri uri, JSONArray jsonPortfolios, String[] projection) {

        for (int i = 0; i < jsonPortfolios.length(); i++) {
            JSONObject row;
            try {
                row = jsonPortfolios.getJSONObject(i);
                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
                for( String field : projection) {
                    builder.withValue(field, row.getString(field));
                }
                builder.withYieldAllowed(true);
                mOperations.add(builder.withYieldAllowed(true).build());

            } catch (JSONException e) {
                e.printStackTrace();
            }


            try {
                getContentResolver().applyBatch(CryptoContract.AUTHORITY, mOperations);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }


            mOperations.clear();
        }
    }

    private Emitter.Listener onConnect = args -> {
        Log.d("DEBUG_INFO", "connect");
    };

    private Emitter.Listener onDisconnect = args -> {
        Log.d("DEBUG_INFO", "EVENT_MESSAGE");
    };

    private void start() {

        Request request = new Request.Builder().url("wss://streamer.cryptocompare.com").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = mClient.newWebSocket(request, listener);

        mClient.dispatcher().executorService().shutdown();
    }

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


}
