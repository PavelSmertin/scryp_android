package com.start.crypto.android;

import android.content.ContentProviderOperation;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.start.crypto.android.api.model.Portfolio;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class PortfoliosActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener  {

    @BindView(R.id.notifications_list)          RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh)               SwipeRefreshLayout mSwipeRefresh;


    private PortfoliosAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private ArrayList<ContentProviderOperation> mOperations = new ArrayList<>();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_portfolios);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new PortfoliosAdapter();
        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        //getSupportLoaderManager().restartLoader(0, null, this);

        mSwipeRefresh.setOnRefreshListener(this);

        refreshPortfolios();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void refreshPortfolios() {
        MainServiceGenerator.createService(MainApiService.class, this).portfolios()
            .compose(bindUntilEvent(ActivityEvent.PAUSE))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    mAdapter.updatePortfolios(response);
                    mSwipeRefresh.setRefreshing(false);
                },
                error -> {
                    mSwipeRefresh.setRefreshing(false);
                    Toast.makeText(getBaseContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            );
    }

    private void updatePortfolios(List<Portfolio> portfolios) {
//        for (Portfolio portfolio: portfolios) {
//
//            Cursor cursor = getContentResolver().query(
//                    CryptoContract.CryptoPortfolios.CONTENT_URI,
//                    CryptoContract.CryptoPortfolios.DEFAULT_PROJECTION,
//                    CryptoContract.CryptoPortfolios.COLUMN_NAME_USER_ID + " = " + portfolio.getUserId(),
//                    null,
//                    null
//            );
//
//            if(cursor != null && cursor.getCount() > 0) {
//                mOperations.add(ContentProviderOperation.newUpdate(CryptoContract.CryptoPortfolios.CONTENT_URI)
//                        .withValue(CryptoContract.CryptoPortfolios.COLUMN_NAME_COINS_COUNT, portfolio.getCoinsCount())
//                        .withValue(CryptoContract.CryptoPortfolios.COLUMN_NAME_USER_ID, portfolio.getUserId())
//                        .withValue(CryptoContract.CryptoPortfolios.COLUMN_NAME_USERNAME, portfolio.getUserName())
//                        .withValue(CryptoContract.CryptoPortfolios.COLUMN_NAME_PROFIT24H, portfolio.getProfit24h())
//                        .withValue(CryptoContract.CryptoPortfolios.COLUMN_NAME_PROFIT7D, portfolio.getProfit7d())
//                        .withYieldAllowed(true)
//                        .build());
//                cursor.close();
//            } else {
//                mOperations.add(ContentProviderOperation.newInsert(CryptoContract.CryptoPortfolios.CONTENT_URI)
//                        .withValue(CryptoContract.CryptoPortfolios.COLUMN_NAME_COINS_COUNT, portfolio.getCoinsCount())
//                        .withValue(CryptoContract.CryptoPortfolios.COLUMN_NAME_USER_ID, portfolio.getUserId())
//                        .withValue(CryptoContract.CryptoPortfolios.COLUMN_NAME_USERNAME, portfolio.getUserName())
//                        .withValue(CryptoContract.CryptoPortfolios.COLUMN_NAME_PROFIT24H, portfolio.getProfit24h())
//                        .withValue(CryptoContract.CryptoPortfolios.COLUMN_NAME_PROFIT7D, portfolio.getProfit7d())
//                        .withYieldAllowed(true)
//                        .build());
//            }
//
//        }
//        try {
//            getContentResolver().applyBatch(CryptoContract.AUTHORITY, mOperations);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        } catch (OperationApplicationException e) {
//            e.printStackTrace();
//        }
//
//
//        mOperations.clear();

    }

//    @Override
//    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        if(id != 0) {
//            throw new IllegalArgumentException("no id handled!");
//        }
//        return new CursorLoader(this, CryptoContract.CryptoPortfolios.CONTENT_URI, null, null, null, null);
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        if(loader.getId() != 0) {
//            throw new IllegalArgumentException("no id handled!");
//        }
//
//        data.moveToFirst();
//        mAdapter.changeCursor(data);
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//        if(loader.getId() != 0) {
//            throw new IllegalArgumentException("no id handled!");
//        }
//        if (mAdapter != null) {
//            mAdapter.changeCursor(null);
//        }
//    }

    @Override
    public void onRefresh() {
        refreshPortfolios();
    }
}
