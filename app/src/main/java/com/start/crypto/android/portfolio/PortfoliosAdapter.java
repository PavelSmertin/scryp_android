package com.start.crypto.android.portfolio;


import android.app.Activity;
import android.app.ActivityOptions;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.start.crypto.android.R;
import com.start.crypto.android.api.model.Portfolio;

import java.util.LinkedList;
import java.util.List;

public class PortfoliosAdapter extends RecyclerView.Adapter {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<Portfolio> mPortfolios = new LinkedList<>();
    private final Activity mContext;


    public PortfoliosAdapter(Activity context) {
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return mPortfolios.size() + 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof PortfolioViewHolder) {
            ((PortfolioViewHolder)holder).bind(mPortfolios.get(position - 1));
            holder.itemView.setOnClickListener(v -> {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    View userLogoView = holder.itemView.findViewById(R.id.user_logo);

                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                            mContext,
                            Pair.create(
                                    userLogoView,
                                    mContext.getString(R.string.transition_user_logo)
                            )
                    );
                    PortfolioActivity.start(
                            mContext,
                            null,
                            mPortfolios.get(position - 1).getUserId(),
                            mPortfolios.get(position - 1).getId(),
                            mPortfolios.get(position - 1).getUserName(),
                            mPortfolios.get(position - 1).getAvatar()
                    );
                } else {
                    PortfolioActivity.start(
                            mContext,
                            mPortfolios.get(position).getUserId(),
                            mPortfolios.get(position).getId(),
                            mPortfolios.get(position).getUserName(),
                            mPortfolios.get(position - 1).getAvatar()
                    );
                }
            });
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.portfolio_list_item_header, parent, false);
            return  new PortfolioHeaderViewHolder(v);
        }

        if(viewType == TYPE_ITEM)  {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.portfolio_list_item, parent, false);
            return new PortfolioViewHolder(v);
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

    public void updatePortfolios(List<Portfolio> portfolios) {
        mPortfolios.clear();
        mPortfolios.addAll(portfolios);
        notifyDataSetChanged();
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

}