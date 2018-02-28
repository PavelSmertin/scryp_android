package com.start.crypto.android.account;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.crashlytics.android.Crashlytics;
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
import retrofit2.HttpException;

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

        mUserName = getIntent().getStringExtra(RestoreRequestActivity.EXTRA_USER_NAME);
        if(mUserName != null) {
            mEmailView.setText(mUserName);
        }

        RxView.clicks(mNextButton).subscribe(success -> {
            mNextButton.setEnabled(false);
            mUserName = mEmailView.getText().toString();
            MainServiceGenerator.createService(MainApiService.class, this).signup(mEmailView.getText().toString().trim(), mPasswordView.getText().toString().trim(), mPasswordRepeatView.getText().toString().trim())
                    .compose(bindUntilEvent(ActivityEvent.PAUSE))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> backToAuth(response.getJwt()),
                            error -> {
                                if(mNextButton != null) {
                                    mNextButton.setEnabled(true);
                                }
                                if(error instanceof HttpException && ((HttpException)error).code() == 422) {
                                    showAlert(R.string.account_duplicate_email);
                                } else {
                                    Crashlytics.logException(error);
                                }
                            }
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
