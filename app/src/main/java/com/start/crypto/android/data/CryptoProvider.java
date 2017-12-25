package com.start.crypto.android.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.HashMap;


public class CryptoProvider extends ContentProvider {

    private static HashMap<String, String> PORTFOLIOS_PROJECTION_MAP = new HashMap<>();
    private static HashMap<String, String> COINS_PROJECTION_MAP = new HashMap<>();
    private static HashMap<String, String> TRANSACTIONS_PROJECTION_MAP = new HashMap<>();
    private static HashMap<String, String> EXCHANGES_PROJECTION_MAP = new HashMap<>();
    private static HashMap<String, String> PORTFOLIO_COINS_PROJECTION_MAP = new HashMap<>();
    private static HashMap<String, String> NOTIFICATIONS_PROJECTION_MAP = new HashMap<>();

    private static final int PORTFOLIO_INDEX            = 1;
    private static final int PORTFOLIO_ID               = 2;

    private static final int COIN_INDEX                 = 3;
    private static final int COIN_ID                    = 4;

    private static final int TRANSACTION_INDEX          = 5;
    private static final int TRANSACTION_ID             = 6;

    private static final int EXCHANGE_INDEX             = 7;
    private static final int EXCHANGE_ID                = 8;

    private static final int PORTFOLIO_COINS_INDEX      = 9;
    private static final int PORTFOLIO_COINS_ID         = 10;

    private static final int NOTIFICATIONS_INDEX        = 11;
    private static final int NOTIFICATIONS_ID           = 12;


    private static final UriMatcher sUriMatcher;
    private DBHelper dbHelper;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(CryptoContract.AUTHORITY, CryptoContract.CryptoPortfolios.TABLE_NAME, PORTFOLIO_INDEX);
        sUriMatcher.addURI(CryptoContract.AUTHORITY, CryptoContract.CryptoPortfolios.TABLE_NAME + "/#", PORTFOLIO_ID);

        sUriMatcher.addURI(CryptoContract.AUTHORITY, CryptoContract.CryptoCoins.TABLE_NAME, COIN_INDEX);
        sUriMatcher.addURI(CryptoContract.AUTHORITY, CryptoContract.CryptoCoins.TABLE_NAME + "/#", COIN_ID);

        sUriMatcher.addURI(CryptoContract.AUTHORITY, CryptoContract.CryptoTransactions.TABLE_NAME, TRANSACTION_INDEX);
        sUriMatcher.addURI(CryptoContract.AUTHORITY, CryptoContract.CryptoTransactions.TABLE_NAME + "/#", TRANSACTION_ID);

        sUriMatcher.addURI(CryptoContract.AUTHORITY, CryptoContract.CryptoExchanges.TABLE_NAME, EXCHANGE_INDEX);
        sUriMatcher.addURI(CryptoContract.AUTHORITY, CryptoContract.CryptoExchanges.TABLE_NAME + "/#", EXCHANGE_ID);

        sUriMatcher.addURI(CryptoContract.AUTHORITY, CryptoContract.CryptoPortfolioCoins.TABLE_NAME, PORTFOLIO_COINS_INDEX);
        sUriMatcher.addURI(CryptoContract.AUTHORITY, CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "/#", PORTFOLIO_COINS_ID);

        sUriMatcher.addURI(CryptoContract.AUTHORITY, CryptoContract.CryptoNotifications.TABLE_NAME, NOTIFICATIONS_INDEX);
        sUriMatcher.addURI(CryptoContract.AUTHORITY, CryptoContract.CryptoNotifications.TABLE_NAME + "/#", NOTIFICATIONS_ID);

        for (int i = 0; i < CryptoContract.CryptoPortfolios.DEFAULT_PROJECTION.length; i++) {
            PORTFOLIOS_PROJECTION_MAP.put(CryptoContract.CryptoPortfolios.DEFAULT_PROJECTION[i], CryptoContract.CryptoPortfolios.DEFAULT_PROJECTION[i]);
        }

        for (int i = 0; i < CryptoContract.CryptoCoins.DEFAULT_PROJECTION.length; i++) {
            COINS_PROJECTION_MAP.put(
                    CryptoContract.CryptoCoins.DEFAULT_PROJECTION[i],
                    CryptoContract.CryptoCoins.DEFAULT_PROJECTION[i]
            );
        }

