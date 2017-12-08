package com.start.crypto.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.api.RestClientMainApi;
import com.trello.rxlifecycle2.android.ActivityEvent;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class SignupActivity extends BaseActivity {

    private static final String TAG = "SignupActivity";


    @BindView(R.id.email)           EditText mEmailView;
    @BindView(R.id.password)        EditText mPasswordView;
    @BindView(R.id.password_repeat) EditText mPasswordRepeatView;
    @BindView(R.id.next)            Button mNextButton;


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_signup);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RxView.clicks(mNextButton).subscribe(success -> {

            RestClientMainApi.INSTANCE.getClient().signup(mEmailView.getText().toString(), mPasswordView.getText().toString(), mPasswordRepeatView.getText().toString())
                    .compose(bindUntilEvent(ActivityEvent.PAUSE))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> {
                                Intent intent = new Intent(this, PortfoliosActivity.class);
                                startActivity(intent);
                                finish();
                            },
                            error -> {
                                showError(error.getMessage());
                            }
                    );

        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }


}
