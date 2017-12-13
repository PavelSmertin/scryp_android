package com.start.crypto.android;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView;
import com.start.crypto.android.data.ColumnsPortfolioCoin;
import com.start.crypto.android.data.CryptoContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import io.reactivex.disposables.CompositeDisposable;


public class TransactionActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_COIN_ID = "coin_id";

    @BindView(R.id.currentey_select)        Spinner mCurrenteySelect;
//    @BindView(R.id.exchange_select)         Spinner mExchangeSelect;
    @BindView(R.id.exchange_complete)       AutoCompleteTextView mExchangeComplete;
    @BindView(R.id.clear_exchange_button)   ImageView mClearTextButton;
    @BindView(R.id.type_select)             Spinner mTypeSelect;
    @BindView(R.id.amount)                  EditText mAmountView;
    @BindView(R.id.price)                   EditText mPriceView;
    @BindView(R.id.date)                    EditText mDateView;
    @BindView(R.id.describtion)             EditText mDescribtionView;
    @BindView(R.id.add_transaction)         Button mAddTransactionButton;
    @BindView(R.id.currentey_select_label)  TextView mCurrenteyLabelView;
    @BindView(R.id.type_select_label)       TextView mTypeLabelView;


    private SimpleCursorAdapter     mAdapterCoin;
    private SimpleCursorAdapter     mAdapterExchange;
    private AutoTextExchangeAdapter mAdapterExchangeComplete;

    private Calendar myCalendar;

    private long mCurrenteyId;
    private long mExchangeId;
    private long mPortfolioId = 1;
    private long mCoinId = 1;
    private TransactionType mTrasactionType = TransactionType.ADD;


    private long mType;

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

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


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

        mAdapterCoin = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item,
                null,
                new String[]{CryptoContract.CryptoCoins.COLUMN_NAME_NAME},
                new int[]{android.R.id.text1}, 0);

        mAdapterCoin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCurrenteySelect.setAdapter(mAdapterCoin);
        RxAdapterView.itemSelections(mCurrenteySelect).subscribe(succ -> Log.d("test", succ.getClass().getSimpleName()));


        mCurrenteySelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrenteyId = id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (mTrasactionType == TransactionType.ADD) {
            mCurrenteySelect.setVisibility(View.GONE);
            mCurrenteyLabelView.setVisibility(View.GONE);
        }


