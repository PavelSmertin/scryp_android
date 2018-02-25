package com.start.crypto.android;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.start.crypto.android.api.model.AutocompleteItem;
import com.start.crypto.android.data.CryptoContract;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;


public class AutocompleteActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_COIN       = "coin";
    public static final String EXTRA_EXCHANGE   = "exchange";
    public static final String EXTRA_LOADER     = "loader_code";

    public static final int LOADER_COINS        = 100;
    public static final int LOADER_EXCHANGES    = 101;


    public static final int REQUEST_COIN        = 100;
    public static final int REQUEST_CURRENTEY   = 101;
    public static final int REQUEST_EXCHANGE    = 102;

    private static final long DELAY_IN_MILLIS = 500;
    public static final int MIN_LENGTH_TO_START = 0;

    @BindView(R.id.coin_select)         EditText mCoinSelect;
    @BindView(R.id.clear_text_button)   ImageView mClearTextButton;
    @BindView(R.id.items)               RecyclerView mRecyclerView;


    private AutocompleteListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    PublishSubject<AutocompleteItem> mCoinPlusSubject = PublishSubject.create();

    private String mSelection;
    private int argLoaderId = LOADER_COINS;



    public static void start(Activity activity, int request) {
        Intent intent = new Intent(activity, AutocompleteActivity.class);
        activity.startActivityForResult(intent, request);
    }
    public static void start(Activity activity, ActivityOptions options, int request) {
        Intent intent = new Intent(activity, AutocompleteActivity.class);
        if(request == REQUEST_COIN || request == REQUEST_CURRENTEY) {
            intent.putExtra(EXTRA_LOADER, LOADER_COINS);
        }
        if(request == REQUEST_EXCHANGE) {
            intent.putExtra(EXTRA_LOADER, LOADER_EXCHANGES);
        }
        activity.startActivityForResult(intent, request, options.toBundle());
    }


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void setupLayout() {
        setContentView(R.layout.autocomplete_activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        argLoaderId = getIntent().getIntExtra(EXTRA_LOADER, 0);

        if(argLoaderId <= 0) {
            throw new IllegalArgumentException("unknown autocomplete type");
        }

        compositeDisposable.add(RxTextView.textChanges(mCoinSelect)
                .debounce(DELAY_IN_MILLIS, TimeUnit.MICROSECONDS)
                .filter(text -> text.toString().length() >= MIN_LENGTH_TO_START)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::filter)
        );

        mAdapter = new AutocompleteListAdapter(this, mCoinPlusSubject, argLoaderId);
        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        compositeDisposable.add(RxView.clicks(mClearTextButton).subscribe(o -> {
            mCoinSelect.setText("");
            mRecyclerView.scrollToPosition(0);
        }));

        compositeDisposable.add(
                mCoinPlusSubject
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(coin -> {
                            Intent intent = new Intent();
                            if (argLoaderId == LOADER_COINS){
                                intent.putExtra(EXTRA_COIN, coin);
                            }
                            if (argLoaderId == LOADER_EXCHANGES){
                                intent.putExtra(EXTRA_EXCHANGE, coin);
                            }
                            setResult(RESULT_OK, intent);
                            finish();
                        })
        );

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

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

        if (loader.getId() == LOADER_COINS) { // Пара по умолчанию
            if(data != null && data.getCount() > 0) {
                mAdapter.changeCursor(data);
            }
            return;
        }

        if (loader.getId() == LOADER_EXCHANGES) {
            if(data != null && data.getCount() > 0) {
                mAdapter.changeCursor(data);
            }
            return;
        }

        throw new IllegalArgumentException("no id handled!");

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        if (loader.getId() == LOADER_COINS) { // Пара по умолчанию
            if (mAdapter != null) {
                mAdapter.changeCursor(null);
            }
            return;
        }

        if (loader.getId() == LOADER_EXCHANGES) {
            if (mAdapter != null) {
                mAdapter.changeCursor(null);
            }
            return;
        }

        throw new IllegalArgumentException("no id handled!");
    }


    private void filter(CharSequence constraint) {
        mSelection = null;
        if(constraint != null && constraint.length() > MIN_LENGTH_TO_START) {
            if (argLoaderId == LOADER_COINS){
                mSelection = CryptoContract.CryptoCoins.COLUMN_NAME_NAME + " LIKE '%" + constraint.toString() + "%'";
            }
            if (argLoaderId == LOADER_EXCHANGES){
                mSelection = CryptoContract.CryptoExchanges.COLUMN_NAME_NAME + " LIKE '%" + constraint.toString() + "%'";
            }
        }

        if (argLoaderId == LOADER_COINS){
            getSupportLoaderManager().restartLoader(LOADER_COINS, null, this);
        }
        if (argLoaderId == LOADER_EXCHANGES){
            getSupportLoaderManager().restartLoader(LOADER_EXCHANGES, null, this);
        }
    }


    public Loader<Cursor> getCoinsLoader() {
        return new CursorLoader(this, CryptoContract.CryptoCoins.CONTENT_URI, CryptoContract.CryptoCoins.DEFAULT_PROJECTION, mSelection, null, null);
    }

    public Loader<Cursor> getExchangesLoader() {
        return new CursorLoader(this, CryptoContract.CryptoExchanges.CONTENT_URI, CryptoContract.CryptoExchanges.DEFAULT_PROJECTION, mSelection, null, null);
    }
}
