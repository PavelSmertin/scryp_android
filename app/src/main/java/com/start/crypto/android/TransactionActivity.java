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
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView;
import com.start.crypto.android.data.ColumnsExchange;
import com.start.crypto.android.data.ColumnsPortfolioCoin;
import com.start.crypto.android.data.CryptoContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import io.reactivex.disposables.CompositeDisposable;


public class TransactionActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_COIN_ID        = "coin_id";
    public static final String EXTRA_EXCHANGE_ID    = "exchange_id";
    public static final String EXTRA_TYPE           = "type";

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

    private AutoTextCoinAdapter     mAdapterCoin;
    private AutoTextExchangeAdapter mAdapterExchangeComplete;

    private Calendar myCalendar;

    private long mCurrenteyId;
    private long mExchangeId;
    private long mPortfolioId = 1;
    private long mCoinId = 1;
    private TransactionType mTrasactionType = TransactionType.ADD;

    private double mAmount;
    private double mPrice;
    private long mDate;
    private String mDescribtion;

    private double mPortfolioCoinOriginal;
    private double mPortfolioCoinPriceOriginal;
    private double mPortfolioCurrenteyOriginal;
    private double mPortfolioCurrenteyPriceOriginal;
    private double mBaseCoinPrice = 1;
    private double mBaseCurrenteyPrice = 1;

    private long mUSDId = 477;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    public static void start(Context context, long coinId) {
        Intent starter = new Intent(context, TransactionActivity.class);
        starter.putExtra(TransactionActivity.EXTRA_COIN_ID, coinId);
        context.startActivity(starter);
    }

    public static void start(Context context, long coinId, long exchangeId, TransactionType type) {
        Intent starter = new Intent(context, TransactionActivity.class);
        starter.putExtra(EXTRA_COIN_ID, coinId);
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

        mCoinId = getIntent().getLongExtra(EXTRA_COIN_ID, 0);
        if (mCoinId == 0) {
            finish();
            return;
        }

        mExchangeId = getIntent().getLongExtra(EXTRA_EXCHANGE_ID, 0);

        if(getIntent().getSerializableExtra(EXTRA_TYPE) != null) {
            mTrasactionType = (TransactionType) getIntent().getSerializableExtra(EXTRA_TYPE);
        }

        if (mTrasactionType == TransactionType.ADD) {
            mCurrenteyComplete.setVisibility(View.GONE);
            mCurrenteyLabelView.setVisibility(View.GONE);
            mClearCoinButton.setVisibility(View.GONE);
        }



        mAdapterCoin = new AutoTextCoinAdapter(this);
        mCurrenteyComplete.setAdapter(mAdapterCoin);
        RxAutoCompleteTextView.itemClickEvents(mCurrenteyComplete)
                .retry()
                .subscribe(placeDetailsResult -> {
                    mCurrenteyId = placeDetailsResult.id();
                    mAddTransactionButton.setEnabled(true);
                });
        RxView.clicks(mClearCoinButton).subscribe(o -> mCurrenteyComplete.setText(""));



        mAdapterExchangeComplete = new AutoTextExchangeAdapter(this);
        mExchangeComplete.setAdapter(mAdapterExchangeComplete);
        RxAutoCompleteTextView.itemClickEvents(mExchangeComplete)
                .retry()
                .subscribe(item -> mExchangeId = item.id());
        RxView.clicks(mClearExchangeButton).subscribe(o -> mExchangeComplete.setText(""));



        myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
            mDate = myCalendar.getTimeInMillis();
        };

        mDateView.setOnClickListener(v -> {
            new DatePickerDialog(
                    TransactionActivity.this,
                    date,
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        mAddTransactionButton.setOnClickListener(v -> {
            mAddTransactionButton.setEnabled(false);
            addTransaction();
            finish();
        });

        initLoaderManager();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
        if (mAdapterCoin != null && loader.getId() == CryptoContract.LOADER_COINS) {
            mAdapterCoin.changeCursor(data);
            return;
        }
        if (loader.getId() == CryptoContract.LOADER_EXCHANGES) {
            if(data.getCount() > 0) {
                data.moveToFirst();
                ColumnsExchange.ColumnsMap columnsMap = new ColumnsExchange.ColumnsMap(data);
                mExchangeComplete.setText(data.getString(columnsMap.mColumnName));
            }
            return;
        }
        throw new IllegalArgumentException("no id handled!");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == CryptoContract.LOADER_COINS) {
            if (mAdapterCoin != null) {
                mAdapterCoin.changeCursor(null);
                return;
            }
            throw new IllegalArgumentException("no id handled!");
        }
    }

    private void updateLabel() {
        String myFormat = "dd.MM.yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        mDateView.setText(sdf.format(myCalendar.getTime()));
    }

    private void initLoaderManager() {
        getSupportLoaderManager().restartLoader(CryptoContract.LOADER_COINS, null, this);
        getSupportLoaderManager().restartLoader(CryptoContract.LOADER_EXCHANGES, null, this);
    }

    private void addTransaction() {
        mAmount = Double.valueOf(mAmountView.getText().toString());
        mPrice = Double.valueOf(mPriceView.getText().toString());
        mCurrenteyId = mCurrenteyId > 0 ? mCurrenteyId : mUSDId;

        // create transaction
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_ID, mCoinId);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_ID, mPortfolioId);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_CORRESPOND_ID, mCurrenteyId);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_EXCHANGE_ID, mExchangeId);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_AMOUNT, mAmount);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PRICE, mPrice);
        values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_DATETIME, mDate);
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
        if(mCurrenteyId <=0) {
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

    private Loader<Cursor> getCoinsLoader() {
        return new CursorLoader(this, CryptoContract.CryptoCoins.CONTENT_URI, CryptoContract.CryptoCoins.DEFAULT_PROJECTION, null, null, null);
    }

    private Loader<Cursor> getExchangesLoader() {
        return new CursorLoader(
                this,
                CryptoContract.CryptoExchanges.CONTENT_URI,
                CryptoContract.CryptoExchanges.DEFAULT_PROJECTION,
                CryptoContract.CryptoExchanges._ID + " = " +  mExchangeId,
                null,
                CryptoContract.CryptoExchanges.COLUMN_NAME_NAME + " ASC"
        );
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


}
