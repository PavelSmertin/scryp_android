package com.start.crypto.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView;
import com.start.crypto.android.data.CryptoContract;

import butterknife.BindView;
import io.reactivex.disposables.CompositeDisposable;


public class AutocompleteActivity extends BaseActivity {

    private static final String TAG = "AutocompleteActivity";

    private static final long DELAY_IN_MILLIS = 500;
    public static final int MIN_LENGTH_TO_START = 2;

    @BindView(R.id.coin_select)         AutoCompleteTextView mCoinSelect;
    @BindView(R.id.clear_text_button)   ImageView mClearTextButton;
    @BindView(R.id.add_transaction)     Button mAddTransactionButton;

    private long argPortfolioId;

    private AutoTextCoinAdapter mAdapterCoin;
    private long mCoinId;
    private String mCoinSymbol;


    public static void start(Context context, long portfolioId) {
        Intent intent = new Intent(context, AutocompleteActivity.class);
        intent.putExtra(CreateTransactionActivity.EXTRA_PORTFOLIO_ID, portfolioId);
        context.startActivity(intent);
    }


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_autocomplete);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Portfolio
        argPortfolioId = getIntent().getLongExtra(CreateTransactionActivity.EXTRA_PORTFOLIO_ID, 0);
        if (argPortfolioId == 0) {
            finish();
            return;
        }

        mAdapterCoin = new AutoTextCoinAdapter(this);

        mCoinSelect.setAdapter(mAdapterCoin);
        RxAutoCompleteTextView.itemClickEvents(mCoinSelect)
                .retry()
                .subscribe(item -> {
                    mCoinId = item.id();
                    Cursor cursor = (Cursor) mAdapterCoin.getItem(item.position());
                    int itemColumnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL);
                    mCoinSymbol = cursor.getString(itemColumnIndex);
                    mAddTransactionButton.setEnabled(true);
                });

        compositeDisposable.add(RxView.clicks(mClearTextButton).subscribe(o -> mCoinSelect.setText("")));

        RxView.clicks(mAddTransactionButton).subscribe(success -> {
            CreateTransactionActivity.start(this, argPortfolioId, mCoinId, mCoinSymbol);
            finish();
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }


}
