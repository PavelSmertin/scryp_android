package com.start.crypto.android;


import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.start.crypto.android.api.model.Portfolio;
import com.start.crypto.android.publicPortfolio.PortfolioActivity;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PortfoliosAdapter extends RecyclerView.Adapter {

    private final List<Portfolio> mPortfolios = new LinkedList<>();


    public PortfoliosAdapter() {
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_portfolio, parent, false);
        return new ViewHolder(view, inflater);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).bind(mPortfolios.get(position));
        holder.itemView.setOnClickListener(v -> PortfolioActivity.start(
                ((ViewHolder) holder).itemView.getContext(),
                mPortfolios.get(position).getUserId(),
                mPortfolios.get(position).getUserName()

        ));
    }

    @Override
    public int getItemCount() {
        return mPortfolios.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void updatePortfolios(List<Portfolio> portfolios) {
        mPortfolios.clear();
        mPortfolios.addAll(portfolios);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected LayoutInflater mInflater;

        private int mUpTendColor;
        private int mDownTendColor;

        @Nullable @BindView(R.id.user_name)     TextView mUserName;
        @Nullable @BindView(R.id.coins_count)   TextView mCoinsCount;
        @Nullable @BindView(R.id.profit_24h)    TextView mProfit24h;
        @Nullable @BindView(R.id.profit_7d)     TextView mprofit7d;

        public ViewHolder(View view, LayoutInflater inflater) {
            super(view);
            mInflater = inflater;
            ButterKnife.bind(this, view);

            mDownTendColor = view.getResources().getColor(R.color.colorDownValue);
            mUpTendColor = view.getResources().getColor(R.color.colorUpValue);
        }

        public void bind(Portfolio portfolio) {
            mUserName.setText(portfolio.getUserName());
            mCoinsCount.setText(String.format("%d coins", portfolio.getCoinsCount()));
            mProfit24h.setText(new BigDecimal(portfolio.getProfit24h()).setScale(0, BigDecimal.ROUND_CEILING)+ "%");
            mprofit7d.setText(new BigDecimal(portfolio.getProfit7d()).setScale(0, BigDecimal.ROUND_CEILING) + "%");

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


}