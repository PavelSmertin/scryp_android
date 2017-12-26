package com.start.crypto.android.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class KeyboardHelper {

    public static DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    static {
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        formatter.setDecimalFormatSymbols(symbols);
    }


    private static final NavigableMap<Double, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000D, "k");
        suffixes.put(1_000_000D, "M");
        suffixes.put(1_000_000_000D, "G");
        suffixes.put(1_000_000_000_000D, "T");
        suffixes.put(1_000_000_000_000_000D, "P");
        suffixes.put(1_000_000_000_000_000_000D, "E");
    }

    public static String cut(double value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return cut(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + cut(-value);
        if (value < 1000_000D) return formatter.format(new BigDecimal(value).setScale(2, BigDecimal.ROUND_CEILING).doubleValue()); //deal with easy case

        Map.Entry<Double, String> e = suffixes.floorEntry(value);
        Double divideBy = e.getKey();
        String suffix = e.getValue();

        return formatter.format(new BigDecimal(value / divideBy).setScale(2, BigDecimal.ROUND_CEILING).doubleValue()) + suffix;
    }

    public static String format(double value) {
        return formatter.format(new BigDecimal(value).setScale(2, BigDecimal.ROUND_CEILING).doubleValue());
    }






    private KeyboardHelper() {

    }






    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
