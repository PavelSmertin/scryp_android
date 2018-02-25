package com.start.crypto.android.portfolio;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.start.crypto.android.R;
import com.start.crypto.android.api.model.PortfolioCoinResponse;
import com.start.crypto.android.transaction.TransactionAddActivity;
import com.start.crypto.android.utils.KeyboardHelper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PublicPortfolioCoinsViewHolder extends RecyclerView.ViewHolder  {

    @BindView(R.id.coin_original)       TextView mValueView;
    @BindView(R.id.coin_symbol)         TextView mSymbolView;
    @BindView(R.id.coin_price)          TextView mPriceNowView;
    @BindView(R.id.coin_profit)         TextView mChangePercentAllView;
    @BindView(R.id.coin_holdings)       TextView mChangePercent24hView;
    @BindView(R.id.coin_profit_value)   TextView mChangeAllView;

    public PublicPortfolioCoinsViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindData(Context context, PortfolioCoinResponse portfolioCoin) {

        String coinSymbol       = portfolioCoin.getSymbol();

        double priceNow         = portfolioCoin.getPriceNow();
        double change24h        = portfolioCoin.getChange24h();
        double changePercent24h = portfolioCoin.getChangePercent24h();
        double value            = portfolioCoin.getValue();
        double changePercentAll = portfolioCoin.getChangePercentAll();
        double changeAll        = portfolioCoin.getChangeAll();

        mSymbolView.setText(coinSymbol);
        mValueView.setText(String.format(Locale.US, "Value %s %s", KeyboardHelper.cut(value), TransactionAddActivity.DEFAULT_SYMBOL_ICON));
        mPriceNowView.setText(String.format(Locale.US, "%s", KeyboardHelper.format(priceNow)));
        mChangePercent24hView.setText(String.format(Locale.US, "24h: %.2f%%", changePercent24h));
        mChangeAllView.setText(String.format(Locale.US, "%s", KeyboardHelper.cut(changeAll)));

        if(changePercentAll < 1000D) {
            mChangePercentAllView.setText(String.format(Locale.US, "%.2f%%", changePercentAll));
        } else {
            mChangePercentAllView.setText(String.format(Locale.US, "%s%%", KeyboardHelper.cut(changePercentAll)));
        }


        // positive/negative colors
        if(change24h < 0) {
            mPriceNowView.setTextColor(context.getResources().getColor(R.color.colorDownValue));
        } else {
            mPriceNowView.setTextColor(context.getResources().getColor(R.color.colorUpValue));
        }

        if(changePercentAll < 0) {
            mChangePercentAllView.setTextColor(context.getResources().getColor(R.color.colorDownValue));
        } else {
            mChangePercentAllView.setTextColor(context.getResources().getColor(R.color.colorUpValue));
        }

    }

}
