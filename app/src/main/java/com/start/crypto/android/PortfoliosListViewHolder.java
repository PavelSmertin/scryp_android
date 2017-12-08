package com.start.crypto.android;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.start.crypto.android.data.ColumnsPortfolio;

import java.util.Locale;

class PortfoliosListViewHolder extends RecyclerView.ViewHolder  {

    public TextView coinThresholdView;


    public PortfoliosListViewHolder(View itemView) {
        super(itemView);
        coinThresholdView = itemView.findViewById(R.id.coin_threshold);

    }

    public void bindData(Context context, Cursor data) {

        ColumnsPortfolio.ColumnsMap columnsMap = new ColumnsPortfolio.ColumnsMap(data);

        double profit = data.getDouble(columnsMap.mColumnBalance) - data.getDouble(columnsMap.mColumnOriginal);
        String id = data.getString(columnsMap.mColumnId);

        coinThresholdView.setText(String.format(Locale.getDefault(), "id: %s profit: %s", id, profit));
    }

}
