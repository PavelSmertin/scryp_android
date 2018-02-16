package com.start.crypto.android.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.start.crypto.android.BaseActivity;
import com.start.crypto.android.R;

import butterknife.BindView;

public class UserActivity extends BaseActivity {

    @BindView(R.id.controller_container)        ViewGroup mControllerContainer;

    private Router mRouter;


    public static void start(Activity activity, int request) {
        Intent starter = new Intent(activity, UserActivity.class);
        activity.startActivityForResult(starter, request);
    }

    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRouter = Conductor.attachRouter(this, mControllerContainer, savedInstanceState);
        if (!mRouter.hasRootController()) {
            mRouter.setRoot(RouterTransaction.with(new UserController()));
        }

    }

    @Override
    public void onBackPressed() {
        if (!mRouter.handleBack()) {
            super.onBackPressed();
        }
    }

}
