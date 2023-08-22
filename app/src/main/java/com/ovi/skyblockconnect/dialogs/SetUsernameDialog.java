package com.ovi.skyblockconnect.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.ovi.skyblockconnect.MainActivity;
import com.ovi.skyblockconnect.R;
import com.ovi.skyblockconnect.utilities.MiniTasks;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SetUsernameDialog extends DialogFragment {

    public static AlertDialog dialog;
    public static AlertDialog.Builder builder;
    public static SharedPreferences sp;
    @SuppressLint("StaticFieldLeak")
    public static Context context;


    public static void showDialog(Context context) {
        SetUsernameDialog.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_username, null);
        builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);


        dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        dialog.show();


        EditText username_input = dialogView.findViewById(R.id.username_input);
        Button submit_button = dialogView.findViewById(R.id.set_button);
        Button dismiss_button = dialogView.findViewById(R.id.dismiss_button);


        sp = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);


        String old_username = sp.getString("username", null);
        username_input.setText(old_username);


        username_input.requestFocus();

        submit_button.setOnClickListener(v -> {
            String username = username_input.getText().toString();
            sp = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("username", username);
            editor.apply();

            if (username.equals("")) {
                Toast.makeText(context.getApplicationContext(), "Please enter your Minecraft username.", Toast.LENGTH_SHORT).show();
            } else {
                if (MiniTasks.isOnline(context)) {
                    new UUIDConversionTask(context, username).execute();
                    dialog.dismiss();
                }
                if (!MiniTasks.isOnline(context)) {
                    Toast.makeText(context, "Can't fetch uuid from server. Turn on the internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });


        dismiss_button.setOnClickListener(v -> dialog.dismiss());
    }

    public static class UUIDConversionTask {
        String username;
        String PlayerUUID = "";
        Context context;
        private final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (PlayerUUID != null && !PlayerUUID.isEmpty()) {
                    SharedPreferences sp = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("UUID", PlayerUUID);
                    editor.apply();
                    Toast.makeText(context.getApplicationContext(), "Username and UUID added successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context.getApplicationContext(), "Unable to add username.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        UUIDConversionTask(Context context, String username) {
            this.context = context;
            this.username = username;
        }

        public void execute() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String uuidUrl = "https://api.mojang.com/users/profiles/minecraft/" + UUIDConversionTask.this.username;
                        URL url = new URL(uuidUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Content-Type", "application/json");

                        InputStream inputStream = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder stringBuilder = new StringBuilder();

                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line);
                        }

                        bufferedReader.close();
                        inputStream.close();

                        String jsonString = stringBuilder.toString();
                        JSONObject jsonObject = new JSONObject(jsonString);

                        PlayerUUID = jsonObject.getString("id");
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                    handler.sendEmptyMessage(0);
                }
            });
            executor.shutdown();
        }
    }

}
