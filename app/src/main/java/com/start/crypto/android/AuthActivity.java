package com.start.crypto.android;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.start.crypto.android.api.model.Auth;
import com.start.crypto.android.utils.PreferencesHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class AuthActivity extends AccountAuthenticatorActivity {

    private static final String TAG = "AuthActivity";


    public final static String ARG_ACCOUNT_TYPE = "account_type";
    public final static String ARG_AUTH_TYPE = "auth_type";
    public final static String ARG_ACCOUNT_NAME = "account_name";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "is_adding_account";

    public static final String ACCOUNT_TYPE = "com.start.crypto.android.sync";
    public final static String  AUTHTOKEN_TYPE_FULL_ACCESS = "full_access";

    public final static String PARAM_USER_PASS = "user_pass";

    private final int REQ_SIGNUP = 1;

    private AccountManager mAccountManager;
    private String mAuthTokenType;



    @BindView(R.id.email)    EditText mEmailView;
    @BindView(R.id.password) EditText mPasswordView;
    @BindView(R.id.next)     Button mNextButton;
    @BindView(R.id.sign_up)  Button mSignUpButton;


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        mAccountManager = AccountManager.get(getBaseContext());

        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);

        if (accountName != null) {
            mEmailView.setText(accountName);
        }

        mNextButton.setOnClickListener(v -> submit());
        mSignUpButton.setOnClickListener(v -> {
            // Since there can only be one AuthenticatorActivity, we call the sign up activity, get his results,
            // and return them in setAccountAuthenticatorResult(). See finishLogin().
            Intent signup = new Intent(getBaseContext(), SignupActivity.class);
            signup.putExtras(getIntent().getExtras());
            startActivityForResult(signup, REQ_SIGNUP);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    public void submit() {

        String userName = mEmailView.getText().toString();
        String userPass = mPasswordView.getText().toString();
        String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);


        compositeDisposable.add(
                MainServiceGenerator.createService(MainApiService.class, this).signin(new Auth(userName, userPass))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            Bundle data = new Bundle();
                            data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
                            data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                            data.putString(AccountManager.KEY_AUTHTOKEN, response.getJwt());
                            data.putString(PARAM_USER_PASS, userPass);

                            Intent res = new Intent();
                            res.putExtras(data);

                            PreferencesHelper.getInstance().setLogin(userName);
                            finishLogin(res);
                        },
                        error -> {
                            Toast.makeText(getBaseContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                )
        );
    }

    private void finishLogin(Intent intent) {
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

}
