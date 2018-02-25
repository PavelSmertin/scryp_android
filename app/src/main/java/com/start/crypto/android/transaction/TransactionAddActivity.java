package com.start.crypto.android.transaction;

import android.app.ActivityOptions;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.start.crypto.android.AutocompleteActivity;
import com.start.crypto.android.BaseActivity;
import com.start.crypto.android.R;
import com.start.crypto.android.api.RestClientMinApi;
import com.start.crypto.android.api.model.AutocompleteItem;
import com.start.crypto.android.api.model.PortfolioCoin;
import com.start.crypto.android.api.model.Transaction;
import com.start.crypto.android.data.CryptoContract;
import com.start.crypto.android.utils.KeyboardHelper;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;


public class TransactionAddActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_PORTFOLIO_COIN_ID          = "portfolio_coin_id";
    public static final String EXTRA_PORTFOLIO_ID               = "portfolio_id";
    public static final String EXTRA_EXCHANGE_ID                = "exchange_id";

    public static final String  DEFAULT_SYMBOL           = "USDT";
    public static final String  DEFAULT_SYMBOL_ICON      = "$";
    private static final String SYMBOL_USD               = "USD";

    public static final int     DEFAULT_COIN_ID          = 171986;
    public static final String  DEFAULT_EXCHANGE         = "Global average";
    public static final String  DEFAULT_EXCHANGE_ALIAS   = "CCCAGG";

    private static final int MAX_DESCRIPTION_LENGTH      = 160;

    protected static final int LOADER_PORTFOLIO_COINS      = 10001;
    protected static final int LOADER_COINS                = 10002;
    protected static final int LOADER_EXCHANGES            = 10003;

    @BindView(R.id.scroll_container)        View mScrollContainer;
    @BindView(R.id.currentey_complete)      EditText mCurrenteyComplete;
    @BindView(R.id.clear_coin_button)       ImageView mClearCoinButton;
    @BindView(R.id.exchange_complete)       EditText mExchangeComplete;
    @BindView(R.id.clear_exchange_button)   ImageView mClearExchangeButton;
    @BindView(R.id.amount)                  EditText mAmountView;
    @BindView(R.id.amount_symbol)           TextView mAmountSymbolView;
    @BindView(R.id.price)                   EditText mPriceView;
    @BindView(R.id.price_symbol)            TextView mPriceSymbolView;
    @BindView(R.id.date)                    EditText mDateView;
    @BindView(R.id.describtion)             EditText mDescribtionView;
    @BindView(R.id.scroll_view)             ScrollView mScrollView;
    @BindView(R.id.coin_complete)           EditText mCoinComplete;
    @BindView(R.id.clear_currentey_button)  ImageView mClearCurrenteyButton;
    @BindView(R.id.price_switch)            Switch mPriceSwitch;

    @Nullable @BindView(R.id.add_transaction)         Button mTransactionButton;

    private Calendar myCalendar;

    private TransactionPresenterAdd mPresenter;

    protected long              argPortfolioCoinId;
    protected long              argPortfolioId = 1;
    protected long              argExchangeId;

    private String  mCoinSymbol;
    private String  mCurrenteySymbol;

    protected long      mCoinId;
    protected long      mCurrenteyId;

    protected double    mAmount;
    protected double    mPricePerCoin;
    protected double    mPriceInTotal;
    protected double    mBasePrice = 1;
    protected double    mCoinPrice = 1;

    protected long      mDate;
    protected String    mDescription;

    BehaviorSubject<Long> mCoinFieldObservable              = BehaviorSubject.create();
    BehaviorSubject<Long> mPairFieldObservable              = BehaviorSubject.create();
    BehaviorSubject<Long> mExchangesFieldObservable         = BehaviorSubject.create();
    BehaviorSubject<Long> mDateFieldObservable              = BehaviorSubject.create();
    BehaviorSubject<Double> mPriceFieldObservable           = BehaviorSubject.create();
    BehaviorSubject<Double> mAmountFieldObservable          = BehaviorSubject.create();
    BehaviorSubject<Integer> mDescribtionFieldObservable    = BehaviorSubject.create();

    protected CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Menu mMenu;

    public static void start(Context context, long portfolioId) {
        Intent starter = new Intent(context, TransactionAddActivity.class);
        starter.putExtra(TransactionAddActivity.EXTRA_PORTFOLIO_ID, portfolioId);
        context.startActivity(starter);
    }

    @Override
    protected void setupLayout() {
        setContentView(R.layout.transaction_activity_create);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Portfolio coin
        argPortfolioCoinId = getIntent().getLongExtra(EXTRA_PORTFOLIO_COIN_ID, 0);

        // Portfolio
        argPortfolioId = getIntent().getLongExtra(EXTRA_PORTFOLIO_ID, 0);
        if (argPortfolioId == 0) {
            finish();
            return;
        }

        // Coin
        mCoinComplete.setFocusable(false);
        RxView.clicks(mCoinComplete)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    mCoinComplete.setText("");
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                                Pair.create(mCoinComplete, getString(R.string.transition_autocomplete_coin)),
                                Pair.create(mClearCoinButton, getString(R.string.transition_autocomplete_clear)));
                        AutocompleteActivity.start(this, options, AutocompleteActivity.REQUEST_COIN);
                    } else {
                        AutocompleteActivity.start(this, AutocompleteActivity.REQUEST_COIN);
                    }
                });

        // Coin pair (Currentey)
        mCurrenteyComplete.setFocusable(false);
        RxView.clicks(mCurrenteyComplete)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    mCurrenteyComplete.setText("");
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                                Pair.create(mCurrenteyComplete, getString(R.string.transition_autocomplete_coin)),
                                Pair.create(mClearCurrenteyButton, getString(R.string.transition_autocomplete_clear)));
                        AutocompleteActivity.start(this, options, AutocompleteActivity.REQUEST_CURRENTEY);
                    } else {
                        AutocompleteActivity.start(this, AutocompleteActivity.REQUEST_CURRENTEY);
                    }
                });

        // Exchange
        argExchangeId = getIntent().getLongExtra(EXTRA_EXCHANGE_ID, 0);

        mExchangeComplete.setFocusable(false);
        RxView.clicks(mExchangeComplete)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    mExchangeComplete.setText("");
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                                Pair.create(mExchangeComplete, getString(R.string.transition_autocomplete_coin)),
                                Pair.create(mClearExchangeButton, getString(R.string.transition_autocomplete_clear)));
                        AutocompleteActivity.start(this, options, AutocompleteActivity.REQUEST_EXCHANGE);
                    } else {
                        AutocompleteActivity.start(this, AutocompleteActivity.REQUEST_EXCHANGE);
                    }
                });
        compositeDisposable.add(RxView.focusChanges(mExchangeComplete)
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe(focus -> {
                    if(focus) {
                        mScrollView.post(() -> mScrollView.scrollTo(0, getScrollBottom()));
                    }
                })
        );


        // Price
        RxTextView.textChanges(mPriceView)
                .debounce(100, TimeUnit.MILLISECONDS)
                .subscribe(price -> {
                    double priceDouble = 0;
                    String priceWithoutSpace = price.toString().replaceAll("\\s", "");
                    try{
                        priceDouble = Double.parseDouble(priceWithoutSpace);
                    }catch(NumberFormatException e){
                        // not double
                    } finally {
                        setPrice(priceDouble);
                        mPriceFieldObservable.onNext(priceDouble);
                    }
                });

        mPriceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            double price;
            if(isChecked) {
                price = mPricePerCoin;
            } else {
                price = mPriceInTotal;
            }

            if(price != 0) {
                mPriceView.setText(KeyboardHelper.format(price));
            } else {
                mPriceView.setText(null);
            }
        });

        // Amount
        mAmountView.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus && mAmountView.getText().toString().equals("0.00")) {
                mAmountView.setText(null);
            }
            if(!hasFocus && mAmountView.getText().length() == 0) {
                if(mAmount < 0.5D) {
                    mAmountView.setText(String.format(Locale.US, "%.08f", mAmount));
                } else if(mAmount < 1D){
                    mAmountView.setText(String.format(Locale.US, "%.05f", mAmount));
                } else {
                    mAmountView.setText(String.format(Locale.US, "%.02f", mAmount));
                }
            }
        });

        RxTextView.textChanges(mAmountView)
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(amount -> {
                    double amountDouble = 0;
                    try{
                        if(amount.length() > 0) {
                            amountDouble = Double.parseDouble(amount.toString());
                        }
                    } catch(NumberFormatException e){
                        mAmountView.setError(getString(R.string.transaction_amount_error_parse));
                    } finally {
                        mAmount = amountDouble;
                        mAmountFieldObservable.onNext(amountDouble);
                    }
                });
        mAmountView.setText(String.format(Locale.US, "%.02f", mAmount));



        // Date of transaction
        myCalendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateOfTransaction();
        };
        compositeDisposable.add(RxView.clicks(mDateView)
                .subscribe(v -> {
                    new DatePickerDialog(
                            TransactionAddActivity.this,
                            date,
                            myCalendar.get(Calendar.YEAR),
                            myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)
                    ).show();
                    mScrollView.post(() -> mScrollView.scrollTo(0, getScrollBottom()));
                })
        );
        updateDateOfTransaction();


        // Description
        RxTextView.textChanges(mDescribtionView)
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(description -> {
                    mDescription = mDescribtionView.getText().toString();
                    if (mDescription.length() > MAX_DESCRIPTION_LENGTH) {
                        mDescribtionView.setError(getString(R.string.transaction_description_error));
                    }
                    mDescribtionFieldObservable.onNext(mDescription.length());

                });

        compositeDisposable.add(RxView.focusChanges(mDescribtionView)
                .debounce(500, TimeUnit.MILLISECONDS) //fix чтобы скролл работал после появления клавиатуры
                .subscribe(focus -> {
                    if(focus) {
                        mScrollView.post(() -> mScrollView.scrollTo(0, getScrollBottom()));
                    }
                })
        );


        // Retrive price
        compositeDisposable.add(Observable.combineLatest(
                mExchangesFieldObservable,
                mCoinFieldObservable,
                mPairFieldObservable,
                mDateFieldObservable,
                (exchange, coin, pair, dateInMillis) -> exchange > 0 && coin > 0 && pair > 0 && dateInMillis > 0)
                .subscribe(res -> {
                    if(res) {
                        retrivePrice();
                    } else {
                        mPriceView.setText(null);
                    }
                })
        );

        // Combine validators
        compositeDisposable.add(Observable.combineLatest(
                mExchangesFieldObservable,
                mCoinFieldObservable,
                mPairFieldObservable,
                mDateFieldObservable,
                mPriceFieldObservable,
                mAmountFieldObservable,
                mDescribtionFieldObservable,
                this::validate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onValid)
        );

        bindActions();


        //
        initLoaderManager();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();

        getSupportLoaderManager().destroyLoader(LOADER_PORTFOLIO_COINS);
        getSupportLoaderManager().destroyLoader(LOADER_COINS);
        getSupportLoaderManager().destroyLoader(LOADER_EXCHANGES);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == AutocompleteActivity.REQUEST_COIN && resultCode == RESULT_OK) {
            AutocompleteItem coin = data.getParcelableExtra(AutocompleteActivity.EXTRA_COIN);
            setCoin(coin);
        }

        if(requestCode == AutocompleteActivity.REQUEST_CURRENTEY && resultCode == RESULT_OK) {
            AutocompleteItem coin = data.getParcelableExtra(AutocompleteActivity.EXTRA_COIN);
            setPair(coin);
        }

        if(requestCode == AutocompleteActivity.REQUEST_EXCHANGE && resultCode == RESULT_OK) {
            AutocompleteItem exchange = data.getParcelableExtra(AutocompleteActivity.EXTRA_EXCHANGE);
            argExchangeId = exchange.getId();
            mExchangeComplete.setText(exchange.getName());
            mExchangesFieldObservable.onNext(exchange.getId());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == LOADER_PORTFOLIO_COINS) {
            return getPortfolioCoinsLoader();
        }

        if (id == LOADER_COINS) {
            return getCoinsLoader();
        }

        if (id == LOADER_EXCHANGES) {
            return getExchangesLoader();
        }

        throw new IllegalArgumentException("no id handled!");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (loader.getId() == LOADER_PORTFOLIO_COINS) { // Пара по умолчанию
            if(data != null && data.getCount() > 0) {
                onPortofolioCoinLoaded(data);
            }
            return;
        }

        if (loader.getId() == LOADER_COINS) { // Пара по умолчанию
            if(data != null && data.getCount() > 0) {
                if(mCurrenteyId > 0) {
                    return;
                }
                data.moveToFirst();
                int itemColumnIndex = data.getColumnIndexOrThrow(CryptoContract.CryptoCoins._ID);
                long defaultCoinId = data.getLong(itemColumnIndex);
                itemColumnIndex = data.getColumnIndexOrThrow(CryptoContract.CryptoCoins.COLUMN_NAME_NAME);
                String defaultCoinName = data.getString(itemColumnIndex);
                setPair(new AutocompleteItem(defaultCoinId, DEFAULT_SYMBOL, defaultCoinName));
            }
            return;
        }

        if (loader.getId() == LOADER_EXCHANGES) {
            if(data.getCount() > 0) {
                data.moveToFirst();
                int idColumnIndex = data.getColumnIndexOrThrow(CryptoContract.CryptoExchanges._ID);
                argExchangeId = data.getLong(idColumnIndex);
                int nameColumnIndex = data.getColumnIndexOrThrow(CryptoContract.CryptoExchanges.COLUMN_NAME_NAME);
                mExchangeComplete.setText(data.getString(nameColumnIndex));
                mExchangeComplete.clearFocus();
                mExchangesFieldObservable.onNext(argExchangeId);
            }
            return;
        }
        throw new IllegalArgumentException("no id handled!");
    }

    protected void onPortofolioCoinLoaded(Cursor data) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.transaction_menu_done:
                createTransaction();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.transaction, menu);
        mMenu = menu;
        return true;
    }

    protected void bindActions() {
        mPresenter = new TransactionPresenterAdd(getContentResolver());

        if(mTransactionButton == null) {
            return;
        }
        RxView.clicks(mTransactionButton).subscribe(v -> {
            createTransaction();
        });
    }

    protected void createTransaction() {
        mTransactionButton.setEnabled(false);
        mPresenter.updatePortfolioByTransaction(
                new PortfolioCoin(argPortfolioId, mCoinId, argExchangeId),
                new Transaction(mAmount, getPrice(), mDate, mDescription, mBasePrice)
        );
        finish();
    }

    protected void onValid(boolean res) {
        if(mTransactionButton == null) {
            return;
        }
        mTransactionButton.setEnabled(res);
        mMenu.findItem(R.id.transaction_menu_done).setEnabled(res);
    }

    protected boolean validate(long exchange, long coin, long pair, long dateInMillis, double price, double amount, int descriptionLength) {
        return exchange > 0 &&
                coin > 0 &&
                pair > 0 &&
                dateInMillis > 0 &&
                price > 0 &&
                descriptionLength < MAX_DESCRIPTION_LENGTH;
    }

    protected void initLoaderManager() {
        getSupportLoaderManager().restartLoader(LOADER_COINS, null, this);
        getSupportLoaderManager().restartLoader(LOADER_EXCHANGES, null, this);
    }

    protected Loader<Cursor> getPortfolioCoinsLoader() {
        return new CursorLoader(
                this,
                CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                null,
                CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins._ID + " = " + argPortfolioCoinId,
                null,
                null
        );
    }
    private Loader<Cursor> getCoinsLoader() {
        return new CursorLoader(
                this,
                CryptoContract.CryptoCoins.CONTENT_URI,
                CryptoContract.CryptoCoins.DEFAULT_PROJECTION,
                CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL + " = '" +  DEFAULT_SYMBOL + "'",
                null,
                null
        );
    }
    private Loader<Cursor> getExchangesLoader() {
        if(argExchangeId > 0) {
            return new CursorLoader(
                    this,
                    CryptoContract.CryptoExchanges.CONTENT_URI,
                    CryptoContract.CryptoExchanges.DEFAULT_PROJECTION,
                    CryptoContract.CryptoExchanges._ID + " = " + argExchangeId,
                    null,
                    null
            );
        } else {
            return new CursorLoader(
                    this,
                    CryptoContract.CryptoExchanges.CONTENT_URI,
                    CryptoContract.CryptoExchanges.DEFAULT_PROJECTION,
                    CryptoContract.CryptoExchanges.COLUMN_NAME_NAME + " = '" + DEFAULT_EXCHANGE + "'",
                    null,
                    null
            );
        }
    }

    protected void setCoin(AutocompleteItem coin) {
        mCoinId = coin.getId();
        mCoinComplete.setText(coin.getSymbol());
        mCoinSymbol = coin.getSymbol();
        mCoinFieldObservable.onNext(coin.getId());
        mAmountSymbolView.setText(coin.getSymbol());
    }

    protected double getPrice() {
        double price = mPricePerCoin;
        if (!mPriceSwitch.isChecked() && mAmount > 0) {
            price = mPriceInTotal / mAmount;
        }
        return price;
    }

    protected void setPair(AutocompleteItem coin) {
        mCurrenteyId = coin.getId();
        mCurrenteyComplete.setText(coin.getSymbol());
        mCurrenteySymbol = coin.getSymbol();
        mPriceSymbolView.setText(coin.getSymbol());
        mPairFieldObservable.onNext(coin.getId());
    }

    private void updateDateOfTransaction() {
        String myFormat = "dd.MM.yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        mDateView.setText(sdf.format(myCalendar.getTime()));
        mDate = myCalendar.getTimeInMillis()/1000;
        mDateFieldObservable.onNext(mDate);
    }

    private void retrivePrice() {

        if( mCoinSymbol.equals(mCurrenteySymbol)) {
            mPricePerCoin = 1;
            bindPriceView();
            return;
        }

        if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) == myCalendar.get(Calendar.DAY_OF_YEAR)) {
            retriveCurrentPrice();
            return;
        }

        retriveHistoricalPrice();

    }

    private void retriveCurrentPrice() {

        startProgressDialog();

        String fromSymbol = null;
        if(!mCoinSymbol.equals(DEFAULT_SYMBOL)) {
            fromSymbol = mCoinSymbol;
        }
        if(!mCurrenteySymbol.equals(DEFAULT_SYMBOL) && !mCoinSymbol.equals(DEFAULT_SYMBOL)) {
            fromSymbol += "," + mCurrenteySymbol;
        }

        String exchange = mExchangeComplete.getText().toString().trim();
        if(exchange.equals(DEFAULT_EXCHANGE)) {
            exchange = DEFAULT_EXCHANGE_ALIAS;
        }

        Observable<HashMap<String, Double>> pricesUSDTObservable =
                RestClientMinApi.INSTANCE.getClient().prices(mCoinSymbol, mCurrenteySymbol, exchange)
                        .subscribeOn(Schedulers.io())
                        .onErrorReturnItem(new HashMap<>());

        Observable<HashMap<String, Double>> pricesUSDObservable;
        if(mCurrenteySymbol.equals(DEFAULT_SYMBOL)) {
            pricesUSDObservable =
                    RestClientMinApi.INSTANCE.getClient().prices(mCoinSymbol, SYMBOL_USD, exchange)
                            .subscribeOn(Schedulers.io())
                            .onErrorReturnItem(new HashMap<>());
        } else {
            pricesUSDObservable = Observable.just(new HashMap<String, Double>());
        }


        //  Запрос базовых цен
        Observable<HashMap<String, HashMap<String, Double>>> pricesBaseObservable;
        if(mCoinSymbol.equals(DEFAULT_SYMBOL) || mCurrenteySymbol.equals(DEFAULT_SYMBOL)) { // Чтобы не отправлять дополнительных запросов
            pricesBaseObservable = Observable.just(new HashMap<String, HashMap<String, Double>>());
        } else {
            pricesBaseObservable =
                    RestClientMinApi.INSTANCE.getClient().priceMulti(fromSymbol, DEFAULT_SYMBOL, null)
                            .subscribeOn(Schedulers.io())
                            .onErrorReturnItem(new HashMap<>());
        }

        compositeDisposable.add(Observable.combineLatest(
                pricesUSDTObservable,
                pricesUSDObservable,
                pricesBaseObservable,
                this::setRetrievedPrice)
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            stopProgressDialog();
                            bindPriceView();
                        },
                        e -> {
                            stopProgressDialog();
                            if (e instanceof PairNotFoundException){
                                Toast.makeText(this, getString(R.string.transaction_pair_not_found), Toast.LENGTH_SHORT).show();
                                switchDefaultExchange();
                            }
                        })
        );

    }

    private void retriveHistoricalPrice() {
        startProgressDialog();

        String exchange = mExchangeComplete.getText().toString().trim();
        if(exchange.equals(DEFAULT_EXCHANGE)) {
            exchange = DEFAULT_EXCHANGE_ALIAS;
        }
        Observable<HashMap<String, HashMap<String, Double>>> pricesHistoricalObservable =
                RestClientMinApi.INSTANCE.getClient().pricesHistorical(mCoinSymbol, mCurrenteySymbol, Long.toString(mDate), exchange)
                        .subscribeOn(Schedulers.io());

        Observable<HashMap<String, HashMap<String, Double>>> pricesHistoricalBaseCoinObservable;
        if (mCoinSymbol.equals(DEFAULT_SYMBOL) || mCurrenteySymbol.equals(DEFAULT_SYMBOL)) {
            pricesHistoricalBaseCoinObservable = Observable.just(new HashMap<String, HashMap<String, Double>>());
        } else {
            pricesHistoricalBaseCoinObservable =
                    RestClientMinApi.INSTANCE.getClient().pricesHistorical(mCoinSymbol, DEFAULT_SYMBOL, Long.toString(mDate), null)
                            .subscribeOn(Schedulers.io());
        }

        Observable<HashMap<String, HashMap<String, Double>>> pricesHistoricalBasePairObservable;
        if (mCurrenteySymbol.equals(DEFAULT_SYMBOL)) {
            pricesHistoricalBasePairObservable = Observable.just(new HashMap<String, HashMap<String, Double>>());
        } else {
            pricesHistoricalBasePairObservable =
                    RestClientMinApi.INSTANCE.getClient().pricesHistorical(mCurrenteySymbol, DEFAULT_SYMBOL, Long.toString(mDate), null)
                            .subscribeOn(Schedulers.io());
        }

        compositeDisposable.add(Observable.combineLatest(
                pricesHistoricalObservable,
                pricesHistoricalBaseCoinObservable,
                pricesHistoricalBasePairObservable,
                (prices, pricesBaseCoin, pricesBasePair) -> {
                    HashMap<String, HashMap<String, Double>> pricesBase = new HashMap<>();
                    if(pricesBaseCoin.containsKey(mCoinSymbol)) {
                        pricesBase.put(mCoinSymbol, pricesBaseCoin.get(mCoinSymbol));
                    }
                    if(pricesBasePair.containsKey(mCurrenteySymbol)) {
                        pricesBase.put(mCurrenteySymbol, pricesBasePair.get(mCurrenteySymbol));
                    }
                    return setRetrievedPrice(prices.get(mCoinSymbol), new HashMap<>(), pricesBase);
                })
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            stopProgressDialog();
                            bindPriceView();
                        },
                        e -> {
                            stopProgressDialog();
                            Toast.makeText(this, getString(R.string.transaction_pair_not_found), Toast.LENGTH_SHORT).show();
                            switchDefaultExchange();
                        })
        );
    }

    private void switchDefaultExchange() {
        argExchangeId = 0;
        getSupportLoaderManager().restartLoader(LOADER_EXCHANGES, null, this);
    }

    private boolean setRetrievedPrice(HashMap<String, Double> prices, HashMap<String, Double> pricesUSD, HashMap<String, HashMap<String, Double>> pricesBase) {
        setRetrievedPricePerCoin(prices, pricesUSD);
        setRetrievedBasePrices(pricesBase);

        return true;
    }

    private void setRetrievedPricePerCoin(HashMap<String, Double> prices, HashMap<String, Double> pricesUSD) {
        mPricePerCoin = 0;
        if(prices.containsKey(mCurrenteySymbol)) {
            mPricePerCoin = prices.get(mCurrenteySymbol);
            return;
        }

        if(pricesUSD.containsKey(SYMBOL_USD)) {
            mPricePerCoin = pricesUSD.get(SYMBOL_USD);
        }

        if(mPricePerCoin == 0) {
            throw new PairNotFoundException();
        }
    }
    private void setRetrievedBasePrices(HashMap<String, HashMap<String, Double>> pricesBase) {
        if(!mCoinSymbol.equals(DEFAULT_SYMBOL) && mCurrenteySymbol.equals(DEFAULT_SYMBOL)) {
            mCoinPrice = mPricePerCoin;
            return;
        }

        if(mCoinSymbol.equals(DEFAULT_SYMBOL) && !mCurrenteySymbol.equals(DEFAULT_SYMBOL)) {
            mBasePrice = mPricePerCoin;
            return;
        }

        if(pricesBase.containsKey(mCoinSymbol)) {
            mCoinPrice = pricesBase.get(mCoinSymbol).get(DEFAULT_SYMBOL);
        }
        if(pricesBase.containsKey(mCurrenteySymbol)) {
            mBasePrice = pricesBase.get(mCurrenteySymbol).get(DEFAULT_SYMBOL);
        }
    }

    private void bindPriceView() {
        if (mPriceSwitch.isChecked()) {
            if (mPricePerCoin != 0) {
                mPriceView.setText(KeyboardHelper.format(mPricePerCoin));
            } else {
                mPriceView.setText(null);
            }
        }
    }

    protected void setPrice(double price) {
        if(mPriceSwitch.isChecked()) {
            mPricePerCoin = price;
        } else {
            mPriceInTotal = price;
        }

    }

    protected int getScrollBottom() {
        return mTransactionButton.getBottom();
    }
}
