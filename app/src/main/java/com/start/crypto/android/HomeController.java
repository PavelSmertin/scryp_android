package com.start.crypto.android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.RouterTransaction;
import com.start.crypto.android.account.AuthView;
import com.start.crypto.android.account.SigninController;
import com.start.crypto.android.portfolio.PortfolioController;
import com.start.crypto.android.utils.PreferencesHelper;

import butterknife.BindView;

public class HomeController extends BaseController implements ControllerPageTitle, AuthView {


    @BindView(R.id.controller_container)  ViewGroup mContainer;

    public HomeController() {
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.activity_main, container, false);
    }



    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        if(PreferencesHelper.getInstance().getLogin() == null) {
            insertAuth();
        } else {
            insertPortfolio();
        }
    }

    @Override
    public String getPageTitle(Context context) {
        return context.getString(R.string.title_activity_main);
    }

    @Override
    public void onAuth() {
        insertPortfolio();
    }


    @Override
    public void onLogout() {
        insertAuth();
    }

    private void insertPortfolio() {
        PortfolioController portfolioController = new PortfolioController();
        portfolioController.setTargetController(this);
        getChildRouter(mContainer).replaceTopController(RouterTransaction.with(portfolioController));
    }

    private void insertAuth() {
        SigninController signinController = new SigninController();
        signinController.setTargetController(this);
        getChildRouter(mContainer).replaceTopController(RouterTransaction.with(signinController));
    }


}
