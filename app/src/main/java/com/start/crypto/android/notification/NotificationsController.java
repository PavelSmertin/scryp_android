package com.start.crypto.android.notification;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.start.crypto.android.BaseController;
import com.start.crypto.android.ControllerPageTitle;
import com.start.crypto.android.R;
import com.start.crypto.android.data.CryptoContract;

import butterknife.BindView;

public class NotificationsController extends BaseController implements LoaderManager.LoaderCallbacks<Cursor>, ControllerPageTitle {

    private static final int LOADER_ID = 2;

    @BindView(R.id.notifications_list)  RecyclerView mRecyclerView;


    private NotificationsListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;


    public NotificationsController() {
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.notification_activity, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        mAdapter = new NotificationsListAdapter(getActivity(), null);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        ((AppCompatActivity)getActivity()).getSupportLoaderManager().initLoader(LOADER_ID, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id != LOADER_ID) {
            throw new IllegalArgumentException("no id handled!");
        }
        return new CursorLoader(getActivity(), CryptoContract.CryptoNotifications.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() != LOADER_ID) {
            throw new IllegalArgumentException("no id handled!");
        }

        data.moveToFirst();
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() != LOADER_ID) {
            throw new IllegalArgumentException("no id handled!");
        }
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
        }
    }

    @Override
    public String getPageTitle(Context context) {
        return context.getString(R.string.title_notifications);
    }
}
