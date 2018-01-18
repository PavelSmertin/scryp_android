package com.start.crypto.android.publicPortfolio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.start.crypto.android.BaseActivity;
import com.start.crypto.android.R;
import com.start.crypto.android.TransactionAddActivity;
import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.start.crypto.android.api.RestClientMinApi;
import com.start.crypto.android.api.model.PortfolioCoin;
import com.start.crypto.android.utils.KeyboardHelper;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class PortfolioActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";

    @BindView(R.id.swipe_refresh)               SwipeRefreshLayout mSwipeRefresh;

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

    private List<PortfolioCoin> mCoins = new ArrayList<>();


    public static void start(Context context, long portfolioId, String username) {
        Intent intent = new Intent(context, PortfolioActivity.class);
        intent.putExtra(EXTRA_USER_ID, portfolioId);
        intent.putExtra(EXTRA_USER_NAME, username);
        context.startActivity(intent);
    }

    @Override
    protected void setupLayout() {
        setContentView(R.layout.portfolio_activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        mSwipeRefresh.setOnRefreshListener(this);

        retrieveCoins();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mSwipeRefresh.post(() -> mSwipeRefresh.setRefreshing(true));
        //refreshPrices();
    }

    @Override
    public void onRefresh() {
        refreshPrices();
    }

    private void refreshPrices() {

        if(mCoins.size() == 0) {
            return;
        }

        RestClientMinApi.INSTANCE.getClient().prices(TransactionAddActivity.DEFAULT_SYMBOL, implode(mCoins), null)
                .compose(bindUntilEvent(ActivityEvent.PAUSE))

                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            writePrices(response);
                            mSwipeRefresh.setRefreshing(false);
                            mAdapter.update(mCoins);
                            calculatePortfolioValues();
                        },
                        error -> {
                            mSwipeRefresh.setRefreshing(false);
                        }
                );

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);


        RestClientMinApi.INSTANCE.getClient().pricesHistorical(TransactionAddActivity.DEFAULT_SYMBOL, implode(mCoins), Long.toString(cal.getTimeInMillis()), null)
                .compose(bindUntilEvent(ActivityEvent.PAUSE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            write24hPrices(response.get(TransactionAddActivity.DEFAULT_SYMBOL));
                            mSwipeRefresh.setRefreshing(false);
                            mAdapter.update(mCoins);
                            calculatePortfolioValues();
                        },
                        error -> {
                            mSwipeRefresh.setRefreshing(false);
                        }
                );
    }

    private void writePrices(HashMap<String, Double> prices) {
        for (Map.Entry<String, Double> currency : prices.entrySet()) {
            updateCoinPriceNow(currency.getKey(), 1/currency.getValue());
        }
    }

    private void updateCoinPriceNow(String symbol, double price) {
        for(PortfolioCoin portfolioCoin : mCoins) {
            if(portfolioCoin.getSymbol().equals(symbol)) {
                portfolioCoin.setPriceNow(price);
            }
        }
    }

    private void write24hPrices(HashMap<String, Double> prices) {
        for (Map.Entry<String, Double> currency : prices.entrySet()) {
            updateCoinPrice24h(currency.getKey(), 1/currency.getValue());
        }
    }

    private void updateCoinPrice24h(String symbol, double price) {
        for (PortfolioCoin portfolioCoin : mCoins) {
            if (portfolioCoin.getSymbol().equals(symbol)) {
                portfolioCoin.setPriceNow(price);
            }
        }
    }

    private String implode(List<PortfolioCoin> coins) {

        StringBuilder builder = new StringBuilder();

        Iterator<PortfolioCoin> iterator = coins.iterator();
        while (iterator.hasNext()) {
            PortfolioCoin el = iterator.next();
            builder.append(el.getSymbol());
            if(iterator.hasNext()) {
                builder.append(",");
            }
        }

        return builder.toString();
    }



    public void retrieveCoins() {
        compositeDisposable.add(
                MainServiceGenerator.createService(MainApiService.class, this).publicPortfolio(Long.toString(argUserId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    mCoins = response.getPortfolioCoins();
                                    mAdapter.update(response.getPortfolioCoins());
                                    calculatePortfolioValues();
                                },
                                error -> {
                                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        )
        );
    }


    private void calculatePortfolioValues() {

        double valueAll = 0;
        double value24h = 0;
        double valueHoldings = 0;

        for (PortfolioCoin portfolioCoin : mCoins) {
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
        mPortfolioCurrentValueUnit.setText(TransactionAddActivity.DEFAULT_SYMBOL);
        mPortfolioProfit24h.setText(KeyboardHelper.cut(profit24h));
        mPortfolioProfit24hUnit.setText(String.format(Locale.US, "%s (%s%%)", TransactionAddActivity.DEFAULT_SYMBOL, Math.round(profit24hPercent)));
        mPortfolioOriginalValue.setText(KeyboardHelper.cut(valueAll));
        mPortfolioOriginalValueUnit.setText(TransactionAddActivity.DEFAULT_SYMBOL);
        mPortfolioProfitAll.setText(KeyboardHelper.cut(profitAll));
        mPortfolioProfitAllUnit.setText(String.format(Locale.US, "%s (%.2f%%)", TransactionAddActivity.DEFAULT_SYMBOL, profitAllPercent));

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
