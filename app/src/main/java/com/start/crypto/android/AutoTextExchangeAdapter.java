package com.start.crypto.android;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.start.crypto.android.data.CryptoContract;

class AutoTextExchangeAdapter extends CursorAdapter
    implements AdapterView.OnItemClickListener {

        private Context mContext;

        public AutoTextExchangeAdapter(Context context) {
            super(context, null);
            mContext = context;

        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (getFilterQueryProvider() != null) {
                return getFilterQueryProvider().runQuery(constraint);
            }

            Cursor cursor = mContext.getContentResolver().query(
                    CryptoContract.CryptoExchanges.CONTENT_URI,
                    CryptoContract.CryptoExchanges.DEFAULT_PROJECTION,
                    CryptoContract.CryptoExchanges.COLUMN_NAME_NAME + " LIKE '%" + (constraint != null ? constraint.toString() : "@@@@") + "%'",
                    null,
                    CryptoContract.CryptoExchanges.COLUMN_NAME_NAME + " ASC");

            return cursor;
        }

        @Override
        public String convertToString(Cursor cursor) {
            final int columnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoExchanges.COLUMN_NAME_NAME);
            return cursor.getString(columnIndex);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final int itemColumnIndex = cursor.getColumnIndexOrThrow(CryptoContract.CryptoExchanges.COLUMN_NAME_NAME);
            TextView text1 = view.findViewById(R.id.coin_item);
            text1.setText(cursor.getString(itemColumnIndex));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(R.layout.autocomplete_list_item, parent, false);
        }


        @Override
        public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
            // Get the cursor, positioned to the corresponding row in the result set
            Cursor cursor = (Cursor) listView.getItemAtPosition(position);

        }

    }