        for (int i = 0; i < CryptoContract.CryptoTransactions.DEFAULT_PROJECTION.length; i++) {
            TRANSACTIONS_PROJECTION_MAP.put(
                    CryptoContract.CryptoTransactions.DEFAULT_PROJECTION[i],
                    CryptoContract.CryptoTransactions.DEFAULT_PROJECTION[i]
            );
        }

        for (int i = 0; i < CryptoContract.CryptoExchanges.DEFAULT_PROJECTION.length; i++) {
            EXCHANGES_PROJECTION_MAP.put(
                    CryptoContract.CryptoExchanges.DEFAULT_PROJECTION[i],
                    CryptoContract.CryptoExchanges.DEFAULT_PROJECTION[i]
            );
        }

        for (int i = 0; i < CryptoContract.CryptoPortfolioCoins.DEFAULT_PROJECTION.length; i++) {
            PORTFOLIO_COINS_PROJECTION_MAP.put(
                    CryptoContract.CryptoPortfolioCoins.DEFAULT_PROJECTION[i],
                    CryptoContract.CryptoPortfolioCoins.DEFAULT_PROJECTION[i]
            );
        }
        PORTFOLIO_COINS_PROJECTION_MAP.put(
                CryptoContract.CryptoCoins.TABLE_NAME + "." + CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL,
                CryptoContract.CryptoCoins.TABLE_NAME + "." + CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL
        );

        PORTFOLIO_COINS_PROJECTION_MAP.put(
                CryptoContract.CryptoExchanges.TABLE_NAME + "." + CryptoContract.CryptoExchanges.COLUMN_NAME_NAME,
                CryptoContract.CryptoExchanges.TABLE_NAME + "." + CryptoContract.CryptoExchanges.COLUMN_NAME_NAME
        );

        for (int i = 0; i < CryptoContract.CryptoNotifications.DEFAULT_PROJECTION.length; i++) {
            NOTIFICATIONS_PROJECTION_MAP.put(
                    CryptoContract.CryptoNotifications.DEFAULT_PROJECTION[i],
                    CryptoContract.CryptoNotifications.TABLE_NAME + "." + CryptoContract.CryptoNotifications.DEFAULT_PROJECTION[i]
            );
        }

        NOTIFICATIONS_PROJECTION_MAP.put(
                CryptoContract.CryptoNotifications.COLUMN_NAME_COIN_SYMBOL,
                CryptoContract.CryptoNotifications.TABLE_COINS + "." + CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL + " as " + CryptoContract.CryptoNotifications.COLUMN_NAME_COIN_SYMBOL
        );

        NOTIFICATIONS_PROJECTION_MAP.put(
                CryptoContract.CryptoNotifications.COLUMN_NAME_CORRESPOND_SYMBOL,
                CryptoContract.CryptoNotifications.TABLE_CORRESPONDS + "." + CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL + " as " + CryptoContract.CryptoNotifications.COLUMN_NAME_CORRESPOND_SYMBOL
        );

