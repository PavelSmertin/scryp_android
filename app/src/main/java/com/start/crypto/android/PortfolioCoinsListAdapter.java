package com.start.crypto.android;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class PortfolioCoinsListAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder>  {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public PortfolioCoinsListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_portfolio_coins_header, parent, false);
            return  new PortfolioCoinsHeaderViewHolder(v);
        }

        if(viewType == TYPE_ITEM)  {
            View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_portfolio_coins, parent, false);
            return new PortfolioCoinsListViewHolder(v);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        if (position > 0 && !getCursor().moveToPosition(position-1)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        onBindViewHolder(viewHolder, getCursor());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        cursor.moveToPosition(cursor.getPosition());
        if(viewHolder instanceof PortfolioCoinsListViewHolder) {
            ((PortfolioCoinsListViewHolder)viewHolder).bindData(mContext, cursor);;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(isPositionHeader(position)) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


    @Override
    public int getItemCount() {
        if(super.getItemCount() > 0) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
    }


}
