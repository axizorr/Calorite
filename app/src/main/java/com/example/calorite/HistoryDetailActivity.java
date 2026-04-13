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
        ImageView ivCal = findViewById(R.id.ivProgressCalorie);
        if (ivCal != null) {
            android.graphics.drawable.Drawable drawableCal = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.img_plate_cal);

            // PERISAI ANTI CRASH: Cek apakah gambarnya beneran ada
            if (drawableCal != null) {
                android.graphics.drawable.ClipDrawable clipCal = new android.graphics.drawable.ClipDrawable(drawableCal, android.view.Gravity.LEFT, android.graphics.drawable.ClipDrawable.HORIZONTAL);
                ivCal.setImageDrawable(clipCal);
            } else {
                // Munculkan pesan di Logcat kalau gambarnya hilang, biar kita tahu tanpa bikin aplikasi crash
                android.util.Log.e("CALORITE_ERROR", "Gawat! Gambar img_plate_cal.png tidak ditemukan!");
            }
        }

        // --- MANTRA SUPER AMAN UNTUK PROTEIN ---
        ImageView ivPro = findViewById(R.id.ivProgressProtein);
        if (ivPro != null) {
            android.graphics.drawable.Drawable drawablePro = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.img_plate_pro);

            if (drawablePro != null) {
                android.graphics.drawable.ClipDrawable clipPro = new android.graphics.drawable.ClipDrawable(drawablePro, android.view.Gravity.LEFT, android.graphics.drawable.ClipDrawable.HORIZONTAL);
                ivPro.setImageDrawable(clipPro);
            } else {
                android.util.Log.e("CALORITE_ERROR", "Gawat! Gambar img_plate_pro.png tidak ditemukan!");
            }
        }
        refreshDailyData();

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

        // --- TAMBAHAN KALKULASI PERSENTASE & GAMBAR PIRING ---
        int targetCal = getTargetCalorie();
        int targetPro = getTargetProtein();
        if (targetCal == 0) targetCal = 1; // Mencegah error dibagi nol
        if (targetPro == 0) targetPro = 1;

        int percentCal = (int) (((float) totalCal / targetCal) * 100);
        int percentPro = (int) (((float) totalProtein / targetPro) * 100);

        if (tvDetailPercent != null) {
            tvDetailPercent.setText(percentCal + "% Daily Calories Reached");
        }

        // Potong gambar piring
        int levelCalClip = Math.min(percentCal, 100) * 100;
        // percentPro adalah persentase aslimu (misal 27%)
        int percentProMentok = Math.min(percentPro, 100);

        // Titik awal daging (misal 20% dari kiri kanvas = 2000)
        int startOffset = 2000;

        // Pengali rentang (dari 20% ke 60% = rentang 40% = pengali 40)
        int levelProClip = startOffset + (percentProMentok * 40);

        // Kalau belum makan protein sama sekali (0%), kembalikan ke 0 biar aman
        if (percentPro == 0) {
            levelProClip = 0;
        }

        if (ivCal != null && ivCal.getDrawable() instanceof android.graphics.drawable.ClipDrawable) {
            ivCal.getDrawable().setLevel(levelCalClip);
        }
        if (ivPro != null && ivPro.getDrawable() instanceof android.graphics.drawable.ClipDrawable) {
            ivPro.getDrawable().setLevel(levelProClip);
        }

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