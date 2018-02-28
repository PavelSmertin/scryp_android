package com.start.crypto.android.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.start.crypto.android.R;
import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.start.crypto.android.api.model.Auth;
import com.start.crypto.android.data.CryptoContract;
import com.start.crypto.android.data.DBHelper;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;


public class SigninActivity extends AccountAuthenticatorActivity {

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
        setContentView(R.layout.account_controller_signin);
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

        mNextButton.setOnClickListener(v -> {
            clearPortfolio();
            submit();
        });
        mSignUpButton.setOnClickListener(v -> {
            // Since there can only be one AuthenticatorActivity, we call the sign up activity, get his results,
            // and return them in setAccountAuthenticatorResult(). See finishLogin().
            Intent signup = new Intent(this, SignupActivity.class);
            signup.putExtra(RestoreRequestActivity.EXTRA_USER_NAME, mEmailView.getText().toString());
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
            finishLogin(data);
        } else if (requestCode == REQ_RESTORE && resultCode == RESULT_OK) {
            finishLogin(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void clearPortfolio() {
        SQLiteDatabase db = new DBHelper(this).getWritableDatabase();

        db.execSQL(CryptoContract.SQL_DELETE_TRANSACTIONS);
        db.execSQL(CryptoContract.SQL_DELETE_PORTFOLIO_COINS);
        db.execSQL(CryptoContract.SQL_DELETE_NOTIFICATIONS);

        db.execSQL(CryptoContract.SQL_CREATE_TRANSACTIONS);
        db.execSQL(CryptoContract.SQL_CREATE_PORTFOLIO_COINS);
        db.execSQL(CryptoContract.SQL_CREATE_NOTIFICATIONS);
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
                            finishLogin(intent);
                        },
                        error -> {
                            if(error instanceof HttpException && ((HttpException)error).code() == 404) {
                                showError(getResources().getString(R.string.account_auth_error));
                            }
                        }
                )
        );
    }

    private void finishLogin(Intent intent) {
        String authtoken    = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
        String username     = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountType  = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        Account[] accounts = mAccountManager.getAccountsByType(SigninActivity.ACCOUNT_TYPE);
        Account currentAccount = null;
        if (accounts.length != 0) {
            currentAccount = getStoredAccount(accounts, username);
        }

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false) || currentAccount == null) {
            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            final Account account = new Account(username, accountType);
            mAccountManager.addAccountExplicitly(account, null, null);
            mAccountManager.setAuthToken(account, mAuthTokenType, authtoken);
        }


        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
        data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);

        Intent res = new Intent();
        res.putExtras(data);
        setAccountAuthenticatorResult(res.getExtras());
        setResult(RESULT_OK, res);
        finish();
    }

    private Account getStoredAccount(Account[] accounts, String userName) {
        for(Account a : accounts) {
            if(a.name.equalsIgnoreCase(userName)) {
                return a;
            }
        }
        return null;
    }

}
