package com.ovi.skyblockconnect.utilities;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeFormatChanger {
    @SuppressLint("DefaultLocale")
    public static String convertTimestamp(long timestampString) {
        if (timestampString < 0) {
            return "Ended";
        }
        long timestamp = Long.parseLong(String.valueOf(timestampString));
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(timestamp * 1000);
        int days = calendar.get(Calendar.DAY_OF_YEAR) - 1;
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        String timeString = "";
        if (days > 0) {
            timeString += String.format("%dd ", days);
        }
        if (hours > 0) {
            timeString += String.format("%dh ", hours);
        }
        if (minutes > 0) {
            timeString += String.format("%dm ", minutes);
        }
        if (seconds > 0) {
            timeString += String.format("%ds", seconds);
        }
        return timeString.trim();
    }

    @SuppressLint("DefaultLocale")
    public static String convertShortTimestamp(long timestampString) {
        if (timestampString < 0) {
            return "Ended";
        }
        long timestamp = Long.parseLong(String.valueOf(timestampString));
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(timestamp * 1000);
        int days = calendar.get(Calendar.DAY_OF_YEAR) - 1;
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        String outputString = "";
        if (days > 0) {
            outputString += String.format("%dd ", days);
            if (outputString.split(" ").length >= 2) {
                return outputString.trim();
            }
        }
        if (hours > 0 || !outputString.isEmpty()) {
            outputString += String.format("%dh ", hours);
            if (outputString.split(" ").length >= 2) {
                return outputString.trim();
            }
        }
        if (minutes > 0 || !outputString.isEmpty()) {
            outputString += String.format("%dm ", minutes);
            if (outputString.split(" ").length >= 2) {
                return outputString.trim();
            }
        }
        outputString += String.format("%ds", seconds);
        return outputString;
    }

    public static String getTimeAgo(long timestamp) {
        final long SECOND_MILLIS = 1000;
        final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
        final long HOUR_MILLIS = 60 * MINUTE_MILLIS;

        long timeElapsed = System.currentTimeMillis() - timestamp;

        if (timeElapsed < MINUTE_MILLIS) {
            return "just now";
        } else if (timeElapsed < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (timeElapsed < 50 * MINUTE_MILLIS) {
            return (timeElapsed / MINUTE_MILLIS) + " minutes ago";
        } else if (timeElapsed < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (timeElapsed < 24 * HOUR_MILLIS) {
            return (timeElapsed / HOUR_MILLIS) + " hours ago";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy 'at' h:mm a");
            String dateString = formatter.format(new Date(timestamp));
            return dateString;
        }
    }


}
