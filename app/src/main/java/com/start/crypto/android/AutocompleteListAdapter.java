package com.start.crypto.android;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.start.crypto.android.api.model.Coin;
import com.start.crypto.android.data.CryptoContract;

import io.reactivex.subjects.PublishSubject;


class AutocompleteListAdapter extends CursorRecyclerViewAdapter<AutocompleteListViewHolder> {

    PublishSubject<Coin> mSubject;

    public AutocompleteListAdapter(Context context, PublishSubject<Coin> subject) {
        super(context, null);
        mSubject = subject;
    }


    @Override
    public AutocompleteListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_autocomplete_coins, parent, false);
        return new AutocompleteListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AutocompleteListViewHolder viewHolder, Cursor cursor) {
        cursor.moveToPosition(cursor.getPosition());
        viewHolder.itemView.setOnClickListener(v -> {
            long coinId = getItemId(viewHolder.getAdapterPosition());
            int itemColumnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL);
            String coinSymbol = cursor.getString(itemColumnIndex);

            itemColumnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoCoins.COLUMN_NAME_NAME);
            String coinName = cursor.getString(itemColumnIndex);

            mSubject.onNext(new Coin(coinId, coinSymbol, coinName));
        });

        viewHolder.bindData(cursor);
    }

}
