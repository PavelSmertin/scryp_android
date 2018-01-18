package com.start.crypto.android;

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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.start.crypto.android.api.RestClientMinApi;
import com.start.crypto.android.api.model.Coin;
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

    public static final String DEFAULT_SYMBOL           = "USDT";
    public static final String DEFAULT_EXCHANGE         = "CCCAGG";
    private static final int MAX_DESCRIPTION_LENGTH     = 160;

    @BindView(R.id.scroll_container)        View mScrollContainer;
    @BindView(R.id.currentey_complete)      EditText mCurrenteyComplete;
    @BindView(R.id.clear_coin_button)       ImageView mClearCoinButton;
    @BindView(R.id.exchange_complete)       AutoCompleteTextView mExchangeComplete;
    @BindView(R.id.clear_exchange_button)   ImageView mClearExchangeButton;
    @BindView(R.id.amount)                  EditText mAmountView;
    @BindView(R.id.price)                   EditText mPriceView;
    @BindView(R.id.date)                    EditText mDateView;
    @BindView(R.id.describtion)             EditText mDescribtionView;
    @BindView(R.id.currentey_select_label)  TextView mCurrenteyLabelView;
    @BindView(R.id.scroll_view)             ScrollView mScrollView;
    @BindView(R.id.coin_complete)           EditText mCoinComplete;
    @BindView(R.id.clear_currentey_button)  ImageView mClearCurrenteyButton;
    @BindView(R.id.price_switch)            Switch mPriceSwitch;

    @Nullable @BindView(R.id.add_transaction)         Button mTransactionButton;

    private AutoTextExchangeAdapter mAdapterExchangeComplete;

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
                        CoinAutocompleteActivity.start(this, options, CoinAutocompleteActivity.REQUEST_COIN);
                    } else {
                        CoinAutocompleteActivity.start(this, CoinAutocompleteActivity.REQUEST_COIN);
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
                        CoinAutocompleteActivity.start(this, options, CoinAutocompleteActivity.REQUEST_CURRENTEY);
                    } else {
                        CoinAutocompleteActivity.start(this, CoinAutocompleteActivity.REQUEST_CURRENTEY);
                    }
                });

        // Exchange
        argExchangeId = getIntent().getLongExtra(EXTRA_EXCHANGE_ID, 0);

        mAdapterExchangeComplete = new AutoTextExchangeAdapter(this);
        mExchangeComplete.setAdapter(mAdapterExchangeComplete);
        compositeDisposable.add(RxAutoCompleteTextView.itemClickEvents(mExchangeComplete)
                .retry()
                .subscribe(item -> {
                    argExchangeId = item.id();
                    mExchangesFieldObservable.onNext(item.id());
                })
        );
        compositeDisposable.add(RxView.clicks(mClearExchangeButton)
                .subscribe(o -> {
                    argExchangeId = 0;
                    mExchangeComplete.setText("");
                    mExchangesFieldObservable.onNext(0L);
                })
        );
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
            if(isChecked) {
                mPriceView.setText(KeyboardHelper.format(mPricePerCoin));
            } else {
                mPriceView.setText(KeyboardHelper.format(mPriceInTotal));
            }
        });

        // Amount
        mAmountView.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus && mAmountView.getText().toString().equals("0.00")) {
                mAmountView.setText(null);
            }
            if(!hasFocus && mAmountView.getText().length() == 0) {
                mAmountView.setText(String.format(Locale.US, "%.02f", mAmount));
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CoinAutocompleteActivity.REQUEST_COIN && resultCode == RESULT_OK) {
            Coin coin = data.getParcelableExtra(CoinAutocompleteActivity.EXTRA_COIN);
            setCoin(coin);
        }

        if(requestCode == CoinAutocompleteActivity.REQUEST_CURRENTEY && resultCode == RESULT_OK) {
            Coin coin = data.getParcelableExtra(CoinAutocompleteActivity.EXTRA_COIN);
            setPair(coin);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == CryptoContract.LOADER_PORTFOLIO_COINS) {
            return getPortfolioCoinsLoader();
        }

        if (id == CryptoContract.LOADER_COINS) {
            return getCoinsLoader();
        }

        if (id == CryptoContract.LOADER_EXCHANGES) {
            return getExchangesLoader();
        }

        throw new IllegalArgumentException("no id handled!");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (loader.getId() == CryptoContract.LOADER_PORTFOLIO_COINS) { // Пара по умолчанию
            onPortofolioCoinLoaded(data);
            return;
        }

        if (loader.getId() == CryptoContract.LOADER_COINS) { // Пара по умолчанию
            if(data.getCount() > 0) {
                if(mCurrenteyId > 0) {
                    return;
                }
                data.moveToFirst();
                int itemColumnIndex = data.getColumnIndexOrThrow(CryptoContract.CryptoCoins._ID);
                long defaultCoinId = data.getLong(itemColumnIndex);
                itemColumnIndex = data.getColumnIndexOrThrow(CryptoContract.CryptoCoins.COLUMN_NAME_NAME);
                String defaultCoinName = data.getString(itemColumnIndex);
                setPair(new Coin(defaultCoinId, DEFAULT_SYMBOL, defaultCoinName));
            }
            return;
        }

        if (loader.getId() == CryptoContract.LOADER_EXCHANGES) {
            if(data.getCount() > 0) {
                data.moveToFirst();
                int idColumnIndex = data.getColumnIndexOrThrow(CryptoContract.CryptoExchanges._ID);
                argExchangeId = data.getLong(idColumnIndex);
                int nameColumnIndex = data.getColumnIndexOrThrow(CryptoContract.CryptoExchanges.COLUMN_NAME_NAME);
                mExchangeComplete.setText(data.getString(nameColumnIndex));
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
                price >= 0 &&
                descriptionLength < MAX_DESCRIPTION_LENGTH;
    }

    protected void initLoaderManager() {
        getSupportLoaderManager().restartLoader(CryptoContract.LOADER_COINS, null, this);
        getSupportLoaderManager().restartLoader(CryptoContract.LOADER_EXCHANGES, null, this);
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

    protected void setCoin(Coin coin) {
        mCoinId = coin.getId();
        mCoinComplete.setText(coin.getSymbol());
        mCoinSymbol = coin.getSymbol();
        mCoinFieldObservable.onNext(coin.getId());
    }

    protected double getPrice() {
        double price = mPricePerCoin;
        if (!mPriceSwitch.isChecked() && mAmount > 0) {
            price = mPriceInTotal / mAmount;
        }
        return price;
    }

    private void setPair(Coin coin) {
        mCurrenteyId = coin.getId();
        mCurrenteyComplete.setText(coin.getSymbol());
        mCurrenteySymbol = coin.getSymbol();
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
        startProgressDialog(R.string.all_loading);
        if(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) == myCalendar.get(Calendar.DAY_OF_YEAR)) {
            RestClientMinApi.INSTANCE.getClient().prices(
                    mCurrenteySymbol,
                    mCurrenteySymbol.equals(DEFAULT_SYMBOL) ? mCoinSymbol : mCoinSymbol + "," + DEFAULT_SYMBOL,
                    null
            )
                    .compose(bindUntilEvent(ActivityEvent.PAUSE))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            prices -> {
                                stopProgressDialog();
                                setRetrievedPrice(prices);
                            },
                            e -> {
                                stopProgressDialog();
                                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
            return;
        }

        RestClientMinApi.INSTANCE.getClient().pricesHistorical(
                mCurrenteySymbol,
                mCurrenteySymbol.equals(DEFAULT_SYMBOL) ? mCoinSymbol : mCoinSymbol + "," + DEFAULT_SYMBOL,
                Long.toString(mDate),
                null
        )
                .compose(bindUntilEvent(ActivityEvent.PAUSE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            stopProgressDialog();
                            HashMap<String, Double> prices = response.get(mCurrenteySymbol);
                            setRetrievedPrice(prices);
                        },
                        e -> {
                            stopProgressDialog();
                            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                );

    }

    private void setRetrievedPrice(HashMap<String, Double> prices) {
        if(!mCurrenteySymbol.equals(DEFAULT_SYMBOL)) {
            mBasePrice = 1 / prices.get(DEFAULT_SYMBOL);
        }
        mPricePerCoin = 1/prices.get(mCoinSymbol);
        if(mPriceSwitch.isChecked()) {
            mPriceView.setText(KeyboardHelper.format(mPricePerCoin));
        }
    }

    private void setPrice(double price) {
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
