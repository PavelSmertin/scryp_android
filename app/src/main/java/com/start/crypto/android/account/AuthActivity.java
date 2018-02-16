package com.start.crypto.android.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.start.crypto.android.BaseActivity;
import com.start.crypto.android.R;
import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.start.crypto.android.api.model.Auth;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class AuthActivity extends BaseActivity {

    public final static String ARG_ACCOUNT_TYPE = "account_type";
    public final static String ARG_AUTH_TYPE = "auth_type";
    public final static String ARG_ACCOUNT_NAME = "account_name";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "is_adding_account";

    public static final String ACCOUNT_TYPE = "com.start.crypto.android.sync";
    public final static String  AUTHTOKEN_TYPE_FULL_ACCESS = "full_access";

    private final int REQ_SIGNUP = 1;
    private final int REQ_RESTORE = 2;

    private AccountManager mAccountManager;
    private String mAuthTokenType;



    @BindView(R.id.email)           EditText mEmailView;
    @BindView(R.id.password)        EditText mPasswordView;
    @BindView(R.id.next)            Button mNextButton;
    @BindView(R.id.sign_up)         Button mSignUpButton;
    @BindView(R.id.fogot_password)  Button mRestoreButton;


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void setupLayout() {
        setContentView(R.layout.account_activity_signin);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountManager = AccountManager.get(this);

        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);

        if (accountName != null) {
            mEmailView.setText(accountName);
        }

        mNextButton.setOnClickListener(v -> submit());
        mSignUpButton.setOnClickListener(v -> {
            // Since there can only be one AuthenticatorActivity, we call the sign up activity, get his results,
            // and return them in setAccountAuthenticatorResult(). See finishLogin().
            Intent signup = new Intent(this, SignupActivity.class);
            startActivityForResult(signup, REQ_SIGNUP);
        });

        mRestoreButton.setOnClickListener(v -> {
            // Since there can only be one AuthenticatorActivity, we call the sign up activity, get his results,
            // and return them in setAccountAuthenticatorResult(). See finishLogin().
            Intent restore = new Intent(this, RestoreRequestActivity.class);
            restore.putExtra(RestoreRequestActivity.EXTRA_USER_NAME, mEmailView.getText().toString());
            startActivityForResult(restore, REQ_RESTORE);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME), data.getStringExtra(AccountManager.KEY_AUTHTOKEN));
        } else if (requestCode == REQ_RESTORE && resultCode == RESULT_OK) {
            finishLogin(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME), data.getStringExtra(AccountManager.KEY_AUTHTOKEN));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    public void submit() {

        String userName = mEmailView.getText().toString().trim();
        String userPass = mPasswordView.getText().toString().trim();


        compositeDisposable.add(
                MainServiceGenerator.createService(MainApiService.class, this).signin(new Auth(userName, userPass))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            Intent intent = new Intent();
                            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, userName);
                            intent.putExtra(AccountManager.KEY_AUTHTOKEN, response.getJwt());
                            finishLogin(userName, response.getJwt());
                        },
                        error -> {
                            Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                )
        );
    }

    private void finishLogin(String userName, String jwt) {
        getTokenForAccountCreateIfNeeded(ACCOUNT_TYPE, AUTHTOKEN_TYPE_FULL_ACCESS, userName, jwt);
    }

    private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType, String username, String authtoken) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(accountType, authTokenType, null, this, null, null,
                future1 -> {

                    final Account account = new Account(username, accountType);
                    if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
                        mAccountManager.addAccountExplicitly(account, null, null);
                    }
                    mAccountManager.setAuthToken(account, mAuthTokenType, authtoken);

                    Intent res = new Intent();
                    res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                    res.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
                    res.putExtra(AccountManager.KEY_AUTHTOKEN, authtoken);
                    setResult(RESULT_OK, res);
                    finish();

                }
                , null);
    }


}
