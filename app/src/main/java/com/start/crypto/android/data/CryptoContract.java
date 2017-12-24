package com.start.crypto.android.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class CryptoContract {

    public static final int LOADER_PORTFOLIOS       = 101;
    public static final int LOADER_COINS            = 102;
    public static final int LOADER_TRANSACTIONS     = 103;
    public static final int LOADER_EXCHANGES        = 104;
    public static final int LOADER_PORTFOLIO_COINS  = 105;
    public static final int LOADER_NOTIFICATIONS    = 106;

    public static final String AUTHORITY = "com.start.crypto.android.sync";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "crypto.db";

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String INTEGER_TYPE_DEFAULT = " DEFAULT 0";
    private static final String REAL_TYPE = " REAL";
    private static final String REAL_TYPE_DEFAULT = " DEFAULT 0";
    private static final String NUMERIC_TYPE = " NUMERIC";
    private static final String NUMERIC_TYPE_DEFAULT = " DEFAULT 0";
    private static final String TEXT_TYPE = " TEXT";
    private static final String TEXT_TYPE_DEFAULT = " DEFAULT ''";
    private static final String COMMA_SEP = ", ";



    /* PORTFOLIOS */
    public static final String SQL_CREATE_PORTFOLIOS =
            "CREATE TABLE " + CryptoPortfolios.TABLE_NAME + " (" +
                    CryptoPortfolios._ID + " INTEGER PRIMARY KEY," +
                    CryptoPortfolios.COLUMN_NAME_BASE_COIN_ID + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolios.COLUMN_NAME_BALANCE + REAL_TYPE + REAL_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolios.COLUMN_NAME_ORIGINAL + REAL_TYPE + REAL_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolios.COLUMN_NAME_PRICE_NOW + REAL_TYPE + REAL_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolios.COLUMN_NAME_PRICE_ORIGINAL + REAL_TYPE + REAL_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolios.COLUMN_NAME_PRICE_24H + REAL_TYPE + REAL_TYPE_DEFAULT + COMMA_SEP +

                    CryptoPortfolios.COLUMN_NAME_COINS_COUNT + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolios.COLUMN_NAME_USER_ID + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolios.COLUMN_NAME_USERNAME + TEXT_TYPE + TEXT_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolios.COLUMN_NAME_PROFIT24H + REAL_TYPE + REAL_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolios.COLUMN_NAME_PROFIT7D + REAL_TYPE + REAL_TYPE_DEFAULT +

                    " )";

    public static final String SQL_DELETE_PORTFOLIOS =
            "DROP TABLE IF EXISTS " + CryptoPortfolios.TABLE_NAME;




    /* COINS */
    public static final String SQL_CREATE_COINS =
            "CREATE TABLE " + CryptoCoins.TABLE_NAME + " (" +
                    CryptoCoins._ID + " INTEGER PRIMARY KEY," +
                    CryptoCoins.COLUMN_NAME_NAME + TEXT_TYPE + TEXT_TYPE_DEFAULT + COMMA_SEP +
                    CryptoCoins.COLUMN_NAME_SYMBOL + TEXT_TYPE + TEXT_TYPE_DEFAULT + COMMA_SEP +
                    CryptoCoins.COLUMN_NAME_LOGO + TEXT_TYPE + TEXT_TYPE_DEFAULT + COMMA_SEP +
                    CryptoCoins.COLUMN_NAME_SORT_ORDER + INTEGER_TYPE + INTEGER_TYPE_DEFAULT +
                    " )";

    public static final String SQL_DELETE_COINS =
            "DROP TABLE IF EXISTS " + CryptoCoins.TABLE_NAME;




    /* TRANSACTIONS */
    public static final String SQL_CREATE_TRANSACTIONS =
            "CREATE TABLE " + CryptoTransactions.TABLE_NAME + " (" +
                    CryptoTransactions._ID + " INTEGER PRIMARY KEY," +
                    CryptoTransactions.COLUMN_NAME_COIN_ID + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoTransactions.COLUMN_NAME_PORTFOLIO_ID + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoTransactions.COLUMN_NAME_COIN_CORRESPOND_ID + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoTransactions.COLUMN_NAME_EXCHANGE_ID + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoTransactions.COLUMN_NAME_PROTFOLIO_BALANCE + REAL_TYPE + REAL_TYPE_DEFAULT + COMMA_SEP +
                    CryptoTransactions.COLUMN_NAME_AMOUNT + REAL_TYPE + REAL_TYPE_DEFAULT + COMMA_SEP +
                    CryptoTransactions.COLUMN_NAME_PRICE + REAL_TYPE + REAL_TYPE_DEFAULT + COMMA_SEP +
                    CryptoTransactions.COLUMN_NAME_DATETIME + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoTransactions.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + TEXT_TYPE_DEFAULT +
                    " )";

    public static final String SQL_DELETE_TRANSACTIONS =
            "DROP TABLE IF EXISTS " + CryptoTransactions.TABLE_NAME;





    /* EXCHANGES */
    public static final String SQL_CREATE_EXCHANGES =
            "CREATE TABLE " + CryptoExchanges.TABLE_NAME + " (" +
                    CryptoExchanges._ID + " INTEGER PRIMARY KEY," +
                    CryptoExchanges.COLUMN_NAME_NAME + TEXT_TYPE + TEXT_TYPE_DEFAULT + COMMA_SEP +
                    CryptoExchanges.COLUMN_NAME_EXTERNAL_ID + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoExchanges.COLUMN_NAME_API_URL + TEXT_TYPE + TEXT_TYPE_DEFAULT +
                    " )";

    public static final String SQL_DELETE_EXCHANGES =
            "DROP TABLE IF EXISTS " + CryptoExchanges.TABLE_NAME;





    /* PORTFOLIO_COINS */
    public static final String SQL_CREATE_PORTFOLIO_COINS =
            "CREATE TABLE " + CryptoPortfolioCoins.TABLE_NAME + " (" +
                    CryptoPortfolioCoins._ID + " INTEGER PRIMARY KEY," +
                    CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolioCoins.COLUMN_NAME_COIN_ID + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL + REAL_TYPE + REAL_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW + REAL_TYPE + REAL_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL + REAL_TYPE + REAL_TYPE_DEFAULT + COMMA_SEP +
                    CryptoPortfolioCoins.COLUMN_NAME_PRICE_24H + REAL_TYPE + REAL_TYPE_DEFAULT +
                    " )";

    public static final String SQL_DELETE_PORTFOLIO_COINS =
            "DROP TABLE IF EXISTS " + CryptoPortfolioCoins.TABLE_NAME;




    /* NOTIFICATIONS */
    public static final String SQL_CREATE_NOTIFICATIONS =
            "CREATE TABLE " + CryptoNotifications.TABLE_NAME + " (" +
                    CryptoNotifications._ID + " INTEGER PRIMARY KEY," +
                    CryptoNotifications.COLUMN_NAME_COIN_ID + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoNotifications.COLUMN_NAME_CORRESPOND_ID + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoNotifications.COLUMN_NAME_EXCHANGE_ID + INTEGER_TYPE + INTEGER_TYPE_DEFAULT + COMMA_SEP +
                    CryptoNotifications.COLUMN_NAME_PRICE_THRESHOLD + REAL_TYPE + REAL_TYPE_DEFAULT + COMMA_SEP +
                    CryptoNotifications.COLUMN_NAME_TYPE + TEXT_TYPE + TEXT_TYPE_DEFAULT + COMMA_SEP +
                    CryptoNotifications.COLUMN_NAME_ACTIVE + NUMERIC_TYPE + NUMERIC_TYPE_DEFAULT + COMMA_SEP +
                    CryptoNotifications.COLUMN_NAME_COMPARE + TEXT_TYPE + TEXT_TYPE_DEFAULT + COMMA_SEP +
                    " UNIQUE(" + CryptoNotifications.COLUMN_NAME_COIN_ID + ", " + CryptoNotifications.COLUMN_NAME_EXCHANGE_ID + ") ON CONFLICT IGNORE" +
                    " )";

    public static final String SQL_DELETE_NOTIFICATIONS =
            "DROP TABLE IF EXISTS " + CryptoNotifications.TABLE_NAME;



    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public CryptoContract() {}

    /* Inner class that defines the table contents */
    public static final class CryptoPortfolios implements BaseColumns {

        public static final String TABLE_NAME                  = "crypto_portfolios";
        public static final String SCHEME                      = "content://";
        public static final String PATH_PORTFOLIOS             = "/crypto_portfolios";
        public static final String PATH_PORTFOLIOS_ID          = "/crypto_portfolios/";
        public static final Uri CONTENT_URI                    = Uri.parse(SCHEME + AUTHORITY + PATH_PORTFOLIOS);
        public static final Uri CONTENT_ID_URI_BASE            = Uri.parse(SCHEME + AUTHORITY + PATH_PORTFOLIOS_ID);
        public static final String CONTENT_TYPE                = "vnd.android.cursor.dir/vnd.google.crypto_portfolios";
        public static final String CONTENT_ITEM_TYPE           = "vnd.android.cursor.item/vnd.google.crypto_portfolios";
        public static final String DEFAULT_SORT_ORDER          = "_id ASC";

        public static final int     PORTFOLIOS_ID_PATH_POSITION           = 1;

        public static final String COLUMN_NAME_BASE_COIN_ID     = "base_coin_id";
        public static final String COLUMN_NAME_BALANCE          = "balance";
        public static final String COLUMN_NAME_ORIGINAL         = "original";
        public static final String COLUMN_NAME_PRICE_NOW        = "price_now";
        public static final String COLUMN_NAME_PRICE_ORIGINAL   = "price_original";
        public static final String COLUMN_NAME_PRICE_24H        = "price_24h";

        public static final String COLUMN_NAME_COINS_COUNT      = "coins_count";
        public static final String COLUMN_NAME_USER_ID          = "user_id";
        public static final String COLUMN_NAME_USERNAME         = "user_name";
        public static final String COLUMN_NAME_PROFIT24H        = "profit_24h";
        public static final String COLUMN_NAME_PROFIT7D         = "profit_7d";

        public static final String[] DEFAULT_PROJECTION = new String[] {
                CryptoPortfolios._ID,
                CryptoPortfolios.COLUMN_NAME_BASE_COIN_ID,
                CryptoPortfolios.COLUMN_NAME_BALANCE,
                CryptoPortfolios.COLUMN_NAME_ORIGINAL,
                CryptoPortfolios.COLUMN_NAME_PRICE_NOW,
                CryptoPortfolios.COLUMN_NAME_PRICE_ORIGINAL,
                CryptoPortfolios.COLUMN_NAME_PRICE_24H,

                CryptoPortfolios.COLUMN_NAME_COINS_COUNT,
                CryptoPortfolios.COLUMN_NAME_USER_ID,
                CryptoPortfolios.COLUMN_NAME_USERNAME,
                CryptoPortfolios.COLUMN_NAME_PROFIT24H,
                CryptoPortfolios.COLUMN_NAME_PROFIT7D

        };
    }

    public static final class CryptoCoins implements BaseColumns {

        public static final String TABLE_NAME          = "crypto_coins";
        public static final String SCHEME              = "content://";
        public static final String PATH_COINS        = "/crypto_coins";
        public static final String PATH_COINS_ID     = "/crypto_coins/";
        public static final Uri CONTENT_URI            = Uri.parse(SCHEME + AUTHORITY + PATH_COINS);
        public static final Uri CONTENT_ID_URI_BASE    = Uri.parse(SCHEME + AUTHORITY + PATH_COINS_ID);
        public static final String CONTENT_TYPE        = "vnd.android.cursor.dir/vnd.google.crypto_coins";
        public static final String CONTENT_ITEM_TYPE   = "vnd.android.cursor.item/vnd.google.crypto_coins";
        public static final String DEFAULT_SORT_ORDER  = "_id ASC";

        public static final int     COINS_ID_PATH_POSITION = 1;

        public static final String COLUMN_NAME_NAME         = "full_name";
        public static final String COLUMN_NAME_SYMBOL       = "symbol";
        public static final String COLUMN_NAME_LOGO         = "logo";
        public static final String COLUMN_NAME_SORT_ORDER   = "sort_order";



        public static final String[] DEFAULT_PROJECTION = new String[] {
                CryptoCoins._ID,
                CryptoCoins.COLUMN_NAME_NAME,
                CryptoCoins.COLUMN_NAME_SYMBOL,
                CryptoCoins.COLUMN_NAME_LOGO,
                CryptoCoins.COLUMN_NAME_SORT_ORDER
        };

    }

    public static final class CryptoTransactions implements BaseColumns {

        public static final String TABLE_NAME          = "crypto_transactions";
        public static final String SCHEME              = "content://";
        public static final String PATH_TRANSACTIONS        = "/crypto_transactions";
        public static final String PATH_TRANSACTIONS_ID     = "/crypto_transactions/";
        public static final Uri CONTENT_URI            = Uri.parse(SCHEME + AUTHORITY + PATH_TRANSACTIONS);
        public static final Uri CONTENT_ID_URI_BASE    = Uri.parse(SCHEME + AUTHORITY + PATH_TRANSACTIONS_ID);
        public static final String CONTENT_TYPE        = "vnd.android.cursor.dir/vnd.google.crypto_transactions";
        public static final String CONTENT_ITEM_TYPE   = "vnd.android.cursor.item/vnd.google.crypto_transactions";
        public static final String DEFAULT_SORT_ORDER  = "_id ASC";

        public static final int     TRANSACTIONS_ID_PATH_POSITION = 1;

        public static final String COLUMN_NAME_COIN_ID              = "coin_id";
        public static final String COLUMN_NAME_PORTFOLIO_ID         = "portfolio_id";
        public static final String COLUMN_NAME_COIN_CORRESPOND_ID   = "coin_correspond_id";
        public static final String COLUMN_NAME_EXCHANGE_ID          = "exchange_id";
        public static final String COLUMN_NAME_PROTFOLIO_BALANCE    = "portfolio_balance";
        public static final String COLUMN_NAME_AMOUNT               = "amount";
        public static final String COLUMN_NAME_PRICE                = "price";
        public static final String COLUMN_NAME_DATETIME             = "datetime";
        public static final String COLUMN_NAME_DESCRIPTION          = "description";


        public static final String[] DEFAULT_PROJECTION = new String[] {
                CryptoTransactions._ID,
                CryptoTransactions.COLUMN_NAME_COIN_ID,
                CryptoTransactions.COLUMN_NAME_PORTFOLIO_ID,
                CryptoTransactions.COLUMN_NAME_COIN_CORRESPOND_ID,
                CryptoTransactions.COLUMN_NAME_EXCHANGE_ID,
                CryptoTransactions.COLUMN_NAME_PROTFOLIO_BALANCE,
                CryptoTransactions.COLUMN_NAME_AMOUNT,
                CryptoTransactions.COLUMN_NAME_PRICE,
                CryptoTransactions.COLUMN_NAME_DATETIME,
                CryptoTransactions.COLUMN_NAME_DESCRIPTION
        };

    }

    public static final class CryptoExchanges implements BaseColumns {

        public static final String TABLE_NAME          = "crypto_exchanges";
        public static final String SCHEME              = "content://";
        public static final String PATH_EXCHANGES        = "/crypto_exchanges";
        public static final String PATH_EXCHANGES_ID     = "/crypto_exchanges/";
        public static final Uri CONTENT_URI            = Uri.parse(SCHEME + AUTHORITY + PATH_EXCHANGES);
        public static final Uri CONTENT_ID_URI_BASE    = Uri.parse(SCHEME + AUTHORITY + PATH_EXCHANGES_ID);
        public static final String CONTENT_TYPE        = "vnd.android.cursor.dir/vnd.google.crypto_exchanges";
        public static final String CONTENT_ITEM_TYPE   = "vnd.android.cursor.item/vnd.google.crypto_exchanges";
        public static final String DEFAULT_SORT_ORDER  = "_id ASC";

        public static final int     EXCHANGES_ID_PATH_POSITION = 1;

        public static final String COLUMN_NAME_NAME          = "name";
        public static final String COLUMN_NAME_EXTERNAL_ID   = "external_id";
        public static final String COLUMN_NAME_API_URL       = "api_url";


        public static final String[] DEFAULT_PROJECTION = new String[] {
                CryptoExchanges._ID,
                CryptoExchanges.COLUMN_NAME_NAME,
                CryptoExchanges.COLUMN_NAME_EXTERNAL_ID,
                CryptoExchanges.COLUMN_NAME_API_URL
        };
    }

    public static final class CryptoPortfolioCoins implements BaseColumns {

        public static final String TABLE_NAME                   = "crypto_portfolio_coins";
        public static final String SCHEME                       = "content://";
        public static final String PATH_PORTFOLIO_COINS         = "/crypto_portfolio_coins";
        public static final String PATH_PORTFOLIO_COINS_ID      = "/crypto_portfolio_coins/";
        public static final Uri CONTENT_URI                     = Uri.parse(SCHEME + AUTHORITY + PATH_PORTFOLIO_COINS);
        public static final Uri CONTENT_ID_URI_BASE             = Uri.parse(SCHEME + AUTHORITY + PATH_PORTFOLIO_COINS_ID);
        public static final String CONTENT_TYPE                 = "vnd.android.cursor.dir/vnd.google.crypto_portfolio_coins";
        public static final String CONTENT_ITEM_TYPE            = "vnd.android.cursor.item/vnd.google.crypto_portfolio_coins";
        public static final String DEFAULT_SORT_ORDER           = "_id ASC";

        public static final int     PORTFOLIO_COINS_ID_PATH_POSITION = 1;


        public static final String COLUMN_NAME_PORTFOLIO_ID     = "portfolio_id";
        public static final String COLUMN_NAME_COIN_ID          = "coin_id";
        public static final String COLUMN_NAME_EXCHANGE_ID      = "exchange_id";
        public static final String COLUMN_NAME_ORIGINAL         = "original";
        public static final String COLUMN_NAME_PRICE_NOW        = "price_now";
        public static final String COLUMN_NAME_PRICE_ORIGINAL   = "price_original";
        public static final String COLUMN_NAME_PRICE_24H        = "price_24h";

        public static final String[] DEFAULT_PROJECTION = new String[] {
                CryptoPortfolioCoins.TABLE_NAME + "." + CryptoPortfolioCoins._ID,
                CryptoPortfolioCoins.TABLE_NAME + "." + CryptoPortfolioCoins.COLUMN_NAME_PORTFOLIO_ID,
                CryptoPortfolioCoins.TABLE_NAME + "." + CryptoPortfolioCoins.COLUMN_NAME_COIN_ID,
                CryptoPortfolioCoins.TABLE_NAME + "." + CryptoPortfolioCoins.COLUMN_NAME_EXCHANGE_ID,
                CryptoPortfolioCoins.TABLE_NAME + "." + CryptoPortfolioCoins.COLUMN_NAME_ORIGINAL,
                CryptoPortfolioCoins.TABLE_NAME + "." + CryptoPortfolioCoins.COLUMN_NAME_PRICE_NOW,
                CryptoPortfolioCoins.TABLE_NAME + "." + CryptoPortfolioCoins.COLUMN_NAME_PRICE_ORIGINAL,
                CryptoPortfolioCoins.TABLE_NAME + "." + CryptoPortfolioCoins.COLUMN_NAME_PRICE_24H,
        };
    }

    public static final class CryptoNotifications implements BaseColumns {

        public static final String TABLE_NAME               = "crypto_notifications";
        public static final String TABLE_COINS              = "coins";
        public static final String TABLE_CORRESPONDS        = "corresponds";
        public static final String TABLE_EXCHANGES          = "exchanges";

        public static final String SCHEME                   = "content://";
        public static final String PATH_NOTIFICATIONS       = "/crypto_notifications";
        public static final String PATH_TNOTIFICATIONS_ID   = "/crypto_notifications/";
        public static final Uri CONTENT_URI                 = Uri.parse(SCHEME + AUTHORITY + PATH_NOTIFICATIONS);
        public static final Uri CONTENT_ID_URI_BASE         = Uri.parse(SCHEME + AUTHORITY + PATH_TNOTIFICATIONS_ID);
        public static final String CONTENT_TYPE             = "vnd.android.cursor.dir/vnd.google.crypto_notifications";
        public static final String CONTENT_ITEM_TYPE        = "vnd.android.cursor.item/vnd.google.crypto_notifications";
        public static final String DEFAULT_SORT_ORDER       = "_id ASC";

        public static final int     NOTIFICATIONS_ID_PATH_POSITION = 1;

        public static final String COLUMN_NAME_COIN_ID              = "coin_id";
        public static final String COLUMN_NAME_COIN_SYMBOL          = "coin_symbol";
        public static final String COLUMN_NAME_CORRESPOND_ID        = "correspond_id";
        public static final String COLUMN_NAME_CORRESPOND_SYMBOL    = "correspond_symbol";
        public static final String COLUMN_NAME_EXCHANGE_ID          = "exchange_id";
        public static final String COLUMN_NAME_EXCHANGE_NAME        = "exchange_name";
        public static final String COLUMN_NAME_PRICE_THRESHOLD      = "price_threshold";
        public static final String COLUMN_NAME_TYPE                 = "type";
        public static final String COLUMN_NAME_ACTIVE               = "active";
        public static final String COLUMN_NAME_COMPARE              = "compare";

        public static final String[] DEFAULT_PROJECTION = new String[] {
                CryptoNotifications._ID,
                CryptoNotifications.COLUMN_NAME_COIN_ID,
                CryptoNotifications.COLUMN_NAME_CORRESPOND_ID,
                CryptoNotifications.COLUMN_NAME_EXCHANGE_ID,
                CryptoNotifications.COLUMN_NAME_PRICE_THRESHOLD,
                CryptoNotifications.COLUMN_NAME_TYPE,
                CryptoNotifications.COLUMN_NAME_ACTIVE,
                CryptoNotifications.COLUMN_NAME_COMPARE
        };

    }

}
