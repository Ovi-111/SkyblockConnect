package com.ovi.skyblockconnect;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ovi.skyblockconnect.API.BazaarApiResponse;
import com.ovi.skyblockconnect.adapters.BazaarAdapter;
import com.ovi.skyblockconnect.database.BazaarContract;
import com.ovi.skyblockconnect.database.BazaarDbHelper;
import com.ovi.skyblockconnect.databinding.FragmentBazaarBinding;
import com.ovi.skyblockconnect.models.BazaarModelClass;
import com.ovi.skyblockconnect.utilities.MiniTasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;


public class BazaarFragment extends Fragment {
    public static boolean isGettingBazaarData = false;
    private View view;

    private Context context;
    private List<BazaarModelClass> bazaarList;
    private BazaarAdapter adapter;
    private BazaarDbHelper DbHelper;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String searchText = "";
    private String sortBy;
    private String sortOrderASC;
    private int CurrentPage = 0;


    public BazaarFragment() {
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        com.ovi.skyblockconnect.databinding.FragmentBazaarBinding binding = FragmentBazaarBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }


    public void onViewCreated(@NonNull View mView, Bundle savedInstanceState) {
        super.onViewCreated(mView, savedInstanceState);
        view = mView;
        context = getActivity();

        isGettingBazaarData = false;
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        RecyclerView recyclerView = view.findViewById(R.id.bz_item_list_recyclerview);
        DbHelper = new BazaarDbHelper(context);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        bazaarList = new ArrayList<>();
        adapter = new BazaarAdapter(context, bazaarList);
        recyclerView.setAdapter(adapter);

        SharedPreferences sp = context.getSharedPreferences("Data", Context.MODE_PRIVATE);

        Spinner bazaarSpinner = view.findViewById(R.id.bazaarSpinner);
        setupSpinner(bazaarSpinner, "bazaarSortBy", sp);

        Spinner bazaarSortSpinner = view.findViewById(R.id.bazaarSortSpinner);
        setupSpinner(bazaarSortSpinner, "bazaarSortOrder", sp);


        SortByString();
        loadItems(CurrentPage, searchText);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE && !isGettingBazaarData) {
                    loadItems(CurrentPage + 1, searchText);
                }
            }
        });

        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                CurrentPage = 0;
                adapter.clearList();
                searchText = newText;
                loadItems(CurrentPage, searchText);
                return true;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isGettingBazaarData) {
                System.out.println("SKIPPED");
                return;
            }
            if (MiniTasks.isOnline(context)) {
                bazaarList.clear();
                new GetBazaarItemsTask(context).execute();
            } else {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(context, "Network connection is turned off.", Toast.LENGTH_SHORT).show();
            }
        });

        if (MiniTasks.isOnline(context)) {
            new GetBazaarItemsTask(context).execute();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadItems(int page, String search) {
        isGettingBazaarData = true;
        bazaarList.addAll(DbHelper.getItems(context, page, search, sortBy, sortOrderASC));
        if (bazaarList.isEmpty() && searchText.isEmpty()) {
            if (!MiniTasks.isOnline(context)) {
                TextView emptyTextView = view.findViewById(R.id.empty_text);
                emptyTextView.setVisibility(View.VISIBLE);
            }
            if (MiniTasks.isOnline(context)) {
                ProgressBar loadingData = view.findViewById(R.id.loading_data);
                loadingData.setVisibility(View.VISIBLE);
            }
        }
        adapter.notifyDataSetChanged();
        CurrentPage = page;
        swipeRefreshLayout.setRefreshing(false);
        isGettingBazaarData = false;
    }

    private void SortByString() {
        SharedPreferences sp = context.getSharedPreferences("Data", Context.MODE_PRIVATE);
        int sortByInt = sp.getInt("bazaarSortBy", 0);
        int sortOrderInt = sp.getInt("bazaarSortOrder", 0);
        if (sortByInt == 0) {
            sortBy = BazaarContract.BazaarEntry.COLUMN_ITEM_NAME;
        } else if (sortByInt == 1) {
            sortBy = BazaarContract.BazaarEntry.COLUMN_BUY_PRICE;
        } else if (sortByInt == 2) {
            sortBy = BazaarContract.BazaarEntry.COLUMN_SELL_PRICE;
        } else {
            sortBy = BazaarContract.BazaarEntry.COLUMN_ITEM_NAME;
        }

        if (sortOrderInt == 0) {
            sortOrderASC = "ASC";
        } else if (sortOrderInt == 1) {
            sortOrderASC = "DESC";
        } else {
            sortOrderASC = "ASC";
        }
    }

    private void setupSpinner(Spinner spinner, String key, SharedPreferences sp) {
        int selection = sp.getInt(key, 0);
        spinner.setSelection(selection);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(key, position);
                editor.apply();
                bazaarList.clear();
                SortByString();
                loadItems(0, searchText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
    }


    public interface BazaarApiService {
        @GET("skyblock/bazaar")
        Call<BazaarApiResponse> getBazaarData();
    }
    private class GetBazaarItemsTask {
        private final Context context;
        private final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                loadItems(0, searchText);
                if (!bazaarList.isEmpty()) {
                    ProgressBar loadingData = view.findViewById(R.id.loading_data);
                    loadingData.setVisibility(View.GONE);
                    TextView emptyTextView = view.findViewById(R.id.empty_text);
                    if (emptyTextView.getVisibility() == View.VISIBLE) {
                        emptyTextView.setVisibility(View.GONE);
                    }
                }
            }
        };

        GetBazaarItemsTask(Context context) {
            this.context = context;
        }

        public void execute() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://api.hypixel.net/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                BazaarApiService service = retrofit.create(BazaarApiService.class);

                try {
                    Response<BazaarApiResponse> response = service.getBazaarData().execute();
                    if (response.isSuccessful()) {
                        BazaarApiResponse bazaarApiResponse = response.body();
                        if (bazaarApiResponse != null && bazaarApiResponse.isSuccess()) {
                            Map<String, BazaarApiResponse.Product> products = bazaarApiResponse.getProducts();
                            BazaarDbHelper dbHelper = new BazaarDbHelper(context);
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            db.beginTransaction();
                            //db.execSQL("DELETE FROM " + BazaarContract.BazaarEntry.TABLE_NAME);
                            try {
                                for (Map.Entry<String, BazaarApiResponse.Product> entry : products.entrySet()) {
                                    String id = entry.getKey().replace(":", "_");
                                    @SuppressLint("DiscouragedApi") int resourceId = context.getResources().getIdentifier(id, "string", context.getPackageName());
                                    String itemName = (resourceId != 0) ? context.getString(resourceId) : null;


                                    BazaarApiResponse.Product product = entry.getValue();
                                    BazaarApiResponse.QuickStatus quickStatus = product.getQuickStatus();
                                    double buyPrice = quickStatus.getBuyPrice();
                                    double sellPrice = quickStatus.getSellPrice();
                                    if (itemName != null && !itemName.isEmpty()) {

                                        ContentValues values = new ContentValues();
                                        values.put(BazaarContract.BazaarEntry.COLUMN_ITEM_NAME, itemName);
                                        values.put(BazaarContract.BazaarEntry.COLUMN_BUY_PRICE, buyPrice);
                                        values.put(BazaarContract.BazaarEntry.COLUMN_SELL_PRICE, sellPrice);

                                        Cursor cursor = db.query(BazaarContract.BazaarEntry.TABLE_NAME,
                                                null,
                                                BazaarContract.BazaarEntry.COLUMN_ITEM_NAME + " = ?",
                                                new String[]{itemName},
                                                null,
                                                null,
                                                null);

                                        if (cursor != null && cursor.getCount() > 0) {
                                            db.update(BazaarContract.BazaarEntry.TABLE_NAME,
                                                    values,
                                                    BazaarContract.BazaarEntry.COLUMN_ITEM_NAME + " = ?",
                                                    new String[]{itemName});
                                        } else {
                                            db.insert(BazaarContract.BazaarEntry.TABLE_NAME, null, values);
                                        }

                                        if (cursor != null) {
                                            cursor.close();
                                        }
                                    }
                                }

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                                db.close();
                            }
                        }
                    }
                    handler.sendMessage(handler.obtainMessage(0));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            executor.shutdown();
        }
    }
}