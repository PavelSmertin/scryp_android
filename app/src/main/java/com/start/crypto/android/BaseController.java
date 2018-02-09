package com.start.crypto.android;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bluelinelabs.conductor.Controller;

public abstract class BaseController extends ButterKnifeController implements BaseView {

    protected BaseController() {}

    protected BaseController(Bundle args) {
        super(args);
    }

    // Note: This is just a quick demo of how an ActionBar *can* be accessed, not necessarily how it *should*
    // be accessed. In a production app, this would use Dagger instead.
    protected ActionBar getActionBar() {
        return getActivity() != null ? ((AppCompatActivity)getActivity()).getSupportActionBar() : null;
    }

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {

        setTitle();

        ViewGroup viewGroup = (ViewGroup) super.onCreateView(inflater, container);

        return viewGroup;
    }


    protected void setTitle() {
        Controller parentController = getParentController();
        while (parentController != null) {
            if (parentController instanceof BaseController && ((BaseController)parentController).getTitle() != null) {
                return;
            }
            parentController = parentController.getParentController();
        }

        String title = getTitle();
        ActionBar actionBar = getActionBar();
        if (title != null && actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    protected String getTitle() {
        return null;
    }

    @Override
    public void showError(String error) {
        if(getActivity() != null && getActivity() instanceof BaseActivity) {
            ((BaseActivity)getActivity()).showError(error);
        }
    }

    @Override
    public void showAlert(int message) {
        if(getActivity() != null) {
            Toast.makeText(getActivity(), getResources().getString(message), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showSuccess(String text) {
        if(getActivity() != null) {
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
        }
    }

    public void startProgress(@StringRes int stringId) {
    }


    @Override
    public void startProgressDialog(@StringRes int stringId) {
        if(getActivity() != null && getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).startProgressDialog(stringId);
        }
    }

    @Override
    public void startProgressDialog() {
        if(getActivity() != null && getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).startProgressDialog();
        }
    }

    @Override
    public void stopProgress() {
    }

    @Override
    public void stopProgressDialog() {
        if(getActivity() != null && getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).stopProgressDialog();
        }
    }

    @Override
    public void onForbidden() {
        if(getActivity() != null && getActivity() instanceof BaseView) {
            ((BaseView)getActivity()).onForbidden();
        }
    }

    @Override
    public void onConnectionError() {
        if(getActivity() != null && getActivity() instanceof BaseView) {
            ((BaseView)getActivity()).onConnectionError();
        }
    }

    @Override
    public void onServerError() {
        if(getActivity() != null && getActivity() instanceof BaseView) {
            ((BaseView)getActivity()).onServerError();
        }
    }

    @Override
    public void onProtocolError() {
        if(getActivity() != null && getActivity() instanceof BaseView) {
            ((BaseView)getActivity()).onProtocolError();
        }
    }

}