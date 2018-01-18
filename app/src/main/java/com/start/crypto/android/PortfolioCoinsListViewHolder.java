package com.start.crypto.android;

import android.content.Context;
import android.database.Cursor;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.data.ColumnsCoin;
import com.start.crypto.android.data.ColumnsPortfolioCoin;
import com.start.crypto.android.utils.KeyboardHelper;

import java.util.Locale;

class PortfolioCoinsListViewHolder extends RecyclerView.ViewHolder  {

    private TextView coinOriginalView;

    private TextView         coinSymbolView;
    private TextView         coinPriceView;
    private TextView         coinProfitView;
    private TextView         coinHoldingsView;
    private TextView         coinProfitValueView;
    private SwipeLayout      swipeLayout;
    private View             bottomWraper;
    private ImageView        changeButton;
    private ConstraintLayout mListRow;

    private double mOriginal;
    private long mPortfolioId;
    private long mPortfolioCoinId;


    public PortfolioCoinsListViewHolder(View itemView) {
        super(itemView);
        coinSymbolView          = itemView.findViewById(R.id.coin_symbol);
        coinOriginalView        = itemView.findViewById(R.id.coin_original);
        coinPriceView           = itemView.findViewById(R.id.coin_price);
        coinProfitView          = itemView.findViewById(R.id.coin_profit);
        coinHoldingsView        = itemView.findViewById(R.id.coin_holdings);
        coinProfitValueView     = itemView.findViewById(R.id.coin_profit_value);
        swipeLayout             = itemView.findViewById(R.id.swipe_layout);
        bottomWraper            = itemView.findViewById(R.id.bottom_wrapper);
        changeButton            = itemView.findViewById(R.id.change);
        mListRow                = itemView.findViewById(R.id.coins_list_row);

    }

    public void bindData(Context context, Cursor data) {


        ColumnsPortfolioCoin.ColumnsMap columnsMap      = new ColumnsPortfolioCoin.ColumnsMap(data);
        ColumnsCoin.ColumnsMap columnsCoinsMap          = new ColumnsCoin.ColumnsMap(data);

        mPortfolioId = data.getLong(columnsMap.mColumnPortfolioId);
        mPortfolioCoinId = data.getLong(columnsMap.mColumnId);
        mOriginal = data.getDouble(columnsMap.mColumnOriginal);
        double priceNow = data.getDouble(columnsMap.mColumnPriceNow);
        double price24h = data.getDouble(columnsMap.mColumnPrice24h);
        double priceOriginal = data.getDouble(columnsMap.mColumnPriceOriginal);

        double profit24h = mOriginal * (priceNow - price24h);
        double coinHolding = mOriginal * priceNow;

        coinSymbolView.setText(data.getString(columnsCoinsMap.mColumnSymbol));

        double priceDelta = 0;
        if(priceNow > 0) {
            priceDelta = (priceNow - price24h) * 100 / priceNow;
        }

        double deltaAll = 0;
        if(priceNow > 0) {
            deltaAll = (priceNow - priceOriginal) * 100 / priceNow;
        }

        coinOriginalView.setText(String.format(Locale.US, "Total %s %s",
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

        coinProfitView.setText(String.format(Locale.US, "%s%%", KeyboardHelper.cut(deltaAll)));
        if(deltaAll < 0) {
            coinProfitView.setTextColor(context.getResources().getColor(R.color.colorDownValue));
        }

        coinHoldingsView.setText(String.format(Locale.US, "24h: %.2f%%", priceDelta));

        double deltaValueAll = 0;
        if(priceNow > 0) {
            deltaValueAll = (priceNow - priceOriginal) * mOriginal;
        }
        coinProfitValueView.setText(String.format(Locale.US, "%s", KeyboardHelper.cut(deltaValueAll)));

        long portfolioCoinId    = data.getLong(columnsMap.mColumnId);

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
                PortfolioCoinActivity.start(
                        context,
                        portfolioCoinId
                );
            }
        });

        //set show mode.
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

        //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, bottomWraper);

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

        RxView.clicks(changeButton).subscribe(el -> TransactionBuySellActivity.start(
                context,
                mPortfolioId,
                mPortfolioCoinId,
                data.getLong(columnsMap.mColumnExchangeId))
        );

    }


}
