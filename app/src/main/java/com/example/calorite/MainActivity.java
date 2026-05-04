package com.example.calorite;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.app.AlertDialog;
import android.net.Uri;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.ClipDrawable;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_RECIPE_IMAGE_PICK = 3; // Kode khusus untuk resep
    private Bitmap selectedRecipeBitmap = null;
    private String manualImageBase64 = ""; // Untuk menyimpan gambar manual
    private TextView tvFileNameGlobal; // Agar onActivityResult bisa mengubah teks di dialog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ImageView ivCal = findViewById(R.id.ivProgressCalorie);
        // Minta Izin Notifikasi untuk Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        android.widget.Button btnAddManual = findViewById(R.id.btnAddManual);
        if (btnAddManual != null) {
            btnAddManual.setOnClickListener(v -> showManualAddDialog());
        }

        // Panggil fungsi penjadwal alarm
        setupMealReminders();
        if (ivCal != null) {
            android.graphics.drawable.Drawable drawableCal = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.img_plate_cal);

            // PERISAI ANTI CRASH: Cek apakah gambarnya beneran ada
            if (drawableCal != null) {
                android.graphics.drawable.ClipDrawable clipCal = new android.graphics.drawable.ClipDrawable(drawableCal, Gravity.START, android.graphics.drawable.ClipDrawable.HORIZONTAL);
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
                android.graphics.drawable.ClipDrawable clipPro = new android.graphics.drawable.ClipDrawable(drawablePro, Gravity.START, android.graphics.drawable.ClipDrawable.HORIZONTAL);
                ivPro.setImageDrawable(clipPro);
            } else {
                android.util.Log.e("CALORITE_ERROR", "Gawat! Gambar img_plate_pro.png tidak ditemukan!");
            }
        }
        long sevenDaysInMillis = 7L * 24 * 60 * 60 * 1000; // 7 hari dalam milidetik
        long thresholdTime = System.currentTimeMillis() - sevenDaysInMillis;
        AppDatabase.getInstance(this).foodDao().deleteOlderThan(thresholdTime);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView btnSettings = findViewById(R.id.btnSettings);
        Button btnAddRecipe = findViewById(R.id.btnAddRecipe);

        // Aksi ketika tombol Settings ditekan
        btnSettings.setOnClickListener(v -> {
            Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_settings);

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

            EditText etCalorieIntake = dialog.findViewById(R.id.etCalorieIntake);
            EditText etProteinIntake = dialog.findViewById(R.id.etProteinIntake); // Tambahkan ID ini di XML dialog_settings
            TextView btnClose = dialog.findViewById(R.id.btnCloseSettings);

            etCalorieIntake.setText(String.valueOf(getTargetCalorie()));
            etProteinIntake.setText(String.valueOf(getTargetProtein()));

            btnClose.setOnClickListener(view -> {
                String inputCalorie = etCalorieIntake.getText().toString();
                String inputProtein = etProteinIntake.getText().toString();

                if (!inputCalorie.isEmpty() && !inputProtein.isEmpty()) {
                    saveTargetCalorie(Integer.parseInt(inputCalorie));
                    saveTargetProtein(Integer.parseInt(inputProtein));
                    updateDashboardUI(); // Kalkulasi ulang layar
                }
                dialog.dismiss();
            });

            dialog.show();
        });

        // Aksi ketika tombol Add Recipe ditekan
        btnAddRecipe.setOnClickListener(v -> {
            Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_add_recipe);

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            Button btnDeleteRecipe = dialog.findViewById(R.id.btnDeleteRecipe);
            if (btnDeleteRecipe != null) {
                btnDeleteRecipe.setVisibility(android.view.View.GONE); // Hilangkan tombol saat nambah resep
            }
            TextView btnClose = dialog.findViewById(R.id.btnCloseAdd);
            btnClose.setOnClickListener(view -> dialog.dismiss());

            // Kenalkan elemen-elemen baru
            Button btnChooseFile = dialog.findViewById(R.id.btnChooseFile);
            tvFileNameGlobal = dialog.findViewById(R.id.tvFileName);
            Button btnConfirmRecipe = dialog.findViewById(R.id.btnConfirmRecipe);

            EditText etRecipeName = dialog.findViewById(R.id.etRecipeName);
            EditText etCal = dialog.findViewById(R.id.etCal);
            EditText etPro = dialog.findViewById(R.id.etPro);
            EditText etRecipeDetails = dialog.findViewById(R.id.etRecipeDetails);

            // Reset gambar tiap kali dialog dibuka
            selectedRecipeBitmap = null;
            tvFileNameGlobal.setText("No file chosen");

            // AKSI 1: Membuka Galeri
            btnChooseFile.setOnClickListener(view -> {
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhotoIntent, REQUEST_RECIPE_IMAGE_PICK);
            });

            // AKSI 2: Menekan Confirm
            btnConfirmRecipe.setOnClickListener(view -> {
                String name = etRecipeName.getText().toString();
                String calStr = etCal.getText().toString();
                String proStr = etPro.getText().toString();
                String details = etRecipeDetails.getText().toString();

                // Validasi agar tidak ada kotak kosong (bisa bikin aplikasi crash!)
                if(name.isEmpty() || calStr.isEmpty() || proStr.isEmpty()) {
                    Toast.makeText(this, "Mohon isi nama, kalori, dan protein!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int cal = Integer.parseInt(calStr);
                int pro = Integer.parseInt(proStr);

                // Ubah gambar ke Base64 (jika user memilih gambar)
                String base64Image = "";
                if (selectedRecipeBitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedRecipeBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] imageBytes = baos.toByteArray();
                    base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                }

                RecipeRecord newRecipe = new RecipeRecord(name, cal, pro, details, base64Image);
                AppDatabase.getInstance(MainActivity.this).recipeDao().insertRecipe(newRecipe);

                Toast.makeText(this, "Resep " + name + " siap disimpan!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                updateDashboardUI();
            });

            dialog.show();
        });

        // 1. Panggil rvHistory dari XML
        RecyclerView rvHistory = findViewById(R.id.rvHistory);

        // 2. Atur arah scroll jadi menyamping (Horizontal)
        rvHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        Button btnCapture = findViewById(R.id.btnCapture);

        btnCapture.setOnClickListener(v -> {
            // Membuat daftar pilihan
            String[] options = {"📷 Buka Kamera", "🖼️ Pilih dari Galeri"};

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Pilih Sumber Foto");
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Opsi 1: Buka Kamera (Kode yang lama)
                    Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                } else if (which == 1) {
                    // Opsi 2: Buka Galeri
                    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
                }
            });
            builder.show();
        });
        updateDashboardUI();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            try {
                // --- 1. LOGIKA UNTUK MENAMBAH RESEP ---
                if (requestCode == REQUEST_RECIPE_IMAGE_PICK) {
                    Uri selectedImageUri = data.getData();
                    selectedRecipeBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                    if (tvFileNameGlobal != null) {
                        tvFileNameGlobal.setText("Image Selected ✓");
                        tvFileNameGlobal.setTextColor(Color.parseColor("#4CAF50"));
                    }
                }

                // --- 2. LOGIKA UNTUK AI SCANNER (Ini yang tadi kehapus!) ---
                else if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_IMAGE_PICK) {
                    Bitmap imageBitmap = null;

                    if (requestCode == REQUEST_IMAGE_CAPTURE) {
                        Bundle extras = data.getExtras();
                        imageBitmap = (Bitmap) extras.get("data");
                    } else if (requestCode == REQUEST_IMAGE_PICK) {
                        Uri selectedImageUri = data.getData();
                        imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    }

                    if (imageBitmap != null) {
                        // Kecilkan gambar dulu agar HP tidak hang saat kirim ke AI
                        Bitmap resizedBitmap = getResizedBitmap(imageBitmap, 1024);

                        Dialog dialogHint = new Dialog(MainActivity.this);
                        dialogHint.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialogHint.setContentView(R.layout.dialog_hint);

                        // Bikin latar belakang di luar pop-up jadi transparan/gelap
                        dialogHint.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialogHint.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

                        EditText etHint = dialogHint.findViewById(R.id.etHint);
                        Button btnSkip = dialogHint.findViewById(R.id.btnSkipHint);
                        Button btnAnalyze = dialogHint.findViewById(R.id.btnAnalyzeHint);

                        // Aksi jika tombol "Analisis" ditekan
                        btnAnalyze.setOnClickListener(v -> {
                            String hint = etHint.getText().toString();
                            String finalBase64 = bitmapToBase64(resizedBitmap);

                            Toast.makeText(MainActivity.this, "AI sedang menghitung...", Toast.LENGTH_SHORT).show();
                            analyzeFoodWithAI(resizedBitmap, finalBase64, hint);
                            dialogHint.dismiss();
                        });

                        // Aksi jika tombol "Lewati" ditekan
                        btnSkip.setOnClickListener(v -> {
                            String finalBase64 = bitmapToBase64(resizedBitmap);
                            Toast.makeText(MainActivity.this, "AI sedang menghitung...", Toast.LENGTH_SHORT).show();
                            analyzeFoodWithAI(resizedBitmap, finalBase64, "Tidak ada keterangan tambahan.");
                            dialogHint.dismiss();
                        });

                        dialogHint.show();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Fungsi pembantu untuk mengubah Gambar menjadi teks Base64
    private String bitmapToBase64(Bitmap bitmap) {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP);
    }
    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    private void analyzeFoodWithAI(Bitmap imageBitmap, String base64Image, String userHint) {
        // 1. Inisialisasi Model (Gunakan API Key kamu)
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash-lite", "AIzaSyBp4YRQDg3nmYZiimJJkfBbtHDNvXFKXtM");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // 2. Siapkan Prompt
        String prompt = "Kamu adalah ahli gizi. Analisis gambar ini." +
                "\n\nKONTEKS DARI PENGGUNA: " + userHint +
                "\n\n1. Jika ada tabel 'Informasi Nilai Gizi', ABAIKAN bentuk makanannya. Baca angkanya. Hitung Total Kalori & Protein untuk SATU BUNGKUS KESELURUHAN." +
                "\n\n2. Jika TIDAK ADA tabel gizi, estimasi berdasarkan porsi di foto. Rincikan SETIAP komponen makanan beserta kalori dan proteinnya." +
                "\n\nBalas WAJIB HANYA format JSON persis seperti ini tanpa markdown: " +
                "{\"kalori\": 510, \"protein\": 17, \"detail\": \"Ayam Goreng: 200 kcal, 15gr Protein\\nNasi 1 porsi: 150 kcal, 0gr Protein\\nTahu: 50 kcal, 2gr Protein\"}";

        // 3. Masukkan Gambar dan Teks ke dalam Content
        Content content = new Content.Builder()
                .addImage(imageBitmap) // SDK resmi bisa langsung terima Bitmap!
                .addText(prompt)
                .build();

        // 4. Panggil AI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            com.google.common.util.concurrent.ListenableFuture<GenerateContentResponse> future = model.generateContent(content);

            future.addListener(() -> {
                try {
                    // Ambil hasil dari future yang sudah kita buat tadi
                    GenerateContentResponse response = future.get();
                    String resultText = response.getText();

                    if (resultText != null) {
                        resultText = resultText.replace("```json", "").replace("```", "").trim();
                        JSONObject resultJson = new JSONObject(resultText);

                        String estKalori = String.valueOf(resultJson.getInt("kalori"));
                        String estProtein = String.valueOf(resultJson.getInt("protein"));

                        // 1. TAMBAHKAN BARIS INI UNTUK MENGAMBIL TEKS RINCIAN:
                        String estDetail = resultJson.optString("detail", "Detail tidak tersedia.");

                        // 2. UBAH PEMANGGILAN DIALOGNYA JADI BEGINI (tambah variabel estDetail):
                        runOnUiThread(() -> showConfirmDialog(estKalori, estProtein, estDetail, base64Image));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "AI gagal memproses gambar.", Toast.LENGTH_SHORT).show());
                }
            }, getMainExecutor());
        }
    }
    private void showConfirmDialog(String kalori, String protein, String detail, String base64Image) {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        EditText etEstCal = dialog.findViewById(R.id.etEstCal);
        EditText etEstPro = dialog.findViewById(R.id.etEstPro);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        TextView tvDetailAnalysis = dialog.findViewById(R.id.tvDetailAnalysis);

        etEstCal.setText(kalori);
        etEstPro.setText(protein);
        etEstCal.setText(kalori);
        etEstPro.setText(protein);
        if (tvDetailAnalysis != null) {
            tvDetailAnalysis.setText(detail);
        }

        btnConfirm.setOnClickListener(v -> {
            String finalCal = etEstCal.getText().toString();
            String finalPro = etEstPro.getText().toString();

            // Mengubah teks menjadi angka (mencegah error jika kosong)
            int calInt = finalCal.isEmpty() ? 0 : Integer.parseInt(finalCal);
            int proInt = finalPro.isEmpty() ? 0 : Integer.parseInt(finalPro);

            // 1. BUNGKUS DATA KE DALAM FOOD RECORD
            FoodRecord newFood = new FoodRecord(
                    getCurrentDateString(),
                    System.currentTimeMillis(),
                    "Scanned Food", // Nama default karena belum ada input nama di pop-up ini
                    calInt,
                    proInt,
                    base64Image
            );

            // 2. SIMPAN KE DATABASE LOKAL
            AppDatabase.getInstance(MainActivity.this).foodDao().insertFood(newFood);

            Toast.makeText(this, "Berhasil disimpan ke Jurnal!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            // 3. REFRESH LAYAR DASHBOARD AGAR PIRINGNYA NAIK!
            updateDashboardUI();
        });

        dialog.show();
    }
    // Fungsi untuk menyimpan Target Kalori
    private void saveTargetCalorie(int target) {
        SharedPreferences sharedPreferences = getSharedPreferences("CaloriteSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("TARGET_CALORIE", target);
        editor.apply();
    }

    // Fungsi untuk mengambil Target Kalori (Default 2000 jika belum pernah di-set)
    private int getTargetCalorie() {
        SharedPreferences sharedPreferences = getSharedPreferences("CaloriteSettings", MODE_PRIVATE);
        return sharedPreferences.getInt("TARGET_CALORIE", 2000);
    }

    private void updateDashboardUI() {
        int targetCal = getTargetCalorie();
        int targetPro = getTargetProtein(); // Ambil target protein
        if (targetCal == 0) targetCal = 1;
        if (targetPro == 0) targetPro = 1;

        String todayDate = getCurrentDateString();
        AppDatabase db = AppDatabase.getInstance(this);

        // Hitung total kalori & protein hari ini
        int currentConsumedCalories = db.foodDao().getTotalCaloriesByDate(todayDate);
        int currentConsumedProtein = db.foodDao().getTotalProteinByDate(todayDate);

        // Kalkulasi Persentase
        int percentCal = (int) (((float) currentConsumedCalories / targetCal) * 100);
        int percentPro = (int) (((float) currentConsumedProtein / targetPro) * 100);

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

        // --- UPDATE TEKS ---
        TextView tvTargetKalori = findViewById(R.id.tvTargetKalori); // "1200 kcal / 3200 kcal"
        TextView tvProgressPersen = findViewById(R.id.tvProgressPersen); // "48% Daily Calories Reached"
        TextView tvTargetProtein = findViewById(R.id.tvTargetProtein); // "with 57gr / 120gr Protein" (TAMBAHKAN ID INI DI XML)

        tvTargetKalori.setText(currentConsumedCalories + " kcal / " + targetCal + " kcal");
        tvProgressPersen.setText(percentCal + "% Daily Calories Reached");
        if (tvTargetProtein != null) {
            tvTargetProtein.setText("with " + currentConsumedProtein + "gr / " + targetPro + "gr Protein");
        }

        // --- UPDATE GAMBAR (PIRING & DAGING) ANTI CRASH ---
        ImageView ivProgressCalorie = findViewById(R.id.ivProgressCalorie);
        ImageView ivProgressProtein = findViewById(R.id.ivProgressProtein);

        // Cek apakah ivProgressCalorie ada isinya, DAN apakah isinya benar-benar sebuah ClipDrawable
        if (ivProgressCalorie != null && ivProgressCalorie.getDrawable() instanceof android.graphics.drawable.ClipDrawable) {
            ivProgressCalorie.getDrawable().setLevel(levelCalClip);
        }
        if (ivProgressProtein != null && ivProgressProtein.getDrawable() instanceof android.graphics.drawable.ClipDrawable) {
            ivProgressProtein.getDrawable().setLevel(levelProClip);
        }

        // --- 2. UPDATE BAGIAN BAWAH (DAFTAR HISTORI ASLI) ---
        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));

        // Ambil semua tanggal yang ada di database
        List<String> savedDates = db.foodDao().getUniqueDates();
        List<HistorySummary> realHistoryList = new java.util.ArrayList<>();

        // Hitung total kalori dan ambil gambar terakhir untuk masing-masing tanggal
        for (String date : savedDates) {
            int totalCalForDate = db.foodDao().getTotalCaloriesByDate(date);
            int percentForDate = (int) (((float) totalCalForDate / targetCal) * 100);

            // Tarik gambar terakhir dari database
            String lastImage = db.foodDao().getLastImageByDate(date);

            // Masukkan gambar ke dalam daftar
            realHistoryList.add(new HistorySummary(date, String.valueOf(totalCalForDate), percentForDate + "%", lastImage));
        }

        // Pasang ke layar
        HistoryAdapter historyAdapter = new HistoryAdapter(this, realHistoryList);
        rvHistory.setAdapter(historyAdapter);

        // --- 3. UPDATE DAFTAR SAVED RECIPES ---
        RecyclerView rvSavedRecipes = findViewById(R.id.rvSavedRecipes);
        rvSavedRecipes.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));

        // Ambil data asli dari database
        List<RecipeRecord> savedRecipesList = db.recipeDao().getAllRecipes();

        // Pasang ke Adapter baru
        RecipeAdapter recipeAdapter = new RecipeAdapter(this, savedRecipesList);
        rvSavedRecipes.setAdapter(recipeAdapter);
    }
    // Fungsi untuk menyimpan Target Protein
    private void saveTargetProtein(int target) {
        SharedPreferences sharedPreferences = getSharedPreferences("CaloriteSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("TARGET_PROTEIN", target);
        editor.apply();
    }

    // Fungsi untuk mengambil Target Protein (Default 100gr jika belum pernah di-set)
    private int getTargetProtein() {
        SharedPreferences sharedPreferences = getSharedPreferences("CaloriteSettings", MODE_PRIVATE);
        return sharedPreferences.getInt("TARGET_PROTEIN", 100);
    }
    private String getCurrentDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }
    private void setupMealReminders() {
        // Jadwal jam makan yang kamu mau (Format 24 Jam)
        int[] mealHours = {7, 12, 16, 20};

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        for (int i = 0; i < mealHours.length; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, mealHours[i]);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // Cerdas: Jika jam tersebut sudah lewat hari ini, pasang untuk besok agar tidak langsung bunyi saat ini juga
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(this, NotificationReceiver.class);
            // i digunakan sebagai RequestCode agar 4 alarm ini dianggap berbeda, bukan saling menimpa
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, i, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Pasang weker berulang setiap hari (setInexactRepeating ramah baterai HP)
            if (alarmManager != null) {
                alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                );
            }
        }
    }
    private void showManualAddDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_add_manual);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        android.widget.Button btnChooseFile = dialog.findViewById(R.id.btnChooseFile);
        android.widget.EditText etCal = dialog.findViewById(R.id.etManualCal);
        android.widget.EditText etPro = dialog.findViewById(R.id.etManualPro);
        android.widget.Button btnSave = dialog.findViewById(R.id.btnSaveManual);

        // Reset variabel setiap kali dialog dibuka
        manualImageBase64 = "";

        // Fungsi pilih file (Bisa kamu sambungkan ke intent gallery nanti)
        btnChooseFile.setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Pilih dari Galeri", android.widget.Toast.LENGTH_SHORT).show();
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
        });

        btnSave.setOnClickListener(v -> {
            String calStr = etCal.getText().toString();
            String proStr = etPro.getText().toString();

            if (calStr.isEmpty() || proStr.isEmpty()) {
                android.widget.Toast.makeText(this, "Kalori dan Protein wajib diisi!", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            int cal = Integer.parseInt(calStr);
            int pro = Integer.parseInt(proStr);

            // LOGIKA PLACEHOLDER JIKA USER TIDAK PILIH GAMBAR
            if (manualImageBase64.isEmpty()) {
                android.graphics.Bitmap placeholderBitmap = android.graphics.BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_write);
                java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                placeholderBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, bos);
                manualImageBase64 = android.util.Base64.encodeToString(bos.toByteArray(), android.util.Base64.DEFAULT);
            }

            // Dapatkan tanggal hari ini (Samakan formatnya dengan DB kamu)
            String today = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(new java.util.Date());

            // Simpan ke Database
            FoodRecord manualRecord = new FoodRecord(today, System.currentTimeMillis(), "Manual Input", cal, pro, manualImageBase64);
            AppDatabase.getInstance(this).foodDao().insertFood(manualRecord);

            // Refresh UI Piring & Layar
            updateDashboardUI();

            android.widget.Toast.makeText(this, "Calorite berhasil ditambah!", android.widget.Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateDashboardUI();
    }
}