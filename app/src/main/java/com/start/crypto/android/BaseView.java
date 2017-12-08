package com.start.crypto.android;

import android.support.annotation.StringRes;

public interface BaseView {
    void showError(String error);
    void showAlert(int message);
    void showSuccess(String text);
    void startProgress(@StringRes int stringId);
    void stopProgress();
    void startProgressDialog(@StringRes int stringId);
    void stopProgressDialog();

    void onForbidden();
    void onConnectionError();
    void onServerError();
    void onProtocolError();
}
