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
import java.util.Locale;

class PortfolioCoinsListViewHolder extends RecyclerView.ViewHolder  {

    private TextView coinOriginalView;
    private TextView coinOriginalBalanceView;

    private TextView         coinExchangeView;
    private TextView         coinSymbolView;
    private TextView         coinPriceView;
    private TextView         coinProfitView;
    private TextView         coinHoldingsView;
    private SwipeLayout      swipeLayout;
    private View             bottomWraper;
    private Button           buyButton;
    private Button           sellButton;
    private ConstraintLayout mListRow;

    private double mOriginal;
    private long mPortfolioId;
    private long mPortfolioCoinId;


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

        mPortfolioId = data.getLong(columnsMap.mPortfolioId);
        mPortfolioCoinId = data.getLong(columnsMap.mColumnId);
        mOriginal = data.getDouble(columnsMap.mOriginal);
        double priceOriginal = data.getDouble(columnsMap.mColumnPriceOriginal);
        double priceNow = data.getDouble(columnsMap.mColumnPriceNow);
        double price24h = data.getDouble(columnsMap.mColumnPrice24h);

        double profit24h = mOriginal * (priceNow - price24h);
        double coinHolding = mOriginal * priceNow;

        coinExchangeView.setText(data.getString(columnsExchangesMap.mColumnName));
        coinSymbolView.setText(data.getString(columnsCoinsMap.mColumnSymbol));
        String originalPrice = data.getString(columnsMap.mOriginal);
        String originalBalance = KeyboardHelper.format(data.getDouble(columnsMap.mOriginal) * data.getDouble(columnsMap.mColumnPriceOriginal));

        coinOriginalView.setText(String.format(Locale.US, "%s @ %s", KeyboardHelper.formatter.format(
                new BigDecimal(originalPrice).setScale(2, BigDecimal.ROUND_CEILING).doubleValue()), originalBalance));

        coinPriceView.setText(
                KeyboardHelper.formatter.format(
                        new BigDecimal(
                                data.getString(columnsMap.mColumnPriceNow)
                        ).setScale(2, BigDecimal.ROUND_CEILING).doubleValue()));

        if(Double.isInfinite(profit24h)) {
            return;
        }

        coinProfitView.setText(KeyboardHelper.format(profit24h));
        if(profit24h < 0) {
            coinProfitView.setTextColor(context.getResources().getColor(R.color.colorDownValue));
        }

        coinHoldingsView.setText(KeyboardHelper.format(coinHolding));

        long portfolioCoinId    = data.getLong(columnsMap.mColumnId);
        long coinId             = data.getLong(columnsMap.mCoinId);
        String coinSymbol       = data.getString(columnsCoinsMap.mColumnSymbol);
        long correspondId       = data.getLong(columnsMap.mCoinId);

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
                        portfolioCoinId,
                        coinSymbol,
                        priceNow,
                        mOriginal,
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

        RxView.clicks(buyButton).subscribe(el -> TransactionActivity.start(
                context,
                mPortfolioId,
                mPortfolioCoinId,
                coinId,
                coinSymbol,
                data.getLong(columnsMap.mExchangeId),
                TransactionType.BUY)
        );

        if(mOriginal <=0 ) {
            sellButton.setEnabled(false);
        } else {
            sellButton.setEnabled(true);
            RxView.clicks(sellButton).subscribe(el -> TransactionActivity.start(
                    context,
                    mPortfolioId,
                    mPortfolioCoinId,
                    coinId,
                    coinSymbol,
                    data.getLong(columnsMap.mExchangeId),
                    TransactionType.SELL)
            );
        }

    }


}
