package com.start.crypto.android;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.start.crypto.android.data.ColumnsTransaction;

class TransactionsListViewHolder extends RecyclerView.ViewHolder  {

    public TextView mDescriptionView;
    private String mDescription;

    public TransactionsListViewHolder(View itemView) {
        super(itemView);
        mDescriptionView = itemView.findViewById(R.id.description);
    }

    public void bindData(Cursor data) {
        ColumnsTransaction.ColumnsMap columnsMap = new ColumnsTransaction.ColumnsMap(data);
        String description = data.getString(columnsMap.mColumnDescription);
        mDescriptionView.setText(description);
    }

}
