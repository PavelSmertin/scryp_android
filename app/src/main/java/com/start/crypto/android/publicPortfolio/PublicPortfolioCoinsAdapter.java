package com.start.crypto.android.publicPortfolio;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.start.crypto.android.R;
import com.start.crypto.android.api.model.PortfolioCoin;

import java.util.ArrayList;
import java.util.List;

class PublicPortfolioCoinsAdapter extends RecyclerView.Adapter<PublicPortfolioCoinsViewHolder> {

    List<PortfolioCoin> mPortfolioCoins = new ArrayList<>();

    public PublicPortfolioCoinsAdapter() {
    }

    @Override
    public int getItemCount() {
        return mPortfolioCoins.size();
    }

    @Override
    public void onBindViewHolder(PublicPortfolioCoinsViewHolder viewHolder, int position) {
        viewHolder.bindData(viewHolder.itemView.getContext(), mPortfolioCoins.get(position));
    }

    @Override
    public PublicPortfolioCoinsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_portfolio_coins_public, parent, false);
        return new PublicPortfolioCoinsViewHolder(v);
    }


    public void update(List<PortfolioCoin> portfolioCoins) {
        mPortfolioCoins = portfolioCoins;
        notifyDataSetChanged();
    }

}
