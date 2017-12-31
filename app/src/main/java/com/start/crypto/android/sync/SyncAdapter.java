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

        Response<Object> newResponse = null;
        try {
            newResponse = MainServiceGenerator.createService(MainApiService.class, mContext).syncUpload(json.toString()).execute();
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
                    try
                    {
                        if( cursor.getString(i) != null ){
                            rowObject.put(cursor.getColumnName(i) ,  cursor.getString(i) );
                        }
                        else{
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


//    private void savePortfolios(JSONArray jsonPortfolios) {
//    }
//
//    private void saveNotifications(JSONArray jsonNotifications) {
//    }
//
//    private void saveTransactions(JSONArray jsonTransactions) {
//    }
//
//
//    private void savePortfolioCoins(JSONArray jsonPortfolioCoins) {
//    }














//    private JSONObject portfoliosTojson(Cursor cursor) {
//        JSONObject jsonPortfolios = null;
//        if(cursor != null) {
//            if(cursor.getCount() > 0) {
//                jsonPortfolios = new JSONObject();
//                while(cursor.moveToNext()) {
//                    ColumnsPortfolio.ColumnsMap columnsMap = new ColumnsPortfolio.ColumnsMap(cursor);
//
//                    JSONObject jsonPortfolio = new JSONObject();
//                    try {
//
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios._ID, cursor.getLong(columnsMap.mColumnId));
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_USER_ID, cursor.getLong(columnsMap.mColumnUserId));
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_USERNAME, cursor.getString(columnsMap.mColumnUsername));
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_BASE_COIN_ID, cursor.getLong(columnsMap.mColumnBaseCoinId));
//
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_BALANCE, cursor.getDouble(columnsMap.mColumnBalance));
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_ORIGINAL, cursor.getDouble(columnsMap.mColumnOriginal));
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_PRICE_NOW, cursor.getDouble(columnsMap.mColumnPriceNow));
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_PRICE_ORIGINAL, cursor.getDouble(columnsMap.mColumnPriceOriginal));
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_PRICE_24H, cursor.getDouble(columnsMap.mColumnPrice24h));
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_COINS_COUNT, cursor.getInt(columnsMap.mColumnCoinsCount));
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_PROFIT24H, cursor.getDouble(columnsMap.mColumnProfit24h));
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_PROFIT7D, cursor.getDouble(columnsMap.mColumnProfit7d));
//
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_CREATED_AT, cursor.getLong(columnsMap.mColumnCreatedAt));
//                        jsonPortfolio.put(CryptoContract.CryptoPortfolios.COLUMN_NAME_UPDATED_AT, cursor.getLong(columnsMap.mColumnUpdatedAt));
//
//                        jsonPortfolios.put(Long.toString(cursor.getLong(columnsMap.mColumnId)), jsonPortfolio);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//            cursor.close();
//        }
//
//        return jsonPortfolios;
//    }
//
//    private JSONObject portfolioCoinsTojson(Cursor cursor) {
//        JSONObject jsonPortfolioCoins = null;
//        if(cursor != null) {
//            if(cursor.getCount() > 0) {
//                jsonPortfolioCoins = new JSONObject();
//                while(cursor.moveToNext()) {
//                    ColumnsPortfolioCoin.ColumnsMap columnsMap = new ColumnsPortfolioCoin.ColumnsMap(cursor);
//
//                    JSONObject jsonPortfolioCoin = new JSONObject();
//                    try {
//
//                        jsonPortfolioCoin.put(CryptoContract.CryptoPortfolioCoins._ID, cursor.getLong(columnsMap.mColumnId));
//                        jsonPortfolioCoin.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID, cursor.getLong(columnsMap.mPortfolioId));
//                        jsonPortfolioCoin.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID, cursor.getString(columnsMap.mCoinId));
//                        jsonPortfolioCoin.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID, cursor.getLong(columnsMap.mExchangeId));
//
//                        jsonPortfolioCoin.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL, cursor.getDouble(columnsMap.mOriginal));
//                        jsonPortfolioCoin.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW, cursor.getDouble(columnsMap.mColumnPriceNow));
//                        jsonPortfolioCoin.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, cursor.getDouble(columnsMap.mColumnPriceOriginal));
//                        jsonPortfolioCoin.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_24H, cursor.getDouble(columnsMap.mColumnPrice24h));
//
//                        jsonPortfolioCoin.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_CREATED_AT, cursor.getLong(columnsMap.mColumnCreatedAt));
//                        jsonPortfolioCoin.put(CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_UPDATED_AT, cursor.getLong(columnsMap.mColumnUpdatedAt));
//
//                        jsonPortfolioCoins.put(Long.toString(cursor.getLong(columnsMap.mColumnId)), jsonPortfolioCoin);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//            cursor.close();
//        }
//
//        return jsonPortfolioCoins;
//    }
//
//    private JSONObject transactionsTojson(Cursor cursor) {
//        JSONObject jsonTransactions = null;
//        if(cursor != null) {
//            if(cursor.getCount() > 0) {
//                jsonTransactions = new JSONObject();
//                while(cursor.moveToNext()) {
//                    ColumnsTransaction.ColumnsMap columnsMap = new ColumnsTransaction.ColumnsMap(cursor);
//
//                    JSONObject jsonTransaction = new JSONObject();
//                    try {
//
//                        jsonTransaction.put(CryptoContract.CryptoTransactions._ID, cursor.getLong(columnsMap.mColumnId));
//                        jsonTransaction.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_ID, cursor.getLong(columnsMap.port));
//                        jsonTransaction.put(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_ID, cursor.getString(columnsMap.mCoinId));
//                        jsonTransaction.put(CryptoContract.CryptoTransactions.COLUMN_NAME_EXCHANGE_ID, cursor.getLong(columnsMap.mExchangeId));
//
//                        jsonTransaction.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_COIN_ID, cursor.getDouble(columnsMap.mOriginal));
//                        jsonTransaction.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PORTFOLIO_CURRENTEY_ID, cursor.getDouble(columnsMap.mColumnPriceNow));
//                        jsonTransaction.put(CryptoContract.CryptoTransactions.COLUMN_NAME_COIN_CORRESPOND_ID, cursor.getDouble(columnsMap.mColumnPriceOriginal));
//                        jsonTransaction.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PROTFOLIO_BALANCE, cursor.getDouble(columnsMap.mColumnPrice24h));
//
//                        jsonTransaction.put(CryptoContract.CryptoTransactions.COLUMN_NAME_AMOUNT, cursor.getDouble(columnsMap.mColumnPrice24h));
//                        jsonTransaction.put(CryptoContract.CryptoTransactions.COLUMN_NAME_PRICE, cursor.getDouble(columnsMap.mColumnPrice24h));
//                        jsonTransaction.put(CryptoContract.CryptoTransactions.COLUMN_NAME_DATETIME, cursor.getDouble(columnsMap.mColumnPrice24h));
//                        jsonTransaction.put(CryptoContract.CryptoTransactions.COLUMN_NAME_DESCRIPTION, cursor.getDouble(columnsMap.mColumnPrice24h));
//
//                        jsonTransaction.put(CryptoContract.CryptoTransactions.COLUMN_NAME_CREATED_AT, cursor.getLong(columnsMap.mColumnCreatedAt));
//                        jsonTransaction.put(CryptoContract.CryptoTransactions.COLUMN_NAME_UPDATED_AT, cursor.getLong(columnsMap.mColumnUpdatedAt));
//
//                        jsonTransactions.put(Long.toString(cursor.getLong(columnsMap.mColumnId)), jsonTransaction);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//            cursor.close();
//        }
//
//        return jsonTransactions;
//    }




}