package com.start.crypto.android.sync;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

import com.start.crypto.android.account.SigninActivity;
import com.start.crypto.android.data.CryptoContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SyncPresenter {

    private ContentResolver mContentResolver;

    public SyncPresenter(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    // Вызывается при изменении локальных данных(обновление цен, добавление удаление монеты и т.д.)
    // Все изменения через syncAdapter отправляются на сервер.
    public void triggerRefresh(String login) {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                new Account(login, SigninActivity.ACCOUNT_TYPE),
                CryptoContract.AUTHORITY,
                b);
    }

    // Вызывается при авторизации. Сервер отдает данные пользователя с последней синхронизации. Данные полностью замещают локальные данные
    public void restorePortfolio(String response) {
        JSONArray jsonPortfolios;
        try {
            jsonPortfolios = (new JSONObject(response)).getJSONArray(SyncAdapter.COLLECTION_PORTFOLIOS);
            saveJsonToDatabase(CryptoContract.CryptoPortfolios.CONTENT_URI, jsonPortfolios, CryptoContract.CryptoPortfolios.DEFAULT_PROJECTION);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonPortfolioCoins = (new JSONObject(response)).getJSONArray(SyncAdapter.COLLECTION_PORTFOLIO_COINS);
            saveJsonToDatabase(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, jsonPortfolioCoins, CryptoContract.CryptoPortfolioCoins.DEFAULT_PROJECTION_SIMPLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonTransactions = (new JSONObject(response)).getJSONArray(SyncAdapter.COLLECTION_TRANSACTIONS);
            saveJsonToDatabase(CryptoContract.CryptoTransactions.CONTENT_URI, jsonTransactions, CryptoContract.CryptoTransactions.DEFAULT_PROJECTION);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonNotifications = (new JSONObject(response)).getJSONArray(SyncAdapter.COLLECTION_NOTIFICATIONS);
            saveJsonToDatabase(CryptoContract.CryptoNotifications.CONTENT_URI, jsonNotifications, CryptoContract.CryptoNotifications.DEFAULT_PROJECTION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveJsonToDatabase(Uri uri, JSONArray jsonPortfolios, String[] projection) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (int i = 0; i < jsonPortfolios.length(); i++) {
            JSONObject row = null;
                try {
                    row = jsonPortfolios.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(row == null) {
                    continue;
                }
                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
                for( String field : projection) {
                    builder.withValue(field, row.optString(field));
                }
                builder.withYieldAllowed(true);
                operations.add(builder.withYieldAllowed(true).build());


            try {
                mContentResolver.applyBatch(CryptoContract.AUTHORITY, operations);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }

            operations.clear();
        }
    }
}
