package com.start.crypto.android;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class PortfolioCoinsListAdapter extends CursorRecyclerViewAdapter<PortfolioCoinsListViewHolder>  {


    public PortfolioCoinsListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public PortfolioCoinsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.swipe_layout, parent, false);
        return new PortfolioCoinsListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PortfolioCoinsListViewHolder viewHolder, Cursor cursor) {
        cursor.moveToPosition(cursor.getPosition());
        //viewHolder.itemView.setOnClickListener(v -> PortfolioCoinActivity.startActivity(mContext));
        viewHolder.bindData(mContext, cursor);
    }

}
