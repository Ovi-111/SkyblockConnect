package com.ovi.skyblockconnect.foreground;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.ovi.skyblockconnect.R;
import com.ovi.skyblockconnect.services.AuctionSoldAlert;
import com.ovi.skyblockconnect.utilities.MiniTasks;

public class ForegroundService extends Service {
    public static boolean isServiceRunning;
    private final String TAG = "ForegroundService";
    private final String CHANNEL_ID = "ForegroundService";
    private Context context;
    private Handler handler;


    public ForegroundService() {
        Log.d(TAG, "constructor called");
        isServiceRunning = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (isServiceRunning) return;
        Log.d(TAG, "onCreate called");
        createNotificationChannel();
        isServiceRunning = true;
        context = getApplicationContext();
        SharedPreferences sp = this.getSharedPreferences("Data", MODE_PRIVATE);
        int currentDelay = sp.getInt("currentDelay", 2);

        long auctionTaskDelay = ((long) currentDelay * 60 * 1000);


        handler = new Handler();

        Runnable auctionTask = new Runnable() {
            @Override
            public void run() {
                new AuctionSoldAlert.AuctionStatus().execute(context);
                handler.postDelayed(this, auctionTaskDelay);
            }
        };
        handler.post(auctionTask);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");

        Intent settingsIntent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        settingsIntent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        settingsIntent.putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID);
        PendingIntent settingsPendingIntent = PendingIntent.getActivity(this, 0, settingsIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name) + " is Running")
                .setContentText("Click to hide this notification.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(settingsPendingIntent)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSound(null)
                .build();

        startForeground(MiniTasks.generateRandomNotificationId(), notification);
        return START_STICKY;
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String appName = getString(R.string.app_name);
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    appName,
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called");
        isServiceRunning = false;
        stopForeground(true);

        // call Receiver which will restart this service via a worker
        Intent broadcastIntent = new Intent(this, Receiver.class);
        sendBroadcast(broadcastIntent);

        super.onDestroy();
    }

}