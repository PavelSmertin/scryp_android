package com.start.crypto.android.portfolio;

import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.start.crypto.android.BaseActivity;
import com.start.crypto.android.R;
import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.start.crypto.android.api.RestClientMinApi;
import com.start.crypto.android.api.model.PortfolioCoinResponse;
import com.start.crypto.android.api.model.PriceMultiFullResponse;
import com.start.crypto.android.data.CryptoContract;
import com.start.crypto.android.imageLoader.GlideApp;
import com.start.crypto.android.transaction.TransactionAddActivity;
import com.start.crypto.android.utils.KeyboardHelper;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;
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

    public static final String EXTRA_USER_ID        = "user_id";
    public static final String EXTRA_PORTFOLIO_ID   = "portfolio_id";
    public static final String EXTRA_USER_NAME      = "user_name";
    public static final String EXTRA_AVATAR         = "avatar";

    @BindView(R.id.swipe_refresh)               SwipeRefreshLayout mSwipeRefresh;

    @BindView(R.id.coins_list)                  RecyclerView mRecyclerView;


    @BindView(R.id.user_logo)                       ImageView mAvatar;
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

    private long argPortfolioId;
    private long argUserId;
    private String argUsername;
    private String argAvatar;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private List<PortfolioCoinResponse> mCoins = new ArrayList<>();


    public static void start(Context context, long userId, long portfolioId, String username, String avatar) {
        Intent intent = new Intent(context, PortfolioActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_PORTFOLIO_ID, portfolioId);
        intent.putExtra(EXTRA_USER_NAME, username);
        intent.putExtra(EXTRA_AVATAR, avatar);
        context.startActivity(intent);
    }

    public static void start(Context context, ActivityOptions options, long userId, long portfolioId, String username, String avatar) {
        Intent intent = new Intent(context, PortfolioActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_PORTFOLIO_ID, portfolioId);
        intent.putExtra(EXTRA_USER_NAME, username);
        intent.putExtra(EXTRA_AVATAR, avatar);
        context.startActivity(intent, options.toBundle());
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

        argPortfolioId = getIntent().getLongExtra(EXTRA_PORTFOLIO_ID, 0);
        if(argPortfolioId <= 0) {
            throw new IllegalStateException("undefined portfolio_id");
        }

        argUsername = getIntent().getStringExtra(EXTRA_USER_NAME);
        if(argUsername != null) {
            mUsernameView.setText(argUsername);
        }

        argAvatar = getIntent().getStringExtra(EXTRA_AVATAR);
        if(argAvatar != null) {
            GlideApp.with(this)
                    .load(argAvatar)
                    .centerCrop()
                    .override(PortfolioViewHolder.AVATAR_IMAGE_WIDTH, PortfolioViewHolder.AVATAR_IMAGE_HEIGHT)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mAvatar);
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
            mSwipeRefresh.setRefreshing(false);
            return;
        }

        RestClientMinApi.INSTANCE.getClient().priceMultiFull(implode(mCoins), TransactionAddActivity.DEFAULT_SYMBOL, null)
                .compose(bindUntilEvent(ActivityEvent.PAUSE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            mSwipeRefresh.setRefreshing(false);
                            updatePrices(response);
                            updatePortfolio();
                        },
                        e -> {
                            mSwipeRefresh.setRefreshing(false);
                        });

    }

    private void updatePortfolio() {
        mAdapter.update(mCoins);
        calculatePortfolioValues();
    }

    private boolean updatePrices(PriceMultiFullResponse response) {
        HashMap<String, HashMap<String, PriceMultiFullResponse.RawCoin>> prices = response.getRaw();
        for (Map.Entry<String, HashMap<String, PriceMultiFullResponse.RawCoin>> rawCoin : prices.entrySet()) {
            for (Map.Entry<String, PriceMultiFullResponse.RawCoin> currency : rawCoin.getValue().entrySet()) {
                ContentValues values = new ContentValues();
                values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW, currency.getValue().getPrice());
                values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_CHANGE_24H, currency.getValue().getChange24Hour());
                values.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_CHANGE_PCT_24H, currency.getValue().getChangePct24Hour());
                updateCoin(
                        rawCoin.getKey(),
                        currency.getValue().getPrice(),
                        currency.getValue().getChange24Hour(),
                        currency.getValue().getChangePct24Hour()
                );

            }
        }
        return true;
    }

    private void updateCoin(String symbol, double price, double change24h, double changePercent24h) {
        for(PortfolioCoinResponse portfolioCoin : mCoins) {
            if(portfolioCoin.getSymbol().equals(symbol)) {
                portfolioCoin.setPriceNow(price);
                portfolioCoin.setChange24h(change24h);
                portfolioCoin.setChangePercent24h(changePercent24h);
            }
        }
    }

    private String implode(List<PortfolioCoinResponse> coins) {

        StringBuilder builder = new StringBuilder();

        Iterator<PortfolioCoinResponse> iterator = coins.iterator();
        while (iterator.hasNext()) {
            PortfolioCoinResponse el = iterator.next();
            builder.append(el.getSymbol());
            if(iterator.hasNext()) {
                builder.append(",");
            }
        }

        return builder.toString();
    }

    public void retrieveCoins() {
        compositeDisposable.add(
                MainServiceGenerator.createService(MainApiService.class, this).publicPortfolio(Long.toString(argUserId), Long.toString(argPortfolioId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    mCoins = response;
                                    mAdapter.update(response);
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

        for (PortfolioCoinResponse portfolioCoin : mCoins) {
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
        mPortfolioCurrentValueUnit.setText(TransactionAddActivity.DEFAULT_SYMBOL_ICON);
        mPortfolioProfit24h.setText(KeyboardHelper.cutForHeader(profit24h));
        mPortfolioProfit24hUnit.setText(String.format(Locale.US, "%s (%s%%)", TransactionAddActivity.DEFAULT_SYMBOL_ICON, Math.round(profit24hPercent)));
        mPortfolioOriginalValue.setText(KeyboardHelper.cut(valueAll));
        mPortfolioOriginalValueUnit.setText(TransactionAddActivity.DEFAULT_SYMBOL_ICON);
        mPortfolioProfitAll.setText(KeyboardHelper.cutForHeader(profitAll));
        mPortfolioProfitAllUnit.setText(String.format(Locale.US, "%s (%.2f%%)", TransactionAddActivity.DEFAULT_SYMBOL_ICON, profitAllPercent));

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
