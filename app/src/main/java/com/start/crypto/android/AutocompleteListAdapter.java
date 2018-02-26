package com.start.crypto.android;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.start.crypto.android.api.model.AutocompleteItem;
import com.start.crypto.android.data.ColumnsCoin;
import com.start.crypto.android.data.ColumnsExchange;

import io.reactivex.subjects.PublishSubject;


class AutocompleteListAdapter extends CursorRecyclerViewAdapter<AutocompleteListViewHolder> {

    private static final int TYPE_FIRST = 0;
    private static final int TYPE_ITEM = 1;

    private final int mListType;

    PublishSubject<AutocompleteItem> mSubject;

    public AutocompleteListAdapter(Context context, PublishSubject<AutocompleteItem> subject, int listType) {
        super(context, null);
        mSubject = subject;
        mListType = listType;
    }


    @Override
    public AutocompleteListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_FIRST) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.autocomplete_list_item_first, parent, false);
            return new AutocompleteListViewHolder(v);
        }

        if(viewType == TYPE_ITEM)  {
            View v = LayoutInflater.from(mContext).inflate(R.layout.autocomplete_list_item, parent, false);
            return new AutocompleteListViewHolder(v);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");

    }

    @Override
    public void onBindViewHolder(AutocompleteListViewHolder viewHolder, Cursor cursor) {

        if(mListType == AutocompleteActivity.LOADER_COINS) {
            AutocompleteItem autocompleteItem = createCoinItem(cursor);
            bind(viewHolder, autocompleteItem);
        }

        if(mListType == AutocompleteActivity.LOADER_EXCHANGES) {
            AutocompleteItem autocompleteItem = createExchangeItem(cursor);
            bind(viewHolder, autocompleteItem);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(isPositionHeader(position)) {
            return TYPE_FIRST;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


    private void bind(AutocompleteListViewHolder viewHolder, AutocompleteItem autocompleteItem) {
        viewHolder.itemView.setOnClickListener(v -> {
            mSubject.onNext(autocompleteItem);
        });
        viewHolder.bindData(autocompleteItem.getName());
    }

    private AutocompleteItem createCoinItem(Cursor cursor) {
        ColumnsCoin.ColumnsMap columnsMap = new ColumnsCoin.ColumnsMap(cursor);
        long coinId = cursor.getLong(columnsMap.mColumnId);
        String coinSymbol = cursor.getString(columnsMap.mColumnSymbol);
        String coinName = cursor.getString(columnsMap.mColumnName);
        return new AutocompleteItem(coinId, coinSymbol, coinName);
    }

    private AutocompleteItem createExchangeItem( Cursor cursor) {
        ColumnsExchange.ColumnsMap columnsMap = new ColumnsExchange.ColumnsMap(cursor);
        long exchangeId = cursor.getLong(columnsMap.mColumnId);
        String exchangeName = cursor.getString(columnsMap.mColumnName);
        return new AutocompleteItem(exchangeId, exchangeName);
    }


    public AutocompleteItem getFirstItem() {
        Cursor cursor = getCursor();

        cursor.moveToFirst();
        if(mListType == AutocompleteActivity.LOADER_COINS) {
            return createCoinItem(cursor);
        }

        if(mListType == AutocompleteActivity.LOADER_EXCHANGES) {
            return createExchangeItem(cursor);
        }

        throw new IllegalArgumentException("no id handled!");

    }

}
