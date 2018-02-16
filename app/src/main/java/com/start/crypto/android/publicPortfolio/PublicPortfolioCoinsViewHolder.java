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

class PublicPortfolioCoinsViewHolder extends RecyclerView.ViewHolder  {

    private TextView coinOriginalView;

    private TextView         coinSymbolView;
    private TextView         coinPriceView;
    private TextView         coinProfitView;
    private TextView         coinHoldingsView;
    private TextView         coinProfitValueView;

    public PublicPortfolioCoinsViewHolder(View itemView) {
        super(itemView);
        coinSymbolView          = itemView.findViewById(R.id.coin_symbol);
        coinOriginalView        = itemView.findViewById(R.id.coin_original);
        coinPriceView           = itemView.findViewById(R.id.coin_price);
        coinProfitView          = itemView.findViewById(R.id.coin_profit);
        coinHoldingsView        = itemView.findViewById(R.id.coin_holdings);
        coinProfitValueView     = itemView.findViewById(R.id.coin_profit_value);
    }

    public void bindData(Context context, PortfolioCoinResponse portfolioCoin) {

        double original         = portfolioCoin.getOriginal();
        double priceOriginal    = portfolioCoin.getPriceOriginal();
        double priceNow         = portfolioCoin.getPriceNow();
        double price24h         = portfolioCoin.getPrice24h();

        String exchangeName = portfolioCoin.getExchangeName();
        String coinSymbol = portfolioCoin.getSymbol();

        double profit24h = original * (priceNow - price24h);
        double coinHolding = original * priceNow;

        coinSymbolView.setText(coinSymbol);

        double priceDelta24h = 0;
        if(price24h > 0) {
            priceDelta24h = (priceNow - price24h) * 100 / price24h;
        }

        double priceDeltaAll = 0;
        if(priceOriginal > 0) {
            priceDeltaAll = (priceNow - priceOriginal) * 100 / priceOriginal;
        }

        coinOriginalView.setText(String.format(Locale.US, "Value %s %s",
                KeyboardHelper.cut(coinHolding),
                TransactionAddActivity.DEFAULT_SYMBOL
        ));

        coinPriceView.setText(String.format(Locale.US, "%s",
                KeyboardHelper.format(priceNow)
        ));
        if(profit24h < 0) {
            coinPriceView.setTextColor(context.getResources().getColor(R.color.colorDownValue));
        }

        if(Double.isInfinite(profit24h)) {
            return;
        }

        if(priceOriginal > 0) {
            if(priceDeltaAll < 1000D) {
                coinProfitView.setText(String.format(Locale.US, "%.2f%%", priceDeltaAll));
            } else {
                coinProfitView.setText(String.format(Locale.US, "%s%%", KeyboardHelper.cut(priceDeltaAll)));
            }
        }

        if(priceDeltaAll < 0) {
            coinProfitView.setTextColor(context.getResources().getColor(R.color.colorDownValue));
        }

        if(price24h > 0) {
            coinHoldingsView.setText(String.format(Locale.US, "24h: %.2f%%", priceDelta24h));
        }

        double deltaValueAll = 0;
        if(priceNow > 0) {
            deltaValueAll = (priceNow - priceOriginal) * original;
        }
        coinProfitValueView.setText(String.format(Locale.US, "%s", KeyboardHelper.cut(deltaValueAll)));

    }


}
