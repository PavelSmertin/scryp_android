package com.start.crypto.android.publicPortfolio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.start.crypto.android.BaseActivity;
import com.start.crypto.android.R;

import butterknife.BindView;


public class PortfolioCoinActivity extends BaseActivity {

    public static final String EXTRA_PORTFOLIO_COIN_ID  = "portfolio_coin_id";

    @BindView(R.id.controller_container) ViewGroup mControllerContainer;

    private Router mRouter;
    private long argPortfolioCoinId;

    public static void start(Context context, long portfolioCoinId) {
        Intent intent = new Intent(context, PortfolioCoinActivity.class);
        intent.putExtra(EXTRA_PORTFOLIO_COIN_ID,    portfolioCoinId);
        context.startActivity(intent);
    }

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        argPortfolioCoinId = getIntent().getLongExtra(EXTRA_PORTFOLIO_COIN_ID, 0);

        mRouter = Conductor.attachRouter(this, mControllerContainer, savedInstanceState);
        if (!mRouter.hasRootController()) {
            mRouter.setRoot(RouterTransaction.with(new PortfolioCoinController(argPortfolioCoinId)));
        }

    }

    @Override
    public void onBackPressed() {
        if (!mRouter.handleBack()) {
            super.onBackPressed();
        }
    }

}
