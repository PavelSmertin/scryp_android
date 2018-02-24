package com.start.crypto.android;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

class AutocompleteListViewHolder extends RecyclerView.ViewHolder  {

    public TextView mCoinView;

    public AutocompleteListViewHolder(View itemView) {
        super(itemView);
        mCoinView = itemView.findViewById(R.id.coin_item);
    }

    public void bindData(String name) {
        mCoinView.setText(name);
    }

}
