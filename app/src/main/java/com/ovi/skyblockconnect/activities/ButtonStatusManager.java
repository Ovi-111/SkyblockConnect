package com.ovi.skyblockconnect.activities;

import android.content.Context;
import android.content.SharedPreferences;

public class ButtonStatusManager {
    public static void saveButtonStatus(String buttonId, boolean status, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("ButtonState", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(buttonId, status);
        editor.apply();
    }

    public static boolean getButtonStatus(String buttonId, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("ButtonState", Context.MODE_PRIVATE);
        return preferences.getBoolean(buttonId, true);
    }

    public static boolean getButtonStatus(String buttonId, Context context, Boolean state) {
        SharedPreferences preferences = context.getSharedPreferences("ButtonState", Context.MODE_PRIVATE);
        return preferences.getBoolean(buttonId, state);
    }
}
