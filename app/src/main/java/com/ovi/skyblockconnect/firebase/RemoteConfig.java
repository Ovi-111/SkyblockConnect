package com.ovi.skyblockconnect.firebase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.ovi.skyblockconnect.R;

public class RemoteConfig {
    public static String UPDATE_URL;
    public static String API_KEY;
    public static String DISCORD_URL;

    public static void firebaseRemoteConfigFetch(Context context) {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setFetchTimeoutInSeconds(5)
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                API_KEY = remoteConfig.getString("API_KEY");
                DISCORD_URL = remoteConfig.getString("discord_url");;
                UPDATE_URL = remoteConfig.getString("update_url");

                if(Integer.parseInt(remoteConfig.getString("new_version_code")) > getCurrentVersionCode(context)){
                    showUpdateDialog(context);
                }
            }
        });
    }

    private static void showUpdateDialog(Context context) {
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("New Update Available")
                .setMessage("Update Now")
                .setPositiveButton("Update", (dialogInterface, i) -> {
                    try {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(UPDATE_URL)));
                    } catch (Exception e) {
                        Toast.makeText(context, "Something went wrong. Try again later.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
        dialog.setCancelable(false);
    }
    private static int getCurrentVersionCode(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (Exception e) {
            Log.d("RemoteConfig", e.getMessage());
        }
        return packageInfo.versionCode;
    }
}
