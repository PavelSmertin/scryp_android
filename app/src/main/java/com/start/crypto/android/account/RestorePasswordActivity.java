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


public class RestorePasswordActivity extends BaseActivity {

    @BindView(R.id.code)            EditText mCodeView;
    @BindView(R.id.password)        EditText mPasswordView;
    @BindView(R.id.password_repeat) EditText mPasswordRepeatView;
    @BindView(R.id.next)            Button mNextButton;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String argUserId;


    @Override
    protected void setupLayout() {
        setContentView(R.layout.account_activity_restore_password);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        argUserId = getIntent().getStringExtra(RestoreRequestActivity.EXTRA_USER_ID);

        RxView.clicks(mNextButton).subscribe(success -> {
            startProgressDialog();
            MainServiceGenerator.createService(MainApiService.class, this).restorePassword(
                        argUserId,
                        mCodeView.getText().toString().trim(),
                        mPasswordView.getText().toString().trim(),
                        mPasswordRepeatView.getText().toString().trim()
                    )
                    .compose(bindUntilEvent(ActivityEvent.PAUSE))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(

                            response -> {
                                stopProgressDialog();
                                backToAuth(response.getJwt());
                            },
                            error -> {
                                stopProgressDialog();
                                showError(error.getMessage());
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
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, jwt);
        setResult(RESULT_OK, intent);
        finish();
    }



}
