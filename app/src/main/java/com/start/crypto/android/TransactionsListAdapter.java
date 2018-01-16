package com.start.crypto.android;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class TransactionsListAdapter extends CursorRecyclerViewAdapter<TransactionsListViewHolder>  {

    public TransactionsListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public TransactionsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.transaction_list_item, parent, false);
        return new TransactionsListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TransactionsListViewHolder viewHolder, Cursor cursor) {
        cursor.moveToPosition(cursor.getPosition());
        viewHolder.bindData(cursor);
    }

}
