package com.start.crypto.android;


import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.api.RestClientMinApi;
import com.start.crypto.android.api.model.CoinResponse;
import com.start.crypto.android.api.model.CoinsResponse;
import com.start.crypto.android.data.ColumnsCoin;
import com.start.crypto.android.data.ColumnsExchange;
import com.start.crypto.android.data.ColumnsPortfolioCoin;
import com.start.crypto.android.data.CryptoContract;
import com.start.crypto.android.data.DBHelper;
import com.start.crypto.android.utils.PreferencesHelper;
import com.trello.rxlifecycle2.android.ActivityEvent;

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
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {


    @BindView(R.id.coins_list)                  RecyclerView mRecyclerView;
    @BindView(R.id.add_transaction)             FloatingActionButton addTransactionView;
    @BindView(R.id.pre_insert)                  FloatingActionButton preInsertView;

    @BindView(R.id.swipe_refresh)               SwipeRefreshLayout mSwipeRefresh;

    @BindView(R.id.navigation)                  BottomNavigationView mNavigation;

    @BindView(R.id.portfolio_current_value)     TextView mPortfolioCurrentValue;
    @BindView(R.id.portfolio_profit_24h)        TextView mPortfolioProfit24h;
    @BindView(R.id.portfolio_profit_24h_unit)   TextView mPortfolioProfit24hUnit;
    @BindView(R.id.portfolio_original_value)    TextView mPortfolioOriginalValue;
    @BindView(R.id.portfolio_profit_all)        TextView mPortfolioProfitAll;
    @BindView(R.id.portfolio_profit_all_unit)   TextView mPortfolioProfitAllUnit;


    private PortfolioCoinsListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private HashMap<String, Long> mCoins = new HashMap<>();
    private ArrayList<String> mExchanges = new ArrayList<>();

    private HashMap<String, Double> mPieData = new HashMap<>();

    private ArrayList<ContentProviderOperation> mOperations = new ArrayList<>();


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        PreferencesHelper preferencesHelper = PreferencesHelper.getInstance();
                        if(preferencesHelper.isAuth()) {
                            startActivity(new Intent(this, PortfoliosActivity.class));
                            return true;
                        }
                        startActivity(new Intent(this, SignupActivity.class));
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

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

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

        getSupportLoaderManager().restartLoader(0, null, this);

        RxView.clicks(addTransactionView).subscribe(success -> startActivity(new Intent(this, AutocompleteActivity.class)));
        RxView.clicks(preInsertView).subscribe(success -> importDB());


        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mSwipeRefresh.setOnRefreshListener(this);



    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id != 0) {
            throw new IllegalArgumentException("no id handled!");
        }
        return new CursorLoader(this, CryptoContract.CryptoPortfolioCoins.CONTENT_URI, null, null, null, null);
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
        mPieData =  new HashMap<>();

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
            if(!mCoins.containsKey(symbol)) {
                mCoins.put(symbol, coinId);
            }

            exchange = data.getString(columnsExchangeMap.mColumnName);
            if(!mExchanges.contains(exchange)) {
                mExchanges.add(exchange);
            }

            mPieData.put(data.getString(columnsCoinsMap.mColumnSymbol), original * priceNow);
        }

        if(Double.isInfinite(valueHoldings)) {
            return;
        }

        double profit24h = valueHoldings - value24h;
        double profitAll = valueHoldings - valueAll;


        mPortfolioCurrentValue.setText(new BigDecimal(valueHoldings).setScale(0, BigDecimal.ROUND_CEILING).toString());
        mPortfolioProfit24h.setText(new BigDecimal(profit24h).setScale(0, BigDecimal.ROUND_CEILING).toString());
        mPortfolioProfit24hUnit.setText("USD");
        mPortfolioOriginalValue.setText(new BigDecimal(valueAll).setScale(0, BigDecimal.ROUND_CEILING).toString());
        mPortfolioProfitAll.setText(new BigDecimal(profitAll).setScale(0, BigDecimal.ROUND_CEILING).toString());
        mPortfolioProfitAllUnit.setText("USD");

        if(profit24h < 0) {
            mPortfolioProfit24h.setTextColor(getResources().getColor(R.color.colorDownValue));
            mPortfolioProfit24hUnit.setTextColor(getResources().getColor(R.color.colorDownValue));
        }

        if(profitAll < 0) {
            mPortfolioProfitAll.setTextColor(getResources().getColor(R.color.colorDownValue));
            mPortfolioProfitAllUnit.setTextColor(getResources().getColor(R.color.colorDownValue));
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
        values.put(CryptoContract.CryptoExchanges.COLUMN_NAME_NAME, "CCCAGG");
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

        RestClientMinApi.INSTANCE.getClient().prices("USD", implode(mCoins), null)
                .compose(bindUntilEvent(ActivityEvent.PAUSE))

                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            refreshPrices(response);
                            mSwipeRefresh.setRefreshing(false);
                        },
                        error -> {
                            mSwipeRefresh.setRefreshing(false);
                        }
                );

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);


        RestClientMinApi.INSTANCE.getClient().pricesHistorical("USD", implode(mCoins), Long.toString(cal.getTimeInMillis()), null)
                .compose(bindUntilEvent(ActivityEvent.PAUSE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            refresh24hPrices(response.get("USD"));
                            mSwipeRefresh.setRefreshing(false);
                        },
                        error -> {
                            mSwipeRefresh.setRefreshing(false);
                        }
                );

    }

    private void refreshPrices(HashMap<String, Double> prices) {
        for (Map.Entry<String, Double> currency : prices.entrySet()) {
            ContentValues values = new ContentValues();
            values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW, 1/currency.getValue());
            getContentResolver().update(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values, CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID + " = " + mCoins.get(currency.getKey()), null);
        }

    }

    private void refresh24hPrices(HashMap<String, Double> prices) {
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
            File backupDB = new File(sd, CryptoContract.DATABASE_NAME);
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
}
