package com.start.crypto.android;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;

import butterknife.BindView;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends BaseActivity {

    private static final String STATE_DIALOG = "state_dialog";


    @BindView(R.id.controller_container)        ViewGroup mControllerContainer;

    private Router mRouter;

    private AlertDialog mAlertDialog;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRouter = Conductor.attachRouter(this, mControllerContainer, savedInstanceState);
        if (!mRouter.hasRootController()) {
            mRouter.setRoot(RouterTransaction.with(new PagerController()));
        }

        if (savedInstanceState != null) {
            boolean showDialog = savedInstanceState.getBoolean(STATE_DIALOG);
            if (showDialog) {
                //
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            outState.putBoolean(STATE_DIALOG, true);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mRouter.handleBack()) {
            super.onBackPressed();
        }
    }


}
