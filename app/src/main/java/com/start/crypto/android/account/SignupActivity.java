package com.start.crypto.android.account;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.BaseActivity;
import com.start.crypto.android.R;
import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.trello.rxlifecycle2.android.ActivityEvent;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SignupActivity extends BaseActivity {

    @BindView(R.id.email)           EditText mEmailView;
    @BindView(R.id.password)        EditText mPasswordView;
    @BindView(R.id.password_repeat) EditText mPasswordRepeatView;
    @BindView(R.id.next)            Button mNextButton;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private String mUserName;


    @Override
    protected void setupLayout() {
        setContentView(R.layout.account_activity_signup);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RxView.clicks(mNextButton).subscribe(success -> {
            mUserName = mEmailView.getText().toString();
            MainServiceGenerator.createService(MainApiService.class, this).signup(mEmailView.getText().toString(), mPasswordView.getText().toString(), mPasswordRepeatView.getText().toString())
                    .compose(bindUntilEvent(ActivityEvent.PAUSE))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> backToAuth(response.getJwt()),
                            error -> showError(error.getMessage())
                    );
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void backToAuth(String jwt) {
        Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUserName);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, jwt);
        setResult(RESULT_OK, intent);
        finish();
    }



}