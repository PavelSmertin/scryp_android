package com.start.crypto.android.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;

import com.start.crypto.android.account.SigninActivity;
import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.start.crypto.android.api.model.SyncData;
import com.start.crypto.android.data.CryptoContract;
import com.start.crypto.android.utils.PreferencesHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Response;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String COLLECTION_PORTFOLIOS        = "portfolios";
    public static final String COLLECTION_PORTFOLIO_COINS   = "portfolio_coins";
    public static final String COLLECTION_TRANSACTIONS      = "transactions";
    public static final String COLLECTION_NOTIFICATIONS     = "notifications";

    private ArrayList<ContentProviderOperation> mOperations = new ArrayList<>();

    // Global variables
    // Define a variable to contain a content resolver instance
    Context mContext;
    ContentResolver mContentResolver;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        JSONObject json = packCollectionsToJson();

        Response newResponse = null;
        try {
            newResponse = MainServiceGenerator.createService(MainApiService.class, mContext).syncUpload(new SyncData(json.toString())).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(newResponse != null && newResponse.code() == 401 ) {
            logout(account);
        }

    }


    private void logout(Account account) {
        PreferencesHelper.getInstance().logout();
        AccountManager accountManager = AccountManager.get(mContext);
        String authToken = accountManager.peekAuthToken(account, SigninActivity.AUTHTOKEN_TYPE_FULL_ACCESS);
        accountManager.invalidateAuthToken(account.type, authToken);
    }


    private JSONObject packCollectionsToJson() {
        JSONObject json = new JSONObject();

        Cursor cursor = mContentResolver.query(CryptoContract.CryptoPortfolios.CONTENT_URI, null, null, null, null);
        appendJsonFromCursor(json, COLLECTION_PORTFOLIOS, cursor);

        cursor = mContentResolver.query(CryptoContract.CryptoPortfolioCoins.CONTENT_URI, null, null, null, null);
        appendJsonFromCursor(json, COLLECTION_PORTFOLIO_COINS, cursor);

        cursor = mContentResolver.query(CryptoContract.CryptoTransactions.CONTENT_URI, null, null, null, null);
        appendJsonFromCursor(json, COLLECTION_TRANSACTIONS, cursor);

        cursor = mContentResolver.query(CryptoContract.CryptoNotifications.CONTENT_URI, null, null, null, null);
        appendJsonFromCursor(json, COLLECTION_NOTIFICATIONS, cursor);
        return json;
    }

    private void appendJsonFromCursor(JSONObject json, String collection, Cursor cursor) {

        if(cursor == null) {
            return;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return;
        }

        JSONArray resultSet     = new JSONArray();

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for( int i=0 ;  i< totalColumn ; i++ ){
                if( cursor.getColumnName(i) != null ){
                    try {
                        if( cursor.getString(i) != null ) {
                            rowObject.put(cursor.getColumnName(i) ,  cursor.getString(i) );
                        } else {
                            rowObject.put( cursor.getColumnName(i) ,  "" );
                        }
                    }
                    catch( Exception e ) {
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();

        try {
            json.put(collection, resultSet);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}