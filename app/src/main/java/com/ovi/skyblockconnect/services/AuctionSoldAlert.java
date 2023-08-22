package com.ovi.skyblockconnect.services;


import static com.ovi.skyblockconnect.firebase.RemoteConfig.API_KEY;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.ovi.skyblockconnect.MainActivity;
import com.ovi.skyblockconnect.R;
import com.ovi.skyblockconnect.utilities.MiniTasks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AuctionSoldAlert {
    private static int NOTIFICATION_ID;

    public static void notifyUser(Context context, String name, String price) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(notificationManager, context);
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("fragment", "user_auctions");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.drawable.auction_master_icon)
                .setContentTitle("Your auction of " + name + " got sold for  " + price + " coins.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOngoing(false)
                .setContentIntent(pendingIntent)
                .build();

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private static void createNotificationChannel(NotificationManager notificationManager, Context context) {
        NotificationChannel channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("default", "Auctions Sold Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Price of the sold auction.");
            channel.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.auction_sold), new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build());
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static class AuctionStatus {

        public void execute(Context context) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                if (MiniTasks.isOnline(context)) {
                    try {
                        NOTIFICATION_ID = MiniTasks.generateRandomNotificationId();
                        SharedPreferences sp = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
                        String UUID = sp.getString("UUID", null);

                        if (UUID != null && !UUID.isEmpty()) {
                            String apiUrl = "https://api.hypixel.net/skyblock/auction?key=" + API_KEY + "&player=" + UUID;
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url(apiUrl)
                                    .build();

                            try (Response response = client.newCall(request).execute()) {
                                String responseBody = response.body().string();
                                JSONObject json = new JSONObject(responseBody);
                                SharedPreferences auctionOldData = context.getSharedPreferences("AuctionOldData", Context.MODE_PRIVATE);
                                JSONArray auctions = json.getJSONArray("auctions");

                                for (int i = 0; i < auctions.length(); i++) {
                                    JSONObject auction = auctions.getJSONObject(i);
                                    int highestBidAmount = auction.getInt("highest_bid_amount");
                                    boolean claimed = auction.getBoolean("claimed");

                                    if (highestBidAmount > 0 && !claimed) {
                                        String itemId = auction.getString("_id");
                                        boolean hasEntry = auctionOldData.contains(itemId);

                                        if (!hasEntry) {
                                            int highest_bid = auction.getInt("highest_bid_amount");
                                            String item_name = auction.getString("item_name");
                                            String item_price = MiniTasks.formatCoins(highest_bid);

                                            SharedPreferences.Editor editor = auctionOldData.edit();
                                            editor.remove(itemId);
                                            notifyUser(context, item_name, item_price);
                                            editor.putBoolean(itemId, true);
                                            editor.apply();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException ignored) {
                        // Handle or log the exception
                    }
                }
            });
            executor.shutdown();
        }
    }
}
