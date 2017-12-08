package com.start.crypto.android;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class NotificationsListAdapter extends CursorRecyclerViewAdapter<NotificationsListViewHolder>  {


    public NotificationsListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public NotificationsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_notifications, parent, false);
        return new NotificationsListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NotificationsListViewHolder viewHolder, Cursor cursor) {
        cursor.moveToPosition(cursor.getPosition());
        viewHolder.bindData(mContext, cursor);
    }

}
