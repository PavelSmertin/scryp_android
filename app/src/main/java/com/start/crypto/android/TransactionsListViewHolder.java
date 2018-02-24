package com.start.crypto.android;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.start.crypto.android.data.ColumnsTransaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

class TransactionsListViewHolder extends RecyclerView.ViewHolder  {

    @BindView(R.id.transaction_date)    TextView mDateView;
    @BindView(R.id.description)         TextView mDescriptionView;

    public TransactionsListViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindData(Cursor data) {
        ColumnsTransaction.ColumnsMap columnsMap = new ColumnsTransaction.ColumnsMap(data);
        String description = data.getString(columnsMap.mColumnDescription);
        mDescriptionView.setText(description);

        long date = data.getLong(columnsMap.mColumnDatetime);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date * 1000);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm", Locale.getDefault());
        mDateView.setText(format.format(cal.getTime()));

    }

}
