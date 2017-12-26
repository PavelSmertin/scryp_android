package com.start.crypto.android;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.start.crypto.android.data.ColumnsCoin;

class AutocompleteListViewHolder extends RecyclerView.ViewHolder  {

    public TextView mCoinView;

    public AutocompleteListViewHolder(View itemView) {
        super(itemView);
        mCoinView = itemView.findViewById(R.id.coin_item);
    }

    public void bindData(Cursor data) {
        ColumnsCoin.ColumnsMap columnsMap = new ColumnsCoin.ColumnsMap(data);
        String description = data.getString(columnsMap.mColumnName);
        mCoinView.setText(description);
    }

}
