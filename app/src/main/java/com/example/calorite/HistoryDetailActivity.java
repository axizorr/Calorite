package com.example.calorite; // Sesuaikan

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryDetailActivity extends AppCompatActivity {
    private EditText etDailyNotes;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        // 1. TERIMA TANGGAL DARI INTENT TERLEBIH DAHULU (PENTING!)
        selectedDate = getIntent().getStringExtra("HISTORY_DATE");
        if (selectedDate == null) selectedDate = "Unknown Date";

        TextView tvDetailDateTitle = findViewById(R.id.tvDetailDateTitle);
        tvDetailDateTitle.setText(selectedDate);

        // 2. SETUP DRAWABLE PIRING & PROTEIN
        ImageView ivCal = findViewById(R.id.ivProgressCalorie);
        if (ivCal != null) {
            android.graphics.drawable.Drawable drawableCal = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.img_plate_cal);
            if (drawableCal != null) {
                android.graphics.drawable.ClipDrawable clipCal = new android.graphics.drawable.ClipDrawable(drawableCal, android.view.Gravity.LEFT, android.graphics.drawable.ClipDrawable.HORIZONTAL);
                ivCal.setImageDrawable(clipCal);
            }
        }

        ImageView ivPro = findViewById(R.id.ivProgressProtein);
        if (ivPro != null) {
            android.graphics.drawable.Drawable drawablePro = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.img_plate_pro);
            if (drawablePro != null) {
                android.graphics.drawable.ClipDrawable clipPro = new android.graphics.drawable.ClipDrawable(drawablePro, android.view.Gravity.LEFT, android.graphics.drawable.ClipDrawable.HORIZONTAL);
                ivPro.setImageDrawable(clipPro);
            }
        }

        // 3. TOMBOL BACK
        ImageView btnBackHistory = findViewById(R.id.btnBackHistory);
        btnBackHistory.setOnClickListener(v -> finish());

        // 4. AMBIL DATA & PASANG ADAPTER RECYCLERVIEW
        AppDatabase db = AppDatabase.getInstance(this);
        List<FoodRecord> dailyFoods = db.foodDao().getFoodsByDate(selectedDate);

        RecyclerView rvDailyFoods = findViewById(R.id.rvDailyFoods);
        rvDailyFoods.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        DailyFoodAdapter adapter = new DailyFoodAdapter(this, dailyFoods);
        rvDailyFoods.setAdapter(adapter);

        // 5. INISIALISASI & LOAD NOTES
        etDailyNotes = findViewById(R.id.etDailyNotes);
        DailyNote savedNote = db.noteDao().getNoteByDate(selectedDate);
        if (savedNote != null && etDailyNotes != null) {
            etDailyNotes.setText(savedNote.noteText);
        }

        // 6. HITUNG KALORI & UPDATE TEKS/PIRING (Dipanggil paling akhir setelah selectedDate siap)
        refreshDailyData();
    }

    // Fungsi bantu untuk mengambil target kalori (Sama seperti di MainActivity)
    private int getTargetCalorie() {
        SharedPreferences sharedPreferences = getSharedPreferences("CaloriteSettings", MODE_PRIVATE);
        return sharedPreferences.getInt("TARGET_CALORIE", 2000);
    }
    private int getTargetProtein() {
        SharedPreferences sharedPreferences = getSharedPreferences("CaloriteSettings", MODE_PRIVATE);
        return sharedPreferences.getInt("TARGET_PROTEIN", 100);
    }
    public void refreshDailyData() {
        AppDatabase db = AppDatabase.getInstance(this);
        java.util.List<FoodRecord> updatedFoods = db.foodDao().getFoodsByDate(selectedDate);

        int totalCal = 0;
        int totalProtein = 0;
        for (FoodRecord food : updatedFoods) {
            totalCal += food.calories;
            totalProtein += food.protein;
        }

        // Update Teks Total
        TextView tvDetailTotalCal = findViewById(R.id.tvDetailTotalCal);
        TextView tvDetailProteinTotal = findViewById(R.id.tvDetailProteinTotal);
        TextView tvDetailPercent = findViewById(R.id.tvDetailPercent);

        if(tvDetailTotalCal != null) tvDetailTotalCal.setText("Calorites - Total " + totalCal + " kcal");
        if(tvDetailProteinTotal != null) tvDetailProteinTotal.setText("with " + totalProtein + "gr Protein");

        // --- TAMBAHAN KALKULASI PERSENTASE & GAMBAR PIRING ---
        int targetCal = getTargetCalorie();
        int targetPro = getTargetProtein();
        if (targetCal == 0) targetCal = 1;
        if (targetPro == 0) targetPro = 1;

        int percentCal = (int) (((float) totalCal / targetCal) * 100);
        int percentPro = (int) (((float) totalProtein / targetPro) * 100);

        if(tvDetailPercent != null) tvDetailPercent.setText(percentCal + "% Daily Calories Reached");

        int levelCalClip = Math.min(percentCal, 100) * 100;
        // percentPro adalah persentase aslimu (misal 27%)
        int percentProMentok = Math.min(percentPro, 100);

        int startOffset = 2000;
        int levelProClip = startOffset + (percentProMentok * 46);
        if (percentPro == 0) {
            levelProClip = 0;
        }

        ImageView ivProgressCalorie = findViewById(R.id.ivProgressCalorie);
        ImageView ivProgressProtein = findViewById(R.id.ivProgressProtein);

        if (ivProgressCalorie != null && ivProgressCalorie.getDrawable() instanceof android.graphics.drawable.ClipDrawable) {
            ivProgressCalorie.getDrawable().setLevel(levelCalClip);
        }
        if (ivProgressProtein != null && ivProgressProtein.getDrawable() instanceof android.graphics.drawable.ClipDrawable) {
            ivProgressProtein.getDrawable().setLevel(levelProClip);
        }
    }
    // Fungsi memunculkan Pop-up Gambar Fullscreen (Dipanggil dari Adapter)
    public void showImagePreviewDialog(String base64Image) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_preview);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        TextView btnClose = dialog.findViewById(R.id.btnClosePreview);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        ImageView ivFullscreenImage = dialog.findViewById(R.id.ivFullscreenImage);

        // Ubah Base64 ke Gambar untuk dipasang di Pop-up
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivFullscreenImage.setImageBitmap(decodedByte);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        dialog.show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (etDailyNotes != null && selectedDate != null) {
            String currentNote = etDailyNotes.getText().toString();
            // Simpan diam-diam ke database
            DailyNote noteToSave = new DailyNote(selectedDate, currentNote);
            AppDatabase.getInstance(this).noteDao().insertOrUpdateNote(noteToSave);
        }
    }
}