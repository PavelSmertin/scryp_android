package com.start.crypto.android.publicPortfolio;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.start.crypto.android.CreateTransactionActivity;
import com.start.crypto.android.R;
import com.start.crypto.android.api.model.PortfolioCoin;
import com.start.crypto.android.utils.KeyboardHelper;

import java.util.Locale;

class PublicPortfolioCoinsViewHolder extends RecyclerView.ViewHolder  {

    private TextView coinOriginalView;
    private TextView coinOriginalBalanceView;

    private TextView         coinExchangeView;
    private TextView         coinSymbolView;
    private TextView         coinPriceView;
    private TextView         coinProfitView;
    private TextView         coinHoldingsView;


    public PublicPortfolioCoinsViewHolder(View itemView) {
        super(itemView);
        coinExchangeView        = itemView.findViewById(R.id.coin_exchange);
        coinSymbolView          = itemView.findViewById(R.id.coin_symbol);
        coinOriginalView        = itemView.findViewById(R.id.coin_original);
        coinOriginalBalanceView = itemView.findViewById(R.id.coin_original_balance);
        coinPriceView           = itemView.findViewById(R.id.coin_price);
        coinProfitView          = itemView.findViewById(R.id.coin_profit);
        coinHoldingsView        = itemView.findViewById(R.id.coin_holdings);
    }

    public void bindData(Context context, PortfolioCoin portfolioCoin) {

        double original = portfolioCoin.getOriginal();
        double priceOriginal    = portfolioCoin.getPriceOriginal();
        double priceNow         = portfolioCoin.getPriceNow();
        double price24h         = portfolioCoin.getPrice24h();

        String exchangeName = portfolioCoin.getExchangeName();
        String coinSymbol = portfolioCoin.getSymbol();

        double profit24h = original * (priceNow - price24h);
        double coinHolding = original * priceNow;

        coinExchangeView.setText(exchangeName);
        coinSymbolView.setText(coinSymbol);
        String originalBalance = KeyboardHelper.cut(original * priceOriginal);

        double priceDelta = 0;
        if(priceNow > 0) {
            priceDelta = (priceNow - price24h) * 100 / priceNow;
        }

        coinOriginalView.setText(String.format(Locale.US, "%s @ %s %s",
                KeyboardHelper.cut(original),
                originalBalance,
                CreateTransactionActivity.DEFAULT_SYMBOL
        ));

        coinPriceView.setText(String.format(Locale.US, "%s(%.2f%%)",
                KeyboardHelper.format(priceNow),
                priceDelta
        ));

        if(Double.isInfinite(profit24h)) {
            return;
        }

        coinProfitView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.cut(profit24h), CreateTransactionActivity.DEFAULT_SYMBOL));
        if(profit24h < 0) {
            coinProfitView.setTextColor(context.getResources().getColor(R.color.colorDownValue));
        }

        coinHoldingsView.setText(String.format(Locale.US, "%s %s", KeyboardHelper.cut(coinHolding), CreateTransactionActivity.DEFAULT_SYMBOL));

    }


}
