package com.ovi.skyblockconnect.foreground;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.ovi.skyblockconnect.activities.ButtonStatusManager;

public class Receiver extends BroadcastReceiver {


    private String TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        if (ForegroundService.isServiceRunning || !ButtonStatusManager.getButtonStatus("AHNotifierToggle", context, false))
            return;

        // We are starting MyService via a worker and not directly because since Android 7
        // (but officially since Lollipop!), any process called by a BroadcastReceiver
        // (only manifest-declared receiver) is run at low priority and hence eventually
        // killed by Android.
        WorkManager workManager = WorkManager.getInstance(context);
        OneTimeWorkRequest startServiceRequest = new OneTimeWorkRequest.Builder(ForegroundWorker.class)
                .build();
        workManager.enqueue(startServiceRequest);
    }
}