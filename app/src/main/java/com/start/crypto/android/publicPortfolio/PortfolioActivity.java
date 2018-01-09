package com.start.crypto.android.publicPortfolio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.start.crypto.android.BaseActivity;
import com.start.crypto.android.CreateTransactionActivity;
import com.start.crypto.android.R;
import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.start.crypto.android.api.model.PortfolioCoin;
import com.start.crypto.android.utils.KeyboardHelper;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class PortfolioActivity extends BaseActivity {

    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";

    @BindView(R.id.coins_list)                  RecyclerView mRecyclerView;


    @BindView(R.id.portfolio_current_value)         TextView mPortfolioCurrentValue;
    @BindView(R.id.portfolio_current_value_unit)    TextView mPortfolioCurrentValueUnit;
    @BindView(R.id.portfolio_profit_24h)            TextView mPortfolioProfit24h;
    @BindView(R.id.portfolio_profit_24h_unit)       TextView mPortfolioProfit24hUnit;
    @BindView(R.id.portfolio_original_value)        TextView mPortfolioOriginalValue;
    @BindView(R.id.portfolio_original_value_unit)   TextView mPortfolioOriginalValueUnit;
    @BindView(R.id.portfolio_profit_all)            TextView mPortfolioProfitAll;
    @BindView(R.id.portfolio_profit_all_unit)       TextView mPortfolioProfitAllUnit;
    @BindView(R.id.user_name)                       TextView mUsernameView;

    private PublicPortfolioCoinsAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private long argUserId;
    private String argUsername;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static void start(Context context, long portfolioId, String username) {
        Intent intent = new Intent(context, PortfolioActivity.class);
        intent.putExtra(EXTRA_USER_ID, portfolioId);
        intent.putExtra(EXTRA_USER_NAME, username);
        context.startActivity(intent);
    }

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_portfolio);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        toolbar.setTitle(null);

        argUserId = getIntent().getLongExtra(EXTRA_USER_ID, 0);
        if(argUserId <= 0) {
            throw new IllegalStateException("undefined user_id");
        }

        argUsername = getIntent().getStringExtra(EXTRA_USER_NAME);
        if(argUsername != null) {
            mUsernameView.setText(argUsername);
        }

        mAdapter = new PublicPortfolioCoinsAdapter();

        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        retrieveCoins();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    public void retrieveCoins() {
        compositeDisposable.add(
                MainServiceGenerator.createService(MainApiService.class, this).publicPortfolio(Long.toString(argUserId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    mAdapter.update(response.getPortfolioCoins());
                                    calculatePortfolioValues(response.getPortfolioCoins());
                                },
                                error -> {
                                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        )
        );
    }


    private void calculatePortfolioValues(List<PortfolioCoin> portfolioCoins) {

        double valueAll = 0;
        double value24h = 0;
        double valueHoldings = 0;

        for (PortfolioCoin portfolioCoin : portfolioCoins) {
            double original = portfolioCoin.getOriginal();
            double priceOriginal = portfolioCoin.getPriceOriginal();
            double priceNow = portfolioCoin.getPriceNow();
            double price24h = portfolioCoin.getPrice24h();

            valueAll += original * priceOriginal;
            value24h += original * price24h;
            valueHoldings += original * priceNow;
        }

        if (Double.isInfinite(valueHoldings)) {
            return;
        }

        double profit24h = valueHoldings - value24h;
        double profitAll = valueHoldings - valueAll;

        double profit24hPercent = 0;
        double profitAllPercent = 0;
        if(valueHoldings > 0) {
            profit24hPercent = (valueHoldings - value24h) * 100 / valueHoldings;
            profitAllPercent = (valueHoldings - valueAll) * 100 / valueHoldings;
        }


        mPortfolioCurrentValue.setText(KeyboardHelper.cut(valueHoldings));
        mPortfolioCurrentValueUnit.setText(CreateTransactionActivity.DEFAULT_SYMBOL);
        mPortfolioProfit24h.setText(KeyboardHelper.cut(profit24h));
        mPortfolioProfit24hUnit.setText(String.format(Locale.US, "%s (%s%%)", CreateTransactionActivity.DEFAULT_SYMBOL, Math.round(profit24hPercent)));
        mPortfolioOriginalValue.setText(KeyboardHelper.cut(valueAll));
        mPortfolioOriginalValueUnit.setText(CreateTransactionActivity.DEFAULT_SYMBOL);
        mPortfolioProfitAll.setText(KeyboardHelper.cut(profitAll));
        mPortfolioProfitAllUnit.setText(String.format(Locale.US, "%s (%.2f%%)", CreateTransactionActivity.DEFAULT_SYMBOL, profitAllPercent));

        if (profit24h < 0) {
            mPortfolioProfit24h.setTextColor(getResources().getColor(R.color.colorDownValue));
            mPortfolioProfit24hUnit.setTextColor(getResources().getColor(R.color.colorDownValue));
        }

        if (profitAll < 0) {
            mPortfolioProfitAll.setTextColor(getResources().getColor(R.color.colorDownValue));
            mPortfolioProfitAllUnit.setTextColor(getResources().getColor(R.color.colorDownValue));
        }

    }


}
