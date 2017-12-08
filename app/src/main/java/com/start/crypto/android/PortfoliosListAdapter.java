package com.start.crypto.android;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class PortfoliosListAdapter extends CursorRecyclerViewAdapter<PortfoliosListViewHolder>  {

    public PortfoliosListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public PortfoliosListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_portfolio, parent, false);
        return new PortfoliosListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PortfoliosListViewHolder viewHolder, Cursor cursor) {
        cursor.moveToPosition(cursor.getPosition());
        viewHolder.bindData(mContext, cursor);
    }

}
