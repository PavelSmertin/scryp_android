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
import com.start.crypto.android.api.model.Coin;
import com.start.crypto.android.data.CryptoContract;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;


public class CoinAutocompleteActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_COIN = "coin";

    public static final int REQUEST_COIN        = 100;
    public static final int REQUEST_CURRENTEY   = 101;

    private static final long DELAY_IN_MILLIS = 500;
    public static final int MIN_LENGTH_TO_START = 1;

    @BindView(R.id.coin_select)         EditText mCoinSelect;
    @BindView(R.id.clear_text_button)   ImageView mClearTextButton;
    @BindView(R.id.items)               RecyclerView mRecyclerView;


    private AutocompleteListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    PublishSubject<Coin> mCoinPlusSubject = PublishSubject.create();


    public static void start(Activity activity, int request) {
        Intent intent = new Intent(activity, CoinAutocompleteActivity.class);
        activity.startActivityForResult(intent, request);
    }
    public static void start(Activity activity, ActivityOptions options, int request) {
        Intent intent = new Intent(activity, CoinAutocompleteActivity.class);
        activity.startActivityForResult(intent, request, options.toBundle());
    }


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_coin_autocomplete);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        compositeDisposable.add(RxTextView.textChanges(mCoinSelect)
                .debounce(DELAY_IN_MILLIS, TimeUnit.MICROSECONDS)
                .filter(text -> text.toString().length() > MIN_LENGTH_TO_START)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::filter)
        );

        mAdapter = new AutocompleteListAdapter(this, mCoinPlusSubject);
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
                            intent.putExtra(EXTRA_COIN, coin);
                            setResult(RESULT_OK, intent);
                            finish();
                        })
        );

        getSupportLoaderManager().initLoader(0, null, this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id != 0) {
            throw new IllegalArgumentException("no id handled!");
        }
        return new CursorLoader(this, CryptoContract.CryptoCoins.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() != 0) {
            throw new IllegalArgumentException("no id handled!");
        }

        mAdapter.changeCursor(data);
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


    private void filter(CharSequence constraint) {
        Cursor cursor = getContentResolver().query(
                CryptoContract.CryptoCoins.CONTENT_URI,
                CryptoContract.CryptoCoins.DEFAULT_PROJECTION,
                CryptoContract.CryptoCoins.COLUMN_NAME_NAME + " LIKE '%" + (constraint != null ? constraint.toString() : "@@@@") + "%'",
                null,
                null);

        mAdapter.changeCursor(cursor);
    }




}
