package com.start.crypto.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView;

import butterknife.BindView;
import io.reactivex.disposables.CompositeDisposable;


public class AutocompleteActivity extends BaseActivity {

    private static final String TAG = "AutocompleteActivity";

    private static final long DELAY_IN_MILLIS = 500;
    public static final int MIN_LENGTH_TO_START = 2;

    @BindView(R.id.coin_select)         AutoCompleteTextView mCoinSelect;
    @BindView(R.id.clear_text_button)   ImageView mClearTextButton;
    @BindView(R.id.add_transaction)     Button mAddTransactionButton;

    private AutoTextCoinAdapter mAdapterCoin;
    private long mCoinId;


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_autocomplete);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mAdapterCoin = new AutoTextCoinAdapter(this);

        mCoinSelect.setAdapter(mAdapterCoin);
        RxAutoCompleteTextView.itemClickEvents(mCoinSelect)
                .retry()
                .subscribe(placeDetailsResult -> {
                    mCoinId = placeDetailsResult.id();
                    mAddTransactionButton.setEnabled(true);
                });

//        RxTextView.textChangeEvents(mCoinSelect)
//                        .debounce(DELAY_IN_MILLIS, TimeUnit.MILLISECONDS)
//                        .map(textViewTextChangeEvent -> textViewTextChangeEvent.text().toString())
//                        .filter(s -> s.length() >= MIN_LENGTH_TO_START)
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .retry()
//                        .subscribe(
//                            coinAutocompleteResult -> getSupportLoaderManager().restartLoader(CryptoContract.LOADER_COINS, null, this),
//                            e -> Log.e(TAG, "onError", e),
//                            () -> Log.i(TAG, "onCompleted")
//                        );

//        addOnAutoCompleteTextViewItemClickedSubscriber(mCoinSelect);
//        addOnAutoCompleteTextViewTextChangedObserver(mCoinSelect);

        compositeDisposable.add(RxView.clicks(mClearTextButton).subscribe(o -> mCoinSelect.setText("")));

        RxView.clicks(mAddTransactionButton).subscribe(success -> {
            Intent intent = new Intent(this, TransactionActivity.class);
            intent.putExtra(TransactionActivity.EXTRA_COIN_ID, mCoinId);
            startActivity(intent);
            finish();
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }


}
