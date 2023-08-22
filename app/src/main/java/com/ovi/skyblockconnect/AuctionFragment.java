package com.ovi.skyblockconnect;


import static com.ovi.skyblockconnect.firebase.RemoteConfig.API_KEY;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ovi.skyblockconnect.adapters.AuctionAdapter;
import com.ovi.skyblockconnect.databinding.FragmentAuctionBinding;
import com.ovi.skyblockconnect.dialogs.AuctionItemDialog;
import com.ovi.skyblockconnect.models.AuctionModelClass;
import com.ovi.skyblockconnect.utilities.MiniTasks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AuctionFragment extends Fragment {
    SharedPreferences sp;
    private View view;
    private String UUID;
    private AuctionAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView TotalCoins;
    private TextView ClaimableCoinsText;
    private RecyclerView recyclerView;
    private List<AuctionModelClass> auctionList;
    private Context context;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        com.ovi.skyblockconnect.databinding.FragmentAuctionBinding binding = FragmentAuctionBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }


    public void onViewCreated(@NonNull View mView, Bundle savedInstanceState) {
        super.onViewCreated(mView, savedInstanceState);
        view = mView;

        context = getActivity();
        assert context != null;


        sp = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        UUID = sp.getString("UUID", null);

        AuctionItemDialog.itemDialogShowing = false;

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.auction_list_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        auctionList = new ArrayList<>();
        adapter = new AuctionAdapter(auctionList);
        recyclerView.setAdapter(adapter);


        loadAuctionList(context);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (MiniTasks.isOnline(context)) {
                auctionList.clear();
                new AuctionStatus(context).execute();
            } else {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(context, "Network connection is turned off.", Toast.LENGTH_SHORT).show();
            }
        });
        if (MiniTasks.isOnline(context)) {
            TextView UserName_Error_Text = view.findViewById(R.id.name_error_text);
            if (UUID != null) {
                UserName_Error_Text.setVisibility(View.GONE);
                new AuctionStatus(context).execute();
            } else {
                UserName_Error_Text.setVisibility(View.VISIBLE);
            }
        }
    }

    private void saveAuctionList(Context context, List<AuctionModelClass> auctionList, String TotalCoins, String ClaimableCoins) {
        sp = context.getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();


        editor.putString("totalCoins", TotalCoins);
        editor.putString("claimableCoins", ClaimableCoins);

        JSONArray jsonArray = new JSONArray();
        for (AuctionModelClass auction : auctionList) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("itemName", auction.getName_textview());
                jsonObject.put("itemLore", auction.getLore_textview());
                jsonObject.put("price", auction.getPrice_textview());
                jsonObject.put("status", auction.getStatus_textview());
                jsonObject.put("endTimer", auction.getEndTimer_textview());
                jsonArray.put(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        editor.putString("AuctionList", jsonArray.toString());
        editor.apply();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadAuctionList(Context context) {
        SharedPreferences sp = context.getSharedPreferences("Data", Context.MODE_PRIVATE);
        String auctionListJson = sp.getString("AuctionList", "");


        if (!auctionListJson.isEmpty()) {
            String CoinsIfSold = sp.getString("totalCoins", "");
            String ClaimableCoins = sp.getString("claimableCoins", "");

            TotalCoins = view.findViewById(R.id.total_coins);
            ClaimableCoinsText = view.findViewById(R.id.claimable_coins);
            TotalCoins.setText(CoinsIfSold);
            ClaimableCoinsText.setText(ClaimableCoins);

            if (auctionList == null) {
                auctionList = new ArrayList<>();
            } else {
                auctionList.clear();
            }

            try {
                JSONArray jsonArray = new JSONArray(auctionListJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String itemName = jsonObject.getString("itemName");
                    String itmLore = jsonObject.getString("itemLore");
                    long price = jsonObject.getLong("price");
                    String status = jsonObject.getString("status");
                    int endTimer = jsonObject.getInt("endTimer");
                    auctionList.add(new AuctionModelClass(context, itemName, itmLore, price, endTimer, status));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            view.findViewById(R.id.name_error_text).setVisibility(View.VISIBLE);
            view.findViewById(R.id.empty_text).setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();

        if (adapter.getItemCount() == 0 && view.findViewById(R.id.name_error_text).getVisibility() == View.GONE) {
            view.findViewById(R.id.empty_text).setVisibility(View.VISIBLE);
        }
    }

    private class AuctionStatus {
        private final Context context;
        int ClaimableCoins = 0;
        int CoinsIfSold = 0;
        String formattedClaimableCoins;
        String formattedCoinsIfSold;
        private final Handler handler = new Handler(Looper.getMainLooper()) {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void handleMessage(Message msg) {

                adapter.notifyDataSetChanged();
                if (auctionList == null || auctionList.isEmpty()) {
                    view.findViewById(R.id.empty_text).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.error_text).setVisibility(View.GONE);
                    view.findViewById(R.id.no_network_text).setVisibility(View.GONE);
                    view.findViewById(R.id.name_error_text).setVisibility(View.GONE);
                }
                TotalCoins = view.findViewById(R.id.total_coins);
                ClaimableCoinsText = view.findViewById(R.id.claimable_coins);
                TotalCoins.setText(formattedCoinsIfSold);
                ClaimableCoinsText.setText(formattedClaimableCoins);


                new Handler().postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 200);

            }
        };

        AuctionStatus(Context context) {
            this.context = context;
        }

        public void execute() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                if (MiniTasks.isOnline(context)) {
                    try {
                        String apiUrl = "https://api.hypixel.net/skyblock/auction?key=" + API_KEY + "&player=" + UUID;

                        URL url = new URL(apiUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }

                        String response = stringBuilder.toString();

                        JSONObject json = new JSONObject(response);
                        JSONArray auctions = json.getJSONArray("auctions");

                        if (auctionList == null) {
                            auctionList = new ArrayList<>();
                        } else {
                            auctionList.clear();
                        }
                        for (int i = 0; i < auctions.length(); i++) {
                            JSONObject auction = auctions.getJSONObject(i);
                            boolean claimed = auction.getBoolean("claimed");

                            if (!claimed) {
                                boolean bin = auction.getBoolean("bin");
                                if (bin) {
                                    String itemName = auction.getString("item_name");
                                    String tier = auction.getString("tier");
                                    String color = "black";

                                    switch (tier) {
                                        case "COMMON":
                                            color = "#D3D3D3";
                                            break;
                                        case "UNCOMMON":
                                            color = "#4ee44e";
                                            break;
                                        case "RARE":
                                            color = "#5555ff";
                                            break;
                                        case "EPIC":
                                            color = "#aa00aa";
                                            break;
                                        case "LEGENDARY":
                                            color = "#ffaa00";
                                            break;
                                        case "MYTHIC":
                                            color = "#ff55ff";
                                            break;
                                        case "DIVINE":
                                            color = "#55ffff";
                                            break;
                                        case "SPECIAL":
                                            color = "#ff5353";
                                            break;
                                        case "VERY SPECIAL":
                                            color = "#ff5555";
                                            break;
                                    }

                                    itemName = "<font color='" + color + "'>" + itemName + "</font><br>";
                                    String itemLore = auction.getString("item_lore");
                                    long startingBid = auction.getLong("starting_bid");
                                    boolean hasBids = auction.getJSONArray("bids").length() > 0;
                                    long endTimeMillis = auction.getLong("end");
                                    long currentTimeMillis = System.currentTimeMillis();
                                    int remainingTimeMillis = (int) (endTimeMillis - currentTimeMillis);

                                    String status = currentTimeMillis >= endTimeMillis && hasBids ? "Sold" :
                                            currentTimeMillis >= endTimeMillis ? "Expired" :
                                                    hasBids ? "Sold" : "Running";
                                    int highestBidAmount = hasBids ? auction.getJSONArray("bids").getJSONObject(0).getInt("amount") : 0;
                                    ClaimableCoins += highestBidAmount;
                                    CoinsIfSold += startingBid;
                                    auctionList.add(new AuctionModelClass(context, itemName, itemLore, startingBid, remainingTimeMillis, status));

                                }
                            }
                        }
                        formattedClaimableCoins = MiniTasks.formatCoins(ClaimableCoins);
                        formattedCoinsIfSold = MiniTasks.formatCoins(CoinsIfSold);
                        auctionList.sort(Comparator.comparing(AuctionModelClass::getEndTimer_textview));
                        handler.sendMessage(handler.obtainMessage(0));
                        saveAuctionList(context, auctionList, formattedCoinsIfSold, formattedClaimableCoins);
                    } catch (IOException e) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> {
                            view.findViewById(R.id.error_text).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.empty_text).setVisibility(View.GONE);
                            view.findViewById(R.id.no_network_text).setVisibility(View.GONE);
                            view.findViewById(R.id.name_error_text).setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);
                        });
                        e.printStackTrace();
                    } catch (JSONException e) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> {
                            view.findViewById(R.id.no_network_text).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.empty_text).setVisibility(View.GONE);
                            view.findViewById(R.id.error_text).setVisibility(View.GONE);
                            view.findViewById(R.id.name_error_text).setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);
                        });
                        throw new RuntimeException(e);
                    }
                }
            });
            executor.shutdown();
        }

    }

}