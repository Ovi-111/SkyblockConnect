package com.ovi.skyblockconnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.snackbar.Snackbar;
import com.ovi.skyblockconnect.databinding.ActivityMainBinding;
import com.ovi.skyblockconnect.foreground.ForegroundService;
import com.ovi.skyblockconnect.foreground.ForegroundWorker;
import com.ovi.skyblockconnect.utilities.MiniTasks;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


        if (!MiniTasks.isOnline(this)) {
            Snackbar.make(findViewById(android.R.id.content),
                    "No network connection. Showing old data.",
                    Snackbar.LENGTH_LONG).show();
        }

        if (getIntent().getStringExtra("fragment") != null) {
            String fragmentToLaunch = getIntent().getStringExtra("fragment");
            if (fragmentToLaunch.equals("main")) {
                navController.navigate(R.id.MainFragment);
                return;
            }
            if (fragmentToLaunch.equals("user_auctions")) {
                navController.navigate(R.id.AuctionFragment);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settings);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void startService() {
        Log.d(TAG, "startService called");
        if (!ForegroundService.isServiceRunning) {
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }

    public void stopService() {
        Log.d(TAG, "stopService called");
        if (ForegroundService.isServiceRunning) {
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            stopService(serviceIntent);
        }
    }

    public void startServiceViaWorker() {
        Log.d(TAG, "startServiceViaWorker called");
        String UNIQUE_WORK_NAME = "StartForegroundServiceViaWorker";
        WorkManager workManager = WorkManager.getInstance(this);

        // As per Documentation: The minimum repeat interval that can be defined is 15 minutes
        // (same as the JobScheduler API), but in practice 15 doesn't work. Using 16 here
        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(
                        ForegroundWorker.class,
                        16,
                        TimeUnit.MINUTES)
                        .build();

        // to schedule a unique work, no matter how many times app is opened i.e. startServiceViaWorker gets called
        // do check for AutoStart permission
        workManager.enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        stopService();
        super.onDestroy();
    }
}