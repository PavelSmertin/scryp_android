package com.start.crypto.android;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.trello.rxlifecycle2.android.ActivityEvent;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class PortfoliosActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener  {

    @BindView(R.id.notifications_list)          RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh)               SwipeRefreshLayout mSwipeRefresh;


    private PortfoliosAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

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

    @Override
    public void onRefresh() {
        refreshPortfolios();
    }
}
