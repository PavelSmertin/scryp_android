package com.start.crypto.android.publicPortfolio;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.start.crypto.android.R;
import com.start.crypto.android.TransactionAddActivity;
import com.start.crypto.android.api.model.PortfolioCoinResponse;
import com.start.crypto.android.utils.KeyboardHelper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PublicPortfolioCoinsViewHolder extends RecyclerView.ViewHolder  {

    @BindView(R.id.coin_original)       TextView coinOriginalView;
    @BindView(R.id.coin_symbol)         TextView coinSymbolView;
    @BindView(R.id.coin_price)          TextView coinPriceView;
    @BindView(R.id.coin_profit)         TextView coinProfitView;
    @BindView(R.id.coin_holdings)       TextView coinHoldingsView;
    @BindView(R.id.coin_profit_value)   TextView coinProfitValueView;

    public PublicPortfolioCoinsViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindData(Context context, PortfolioCoinResponse portfolioCoin) {

        double original         = portfolioCoin.getOriginal();
        double priceOriginal    = portfolioCoin.getPriceOriginal();
        double priceNow         = portfolioCoin.getPriceNow();
        double price24h         = portfolioCoin.getPrice24h();

        String coinSymbol = portfolioCoin.getSymbol();


        double profit24h = original * (priceNow - price24h);
        double coinHolding = original * priceNow;

        coinSymbolView.setText(coinSymbol);

        double priceDeltaPercent24h = 0;
        if(price24h > 0) {
            priceDeltaPercent24h = (priceNow - price24h) * 100 / price24h;
        }

        double priceDeltaPercentAll = 0;
        if(priceOriginal > 0) {
            priceDeltaPercentAll = (priceNow - priceOriginal) * 100 / priceOriginal;
        }

        if(!Double.isNaN(coinHolding) && !Double.isInfinite(coinHolding) && coinHolding > 0) {
            coinOriginalView.setText(String.format(Locale.US, "Value %s %s",
                    KeyboardHelper.cut(coinHolding),
                    TransactionAddActivity.DEFAULT_SYMBOL
            ));
        }

        if(!Double.isNaN(priceNow) && !Double.isInfinite(priceNow) && priceNow > 0) {
            coinPriceView.setText(String.format(Locale.US, "%s",
                    KeyboardHelper.format(priceNow)
            ));
        }
        if(profit24h < 0) {
            coinPriceView.setTextColor(context.getResources().getColor(R.color.colorDownValue));
        } else {
            coinPriceView.setTextColor(context.getResources().getColor(R.color.colorUpValue));
        }

        if(!Double.isNaN(priceDeltaPercentAll) && !Double.isInfinite(priceDeltaPercentAll) && priceDeltaPercentAll > 0) {
            if(priceDeltaPercentAll < 1000D) {
                coinProfitView.setText(String.format(Locale.US, "%.2f%%", priceDeltaPercentAll));
            } else {
                coinProfitView.setText(String.format(Locale.US, "%s%%", KeyboardHelper.cut(priceDeltaPercentAll)));
            }
        }

        if(priceDeltaPercentAll < 0) {
            coinProfitView.setTextColor(context.getResources().getColor(R.color.colorDownValue));
        } else {
            coinProfitView.setTextColor(context.getResources().getColor(R.color.colorUpValue));
        }

        if(!Double.isNaN(priceDeltaPercent24h) && !Double.isInfinite(priceDeltaPercent24h) && priceDeltaPercent24h > 0) {
            coinHoldingsView.setText(String.format(Locale.US, "24h: %.2f%%", priceDeltaPercent24h));
        } else {
            coinHoldingsView.setText(context.getString(R.string.portfolio_coin_24h_change_unknown));
        }

        double deltaValueAll = 0;
        if(priceNow > 0) {
            deltaValueAll = (priceNow - priceOriginal) * original;
        }
        coinProfitValueView.setText(String.format(Locale.US, "%s", KeyboardHelper.cut(deltaValueAll)));

    }

}
