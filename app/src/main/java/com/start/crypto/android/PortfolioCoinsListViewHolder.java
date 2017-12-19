package com.start.crypto.android;

import android.content.Context;
import android.database.Cursor;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.data.ColumnsCoin;
import com.start.crypto.android.data.ColumnsExchange;
import com.start.crypto.android.data.ColumnsPortfolioCoin;
import com.start.crypto.android.utils.KeyboardHelper;

import java.math.BigDecimal;

class PortfolioCoinsListViewHolder extends RecyclerView.ViewHolder  {

    private TextView coinOriginalView;
    private TextView coinOriginalBalanceView;

    public TextView         coinExchangeView;
    public TextView         coinSymbolView;
    public TextView         coinPriceView;
    public TextView         coinProfitView;
    public TextView         coinHoldingsView;
    public SwipeLayout      swipeLayout;
    public View             bottomWraper;
    public Button           buyButton;
    public Button           sellButton;
    public ConstraintLayout mListRow;

    public PortfolioCoinsListViewHolder(View itemView) {
        super(itemView);
        coinExchangeView        = itemView.findViewById(R.id.coin_exchange);
        coinSymbolView          = itemView.findViewById(R.id.coin_symbol);
        coinOriginalView        = itemView.findViewById(R.id.coin_original);
        coinOriginalBalanceView = itemView.findViewById(R.id.coin_original_balance);
        coinPriceView           = itemView.findViewById(R.id.coin_price);
        coinProfitView          = itemView.findViewById(R.id.coin_profit);
        coinHoldingsView        = itemView.findViewById(R.id.coin_holdings);
        swipeLayout             = itemView.findViewById(R.id.swipe_layout);
        bottomWraper            = itemView.findViewById(R.id.bottom_wrapper);
        buyButton               = itemView.findViewById(R.id.buy);
        sellButton              = itemView.findViewById(R.id.sell);
        mListRow                = itemView.findViewById(R.id.conversation_list_row);

    }

    public void bindData(Context context, Cursor data) {


        ColumnsPortfolioCoin.ColumnsMap columnsMap      = new ColumnsPortfolioCoin.ColumnsMap(data);
        ColumnsCoin.ColumnsMap columnsCoinsMap          = new ColumnsCoin.ColumnsMap(data);
        ColumnsExchange.ColumnsMap columnsExchangesMap  = new ColumnsExchange.ColumnsMap(data);

        double original = data.getDouble(columnsMap.mOriginal);
        double priceOriginal = data.getDouble(columnsMap.mColumnPriceOriginal);
        double priceNow = data.getDouble(columnsMap.mColumnPriceNow);
        double price24h = data.getDouble(columnsMap.mColumnPrice24h);

        double profit24h = original * (priceNow - price24h);
        double coinHolding = original * priceNow;

        coinExchangeView.setText(data.getString(columnsExchangesMap.mColumnName));
        coinSymbolView.setText(data.getString(columnsCoinsMap.mColumnSymbol));
        coinOriginalView.setText(data.getString(columnsMap.mOriginal));
        coinOriginalBalanceView.setText(
                KeyboardHelper.formatter.format(
                        new BigDecimal(
                                data.getDouble(columnsMap.mOriginal) * data.getDouble(columnsMap.mColumnPriceOriginal)
                        ).setScale(2, BigDecimal.ROUND_CEILING).doubleValue()));
        coinPriceView.setText(
                KeyboardHelper.formatter.format(
                        new BigDecimal(
                                data.getString(columnsMap.mColumnPriceNow)
                        ).setScale(2, BigDecimal.ROUND_CEILING).doubleValue()));

        if(Double.isInfinite(profit24h)) {
            return;
        }

        coinProfitView.setText(KeyboardHelper.formatter.format(new BigDecimal(profit24h).setScale(2, BigDecimal.ROUND_CEILING).doubleValue()));
        if(profit24h < 0) {
            coinProfitView.setTextColor(context.getResources().getColor(R.color.colorDownValue));
        }

        coinHoldingsView.setText(KeyboardHelper.formatter.format(new BigDecimal(coinHolding).setScale(2, BigDecimal.ROUND_CEILING).doubleValue()));

        long coinId         = data.getLong(columnsMap.mCoinId);
        String coinSymbol   = data.getString(columnsCoinsMap.mColumnSymbol);
        long correspondId   = data.getLong(columnsMap.mCoinId);

//        RxView.clicks(addNOtificationButton).subscribe(el -> NotificationFormActivity.startActivity(
//                        context,
//                        coinId,
//                        correspondId,
//                        data.getLong(columnsMap.mExchangeId),
//                        data.getDouble(columnsMap.mColumnPriceNow)
//                )
//        );

        mListRow.setOnClickListener(view -> {
            if(swipeLayout.getOpenStatus() == SwipeLayout.Status.Close){
                PortfolioCoinActivity.startActivity(
                        context,
                        coinId,
                        coinSymbol,
                        priceNow,
                        original,
                        priceOriginal,
                        profit24h

                );
            }
        });

        //set show mode.
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

        //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, bottomWraper);

        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
                //when the SurfaceView totally cover the BottomView.
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                //you are swiping.
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {
                //when the BottomView totally show.
            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                //when user's hand released.
            }
        });

        RxView.clicks(buyButton).subscribe(el -> TransactionActivity.start(context, coinId, coinSymbol, data.getLong(columnsMap.mExchangeId), TransactionType.BUY));
        RxView.clicks(sellButton).subscribe(el -> TransactionActivity.start(context, coinId, coinSymbol, data.getLong(columnsMap.mExchangeId), TransactionType.SELL));

    }


}
