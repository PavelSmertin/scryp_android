package com.start.crypto.android;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.start.crypto.android.data.CryptoContract;

import butterknife.BindView;

public class NotificationsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.notifications_list)  RecyclerView mRecyclerView;


    private NotificationsListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_notifications);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new NotificationsListAdapter(this, null);
        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        getSupportLoaderManager().restartLoader(0, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id != 0) {
            throw new IllegalArgumentException("no id handled!");
        }
        return new CursorLoader(this, CryptoContract.CryptoNotifications.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() != 0) {
            throw new IllegalArgumentException("no id handled!");
        }

        data.moveToFirst();
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() != 0) {
            throw new IllegalArgumentException("no id handled!");
        }
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
        }
    }
}
