package com.ovi.skyblockconnect.foreground;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ovi.skyblockconnect.activities.ButtonStatusManager;

public class ForegroundWorker extends Worker {
    private final Context context;
    private final String TAG = "Worker";

    public ForegroundWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        if (ForegroundService.isServiceRunning || !ButtonStatusManager.getButtonStatus("AHNotifierToggle", context, false))
            return Result.success();
        Intent intent = new Intent(this.context, ForegroundService.class);
        ContextCompat.startForegroundService(context, intent);

        return Result.success();
    }

    @Override
    public void onStopped() {
        Log.d(TAG, "onStopped called for: " + this.getId());
        super.onStopped();
    }
}