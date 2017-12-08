package com.start.crypto.android;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.data.ColumnsCoin;
import com.start.crypto.android.data.ColumnsPortfolioCoin;

import java.math.BigDecimal;

class PortfolioCoinsListViewHolder extends RecyclerView.ViewHolder  {

    private TextView coinOriginalView;
    private TextView coinOriginalBalanceView;

    public TextView coinSymbolView;
    public TextView coinPriceView;
    public TextView coinProfitView;
    public TextView coinHoldingsView;
    public ImageView addNOtificationButton;

    public PortfolioCoinsListViewHolder(View itemView) {
        super(itemView);
        coinSymbolView          = itemView.findViewById(R.id.coin_symbol);
        coinOriginalView        = itemView.findViewById(R.id.coin_original);
        coinOriginalBalanceView = itemView.findViewById(R.id.coin_original_balance);
        coinPriceView           = itemView.findViewById(R.id.coin_price);
        coinProfitView          = itemView.findViewById(R.id.coin_profit);
        coinHoldingsView        = itemView.findViewById(R.id.coin_holdings);
        addNOtificationButton   = itemView.findViewById(R.id.add_notification);


    }

    public void bindData(Context context, Cursor data) {


        ColumnsPortfolioCoin.ColumnsMap columnsMap = new ColumnsPortfolioCoin.ColumnsMap(data);
        ColumnsCoin.ColumnsMap columnsCoinsMap = new ColumnsCoin.ColumnsMap(data);

        double original = data.getDouble(columnsMap.mOriginal);
        double priceOriginal = data.getDouble(columnsMap.mColumnPriceOriginal);
        double priceNow = data.getDouble(columnsMap.mColumnPriceNow);
        double price24h = data.getDouble(columnsMap.mColumnPrice24h);

        double profit24h = original * (priceNow - price24h);
        double coinHolding = original * priceNow;

        coinSymbolView.setText(data.getString(columnsCoinsMap.mColumnSymbol));
        coinOriginalView.setText(data.getString(columnsMap.mOriginal));
        coinOriginalBalanceView.setText((data.getDouble(columnsMap.mOriginal) * data.getDouble(columnsMap.mColumnPriceOriginal)) + "");
        coinPriceView.setText(data.getString(columnsMap.mColumnPriceNow));

        if(Double.isInfinite(profit24h)) {
            return;
        }

        coinProfitView.setText(new BigDecimal(profit24h).setScale(0, BigDecimal.ROUND_CEILING).toString());
        if(profit24h < 0) {
            coinProfitView.setTextColor(context.getResources().getColor(R.color.colorDownValue));
        }

        coinHoldingsView.setText(new BigDecimal(coinHolding).setScale(0, BigDecimal.ROUND_CEILING).toString());

        RxView.clicks(addNOtificationButton).subscribe(el -> NotificationFormActivity.startActivity(
                        context,
                        data.getLong(columnsMap.mCoinId),
                        477,
                        data.getLong(columnsMap.mExchangeId),
                        data.getDouble(columnsMap.mColumnPriceNow)
                )
        );
    }
}
