package com.start.crypto.android;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.start.crypto.android.api.model.AutocompleteItem;
import com.start.crypto.android.data.ColumnsCoin;
import com.start.crypto.android.data.ColumnsExchange;
import com.start.crypto.android.data.CryptoContract;

import io.reactivex.subjects.PublishSubject;


class AutocompleteListAdapter extends CursorRecyclerViewAdapter<AutocompleteListViewHolder> {

    private final int mListType;

    PublishSubject<AutocompleteItem> mSubject;

    public AutocompleteListAdapter(Context context, PublishSubject<AutocompleteItem> subject, int listType) {
        super(context, null);
        mSubject = subject;
        mListType = listType;
    }


    @Override
    public AutocompleteListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_autocomplete_coins, parent, false);
        return new AutocompleteListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AutocompleteListViewHolder viewHolder, Cursor cursor) {

        if(mListType == CoinAutocompleteActivity.LOADER_COINS) {
            viewHolder.bindData(bindCoin(viewHolder, cursor));
            return;
        }

        if(mListType == CoinAutocompleteActivity.LOADER_EXCHANGES) {
            viewHolder.bindData(bindExchange(viewHolder, cursor));
            return;
        }
    }

    private String bindCoin(AutocompleteListViewHolder viewHolder, Cursor cursor) {
        cursor.moveToPosition(cursor.getPosition());
        viewHolder.itemView.setOnClickListener(v -> {
            long coinId = getItemId(viewHolder.getAdapterPosition());
            int itemColumnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL);
            String coinSymbol = cursor.getString(itemColumnIndex);

            itemColumnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoCoins.COLUMN_NAME_NAME);
            String coinName = cursor.getString(itemColumnIndex);

            mSubject.onNext(new AutocompleteItem(coinId, coinSymbol, coinName));
        });

        ColumnsCoin.ColumnsMap columnsMap = new ColumnsCoin.ColumnsMap(cursor);
        return cursor.getString(columnsMap.mColumnName);
    }


    private String bindExchange(AutocompleteListViewHolder viewHolder, Cursor cursor) {

        cursor.moveToPosition(cursor.getPosition());

        viewHolder.itemView.setOnClickListener(v -> {
            int itemColumnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoExchanges.COLUMN_NAME_NAME);
            String exchangeName = cursor.getString(itemColumnIndex);
            mSubject.onNext(new AutocompleteItem(getItemId(viewHolder.getAdapterPosition()), exchangeName));
        });

        ColumnsExchange.ColumnsMap columnsMap = new ColumnsExchange.ColumnsMap(cursor);
        return cursor.getString(columnsMap.mColumnName);
    }





}
