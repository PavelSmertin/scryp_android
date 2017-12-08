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

public class PortfoliosActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.notifications_list)  RecyclerView mRecyclerView;

    private PortfoliosListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_portfolios);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new PortfoliosListAdapter(this, null);
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
        return new CursorLoader(this, CryptoContract.CryptoPortfolios.CONTENT_URI, null, null, null, null);
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
