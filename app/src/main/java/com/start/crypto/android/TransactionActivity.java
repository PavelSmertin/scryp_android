package com.start.crypto.android;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.start.crypto.android.api.RestClientMinApi;
import com.start.crypto.android.data.ColumnsPortfolioCoin;
import com.start.crypto.android.data.CryptoContract;
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


public class TransactionActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_COIN_ID        = "coin_id";
    public static final String EXTRA_COIN_SYMBOL    = "coin_symbol";
    public static final String EXTRA_EXCHANGE_ID    = "exchange_id";
    public static final String EXTRA_TYPE           = "type";

    private static final String DEFAULT_SYMBOL      = "USDT";
    private static final String DEFAULT_EXCHANGE    = "CCCAGG";

    @BindView(R.id.currentey_select)        Spinner mCurrenteySelect;
    @BindView(R.id.currentey_complete)      AutoCompleteTextView mCurrenteyComplete;
    @BindView(R.id.clear_coin_button)       ImageView mClearCoinButton;
    @BindView(R.id.exchange_complete)       AutoCompleteTextView mExchangeComplete;
    @BindView(R.id.clear_exchange_button)   ImageView mClearExchangeButton;
    @BindView(R.id.amount)                  EditText mAmountView;
    @BindView(R.id.price)                   EditText mPriceView;
    @BindView(R.id.date)                    EditText mDateView;
    @BindView(R.id.describtion)             EditText mDescribtionView;
    @BindView(R.id.add_transaction)         Button mAddTransactionButton;
    @BindView(R.id.currentey_select_label)  TextView mCurrenteyLabelView;
    @BindView(R.id.scroll_view)             ScrollView mScrollView;

    private AutoTextCoinAdapter     mAdapterCoinComplete;
    private SimpleCursorAdapter     mAdapterCoinSpinner;
    private AutoTextExchangeAdapter mAdapterExchangeComplete;

    private Calendar myCalendar;

    private long    mExchangeId;
    private long    mPortfolioId = 1;
    private long    mCoinId;
    private String  mCoinSymbol;
    private long    mCurrenteyId;
    private String  mCurrenteySymbol;


    private TransactionType mTrasactionType = TransactionType.ADD;

    private double  mAmount;
    private double  mAmountMax;
    private double  mPrice;
    private long    mDate;
    private String  mDescription;

    private double mPortfolioCoinOriginal;
    private double mPortfolioCoinPriceOriginal;
    private double mPortfolioCurrenteyOriginal;
    private double mPortfolioCurrenteyPriceOriginal;
    private double mBaseCoinPrice = 1;
    private double mBaseCurrenteyPrice = 1;

    BehaviorSubject<Long> mPairFieldObservable          = BehaviorSubject.create();
    BehaviorSubject<Long> mExchangesFieldObservable     = BehaviorSubject.create();
    BehaviorSubject<Long> mDateFieldObservable          = BehaviorSubject.create();
    BehaviorSubject<Double> mPriceFieldObservable       = BehaviorSubject.create();
    BehaviorSubject<Double> mAmountFieldObservable      = BehaviorSubject.create();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    public static void start(Context context, long coinId, String coinSymbol) {
        Intent starter = new Intent(context, TransactionActivity.class);
        starter.putExtra(TransactionActivity.EXTRA_COIN_ID, coinId);
        starter.putExtra(TransactionActivity.EXTRA_COIN_SYMBOL, coinSymbol);
        context.startActivity(starter);
    }

    public static void start(Context context, long coinId, String coinSymbol, long exchangeId, TransactionType type) {
        Intent starter = new Intent(context, TransactionActivity.class);
        starter.putExtra(EXTRA_COIN_ID, coinId);
        starter.putExtra(EXTRA_COIN_SYMBOL, coinSymbol);
        starter.putExtra(EXTRA_EXCHANGE_ID, exchangeId);
        starter.putExtra(EXTRA_TYPE, type);
        context.startActivity(starter);
    }

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_transaction);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Coin
        mCoinId = getIntent().getLongExtra(EXTRA_COIN_ID, 0);
        if (mCoinId == 0) {
            finish();
            return;
        }
        mCoinSymbol = getIntent().getStringExtra(EXTRA_COIN_SYMBOL);
        if (mCoinSymbol == null) {
            finish();
            return;
        }
        setTitle(mCoinSymbol);


        // Transaction type
        if(getIntent().getSerializableExtra(EXTRA_TYPE) != null) {
            mTrasactionType = (TransactionType) getIntent().getSerializableExtra(EXTRA_TYPE);
        }


        // Coin pair (Currentey)
        if(mTrasactionType != TransactionType.BUY) {
            mAdapterCoinComplete = new AutoTextCoinAdapter(this);
            mCurrenteyComplete.setAdapter(mAdapterCoinComplete);
            compositeDisposable.add(RxAutoCompleteTextView.itemClickEvents(mCurrenteyComplete)
                    .retry()
                    .subscribe(item -> {
                        mCurrenteyId = item.id();
                        Cursor cursor = (Cursor) mAdapterCoinComplete.getItem(item.position());
                        int itemColumnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL);
                        mCurrenteySymbol = cursor.getString(itemColumnIndex);
                        mPairFieldObservable.onNext(item.id());
                    })
            );
            compositeDisposable.add(RxView.clicks(mClearCoinButton)
                    .subscribe(o -> {
                        mCurrenteyId = 0;
                        mCurrenteySymbol = null;
                        mCurrenteyComplete.setText("");
                        mPairFieldObservable.onNext(0L);
                    })
            );
            mScrollView.post(() -> mScrollView.scrollTo(0, mCurrenteyComplete.getBottom()));
        }

        if(mTrasactionType == TransactionType.BUY) {

            mCurrenteyComplete.setVisibility(View.GONE);
            mClearCoinButton.setVisibility(View.GONE);
            mCurrenteySelect.setVisibility(View.VISIBLE);

            mAdapterCoinSpinner = new SimpleCursorAdapter(this,
                    android.R.layout.simple_spinner_item,
                    null,
                    new String[]{CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL},
                    new int[]{android.R.id.text1}, 0);

            mAdapterCoinSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mCurrenteySelect.setAdapter(mAdapterCoinSpinner);

            mCurrenteySelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mCurrenteyId = id;
                    Cursor cursor = (Cursor) mAdapterCoinSpinner.getItem(position);
                    int symbolColumnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL);
                    mCurrenteySymbol = cursor.getString(symbolColumnIndex);
                    mPairFieldObservable.onNext(id);

                    int amountColumnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL);
                    mAmountMax = cursor.getDouble(amountColumnIndex);
                    mAmount = cursor.getDouble(amountColumnIndex);
                    mAmountView.setText(String.format(Locale.US, "%.02f", mAmount));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }


        // Exchange
        mExchangeId = getIntent().getLongExtra(EXTRA_EXCHANGE_ID, 0);

        mAdapterExchangeComplete = new AutoTextExchangeAdapter(this);
        mExchangeComplete.setAdapter(mAdapterExchangeComplete);
        compositeDisposable.add(RxAutoCompleteTextView.itemClickEvents(mExchangeComplete)
                .retry()
                .subscribe(item -> {
                    mExchangeId = item.id();
                    mExchangesFieldObservable.onNext(item.id());
                })
        );
        compositeDisposable.add(RxView.clicks(mClearExchangeButton)
                .subscribe(o -> {
                    mExchangeId = 0;
                    mExchangeComplete.setText("");
                    mExchangesFieldObservable.onNext(0L);
                })
        );

        // Price
        RxTextView.textChanges(mPriceView)
                .debounce(100, TimeUnit.MILLISECONDS)
                .subscribe(price -> {
                    double priceDouble = 0;
                    try{
                        priceDouble = Double.parseDouble(price.toString());
                    }catch(NumberFormatException e){
                        // not double
                    } finally {
                        mPrice = priceDouble;
                        mPriceFieldObservable.onNext(priceDouble);
                    }
                });

        // Amount
        RxTextView.textChanges(mAmountView)
                .debounce(100, TimeUnit.MILLISECONDS)
                .subscribe(amount -> {
                    double amountDouble = 0;
                    try{
                        amountDouble = Double.parseDouble(amount.toString());
                    } catch(NumberFormatException e){
                        // not double
                    } finally {
                        mAmount = amountDouble;
                        mAmountFieldObservable.onNext(amountDouble);
                    }
                });
        mAmountView.setText(String.format(Locale.US, "%.02f", 0D));


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
                            TransactionActivity.this,
                            date,
                            myCalendar.get(Calendar.YEAR),
                            myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)
                    ).show();
                })
        );
        updateDateOfTransaction();

        // Description
        RxTextView.textChanges(mDescribtionView)
                .debounce(100, TimeUnit.MILLISECONDS)
                .subscribe(description -> {
                    mDescription = mDescribtionView.getText().toString();
                });

        // Combine validators
        compositeDisposable.add(Observable.combineLatest(
                mExchangesFieldObservable,
                mPairFieldObservable,
                mDateFieldObservable,
                (exchange, pair, dateInMillis) -> exchange > 0 && pair > 0 && dateInMillis > 0)
                .subscribe(res -> {
                    if(res) {
                        retrivePrice();
                    } else {
                        mPriceView.setText(null);
                    }
                })
        );
        compositeDisposable.add(Observable.combineLatest(
                mExchangesFieldObservable,
                mPairFieldObservable,
                mDateFieldObservable,
                mPriceFieldObservable,
                mAmountFieldObservable,
                (exchange, pair, dateInMillis, price, amount) ->
                        exchange > 0 &&
                        pair > 0 &&
                        dateInMillis > 0 &&
                        Double.parseDouble(price.toString()) >= 0 &&
                        Double.parseDouble(amount.toString()) >= 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    mAddTransactionButton.setEnabled(res);
                })
        );



        //
        RxView.clicks(mAddTransactionButton).subscribe(v -> {
            mAddTransactionButton.setEnabled(false);
            addTransaction();
            finish();
        });


        //
        initLoaderManager();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
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

        if (loader.getId() == CryptoContract.LOADER_PORTFOLIO_COINS) {
            if(mAdapterCoinSpinner != null && data.getCount() > 0) {
                mAdapterCoinSpinner.changeCursor(data);
            }
            return;
        }

        if (loader.getId() == CryptoContract.LOADER_COINS) {
            if(data.getCount() > 0) {
                data.moveToFirst();
                int itemColumnIndex = data.getColumnIndexOrThrow(CryptoContract.CryptoCoins._ID);
                mCurrenteyId = data.getLong(itemColumnIndex);
                mCurrenteySymbol = DEFAULT_SYMBOL;
                mCurrenteyComplete.setText(DEFAULT_SYMBOL);
                mPairFieldObservable.onNext(mCurrenteyId);
            }
            return;
        }

        if (loader.getId() == CryptoContract.LOADER_EXCHANGES) {
            if(data.getCount() > 0) {
                data.moveToFirst();
                int idColumnIndex = data.getColumnIndexOrThrow(CryptoContract.CryptoExchanges._ID);
                mExchangeId = data.getLong(idColumnIndex);
                int nameColumnIndex = data.getColumnIndexOrThrow(CryptoContract.CryptoExchanges.COLUMN_NAME_NAME);
                mExchangeComplete.setText(data.getString(nameColumnIndex));
                mExchangesFieldObservable.onNext(mExchangeId);
            }
            return;
        }
        throw new IllegalArgumentException("no id handled!");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == CryptoContract.LOADER_PORTFOLIO_COINS) {
            mAdapterCoinSpinner.changeCursor(null);
        }
    }

    private void initLoaderManager() {
        if(mTrasactionType == TransactionType.BUY) {
            getSupportLoaderManager().restartLoader(CryptoContract.LOADER_PORTFOLIO_COINS, null, this);
        }
        getSupportLoaderManager().restartLoader(CryptoContract.LOADER_COINS, null, this);
        getSupportLoaderManager().restartLoader(CryptoContract.LOADER_EXCHANGES, null, this);
    }
    private Loader<Cursor> getPortfolioCoinsLoader() {
        return new CursorLoader(
                this,
                CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                null,
                CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL + " != '" + mCoinSymbol + "'",
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
        if(mExchangeId > 0) {
            return new CursorLoader(
                    this,
                    CryptoContract.CryptoExchanges.CONTENT_URI,
                    CryptoContract.CryptoExchanges.DEFAULT_PROJECTION,
                    CryptoContract.CryptoExchanges._ID + " = " + mExchangeId,
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

    private void updateDateOfTransaction() {
        String myFormat = "dd.MM.yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        mDateView.setText(sdf.format(myCalendar.getTime()));
        mDate = myCalendar.getTimeInMillis()/1000;
        mDateFieldObservable.onNext(mDate);
    }

    private void addTransaction() {
        if(mCurrenteyId <= 0) {
            throw new IllegalStateException("not selsected pair for transaction");
        }

        // create transaction
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_ID, mCoinId);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_ID, mPortfolioId);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_CORRESPOND_ID, mCurrenteyId);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_EXCHANGE_ID, mExchangeId);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_AMOUNT, mAmount);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PRICE, mPrice);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_DATETIME, mDate);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_DESCRIPTION, mDescription);
        getContentResolver().insert(CryptoContract.CryptoTransactions.CONTENT_URI, values);

        // insert coin to portfolio
        long portfolioCoinId = checkPortfolioCoins();
        long portfolioCurrenteyId = checkPortfolioCurrentey();

        // create portfolio coin
        if (portfolioCoinId <= 0) {
            insertCoin();
        }
        if (portfolioCurrenteyId <= 0 && mTrasactionType != TransactionType.ADD) {
            insertCurrentey();
        }

        if (mTrasactionType == TransactionType.BUY || mTrasactionType == TransactionType.SELL ) {
            // update portfolio coin
            if (portfolioCoinId > 0) {
                values = new ContentValues();
                values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL, getPortfolioCoinOriginal());
                values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, getPortfolioCoinPrice());
                getContentResolver().update(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values, CryptoContract.CryptoPortfolioCoins._ID + " = " + portfolioCoinId, null);
            }

            if (portfolioCurrenteyId > 0) {
                values = new ContentValues();
                values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL, getPortfolioCurrenteyOriginal());
                values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, getPortfolioCurrenteyPrice());
                getContentResolver().update(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values, CryptoContract.CryptoPortfolioCoins._ID + " = " + portfolioCurrenteyId, null);
            }
        }

    }

    private void insertCoin() {
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID, mCoinId);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID, mPortfolioId);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID, mExchangeId);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL, mAmount);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_24H, mPrice * mBaseCurrenteyPrice);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW, mPrice * mBaseCurrenteyPrice);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, mPrice * mBaseCurrenteyPrice);
        getContentResolver().insert(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values);
    }

    private void insertCurrentey() {
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID, mCurrenteyId);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID, mPortfolioId);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID, mExchangeId);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL, mAmount * mPrice);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_24H, mBaseCoinPrice / mPrice);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW, mBaseCoinPrice / mPrice);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, mBaseCoinPrice / mPrice);
        getContentResolver().insert(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values);
    }

    private long checkPortfolioCoins() {
        Cursor cursor = getContentResolver().query(
                CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                CryptoContract.CryptoPortfolioCoins.DEFAULT_PROJECTION,
                CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID + " = " + mCoinId +
                    " AND " + CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." +CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID + " = " + mPortfolioId,
                null,
                CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins._ID + " ASC");

        if(cursor.getCount() > 0) {
            cursor.moveToNext();
            ColumnsPortfolioCoin.ColumnsMap columnsMap = new ColumnsPortfolioCoin.ColumnsMap(cursor);

            mPortfolioCoinOriginal = cursor.getDouble(columnsMap.mOriginal);
            mPortfolioCoinPriceOriginal = cursor.getDouble(columnsMap.mColumnPriceOriginal);

            mBaseCoinPrice = cursor.getDouble(columnsMap.mColumnPriceNow);

            return cursor.getLong(columnsMap.mColumnId);
        }
        return -1;
    }

    private long checkPortfolioCurrentey() {
        if(mCurrenteyId <= 0) {
            return -1;
        }
        Cursor cursor = getContentResolver().query(
                CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                CryptoContract.CryptoPortfolioCoins.DEFAULT_PROJECTION,
                CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID + " = " + mCurrenteyId +
                        " AND " + CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." +CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID + " = " + mPortfolioId,
                null,
                CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins._ID + " ASC");

        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToNext();
            ColumnsPortfolioCoin.ColumnsMap columnsMap = new ColumnsPortfolioCoin.ColumnsMap(cursor);

            mPortfolioCurrenteyOriginal = cursor.getDouble(columnsMap.mOriginal);
            mPortfolioCurrenteyPriceOriginal = cursor.getDouble(columnsMap.mColumnPriceOriginal);

            mBaseCurrenteyPrice = cursor.getDouble(columnsMap.mColumnPriceNow);


            return cursor.getLong(columnsMap.mColumnId);
        }

        return -1;
    }

    // Количество монеты в портфолио.
    // Меняется при любой транзакции
    private double getPortfolioCoinOriginal() {
        // берем текущее количество вычитаем или прибавляем сумму из транзакции
        if(mTrasactionType == TransactionType.BUY) {
            return mPortfolioCoinOriginal + mAmount;
        }
        if(mTrasactionType == TransactionType.SELL) {
            return mPortfolioCoinOriginal - mAmount;
        }
        throw new IllegalStateException("unhundled type transaction");
    }
    private double getPortfolioCurrenteyOriginal() {
        // берем текущее количество вычитаем или прибавляем сумму из транзакции
        if (mTrasactionType == TransactionType.SELL) {
            return mPortfolioCurrenteyOriginal + mAmount * mPrice;
        }
        if (mTrasactionType == TransactionType.BUY) {
            return mPortfolioCurrenteyOriginal - mAmount * mPrice;
        }
        throw new IllegalStateException("unhundled type transaction");

    }

    // Цена закупа монеты в портфолио. Если было несколько транзакций, считается среднее.
    // Меняется при любой транзакции
    private double getPortfolioCoinPrice() {
        double originalSum = mPortfolioCoinOriginal * mPortfolioCoinPriceOriginal; // in base currency
        double transactionSum = mAmount * mBaseCoinPrice; // in base currency

        if (mTrasactionType  == TransactionType.BUY) {
            return (originalSum + transactionSum) / (mPortfolioCoinOriginal + mAmount);
        }
        if (mTrasactionType == TransactionType.SELL) {
            return (originalSum - transactionSum) / (mPortfolioCoinOriginal - mAmount);
        }
        throw new IllegalStateException("unhundled type transaction");
    }
    private double getPortfolioCurrenteyPrice() {
        double originalSum = mPortfolioCurrenteyOriginal * mPortfolioCurrenteyPriceOriginal; // in base currency
        double transactionSum = mAmount * mPrice * mBaseCurrenteyPrice; // in base currency

        if (mTrasactionType == TransactionType.SELL) {
            return (originalSum + transactionSum) / (mPortfolioCurrenteyOriginal + mAmount * mPrice);
        }
        if (mTrasactionType  == TransactionType.BUY){
            return (originalSum - transactionSum) / (mPortfolioCurrenteyOriginal - mAmount * mPrice);
        }
        throw new IllegalStateException("unhundled type transaction");
    }

    private void retrivePrice() {
        startProgressDialog(R.string.all_loading);
        RestClientMinApi.INSTANCE.getClient().pricesHistorical(mCoinSymbol, mCurrenteySymbol, Long.toString(mDate), null)
                .compose(bindUntilEvent(ActivityEvent.PAUSE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            stopProgressDialog();
                            HashMap<String, Double> prices = response.get(mCoinSymbol);
                            mPriceView.setText(String.format(Locale.US, "%.2f", prices.get(mCurrenteySymbol)));
                        },
                        e -> {
                            stopProgressDialog();
                            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                );
    }

}
