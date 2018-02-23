package com.start.crypto.android.publicPortfolio;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bluelinelabs.conductor.rxlifecycle2.ControllerEvent;
import com.start.crypto.android.BaseController;
import com.start.crypto.android.ControllerPageTitle;
import com.start.crypto.android.R;
import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class PortfoliosController extends BaseController implements SwipeRefreshLayout.OnRefreshListener, ControllerPageTitle {

    @BindView(R.id.notifications_list)          RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh)               SwipeRefreshLayout mSwipeRefresh;


    private PortfoliosAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public PortfoliosController() {
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.activity_portfolios, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {

        super.onViewBound(view);

        mAdapter = new PortfoliosAdapter(getActivity());
        mLayoutManager = new LinearLayoutManager(getActivity());

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
        MainServiceGenerator.createService(MainApiService.class, getActivity()).portfolios()
            .compose(bindUntilEvent(ControllerEvent.DETACH))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    mAdapter.updatePortfolios(response);
                    mSwipeRefresh.setRefreshing(false);
                },
                error -> {
                    mSwipeRefresh.setRefreshing(false);
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            );
    }

    @Override
    public void onRefresh() {
        refreshPortfolios();
    }

    @Override
    public String getPageTitle(Context context) {
        return context.getString(R.string.title_activity_portfolios);
    }
}
