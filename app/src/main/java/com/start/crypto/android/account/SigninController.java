package com.start.crypto.android.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.crashlytics.android.Crashlytics;
import com.start.crypto.android.BaseController;
import com.start.crypto.android.R;
import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.start.crypto.android.api.model.Auth;
import com.start.crypto.android.data.CryptoContract;
import com.start.crypto.android.data.DBHelper;
import com.start.crypto.android.sync.SyncPresenter;
import com.start.crypto.android.utils.PreferencesHelper;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class SigninController extends BaseController {

    private final int REQ_SIGNUP = 1;
    private final int REQ_RESTORE = 2;

    private AccountManager mAccountManager;
    private SyncPresenter mSyncPresenter;


    @BindView(R.id.email)           EditText mEmailView;
    @BindView(R.id.password)        EditText mPasswordView;
    @BindView(R.id.next)            Button mNextButton;
    @BindView(R.id.sign_up)         Button mSignUpButton;
    @BindView(R.id.fogot_password)  Button mRestoreButton;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    public SigninController() {
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.account_controller_signin, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        mSyncPresenter = new SyncPresenter(getActivity().getContentResolver());

        mAccountManager = AccountManager.get(getActivity());

        clearPortfolio();

        mNextButton.setOnClickListener(v -> {
            submit();
        });

        mSignUpButton.setOnClickListener(v -> {
            // Since there can only be one AuthenticatorActivity, we call the sign up activity, get his results,
            // and return them in setAccountAuthenticatorResult(). See finishLogin().
            Intent signup = new Intent(getActivity(), SignupActivity.class);
            signup.putExtra(RestoreRequestActivity.EXTRA_USER_NAME, mEmailView.getText().toString());
            startActivityForResult(signup, REQ_SIGNUP);
        });

        mRestoreButton.setOnClickListener(v -> {
            // Since there can only be one AuthenticatorActivity, we call the sign up activity, get his results,
            // and return them in setAccountAuthenticatorResult(). See finishLogin().
            Intent restore = new Intent(getActivity(), RestoreRequestActivity.class);
            restore.putExtra(RestoreRequestActivity.EXTRA_USER_NAME, mEmailView.getText().toString());
            startActivityForResult(restore, REQ_RESTORE);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_SIGNUP && resultCode == Activity.RESULT_OK) {
            finishLogin(data);
        } else if (requestCode == REQ_RESTORE && resultCode == Activity.RESULT_OK) {
            finishLogin(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void finishLogin(Intent intent) {
        String authtoken    = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
        String username     = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

        Account[] accounts = mAccountManager.getAccountsByType(SigninActivity.ACCOUNT_TYPE);
        Account account = null;
        if (accounts.length != 0) {
            account = getStoredAccount(accounts, username);
        }

        if (account == null) {
            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            account = new Account(username, SigninActivity.ACCOUNT_TYPE);
            mAccountManager.addAccountExplicitly(account, null, null);
        }
        mAccountManager.setAuthToken(account, SigninActivity.AUTHTOKEN_TYPE_FULL_ACCESS, authtoken);

        PreferencesHelper.getInstance().setLogin(username);

        AuthView target = (AuthView)getTargetController();
        target.onAuth();

        compositeDisposable.add(
                MainServiceGenerator.createService(MainApiService.class, getActivity()).syncDownload()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> mSyncPresenter.restorePortfolio(response.string()),
                                error -> {
                                    Crashlytics.logException(error);
                                }
                        )
        );
    }

    private void clearPortfolio() {
        SQLiteDatabase db = new DBHelper(getActivity()).getWritableDatabase();

        db.execSQL(CryptoContract.SQL_DELETE_TRANSACTIONS);
        db.execSQL(CryptoContract.SQL_DELETE_PORTFOLIO_COINS);
        db.execSQL(CryptoContract.SQL_DELETE_NOTIFICATIONS);

        db.execSQL(CryptoContract.SQL_CREATE_TRANSACTIONS);
        db.execSQL(CryptoContract.SQL_CREATE_PORTFOLIO_COINS);
        db.execSQL(CryptoContract.SQL_CREATE_NOTIFICATIONS);
    }

    private Account getStoredAccount(Account[] accounts, String userName) {
        for(Account a : accounts) {
            if(a.name.equalsIgnoreCase(userName)) {
                return a;
            }
        }
        return null;
    }

    private void submit() {

        String userName = mEmailView.getText().toString().trim();
        String userPass = mPasswordView.getText().toString().trim();

        mNextButton.setEnabled(false);
        compositeDisposable.add(
                MainServiceGenerator.createService(MainApiService.class, getActivity()).signin(new Auth(userName, userPass))
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
                                    mNextButton.setEnabled(true);
                                    if(error instanceof HttpException && ((HttpException)error).code() == 404) {
                                        showError(getResources().getString(R.string.account_auth_error));
                                    }
                                }
                        )
        );
    }

}
