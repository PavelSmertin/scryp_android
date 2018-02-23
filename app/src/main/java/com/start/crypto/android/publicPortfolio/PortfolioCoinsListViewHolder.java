package com.start.crypto.android.publicPortfolio;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;

import com.daimajia.swipe.SwipeLayout;
import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.R;
import com.start.crypto.android.TransactionBuySellActivity;
import com.start.crypto.android.api.model.PortfolioCoinResponse;

import butterknife.BindView;

class PortfolioCoinsListViewHolder extends PublicPortfolioCoinsViewHolder {

    @BindView(R.id.swipe_layout)        SwipeLayout      swipeLayout;
    @BindView(R.id.bottom_wrapper)      View             bottomWraper;
    @BindView(R.id.change)              ImageView        changeButton;
    @BindView(R.id.coins_list_row)      ConstraintLayout mListRow;

    public PortfolioCoinsListViewHolder(View itemView) {
        super(itemView);
    }

    public void bindActions(Context context, PortfolioCoinResponse portfolioCoin)  {
        long portfolioId        = portfolioCoin.getPortfolioId();
        long portfolioCoinId    = portfolioCoin.getId();
        long exchangeId         = portfolioCoin.getExchangeId();

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
                portfolioId,
                portfolioCoinId,
                exchangeId
        ));
    }


}
