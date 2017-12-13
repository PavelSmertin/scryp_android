package com.start.crypto.android;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
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

    private String mAccountType;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_signup);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountType = getIntent().getStringExtra(AuthActivity.ARG_ACCOUNT_TYPE);

        RxView.clicks(mNextButton).subscribe(success -> {
            MainServiceGenerator.createService(MainApiService.class, this).signup(mEmailView.getText().toString(), mPasswordView.getText().toString(), mPasswordRepeatView.getText().toString())
                    .compose(bindUntilEvent(ActivityEvent.PAUSE))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> createAccount(),
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


    private void createAccount() {

        String userName         = mEmailView.getText().toString().trim();
        String password         = mPasswordView.getText().toString().trim();
        String passwordRepeat   = mPasswordRepeatView.getText().toString().trim();

        compositeDisposable.add(
                MainServiceGenerator.createService(MainApiService.class, this).signup(userName, password, passwordRepeat)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
//                                    Bundle data = new Bundle();
//                                    data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
//                                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
//                                    data.putString(AccountManager.KEY_AUTHTOKEN, response.getJwt());
//                                    data.putString(PARAM_USER_PASS, userPass);
//                                    Intent res = new Intent();
//                                    res.putExtras(data);
//                                    finishLogin(res);
                                    Toast.makeText(getBaseContext(), "user created successfully", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_CANCELED);
                                    finish();
                                },
                                error -> {
                                    Toast.makeText(getBaseContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        )
        );

    }



}
