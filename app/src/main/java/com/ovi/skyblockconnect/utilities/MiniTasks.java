package com.ovi.skyblockconnect.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MiniTasks {

    @SuppressLint("DefaultLocale")
    public static String formatCoins(Number coins) {
        double coinsValue = coins.doubleValue();
        if (coinsValue >= 1000000000) {
            return String.format("%.2fB", coinsValue / 1000000000.0);
        } else if (coinsValue >= 1000000) {
            return String.format("%.1fM", coinsValue / 1000000.0);
        } else if (coinsValue >= 1000) {
            int num = (int) (coinsValue / 1000.0);
            return String.format(num * 1000 == coinsValue ? "%.0fK" : "%.1fK", coinsValue / 1000.0);
        } else {
            return String.format("%.0f", coinsValue);
        }
    }

    public static String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM h:mm a", Locale.getDefault());
        String formattedDate = sdf.format(date);
        return formattedDate.toUpperCase(Locale.getDefault());
    }

    public static int generateRandomNotificationId() {
        Random random = new Random();
        return Math.abs(random.nextInt());
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @SuppressLint("DefaultLocale")
    public static String formatPriceWithComma(Number price) {
        if (price instanceof Integer || price instanceof Long) {
            return String.format("%,d", price.longValue());
        } else if (price instanceof Float || price instanceof Double) {
            return String.format("%,.2f", price.doubleValue());
        } else {
            return "N/A";
        }
    }
}
