package com.start.crypto.android;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.start.crypto.android.data.CryptoContract;

import butterknife.BindView;


public class NotificationFormActivity extends BaseActivity {

    public static final String EXTRA_COIN_ID        = "coin_id";
    public static final String EXTRA_CORRESPOND_ID  = "correspond_id";
    public static final String EXTRA_EXCHANGE_ID    = "exchange_id";
    public static final String EXTRA_PRICE          = "price";

    @BindView(R.id.price)                   EditText mPriceView;
    @BindView(R.id.add_notification)        Button mAddNotificationButton;

    private long    argExchangeId;
    private long    argCoinId;
    private long    argCorrespondId;
    private double  argPrice;


    public static void startActivity(Context context, long coinId, long correspondId, long exchangeId, double price) {
        Intent intent = new Intent(context, NotificationFormActivity.class);
        intent.putExtra(EXTRA_COIN_ID, coinId);
        intent.putExtra(EXTRA_CORRESPOND_ID, correspondId);
        intent.putExtra(EXTRA_EXCHANGE_ID, exchangeId);
        intent.putExtra(EXTRA_PRICE, price);
        context.startActivity(intent);
    }

    
    @Override
    protected void setupLayout() {
        setContentView(R.layout.activity_notification_form);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        argExchangeId = getIntent().getLongExtra(EXTRA_EXCHANGE_ID, 0);
        if (argExchangeId == 0) {
            finish();
            return;
        }
        argCoinId = getIntent().getLongExtra(EXTRA_COIN_ID, 0);
        if (argCoinId == 0) {
            finish();
            return;
        }
        argCorrespondId = getIntent().getLongExtra(EXTRA_CORRESPOND_ID, 0);
        if (argCorrespondId == 0) {
            finish();
            return;
        }
        argPrice = getIntent().getDoubleExtra(EXTRA_PRICE, 0);
        if (argPrice == 0) {
            finish();
            return;
        }

        mAddNotificationButton.setOnClickListener(v -> {
            mAddNotificationButton.setEnabled(false);

            double price = Double.valueOf(mPriceView.getText().toString());
            if (price == argPrice) {
                return ;
            }

            // create notification
            ContentValues values = new ContentValues();
            values.put(CryptoContract.CryptoNotifications.COLUMN_NAME_COIN_ID, argCoinId);
            values.put(CryptoContract.CryptoNotifications.COLUMN_NAME_CORRESPOND_ID, argCorrespondId);
            values.put(CryptoContract.CryptoNotifications.COLUMN_NAME_EXCHANGE_ID, argExchangeId);
            if (price > argPrice) {
                values.put(CryptoContract.CryptoNotifications.COLUMN_NAME_COMPARE, NotificationComparePrice.GREATER_THAN.name());
            } else {
                values.put(CryptoContract.CryptoNotifications.COLUMN_NAME_COMPARE, NotificationComparePrice.LESS_THAN.name());
            }
            values.put(CryptoContract.CryptoNotifications.COLUMN_NAME_PRICE_THRESHOLD, price);
            values.put(CryptoContract.CryptoNotifications.COLUMN_NAME_TYPE, "ONE_OFF");
            values.put(CryptoContract.CryptoNotifications.COLUMN_NAME_ACTIVE, 1);
            getContentResolver().insert(CryptoContract.CryptoNotifications.CONTENT_URI, values);

            finish();
        });

    }




}
