package com.example.calorite;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryListActivity extends AppCompatActivity {

    private RecyclerView rvHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_list);

        ImageView btnBack = findViewById(R.id.btnBackHistoryList);
        btnBack.setOnClickListener(v -> finish());

        rvHistoryList = findViewById(R.id.rvHistoryList);
        rvHistoryList.setLayoutManager(new LinearLayoutManager(this));

        loadHistoryData();
    }

    private void loadHistoryData() {
        AppDatabase db = AppDatabase.getInstance(this);
        SharedPreferences prefs = getSharedPreferences("CaloriteSettings", MODE_PRIVATE);
        int targetCal = prefs.getInt("TARGET_CALORIE", 2000);

        List<String> savedDates = db.foodDao().getUniqueDates();
        List<HistorySummary> historySummaries = new ArrayList<>();

        for (String date : savedDates) {
            int totalCalForDate = db.foodDao().getTotalCaloriesByDate(date);
            int percentForDate = (int) (((float) totalCalForDate / targetCal) * 100);
            String lastImage = db.foodDao().getLastImageByDate(date);

            historySummaries.add(new HistorySummary(date, String.valueOf(totalCalForDate), percentForDate + "%", lastImage));
        }

        HistoryListAdapter adapter = new HistoryListAdapter(this, historySummaries);
        rvHistoryList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryData();
    }
}