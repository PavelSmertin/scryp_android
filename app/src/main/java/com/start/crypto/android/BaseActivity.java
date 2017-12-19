package com.start.crypto.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import butterknife.ButterKnife;

public abstract class BaseActivity extends RxAppCompatActivity implements BaseView {

//    @Nullable @BindView(R.id.toolbar) public Toolbar mToolbar;
//    @Nullable @BindView(R.id.loading) ProgressBar mLoading;

    private LoaderFragment mLoaderFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout
        setupLayout();
        ButterKnife.bind(this);

//        // Toolbar
//        if(mToolbar != null) {
//            setSupportActionBar(mToolbar);
//        }
//        if(mLoading != null) {
//            setLoadingStyle();
//        }
    }

    // Цвета анимации загрузки
    private void setLoadingStyle() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            mLoading.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.teal300), android.graphics.PorterDuff.Mode.SRC_IN);
//        } else {
//            mLoading.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.teal300)));
//        }
    }

    abstract protected void setupLayout();

    @Override
    public void showError(String error) {
    }

    @Override
    public void showAlert(int message) {
    }

    @Override
    public void showSuccess(String text) {
        Toast.makeText(BaseActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    public void showNoInternet() {
//        Toast.makeText(BaseActivity.this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
    }

    @Override
    public void startProgress(@StringRes int stringId) {
//        if (mLoading != null && mLoading.getVisibility() != View.VISIBLE) {
//            mLoading.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    public void stopProgress() {
//        if(mLoading != null) {
//            mLoading.setVisibility(View.GONE);
//        }
    }


    @Override
    public void startProgressDialog(@StringRes int stringId) {
        mLoaderFragment = DialogFactory.showLoader(this);
        try {
            mLoaderFragment.show(getSupportFragmentManager(), getClass().getName());
        }catch (IllegalStateException ex){
            //ErrorReportHelper.report(ex);
        }
    }

    @Override
    public void stopProgressDialog() {
        if (mLoaderFragment != null) {
            try {
                mLoaderFragment.dismiss();
            } catch(NullPointerException e) {
                //ErrorReportHelper.report(e);
            } catch (IllegalStateException e){
                //ErrorReportHelper.report(e);
            }
        }
    }


    @Override
    public void onForbidden() {
    }

    @Override
    public void onConnectionError() {

    }

    @Override
    public void onServerError() {
    }

    @Override
    public void onProtocolError() {
    }

    @Nullable
    public Toolbar getToolbar() {
        return null; //mToolbar;
    }

    @Nullable
    public ProgressBar getLoading() {
        return null; //mLoading;
    }

}