//        mAdapterExchange = new SimpleCursorAdapter(this,
//                android.R.layout.simple_spinner_item,
//                null,
//                new String[]{CryptoContract.CryptoExchanges.COLUMN_NAME_NAME},
//                new int[]{android.R.id.text1}, 0);
//
//        mAdapterExchange.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mExchangeSelect.setAdapter(mAdapterExchange);
//        mExchangeSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                mExchangeId = id;
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });


        mAdapterExchangeComplete = new AutoTextExchangeAdapter(this);
        mExchangeComplete.setAdapter(mAdapterExchangeComplete);
        RxAutoCompleteTextView.itemClickEvents(mExchangeComplete)
                .retry()
                .subscribe(placeDetailsResult -> {});
        RxView.clicks(mClearTextButton).subscribe(o -> mExchangeComplete.setText(""));



        ArrayAdapter<String> mAdapterType = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.transaction_types)
        );

        mAdapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTypeSelect.setAdapter(mAdapterType);
        mTypeSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mType = id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if(mTrasactionType == TransactionType.ADD) {
            mTypeLabelView.setVisibility(View.GONE);
            mTypeSelect.setVisibility(View.GONE);
        }


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

            mAmount = Double.valueOf(mAmountView.getText().toString());
            mPrice = Double.valueOf(mPriceView.getText().toString());

            if(mType != 0 && mType != 1) {
                mCurrenteyId = -1;
            }

            // create transaction
            ContentValues values = new ContentValues();
            values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_ID, mCoinId);
            values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_ID, mPortfolioId);
            if( mCurrenteyId > 0 ) {
                values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_CORRESPOND_ID, mCurrenteyId);
            }
            values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_EXCHANGE_ID, mExchangeId);
            values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_AMOUNT, mAmount);
            values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PRICE, mPrice);
            values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_DATETIME, mDate);
            //values.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PROTFOLIO_BALANCE, getPortfolioBalance());
            getContentResolver().insert(CryptoContract.CryptoTransactions.CONTENT_URI, values);



            // insert coin to portfolio
            long portfolioCoinId = checkPortfolioCoins();
            long portfolioCurrenteyId = checkPortfolioCurrentey();

            // create portfolio coin
            if (portfolioCoinId <= 0) {
                insertCoin();
            }
            if (portfolioCurrenteyId <= 0 && mCurrenteyId > 0) {
                insertCurrentey();
            }

            if(mType == 0 || mType == 1) {
                // update portfolio coin
                if (portfolioCoinId > 0) {
                    values = new ContentValues();
                    values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL, getPortfolioCoinOriginal(mType == 0));
                    values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, getPortfolioCoinPrice(mType == 0));
                    getContentResolver().update(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values, CryptoContract.CryptoPortfolioCoins._ID + " = " + portfolioCoinId, null);
                }

                if (portfolioCurrenteyId > 0) {
                    values = new ContentValues();
                    values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL, getPortfolioCurrenteyOriginal(mType == 1));
                    values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, getPortfolioCurrenteyPrice(mType == 1));
                    getContentResolver().update(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, values, CryptoContract.CryptoPortfolioCoins._ID + " = " + portfolioCurrenteyId, null);
                }
            }

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
        if (mAdapterExchange != null && loader.getId() == CryptoContract.LOADER_EXCHANGES) {
            mAdapterExchange.changeCursor(data);
            return;
        }
        throw new IllegalArgumentException("no id handled!");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapterCoin != null) {
            if (loader.getId() == CryptoContract.LOADER_COINS) {
                mAdapterCoin.changeCursor(null);
                return;
            }
            if (loader.getId() == CryptoContract.LOADER_EXCHANGES) {
                mAdapterExchange.changeCursor(null);
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
        //getSupportLoaderManager().restartLoader(CryptoContract.LOADER_EXCHANGES, null, this);
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

    private int getPortfolioCoinsCount() {
        Cursor cursor = getContentResolver().query(
                CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                CryptoContract.CryptoPortfolioCoins.DEFAULT_PROJECTION,
                CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." +CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID + " = " + mPortfolioId,
                null,
                CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins._ID + " ASC");

        if(cursor != null && cursor.getCount() > 0) {
            int count = cursor.getCount();
            cursor.close();
            return count;
        }

        return 0;
    }

    private long checkPortfolioCurrentey() {
        if(mCurrenteyId <=0) {
            return -1;
        }
        Cursor cursor = getContentResolver().query(
                CryptoContract.CryptoPortfolioCoins.CONTENT_URI,
                CryptoContract.CryptoPortfolioCoins.DEFAULT_PROJECTION,
                CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID + " = " + mCoinId +
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
        return new CursorLoader(this, CryptoContract.CryptoExchanges.CONTENT_URI, CryptoContract.CryptoExchanges.DEFAULT_PROJECTION, null, null, CryptoContract.CryptoExchanges.COLUMN_NAME_NAME + " ASC");
    }


    // Количество монеты в портфолио.
    // Меняется при любой транзакции
    private double getPortfolioCoinOriginal(boolean operation) {
        // берем текущее количество вычитаем или прибавляем сумму из транзакции
        if(operation) {
            return mPortfolioCoinOriginal + mAmount;
        } else {
            return mPortfolioCoinOriginal - mAmount;
        }
    }
    private double getPortfolioCurrenteyOriginal(boolean operation) {
        // берем текущее количество вычитаем или прибавляем сумму из транзакции
        if(operation) {
            return mPortfolioCurrenteyOriginal + mAmount * mPrice;
        } else {
            return mPortfolioCurrenteyOriginal - mAmount * mPrice;
        }
    }

    // Цена закупа монеты в портфолио. Если было несколько транзакций, считается среднее.
    // Меняется при любой транзакции
    private double getPortfolioCoinPrice(boolean operation) {
        double originalSum = mPortfolioCoinOriginal * mPortfolioCoinPriceOriginal; // in base currency
        double transactionSum = mAmount * mBaseCoinPrice; // in base currency

        if(operation) {
            return (originalSum + transactionSum) / (mPortfolioCoinOriginal + mAmount);
        } else {
            return (originalSum - transactionSum) / (mPortfolioCoinOriginal - mAmount);
        }
    }
    private double getPortfolioCurrenteyPrice(boolean operation) {
        double originalSum = mPortfolioCurrenteyOriginal * mPortfolioCurrenteyPriceOriginal; // in base currency
        double transactionSum = mAmount * mPrice * mBaseCurrenteyPrice; // in base currency

        if(operation) {
            return (originalSum + transactionSum) / (mPortfolioCurrenteyOriginal + mAmount * mPrice);
        } else {
            return (originalSum - transactionSum) / (mPortfolioCurrenteyOriginal - mAmount * mPrice);
        }
    }


}
