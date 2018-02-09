package com.start.crypto.android.publicPortfolio;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.start.crypto.android.R;
import com.start.crypto.android.api.model.Portfolio;
import com.start.crypto.android.imageLoader.GlideApp;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PortfolioViewHolder extends RecyclerView.ViewHolder  {

    public static final int AVATAR_IMAGE_WIDTH = 96;
    public static final int AVATAR_IMAGE_HEIGHT = 96;


    private Context mContext;

    private int mUpTendColor;
    private int mDownTendColor;

    @Nullable @BindView(R.id.user_logo)     ImageView mAvatar;
    @Nullable @BindView(R.id.user_name)     TextView mUserName;
    @Nullable @BindView(R.id.coins_count)   TextView mCoinsCount;
    @Nullable @BindView(R.id.profit_24h)    TextView mProfit24h;
    @Nullable @BindView(R.id.profit_7d)     TextView mprofit7d;


    public PortfolioViewHolder(View view) {
        super(view);
        mContext = view.getContext();
        ButterKnife.bind(this, view);

        mDownTendColor = view.getResources().getColor(R.color.colorDownValue);
        mUpTendColor = view.getResources().getColor(R.color.colorUpValue);
    }

    public void bind(Portfolio portfolio) {

        if(portfolio.getAvatar() != null) {
            GlideApp.with(mContext)
                    .load(portfolio.getAvatar())
                    .centerCrop()
                    .override(AVATAR_IMAGE_WIDTH, AVATAR_IMAGE_HEIGHT)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mAvatar);
        }

        mUserName.setText(portfolio.getUserName());
        mCoinsCount.setText(String.format("%d coins", portfolio.getCoinsCount()));
        mProfit24h.setText(new BigDecimal(portfolio.getProfit24h()).setScale(0, BigDecimal.ROUND_FLOOR)+ "%");
        mprofit7d.setText(new BigDecimal(portfolio.getProfit7d()).setScale(0, BigDecimal.ROUND_FLOOR) + "%");

        if(portfolio.getProfit24h() < 0) {
            mProfit24h.setTextColor(mDownTendColor);
        } else {
            mProfit24h.setTextColor(mUpTendColor);
        }

        if(portfolio.getProfit7d() < 0) {
            mprofit7d.setTextColor(mDownTendColor);
        } else {
            mprofit7d.setTextColor(mUpTendColor);
        }
    }
}
