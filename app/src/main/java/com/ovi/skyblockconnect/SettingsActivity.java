package com.ovi.skyblockconnect;

import static com.ovi.skyblockconnect.firebase.RemoteConfig.DISCORD_URL;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.ovi.skyblockconnect.activities.ButtonStatusManager;
import com.ovi.skyblockconnect.dialogs.SetUsernameDialog;
import com.ovi.skyblockconnect.foreground.ForegroundService;

public class SettingsActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        context = this;

        MaterialButton GoBack = findViewById(R.id.go_back);
        MaterialButton SetUsername = findViewById(R.id.username_set);
        MaterialButton DiscordJoin = findViewById(R.id.DiscordButton);


        GoBack.setOnClickListener(view -> onBackPressed());

        SetUsername.setOnClickListener(view -> SetUsernameDialog.showDialog(context));

        SharedPreferences sp = this.getSharedPreferences("Data", MODE_PRIVATE);
        AutoCompleteTextView autoCompleteDelayTextView;
        String currentDelay = sp.getInt("currentDelay", 2) + " minutes";

        autoCompleteDelayTextView = findViewById(R.id.foregroundDelay);
        autoCompleteDelayTextView.setText(currentDelay);
        String[] delay_array = getResources().getStringArray(R.array.delay_array);
        ArrayAdapter<String> delayArrayAdapter = new ArrayAdapter<>(context,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, delay_array);
        autoCompleteDelayTextView.setAdapter(delayArrayAdapter);
        autoCompleteDelayTextView.setOnItemClickListener((adapterView, view, i, l) -> {
            int delay = Integer.parseInt(adapterView.getItemAtPosition(i).toString().replace(" minutes", ""));
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("currentDelay", delay);
            editor.apply();
        });

        DiscordJoin.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(DISCORD_URL));
            startActivity(intent);
        });


        AppCompatToggleButton AHNotifierToggle = findViewById(R.id.AHNotifierToggle);
        AHNotifierToggle.setChecked(ButtonStatusManager.getButtonStatus("AHNotifierToggle", context, false));
        AHNotifierToggle.setOnClickListener(view -> {
            ButtonStatusManager.saveButtonStatus("AHNotifierToggle", AHNotifierToggle.isChecked(), context);

            if (AHNotifierToggle.isChecked()) {
                if (!ForegroundService.isServiceRunning) {
                    startService();
                    notificationPermission();
                }
            } else if (!AHNotifierToggle.isChecked()) {

                if (ForegroundService.isServiceRunning) {
                    Intent serviceIntent = new Intent(this, ForegroundService.class);
                    stopService(serviceIntent);
                }
            }
        });
    }




    private void notificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void startService() {
        if (!ForegroundService.isServiceRunning) {
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }
}
