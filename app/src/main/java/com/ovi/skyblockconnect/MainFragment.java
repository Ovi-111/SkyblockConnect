package com.ovi.skyblockconnect;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ovi.skyblockconnect.databinding.FragmentMainBinding;
import com.ovi.skyblockconnect.dialogs.MayorPerksDialog;
import com.ovi.skyblockconnect.firebase.RemoteConfig;
import com.ovi.skyblockconnect.utilities.HypixelTimers;
import com.ovi.skyblockconnect.utilities.MiniTasks;
import com.ovi.skyblockconnect.utilities.TimeFormatChanger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainFragment extends Fragment {


    private FragmentMainBinding binding;
    private Context context;
    private Activity activity;
    private SharedPreferences sp;
    private View view;
    private String currentMayorName;
    private String currentMayorPerks;
    private String nextMayorPerks;
    private String nextMayorName;

    private final Handler handler = new Handler();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View mView, Bundle savedInstanceState) {
        super.onViewCreated(mView, savedInstanceState);
        view = requireView();
        context = getActivity();
        activity = getActivity();

        MayorPerksDialog.mayorDialogIsShowing = false;


        setupClickListeners();
        RemoteConfig.firebaseRemoteConfigFetch(context);
        GameTime();
        mayorDataLoad();
        setupMayorCardListeners();

        if (MiniTasks.isOnline(context)) {
            new GetDataTask(context).execute();
        }
    }


    private void setupClickListeners() {
        binding.userAuctions.setOnClickListener(view -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_MainFragment_to_AuctionFragment));

        binding.bazaarVisit.setOnClickListener(view -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_MainFragment_to_BazaarFragment));
    }

    private void setupMayorCardListeners() {
        CardView currentMayorCard = view.findViewById(R.id.current_mayor);
        CardView nextMayorCard = view.findViewById(R.id.next_mayor);

        currentMayorCard.setOnClickListener(view -> showMayorPerksDialog(currentMayorPerks, currentMayorName, "Current"));
        nextMayorCard.setOnClickListener(view -> showNextMayorDialog());
    }

    private void showMayorPerksDialog(String perks, String name, String type) {
        if (perks == null) {
            Toast.makeText(context, "Getting data from Hypixel.", Toast.LENGTH_SHORT).show();
        } else {
            MayorPerksDialog.showDialog(context, perks, name, type);
        }
    }

    private void showNextMayorDialog() {
        if (nextMayorPerks == null || nextMayorPerks.isEmpty()) {
            if (Objects.equals(nextMayorName, "Unknown")) {
                HypixelTimers electionStarts = HypixelTimers.getEventsList()[9];
                String electionStartTimer = TimeFormatChanger.convertTimestamp((int) electionStarts.getEventTimer());
                Toast.makeText(context, "Elections starts in: " + electionStartTimer, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Getting data from Hypixel.", Toast.LENGTH_SHORT).show();
            }
        } else {
            showMayorPerksDialog(nextMayorPerks, nextMayorName, "Next");
        }
    }

    private void GameTime() {

        final TextView sb_time = view.findViewById(R.id.sb_time);
        final Handler handler = new Handler();
        final Runnable updateTime = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                String timeString = HypixelTimers.getGameTime();
                sb_time.setText("Skyblock Time: " + timeString);
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateTime);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        handler.removeCallbacksAndMessages(null);
    }

    private void mayorDataSet() {
        activity.runOnUiThread(() -> {
            setMayorInfo(R.id.current_mayor_name, R.id.current_mayor_skin, currentMayorName);
            setMayorInfo(R.id.next_mayor_name, R.id.next_mayor_skin, nextMayorName);
        });
    }

    private void setMayorInfo(int nameViewId, int skinViewId, String mayorName) {
        if (mayorName != null && !mayorName.isEmpty()) {
            TextView mayorNameView = view.findViewById(nameViewId);
            ImageView mayorSkinView = view.findViewById(skinViewId);
            mayorNameView.setText(mayorName);

            int mayorDrawableId = getResources().getIdentifier(mayorName.toLowerCase(), "drawable", context.getPackageName());
            mayorSkinView.setImageResource(mayorDrawableId);
        }
    }

    private void mayorDataSave() {
        sp = context.getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("CurrentMayorName", currentMayorName);
        editor.putString("CurrentMayorPerks", currentMayorPerks);
        editor.putString("NextMayorName", nextMayorName);
        editor.putString("NextMayorPerks", nextMayorPerks);
        editor.apply();
    }

    private void mayorDataLoad() {
        sp = context.getSharedPreferences("Data", Context.MODE_PRIVATE);
        currentMayorName = sp.getString("CurrentMayorName", null);
        if (currentMayorName != null) {
            nextMayorName = sp.getString("NextMayorName", null);
            currentMayorPerks = sp.getString("CurrentMayorPerks", null);
            nextMayorPerks = sp.getString("NextMayorPerks", null);
            mayorDataSet();
        }
    }

    public class GetDataTask {
        private final Context context;

        public GetDataTask(Context context) {
            this.context = context;
        }

        public void execute() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                if (MiniTasks.isOnline(context)) {
                    try {
                        URL mayorUrl = new URL("https://api.hypixel.net/resources/skyblock/election");
                        HttpURLConnection connection = (HttpURLConnection) mayorUrl.openConnection();
                        connection.setRequestMethod("GET");

                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder content = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                        in.close();
                        connection.disconnect();

                        JsonElement jsonTree = JsonParser.parseString(content.toString());
                        JsonObject jsonObject = jsonTree.getAsJsonObject();

                        currentMayorPerks = "";
                        nextMayorPerks = "";

                        currentMayorName = jsonObject.get("mayor").getAsJsonObject().get("name").getAsString();
                        JsonArray perksData = jsonObject.get("mayor").getAsJsonObject().get("perks").getAsJsonArray();

                        for (JsonElement perk : perksData) {
                            String name = perk.getAsJsonObject().get("name").getAsString();
                            String description = perk.getAsJsonObject().get("description").getAsString();
                            currentMayorPerks += "<font color='red'>Name: </font>" +
                                    "<font color='#19bf8d'>" + name + "</font>" + "<br>" +
                                    "<font color='#199ebf'>Description: </font>" +
                                    "<font color='yellow'>" + description + "</font>" + "<br>" + "<br>";
                            currentMayorPerks = currentMayorPerks.replaceAll("§[\\da-fk-or]", "");
                        }

                        if (jsonObject.has("current")) {
                            JsonArray candidates = jsonObject.get("current").getAsJsonObject().get("candidates").getAsJsonArray();
                            int highestVote = 0;
                            for (JsonElement candidate : candidates) {
                                int totalVotes = candidate.getAsJsonObject().get("votes").getAsInt();
                                if (totalVotes > highestVote) {
                                    highestVote = totalVotes;
                                    nextMayorName = candidate.getAsJsonObject().get("name").getAsString();
                                }
                            }

                            for (JsonElement candidate : candidates) {
                                if (candidate.getAsJsonObject().get("name").getAsString().equals(nextMayorName)) {
                                    JsonArray nextPerksData = candidate.getAsJsonObject().get("perks").getAsJsonArray();

                                    for (JsonElement perk : nextPerksData) {
                                        String name = perk.getAsJsonObject().get("name").getAsString();
                                        String description = perk.getAsJsonObject().get("description").getAsString();
                                        nextMayorPerks += "<font color='red'>Name: </font>" +
                                                "<font color='#19bf8d'>" + name + "</font>" + "<br>" +
                                                "<font color='#199ebf'>Description: </font>" +
                                                "<font color='yellow'>" + description + "</font>" + "<br>" + "<br>";
                                        nextMayorPerks = nextMayorPerks.replaceAll("§[\\da-fk-or]", "");
                                    }
                                }
                            }
                        } else {
                            nextMayorName = "Unknown";
                        }
                        mayorDataSet();
                        mayorDataSave();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            executor.shutdown();
        }
    }
}