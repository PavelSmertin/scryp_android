package com.start.crypto.android;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
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

    public static final String EXTRA_PORTFOLIO_ID               = "portfolio_id";
    public static final String EXTRA_PORTFOLIO_COIN_ID          = "portfolio_coin_id";
    public static final String EXTRA_COIN_ID                    = "coin_id";
    public static final String EXTRA_COIN_SYMBOL                = "coin_symbol";
    public static final String EXTRA_EXCHANGE_ID                = "exchange_id";
    public static final String EXTRA_TYPE                       = "type";

    public static final String DEFAULT_SYMBOL       = "USDT";
    public static final String DEFAULT_EXCHANGE    = "CCCAGG";
    private static final int MAX_DESCRIPTION_LENGTH = 160;

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
    private AutoTextExchangeAdapter mAdapterExchangeComplete;

    private Calendar myCalendar;

    private TransactionType     argTrasactionType = TransactionType.ADD;
    private long                argPortfolioCoinId;
    private long                argCoinId;
    private String              argCoinSymbol;
    private long                argPortfolioId = 1;
    private long                argExchangeId;

    private long    mCurrenteyId;
    private String  mCurrenteySymbol;

    private double  mAmount;
    private double  mAmountMax;
    private double  mPrice;
    private long    mDate;
    private String  mDescription;

    private double  mPortfolioCoinOriginal;
    private double  mPortfolioCoinPriceOriginal;

    private long    mPortfolioCurrenteyId;
    private double  mPortfolioCurrenteyOriginal;
    private double  mPortfolioCurrenteyPriceOriginal;

    private double  mBaseCoinPrice = 1;
    private double  mBaseCurrenteyPrice = 1;


    BehaviorSubject<Long> mPairFieldObservable              = BehaviorSubject.create();
    BehaviorSubject<Long> mExchangesFieldObservable         = BehaviorSubject.create();
    BehaviorSubject<Long> mDateFieldObservable              = BehaviorSubject.create();
    BehaviorSubject<Double> mPriceFieldObservable           = BehaviorSubject.create();
    BehaviorSubject<Double> mAmountFieldObservable          = BehaviorSubject.create();
    BehaviorSubject<Integer> mDescribtionFieldObservable    = BehaviorSubject.create();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    // Транзакция на добавление монеты
    public static void start(Context context, long portfolioId, long coinId, String coinSymbol) {
        Intent starter = new Intent(context, TransactionActivity.class);
        starter.putExtra(TransactionActivity.EXTRA_PORTFOLIO_ID, portfolioId);
        starter.putExtra(TransactionActivity.EXTRA_COIN_ID, coinId);
        starter.putExtra(TransactionActivity.EXTRA_COIN_SYMBOL, coinSymbol);
        context.startActivity(starter);
    }

    // Транзакция на Buy/Sell
    public static void start(Context context, long portfolioId, long portfolioCoinId, long coinId, String coinSymbol, long exchangeId, TransactionType type) {
        Intent starter = new Intent(context, TransactionActivity.class);
        starter.putExtra(EXTRA_PORTFOLIO_ID, portfolioId);
        starter.putExtra(EXTRA_PORTFOLIO_COIN_ID, portfolioCoinId);
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

        // Portfolio
        argPortfolioId = getIntent().getLongExtra(EXTRA_PORTFOLIO_ID, 0);
        if (argPortfolioId == 0) {
            finish();
            return;
        }

        // Coin
        argCoinId = getIntent().getLongExtra(EXTRA_COIN_ID, 0);
        if (argCoinId == 0) {
            finish();
            return;
        }

        // Portfolio coin
        argPortfolioCoinId = getIntent().getLongExtra(EXTRA_PORTFOLIO_COIN_ID, 0);


        argCoinSymbol = getIntent().getStringExtra(EXTRA_COIN_SYMBOL);
        if (argCoinSymbol == null) {
            finish();
            return;
        }
        setTitle(argCoinSymbol);


        // Transaction type
        if(getIntent().getSerializableExtra(EXTRA_TYPE) != null) {
            argTrasactionType = (TransactionType) getIntent().getSerializableExtra(EXTRA_TYPE);
        }


        // Coin pair (Currentey)
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(amount -> {
                    double amountDouble = 0;
                    try{
                        amountDouble = Double.parseDouble(amount.toString());
                        // Нельзя продать монет больше, чем их в портфеле
                        if(mAmountMax > 0 && amountDouble > mAmountMax) {
                            mAmountView.setError(getString(R.string.transaction_amount_error));
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(description -> {
                    mDescription = mDescribtionView.getText().toString();
                    if (mDescription.length() > MAX_DESCRIPTION_LENGTH) {
                        mDescribtionView.setError(getString(R.string.transaction_description_error));
                    }
                    mDescribtionFieldObservable.onNext(mDescription.length());

                });

        // Retrive price
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

        // Combine validators
        compositeDisposable.add(Observable.combineLatest(
                mExchangesFieldObservable,
                mPairFieldObservable,
                mDateFieldObservable,
                mPriceFieldObservable,
                mAmountFieldObservable,
                mDescribtionFieldObservable,
                (exchange, pair, dateInMillis, price, amount, descriptionLength) ->
                                exchange > 0 &&
                                pair > 0 &&
                                dateInMillis > 0 &&
                                Double.parseDouble(price.toString()) >= 0 &&
                                Double.parseDouble(amount.toString()) >= 0 && (Double.parseDouble(amount.toString()) <= mAmountMax || mAmountMax <= 0) &&
                                descriptionLength < MAX_DESCRIPTION_LENGTH)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    mAddTransactionButton.setEnabled(res);
                })
        );



        //
        RxView.clicks(mAddTransactionButton).subscribe(v -> {
            mAddTransactionButton.setEnabled(false);
            updatePortfolioByTransaction();
            createTransaction();
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
            data.moveToNext();
            ColumnsPortfolioCoin.ColumnsMap columnsMap = new ColumnsPortfolioCoin.ColumnsMap(data);

            mPortfolioCoinOriginal = data.getDouble(columnsMap.mOriginal);
            mPortfolioCoinPriceOriginal = data.getDouble(columnsMap.mColumnPriceOriginal);
            mBaseCoinPrice = data.getDouble(columnsMap.mColumnPriceNow);

            if(argTrasactionType == TransactionType.SELL) {
                mAmountMax = mPortfolioCoinOriginal;
                mAmountView.setText(String.format(Locale.US, "%.02f", mPortfolioCoinOriginal));
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
                argExchangeId = data.getLong(idColumnIndex);
                int nameColumnIndex = data.getColumnIndexOrThrow(CryptoContract.CryptoExchanges.COLUMN_NAME_NAME);
                mExchangeComplete.setText(data.getString(nameColumnIndex));
                mExchangesFieldObservable.onNext(argExchangeId);
            }
            return;
        }
        throw new IllegalArgumentException("no id handled!");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void initLoaderManager() {
        if(argTrasactionType == TransactionType.BUY || argTrasactionType == TransactionType.SELL) {
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

    private void updateDateOfTransaction() {
        String myFormat = "dd.MM.yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        mDateView.setText(sdf.format(myCalendar.getTime()));
        mDate = myCalendar.getTimeInMillis()/1000;
        mDateFieldObservable.onNext(mDate);
    }

    private void updatePortfolioByTransaction() {

        if (argTrasactionType == TransactionType.ADD) {
            Uri uriPortfoloioCoin = insertCoin();
            argPortfolioCoinId = newPortfolioCoin(uriPortfoloioCoin);
            return;
        }

        if (argTrasactionType == TransactionType.BUY) {
            updateCoin();
            return;
        }

        if (argTrasactionType == TransactionType.SELL ) {
            updateCoin();

            selectPortfolioCurrentey();
            if (mPortfolioCurrenteyId <= 0) {
                Uri uriPortfoloioCoin = insertCurrentey();
                mPortfolioCurrenteyId = newPortfolioCoin(uriPortfoloioCoin);
            } else {
                updateCurrentey();
            }
        }

    }

    private long newPortfolioCoin(Uri uriPortfoloioCoin) {
        Cursor cursor = getContentResolver().query(
                uriPortfoloioCoin,
                null,
                null,
                null,
                null
        );
        if(cursor != null) {
            cursor.moveToFirst();
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                int itemColumnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoPortfolioCoins._ID);
                return cursor.getLong(itemColumnIndex);
            }
            cursor.close();
        }
        throw new IllegalStateException("illegal new coin");
    }

    private void createTransaction() {
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_COIN_ID, argPortfolioCoinId);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_ID, argCoinId);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_ID, argPortfolioId);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_EXCHANGE_ID, argExchangeId);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_AMOUNT, mAmount);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PRICE, mPrice);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_DATETIME, mDate);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_DESCRIPTION, mDescription);

        if(argTrasactionType == TransactionType.SELL) {
            values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_CORRESPOND_ID, mCurrenteyId);
            values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_CURRENTEY_ID, mPortfolioCurrenteyId);
        }

        getContentResolver().insert(CryptoContract.CryptoTransactions.CONTENT_URI, values);
    }

    private Uri insertCoin() {
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID, argCoinId);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID, argPortfolioId);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID, argExchangeId);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL, mAmount);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_24H, mPrice * mBaseCurrenteyPrice);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW, mPrice * mBaseCurrenteyPrice);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, mPrice * mBaseCurrenteyPrice);
        return getContentResolver().insert(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values);
    }
    private void updateCoin() {
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL, getPortfolioCoinOriginal());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, getPortfolioCoinPrice());
        getContentResolver().update(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values, CryptoContract.CryptoPortfolioCoins._ID + " = " + argPortfolioCoinId, null);
    }

    private void selectPortfolioCurrentey() {
        if(mCurrenteyId <= 0) {
            return;
        }
        Cursor cursor = getContentResolver().query(
                CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                CryptoContract.CryptoPortfolioCoins.DEFAULT_PROJECTION,
                CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID + " = " + mCurrenteyId +
                        " AND " + CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." +CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID + " = " + argPortfolioId,
                null,
                null);

        if(cursor != null) {
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                ColumnsPortfolioCoin.ColumnsMap columnsMap = new ColumnsPortfolioCoin.ColumnsMap(cursor);

                mPortfolioCurrenteyOriginal = cursor.getDouble(columnsMap.mOriginal);
                mPortfolioCurrenteyPriceOriginal = cursor.getDouble(columnsMap.mColumnPriceOriginal);
                mBaseCurrenteyPrice = cursor.getDouble(columnsMap.mColumnPriceNow);
                mPortfolioCurrenteyId = cursor.getLong(columnsMap.mColumnId);
            }
            cursor.close();
        }
    }
    private Uri insertCurrentey() {
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID, mCurrenteyId);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID, argPortfolioId);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID, argExchangeId);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL, mAmount * mPrice);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_24H, mBaseCoinPrice / mPrice);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW, mBaseCoinPrice / mPrice);
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, mBaseCoinPrice / mPrice);
        return getContentResolver().insert(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values);
    }
    private void updateCurrentey() {
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL, getPortfolioCurrenteyOriginal());
        values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, getPortfolioCurrenteyPrice());
        getContentResolver().update(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values, CryptoContract.CryptoPortfolioCoins._ID + " = " + mPortfolioCurrenteyId, null);
    }


    // Количество монеты в портфолио.
    // Меняется при любой транзакции
    private double getPortfolioCoinOriginal() {
        // берем текущее количество вычитаем или прибавляем сумму из транзакции
        if(argTrasactionType == TransactionType.BUY) {
            return mPortfolioCoinOriginal + mAmount;
        }
        if(argTrasactionType == TransactionType.SELL) {
            return mPortfolioCoinOriginal - mAmount;
        }
        throw new IllegalStateException("unhundled type transaction");
    }
    private double getPortfolioCurrenteyOriginal() {
        // берем текущее количество вычитаем или прибавляем сумму из транзакции
        if (argTrasactionType == TransactionType.SELL) {
            return mPortfolioCurrenteyOriginal + mAmount * mPrice;
        }
        if (argTrasactionType == TransactionType.BUY) {
            return mPortfolioCurrenteyOriginal - mAmount * mPrice;
        }
        throw new IllegalStateException("unhundled type transaction");

    }

    // Цена закупа монеты в портфолио. Если было несколько транзакций, считается среднее.
    // Меняется при любой транзакции
    private double getPortfolioCoinPrice() {
        double originalSum = mPortfolioCoinOriginal * mPortfolioCoinPriceOriginal; // in base currency
        double transactionSum = mAmount * mBaseCoinPrice; // in base currency

        if (argTrasactionType == TransactionType.BUY) {
            return (originalSum + transactionSum) / (mPortfolioCoinOriginal + mAmount);
        }
        if (argTrasactionType == TransactionType.SELL) {
            if(mPortfolioCoinOriginal - mAmount <= 0) {
                return mPortfolioCoinPriceOriginal;
            }
            return (originalSum - transactionSum) / (mPortfolioCoinOriginal - mAmount);
        }
        throw new IllegalStateException("unhundled type transaction");
    }
    private double getPortfolioCurrenteyPrice() {
        double originalSum = mPortfolioCurrenteyOriginal * mPortfolioCurrenteyPriceOriginal; // in base currency
        double transactionSum = mAmount * mPrice * mBaseCurrenteyPrice; // in base currency

        if (argTrasactionType == TransactionType.SELL) {
            return (originalSum + transactionSum) / (mPortfolioCurrenteyOriginal + mAmount * mPrice);
        }
        if (argTrasactionType == TransactionType.BUY){
            return (originalSum - transactionSum) / (mPortfolioCurrenteyOriginal - mAmount * mPrice);
        }
        throw new IllegalStateException("unhundled type transaction");
    }

    private void retrivePrice() {
        startProgressDialog(R.string.all_loading);
        if(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) == myCalendar.get(Calendar.DAY_OF_YEAR)) {
            RestClientMinApi.INSTANCE.getClient().prices(argCoinSymbol, mCurrenteySymbol, null)
                    .compose(bindUntilEvent(ActivityEvent.PAUSE))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> {
                                stopProgressDialog();
                                mPriceView.setText(String.format(Locale.US, "%.2f", response.get(mCurrenteySymbol)));
                            },
                            e -> {
                                stopProgressDialog();
                                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
            return;
        }

        RestClientMinApi.INSTANCE.getClient().pricesHistorical(argCoinSymbol, mCurrenteySymbol, Long.toString(mDate), null)
                .compose(bindUntilEvent(ActivityEvent.PAUSE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            stopProgressDialog();
                            HashMap<String, Double> prices = response.get(argCoinSymbol);
                            mPriceView.setText(String.format(Locale.US, "%.2f", prices.get(mCurrenteySymbol)));
                        },
                        e -> {
                            stopProgressDialog();
                            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                );
    }

}
