package com.start.crypto.android.account;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.start.crypto.android.BaseController;
import com.start.crypto.android.R;

import io.reactivex.disposables.CompositeDisposable;

public class SigninController extends BaseController {


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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }








}
