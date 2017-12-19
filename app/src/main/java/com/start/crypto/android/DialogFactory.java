package com.start.crypto.android;

import android.content.Context;

public class DialogFactory {

    public static LoaderFragment showLoader(Context context) {
        LoaderFragment loaderFragment = new LoaderFragment();
        loaderFragment.setCancelable(false);
        return loaderFragment;
    }



}
