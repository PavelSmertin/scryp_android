package com.start.crypto.android.publicPortfolio;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.start.crypto.android.R;
import com.start.crypto.android.api.model.PortfolioCoinResponse;

import java.util.ArrayList;
import java.util.List;

class PublicPortfolioCoinsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    List<PortfolioCoinResponse> mPortfolioCoins = new ArrayList<>();

    public PublicPortfolioCoinsAdapter() {
    }

    @Override
    public int getItemCount() {
        return mPortfolioCoins.size() + 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if(viewHolder instanceof PublicPortfolioCoinsViewHolder) {
            ((PublicPortfolioCoinsViewHolder)viewHolder).bindData(viewHolder.itemView.getContext(), mPortfolioCoins.get(position-1));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_portfolio_coins_header, parent, false);
            return  new PortfolioCoinsHeaderViewHolder(v);
        }

        if(viewType == TYPE_ITEM)  {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_portfolio_coins_base, parent, false);
            return new PublicPortfolioCoinsViewHolder(v);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public int getItemViewType(int position) {
        if(isPositionHeader(position)) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    public void update(List<PortfolioCoinResponse> portfolioCoins) {
        mPortfolioCoins = portfolioCoins;
        notifyDataSetChanged();
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

}
