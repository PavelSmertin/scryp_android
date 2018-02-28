package com.start.crypto.android.account;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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


public class RestoreRequestActivity extends BaseActivity {

    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";

    private static final int REQ_RESTORE_PASSWORD = 1;

    @BindView(R.id.email)           EditText mEmailView;
    @BindView(R.id.next)            Button mNextButton;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String mUserName;


    @Override
    protected void setupLayout() {
        setContentView(R.layout.account_activity_restore_request);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserName = getIntent().getStringExtra(EXTRA_USER_NAME);
        if(mUserName != null) {
            mEmailView.setText(mUserName);
        }

        RxView.clicks(mNextButton).subscribe(success -> {
            startProgressDialog();
            mNextButton.setEnabled(false);

            mUserName = mEmailView.getText().toString().trim();

            MainServiceGenerator.createService(MainApiService.class, this).restoreRequest(mUserName)
                    .compose(bindUntilEvent(ActivityEvent.PAUSE))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> {
                                stopProgressDialog();
                                Toast.makeText(this, getString(R.string.account_code_was_sent), Toast.LENGTH_LONG).show();
                                Intent restore = new Intent(this, RestorePasswordActivity.class);
                                restore.putExtra(EXTRA_USER_ID, response.getUserId());
                                startActivityForResult(restore, REQ_RESTORE_PASSWORD);
                            },
                            error -> {
                                showError(error.getMessage());
                                startProgressDialog();
                                if(mNextButton != null) {
                                    mNextButton.setEnabled(true);
                                }

                            }
                    );
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_RESTORE_PASSWORD && resultCode == RESULT_OK) {
            backToAuth(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    private void backToAuth(Intent intent) {
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUserName);
        setResult(RESULT_OK, intent);
        finish();
    }

}