        NOTIFICATIONS_PROJECTION_MAP.put(
                CryptoContract.CryptoNotifications.COLUMN_NAME_EXCHANGE_NAME,
                CryptoContract.CryptoExchanges.TABLE_NAME + "." + CryptoContract.CryptoExchanges.COLUMN_NAME_NAME + " as " + CryptoContract.CryptoNotifications.COLUMN_NAME_EXCHANGE_NAME
        );
    }

    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String finalWhere;
        int count;
        switch (sUriMatcher.match(uri)) {

            case PORTFOLIO_INDEX:
                count = db.delete(CryptoContract.CryptoPortfolios.TABLE_NAME, where, whereArgs);
                break;
            case PORTFOLIO_ID:
                finalWhere = CryptoContract.CryptoPortfolios._ID + " = " + uri.getPathSegments().get(CryptoContract.CryptoPortfolios.PORTFOLIOS_ID_PATH_POSITION);
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.delete(CryptoContract.CryptoPortfolios.TABLE_NAME, finalWhere, whereArgs);
                break;

            case COIN_INDEX:
                count = db.delete(CryptoContract.CryptoCoins.TABLE_NAME, where, whereArgs);
                break;
            case COIN_ID:
                finalWhere = CryptoContract.CryptoCoins._ID + " = " + uri.getPathSegments().get(CryptoContract.CryptoCoins.COINS_ID_PATH_POSITION);
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.delete(CryptoContract.CryptoCoins.TABLE_NAME, finalWhere, whereArgs);
                break;

            case TRANSACTION_INDEX:
                count = db.delete(CryptoContract.CryptoTransactions.TABLE_NAME, where, whereArgs);
                break;
            case TRANSACTION_ID:
                finalWhere = CryptoContract.CryptoTransactions._ID + " = " + uri.getPathSegments().get(CryptoContract.CryptoTransactions.TRANSACTIONS_ID_PATH_POSITION);
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.delete(CryptoContract.CryptoTransactions.TABLE_NAME, finalWhere, whereArgs);
                break;

            case EXCHANGE_INDEX:
                count = db.delete(CryptoContract.CryptoExchanges.TABLE_NAME, where, whereArgs);
                break;
            case EXCHANGE_ID:
                finalWhere = CryptoContract.CryptoExchanges._ID + " = " + uri.getPathSegments().get(CryptoContract.CryptoExchanges.EXCHANGES_ID_PATH_POSITION);
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.delete(CryptoContract.CryptoExchanges.TABLE_NAME, finalWhere, whereArgs);
                break;

            case PORTFOLIO_COINS_INDEX:
                count = db.delete(CryptoContract.CryptoPortfolioCoins.TABLE_NAME, where, whereArgs);
                break;
            case PORTFOLIO_COINS_ID:
                finalWhere = CryptoContract.CryptoPortfolioCoins._ID + " = " + uri.getPathSegments().get(CryptoContract.CryptoPortfolioCoins.PORTFOLIO_COINS_ID_PATH_POSITION);
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.delete(CryptoContract.CryptoPortfolioCoins.TABLE_NAME, finalWhere, whereArgs);
                break;

            case NOTIFICATIONS_INDEX:
                count = db.delete(CryptoContract.CryptoNotifications.TABLE_NAME, where, whereArgs);
                break;
            case NOTIFICATIONS_ID:
                finalWhere = CryptoContract.CryptoNotifications._ID + " = " + uri.getPathSegments().get(CryptoContract.CryptoNotifications.NOTIFICATIONS_ID_PATH_POSITION);
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.delete(CryptoContract.CryptoNotifications.TABLE_NAME, finalWhere, whereArgs);
                break;


            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PORTFOLIO_INDEX:
                return CryptoContract.CryptoPortfolios.CONTENT_TYPE;
            case PORTFOLIO_ID:
                return CryptoContract.CryptoPortfolios.CONTENT_ITEM_TYPE;

            case COIN_INDEX:
                return CryptoContract.CryptoCoins.CONTENT_TYPE;
            case COIN_ID:
                return CryptoContract.CryptoCoins.CONTENT_ITEM_TYPE;

            case TRANSACTION_INDEX:
                return CryptoContract.CryptoTransactions.CONTENT_TYPE;
            case TRANSACTION_ID:
                return CryptoContract.CryptoTransactions.CONTENT_ITEM_TYPE;

            case EXCHANGE_INDEX:
                return CryptoContract.CryptoExchanges.CONTENT_TYPE;
            case EXCHANGE_ID:
                return CryptoContract.CryptoExchanges.CONTENT_ITEM_TYPE;

            case PORTFOLIO_COINS_INDEX:
                return CryptoContract.CryptoPortfolioCoins.CONTENT_TYPE;
            case PORTFOLIO_COINS_ID:
                return CryptoContract.CryptoPortfolioCoins.CONTENT_ITEM_TYPE;

            case NOTIFICATIONS_INDEX:
                return CryptoContract.CryptoNotifications.CONTENT_TYPE;
            case NOTIFICATIONS_ID:
                return CryptoContract.CryptoNotifications.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {

        if (sUriMatcher.match(uri) != PORTFOLIO_INDEX
                && sUriMatcher.match(uri) != COIN_INDEX
                && sUriMatcher.match(uri) != TRANSACTION_INDEX
                && sUriMatcher.match(uri) != EXCHANGE_INDEX
                && sUriMatcher.match(uri) != PORTFOLIO_COINS_INDEX
                && sUriMatcher.match(uri) != NOTIFICATIONS_INDEX
                ) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        long rowId;
        Uri rowUri = Uri.EMPTY;

        switch (sUriMatcher.match(uri)) {

            case PORTFOLIO_INDEX:
                rowId = db.insert(CryptoContract.CryptoPortfolios.TABLE_NAME, CryptoContract.CryptoPortfolios.COLUMN_NAME_ORIGINAL, values);
                if (rowId > 0) {
                    rowUri = ContentUris.withAppendedId(CryptoContract.CryptoPortfolios.CONTENT_ID_URI_BASE, rowId);
                    getContext().getContentResolver().notifyChange(rowUri, null);
                }
                break;

            case COIN_INDEX:
                rowId = db.insert(CryptoContract.CryptoCoins.TABLE_NAME, CryptoContract.CryptoCoins.COLUMN_NAME_SYMBOL, values);
                if (rowId > 0) {
                    rowUri = ContentUris.withAppendedId(CryptoContract.CryptoCoins.CONTENT_ID_URI_BASE, rowId);
                    getContext().getContentResolver().notifyChange(rowUri, null);
                }
                break;

            case TRANSACTION_INDEX:
                rowId = db.insert(CryptoContract.CryptoTransactions.TABLE_NAME, CryptoContract.CryptoTransactions.COLUMN_NAME_AMOUNT, values);
                if (rowId > 0) {
                    rowUri = ContentUris.withAppendedId(CryptoContract.CryptoTransactions.CONTENT_ID_URI_BASE, rowId);
                    getContext().getContentResolver().notifyChange(rowUri, null);
                }
                break;

            case EXCHANGE_INDEX:
                rowId = db.insert(CryptoContract.CryptoExchanges.TABLE_NAME, CryptoContract.CryptoExchanges.COLUMN_NAME_NAME, values);
                if (rowId > 0) {
                    rowUri = ContentUris.withAppendedId(CryptoContract.CryptoExchanges.CONTENT_ID_URI_BASE, rowId);
                    getContext().getContentResolver().notifyChange(rowUri, null);
                }
                break;

            case PORTFOLIO_COINS_INDEX:
                rowId = db.insert(CryptoContract.CryptoPortfolioCoins.TABLE_NAME, CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL, values);
                if (rowId > 0) {
                    rowUri = ContentUris.withAppendedId(CryptoContract.CryptoPortfolioCoins.CONTENT_ID_URI_BASE, rowId);
                    getContext().getContentResolver().notifyChange(rowUri, null);
                }
                break;

            case NOTIFICATIONS_INDEX:
                rowId = db.insert(CryptoContract.CryptoNotifications.TABLE_NAME, CryptoContract.CryptoNotifications.COLUMN_NAME_TYPE, values);
                if (rowId > 0) {
                    rowUri = ContentUris.withAppendedId(CryptoContract.CryptoNotifications.CONTENT_ID_URI_BASE, rowId);
                    getContext().getContentResolver().notifyChange(rowUri, null);
                }
                break;
        }
        return rowUri;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy;
        switch (sUriMatcher.match(uri)) {

            case PORTFOLIO_INDEX:
                qb.setTables(CryptoContract.CryptoPortfolios.TABLE_NAME);
                qb.setProjectionMap(PORTFOLIOS_PROJECTION_MAP);
                orderBy = CryptoContract.CryptoPortfolios.DEFAULT_SORT_ORDER;
                break;
            case PORTFOLIO_ID:
                qb.setTables(CryptoContract.CryptoPortfolios.TABLE_NAME);
                qb.setProjectionMap(PORTFOLIOS_PROJECTION_MAP);
                qb.appendWhere(CryptoContract.CryptoPortfolios._ID + "=" + uri.getPathSegments().get(CryptoContract.CryptoPortfolios.PORTFOLIOS_ID_PATH_POSITION));
                orderBy = CryptoContract.CryptoPortfolios.DEFAULT_SORT_ORDER;
                break;

            case COIN_INDEX:
                qb.setTables(CryptoContract.CryptoCoins.TABLE_NAME);
                qb.setProjectionMap(COINS_PROJECTION_MAP);
                orderBy = CryptoContract.CryptoCoins.DEFAULT_SORT_ORDER;
                break;
            case COIN_ID:
                qb.setTables(CryptoContract.CryptoCoins.TABLE_NAME);
                qb.setProjectionMap(COINS_PROJECTION_MAP);
                qb.appendWhere(CryptoContract.CryptoCoins._ID + "=" + uri.getPathSegments().get(CryptoContract.CryptoCoins.COINS_ID_PATH_POSITION));
                orderBy = CryptoContract.CryptoCoins.DEFAULT_SORT_ORDER;
                break;

            case TRANSACTION_INDEX:
                qb.setTables(CryptoContract.CryptoTransactions.TABLE_NAME);
                qb.setProjectionMap(TRANSACTIONS_PROJECTION_MAP);
                orderBy = CryptoContract.CryptoTransactions.DEFAULT_SORT_ORDER;
                break;
            case TRANSACTION_ID:
                qb.setTables(CryptoContract.CryptoTransactions.TABLE_NAME);
                qb.setProjectionMap(TRANSACTIONS_PROJECTION_MAP);
                qb.appendWhere(CryptoContract.CryptoTransactions._ID + "=" + uri.getPathSegments().get(CryptoContract.CryptoTransactions.TRANSACTIONS_ID_PATH_POSITION));
                orderBy = CryptoContract.CryptoTransactions.DEFAULT_SORT_ORDER;
                break;

            case EXCHANGE_INDEX:
                qb.setTables(CryptoContract.CryptoExchanges.TABLE_NAME);
                qb.setProjectionMap(EXCHANGES_PROJECTION_MAP);
                orderBy = CryptoContract.CryptoExchanges.DEFAULT_SORT_ORDER;
                break;
            case EXCHANGE_ID:
                qb.setTables(CryptoContract.CryptoExchanges.TABLE_NAME);
                qb.setProjectionMap(EXCHANGES_PROJECTION_MAP);
                qb.appendWhere(CryptoContract.CryptoExchanges._ID + "=" + uri.getPathSegments().get(CryptoContract.CryptoExchanges.EXCHANGES_ID_PATH_POSITION));
                orderBy = CryptoContract.CryptoExchanges.DEFAULT_SORT_ORDER;
                break;

            case PORTFOLIO_COINS_INDEX:
                qb.setTables(CryptoContract.CryptoPortfolioCoins.TABLE_NAME);
                qb.setTables(
                        CryptoContract.CryptoPortfolioCoins.TABLE_NAME +
                        " LEFT JOIN " +
                        CryptoContract.CryptoCoins.TABLE_NAME +
                        " ON (" +
                        CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID +
                        " = " +
                        CryptoContract.CryptoCoins.TABLE_NAME + "." + CryptoContract.CryptoCoins._ID +
                        ")" +
                        " LEFT JOIN " +
                        CryptoContract.CryptoExchanges.TABLE_NAME +
                        " ON (" +
                        CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID +
                        " = " +
                        CryptoContract.CryptoExchanges.TABLE_NAME + "." + CryptoContract.CryptoExchanges._ID +
                        ")"
                );

                qb.setProjectionMap(PORTFOLIO_COINS_PROJECTION_MAP);
                orderBy = CryptoContract.CryptoPortfolioCoins.DEFAULT_SORT_ORDER;
                break;
            case PORTFOLIO_COINS_ID:
                qb.setTables(
                        CryptoContract.CryptoPortfolioCoins.TABLE_NAME +
                                " LEFT JOIN " +
                                CryptoContract.CryptoCoins.TABLE_NAME +
                                " ON (" +
                                CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_COIN_ID +
                                " = " +
                                CryptoContract.CryptoCoins.TABLE_NAME + "." + CryptoContract.CryptoCoins._ID +
                                ")" +
                                " LEFT JOIN " +
                                CryptoContract.CryptoExchanges.TABLE_NAME +
                                " ON (" +
                                CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID +
                                " = " +
                                CryptoContract.CryptoExchanges.TABLE_NAME + "." + CryptoContract.CryptoExchanges._ID +
                                ")"
                );
                qb.setProjectionMap(PORTFOLIO_COINS_PROJECTION_MAP);
                qb.appendWhere(CryptoContract.CryptoPortfolioCoins.TABLE_NAME + "." + CryptoContract.CryptoPortfolioCoins._ID + "=" + uri.getPathSegments().get(CryptoContract.CryptoPortfolioCoins.PORTFOLIO_COINS_ID_PATH_POSITION));
                orderBy = CryptoContract.CryptoPortfolioCoins.DEFAULT_SORT_ORDER;
                break;


            case NOTIFICATIONS_INDEX:
                qb.setTables(
                        CryptoContract.CryptoNotifications.TABLE_NAME +
                        " LEFT JOIN " +
                        CryptoContract.CryptoCoins.TABLE_NAME + " as " + CryptoContract.CryptoNotifications.TABLE_COINS +
                        " ON (" +
                        CryptoContract.CryptoNotifications.TABLE_NAME + "." + CryptoContract.CryptoNotifications.COLUMN_NAME_COIN_ID +
                        " = " +
                        CryptoContract.CryptoNotifications.TABLE_COINS + "." + CryptoContract.CryptoCoins._ID +
                        ")" +
                        " LEFT JOIN " +
                        CryptoContract.CryptoCoins.TABLE_NAME + " as " + CryptoContract.CryptoNotifications.TABLE_CORRESPONDS +
                        " ON (" +
                        CryptoContract.CryptoNotifications.TABLE_NAME + "." + CryptoContract.CryptoNotifications.COLUMN_NAME_CORRESPOND_ID +
                        " = " +
                        CryptoContract.CryptoNotifications.TABLE_CORRESPONDS + "." + CryptoContract.CryptoCoins._ID +
                        ")" +
                        " LEFT JOIN " +
                        CryptoContract.CryptoExchanges.TABLE_NAME +
                        " ON (" +
                        CryptoContract.CryptoNotifications.TABLE_NAME + "." + CryptoContract.CryptoNotifications.COLUMN_NAME_EXCHANGE_ID +
                        " = " +
                        CryptoContract.CryptoExchanges.TABLE_NAME + "." + CryptoContract.CryptoExchanges._ID +
                        ")"
                );
                qb.setProjectionMap(NOTIFICATIONS_PROJECTION_MAP);
                orderBy = CryptoContract.CryptoNotifications.DEFAULT_SORT_ORDER;
                break;
            case NOTIFICATIONS_ID:
                qb.setTables(
                        CryptoContract.CryptoNotifications.TABLE_NAME +
                        " LEFT JOIN " +
                        CryptoContract.CryptoCoins.TABLE_NAME + " as " + CryptoContract.CryptoNotifications.TABLE_COINS +
                        " ON (" +
                        CryptoContract.CryptoNotifications.TABLE_NAME + "." + CryptoContract.CryptoNotifications.COLUMN_NAME_COIN_ID +
                        " = " +
                        CryptoContract.CryptoNotifications.TABLE_COINS + "." + CryptoContract.CryptoCoins._ID +
                        ")" +
                        " LEFT JOIN " +
                        CryptoContract.CryptoCoins.TABLE_NAME + " as " + CryptoContract.CryptoNotifications.TABLE_CORRESPONDS +
                        " ON (" +
                        CryptoContract.CryptoNotifications.TABLE_NAME + "." + CryptoContract.CryptoNotifications.COLUMN_NAME_CORRESPOND_ID +
                        " = " +
                        CryptoContract.CryptoNotifications.TABLE_CORRESPONDS + "." + CryptoContract.CryptoCoins._ID +
                        ")" +
                        " LEFT JOIN " +
                        CryptoContract.CryptoExchanges.TABLE_NAME +
                        " ON (" +
                        CryptoContract.CryptoNotifications.TABLE_NAME + "." + CryptoContract.CryptoNotifications.COLUMN_NAME_EXCHANGE_ID +
                        " = " +
                        CryptoContract.CryptoExchanges.TABLE_NAME + "." + CryptoContract.CryptoExchanges._ID +
                        ")"
                );
                qb.setProjectionMap(NOTIFICATIONS_PROJECTION_MAP);
                qb.appendWhere(CryptoContract.CryptoNotifications.TABLE_NAME + "." + CryptoContract.CryptoNotifications._ID + "=" + uri.getPathSegments().get(CryptoContract.CryptoNotifications.NOTIFICATIONS_ID_PATH_POSITION));
                orderBy = CryptoContract.CryptoNotifications.DEFAULT_SORT_ORDER;
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if(sortOrder != null) {
            orderBy = sortOrder;
        }
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        String finalWhere;
        String id;

        switch (sUriMatcher.match(uri)) {
            case PORTFOLIO_INDEX:
                count = db.update(CryptoContract.CryptoPortfolios.TABLE_NAME, values, where, whereArgs);
                break;
            case PORTFOLIO_ID:
                id = uri.getPathSegments().get(CryptoContract.CryptoPortfolios.PORTFOLIOS_ID_PATH_POSITION);
                finalWhere = CryptoContract.CryptoPortfolios._ID + " = " + id;
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.update(CryptoContract.CryptoPortfolios.TABLE_NAME, values, finalWhere, whereArgs);
                break;

            case COIN_INDEX:
                count = db.update(CryptoContract.CryptoCoins.TABLE_NAME, values, where, whereArgs);
                break;
            case COIN_ID:
                id = uri.getPathSegments().get(CryptoContract.CryptoCoins.COINS_ID_PATH_POSITION);
                finalWhere = CryptoContract.CryptoCoins._ID + " = " + id;
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.update(CryptoContract.CryptoCoins.TABLE_NAME, values, finalWhere, whereArgs);
                break;

            case TRANSACTION_INDEX:
                count = db.update(CryptoContract.CryptoTransactions.TABLE_NAME, values, where, whereArgs);
                break;
            case TRANSACTION_ID:
                id = uri.getPathSegments().get(CryptoContract.CryptoTransactions.TRANSACTIONS_ID_PATH_POSITION);
                finalWhere = CryptoContract.CryptoTransactions._ID + " = " + id;
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.update(CryptoContract.CryptoTransactions.TABLE_NAME, values, finalWhere, whereArgs);
                break;

            case EXCHANGE_INDEX:
                count = db.update(CryptoContract.CryptoExchanges.TABLE_NAME, values, where, whereArgs);
                break;
            case EXCHANGE_ID:
                id = uri.getPathSegments().get(CryptoContract.CryptoExchanges.EXCHANGES_ID_PATH_POSITION);
                finalWhere = CryptoContract.CryptoExchanges._ID + " = " + id;
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.update(CryptoContract.CryptoExchanges.TABLE_NAME, values, finalWhere, whereArgs);
                break;

            case PORTFOLIO_COINS_INDEX:
                count = db.update(CryptoContract.CryptoPortfolioCoins.TABLE_NAME, values, where, whereArgs);
                break;
            case PORTFOLIO_COINS_ID:
                id = uri.getPathSegments().get(CryptoContract.CryptoPortfolioCoins.PORTFOLIO_COINS_ID_PATH_POSITION);
                finalWhere = CryptoContract.CryptoPortfolioCoins._ID + " = " + id;
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.update(CryptoContract.CryptoPortfolioCoins.TABLE_NAME, values, finalWhere, whereArgs);
                break;

            case NOTIFICATIONS_INDEX:
                count = db.update(CryptoContract.CryptoNotifications.TABLE_NAME, values, where, whereArgs);
                break;
            case NOTIFICATIONS_ID:
                id = uri.getPathSegments().get(CryptoContract.CryptoNotifications.NOTIFICATIONS_ID_PATH_POSITION);
                finalWhere = CryptoContract.CryptoNotifications._ID + " = " + id;
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.update(CryptoContract.CryptoNotifications.TABLE_NAME, values, finalWhere, whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


}
