package com.example.calorite; // Sesuaikan

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ClipDrawable;
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

        ImageView btnBackHistory = findViewById(R.id.btnBackHistory);
        btnBackHistory.setOnClickListener(v -> finish()); // Tombol kembali

        // 1. Terima tanggal dari Dasbor
        selectedDate = getIntent().getStringExtra("HISTORY_DATE");
        if (selectedDate == null) selectedDate = "Unknown Date";

        TextView tvDetailDateTitle = findViewById(R.id.tvDetailDateTitle);
        tvDetailDateTitle.setText(selectedDate);

        // 2. Ambil Data dari Database
        AppDatabase db = AppDatabase.getInstance(this);
        List<FoodRecord> dailyFoods = db.foodDao().getFoodsByDate(selectedDate);

        // 3. Hitung Total Kalori & Protein
        int totalCal = 0;
        int totalProtein = 0;
        for (FoodRecord food : dailyFoods) {
            totalCal += food.calories;
            totalProtein += food.protein;
        }

        // 4. Update Teks UI
        TextView tvDetailTotalCal = findViewById(R.id.tvDetailTotalCal);
        TextView tvDetailPercent = findViewById(R.id.tvDetailPercent);
        TextView tvDetailProteinTotal = findViewById(R.id.tvDetailProteinTotal);

        tvDetailTotalCal.setText("Calorites - Total " + totalCal + " kcal");
        tvDetailProteinTotal.setText("with " + totalProtein + "gr Protein");

        // 6. Pasang Adapter ke RecyclerView
        RecyclerView rvDailyFoods = findViewById(R.id.rvDailyFoods);
        rvDailyFoods.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        DailyFoodAdapter adapter = new DailyFoodAdapter(this, dailyFoods);
        rvDailyFoods.setAdapter(adapter);

        // 1. Inisialisasi kotak Notes (Sesuaikan ID-nya dengan yang ada di XML kamu)
        etDailyNotes = findViewById(R.id.etDailyNotes); // <-- PASTIKAN ID INI BENAR

        // 2. Tarik catatan lama (jika ada) saat halaman dibuka
        DailyNote savedNote = AppDatabase.getInstance(this).noteDao().getNoteByDate(selectedDate);
        if (savedNote != null && etDailyNotes != null) {
            etDailyNotes.setText(savedNote.noteText);
        }
    }

    // Fungsi bantu untuk mengambil target kalori (Sama seperti di MainActivity)
    private int getTargetCalorie() {
        SharedPreferences sharedPreferences = getSharedPreferences("CaloriteSettings", MODE_PRIVATE);
        return sharedPreferences.getInt("TARGET_CALORIE", 2000);
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