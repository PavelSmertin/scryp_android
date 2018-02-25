package com.start.crypto.android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.start.crypto.android.portfolio.PortfolioController;

import butterknife.BindView;

public class HomeController extends BaseController implements ControllerPageTitle {


    @BindView(R.id.controller_container)  ViewGroup mContainer;

    private Router mRouter;

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

        getChildRouter(mContainer).pushController(RouterTransaction.with(new PortfolioController()));

//        if(PreferencesHelper.getInstance().getLogin() == null) {
//            getChildRouter(mContainer).pushController(RouterTransaction.with(new SigninController()));
//        } else {
//            getChildRouter(mContainer).pushController(RouterTransaction.with(new PortfolioController()));
//        }
    }

    @Override
    public String getPageTitle(Context context) {
        return context.getString(R.string.title_activity_main);
    }

}
