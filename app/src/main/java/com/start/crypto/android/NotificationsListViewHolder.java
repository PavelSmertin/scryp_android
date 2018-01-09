package com.start.crypto.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.start.crypto.android.data.ColumnsNotifications;
import com.start.crypto.android.data.CryptoContract;
import com.start.crypto.android.data.DBHelper;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

class NotificationsListViewHolder extends RecyclerView.ViewHolder  {

    public TextView exchangeView;
    public TextView coinThresholdView;
    public Switch activeSwitch;

    private long mCoinId;
    private long mExchangeId;
    private boolean mActive;


    public NotificationsListViewHolder(View itemView) {
        super(itemView);
        exchangeView = itemView.findViewById(R.id.exchange);
        coinThresholdView = itemView.findViewById(R.id.coin_threshold);
        activeSwitch = itemView.findViewById(R.id.active);

    }

    public void bindData(Context context, Cursor data) {

        ColumnsNotifications.ColumnsMap columnsMap = new ColumnsNotifications.ColumnsMap(data);

        String coin = data.getString(columnsMap.mColumnCoinSymbol);
        String pair = data.getString(columnsMap.mColumnCorrespondSymbol);
        String exchange = data.getString(columnsMap.mColumnExchangeName);
        double priceThreshold = data.getDouble(columnsMap.mColumnPriceThreshold);
        String type = data.getString(columnsMap.mColumnType);
        String compare = data.getString(columnsMap.mColumnCompare);

        mCoinId = data.getLong(columnsMap.mColumnCoinId);
        mExchangeId = data.getLong(columnsMap.mColumnExchangeId);
        mActive = data.getInt(columnsMap.mColumnActive) > 0;

        exchangeView.setText(String.format(Locale.getDefault(), "%s", exchange));
        coinThresholdView.setText(String.format(Locale.getDefault(), "1 %s %s %s %s",
                coin,
                NotificationComparePrice.valueOf(compare) == NotificationComparePrice.LESS_THAN ? "<" : ">",
                new BigDecimal(priceThreshold).setScale(0, BigDecimal.ROUND_FLOOR).toString(),
                pair
        ));

        activeSwitch.setChecked(mActive);

        RxCompoundButton.checkedChanges(activeSwitch)
                .distinctUntilChanged()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe(value -> changeActive(context, value));

    }

    private void changeActive(Context context, boolean isChecked){

        if(mActive == isChecked) {
            return;
        }

        SQLiteDatabase db = new DBHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CryptoContract.CryptoNotifications.COLUMN_NAME_ACTIVE, isChecked ? 1 : 0);
        int count = db.update(
                CryptoContract.CryptoNotifications.TABLE_NAME,
                values,
                CryptoContract.CryptoNotifications.COLUMN_NAME_COIN_ID +
                        " = " + mCoinId +
                        " AND " + CryptoContract.CryptoNotifications.COLUMN_NAME_EXCHANGE_ID +
                        " = "+  mExchangeId,
                null
        );

        if(count > 0) {
            mActive = isChecked;
        }

    }
}
