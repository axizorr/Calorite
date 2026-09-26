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
    private Dialog loadingDialog; // <-- TAMBAHKAN INI
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
        // Aksi klik menu Saved Recipes
        android.widget.LinearLayout btnMenuRecipes = findViewById(R.id.btnMenuRecipes);
        btnMenuRecipes.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SavedRecipesActivity.class);
            startActivity(intent);
        });

        // Aksi klik menu Calorite History
        android.widget.LinearLayout btnMenuHistory = findViewById(R.id.btnMenuHistory);
        btnMenuHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryListActivity.class);
            startActivity(intent);
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

                        dialogHint.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialogHint.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

                        EditText etHint = dialogHint.findViewById(R.id.etHint);
                        Button btnSkip = dialogHint.findViewById(R.id.btnSkipHint);
                        Button btnAnalyze = dialogHint.findViewById(R.id.btnAnalyzeHint);

                        // --- BARIS TAMBAHAN UNTUK FOTO & TOMBOL RETAKE ---
                        ImageView ivHintImage = dialogHint.findViewById(R.id.ivHintImage);
                        androidx.cardview.widget.CardView btnRetake = dialogHint.findViewById(R.id.btnRetakeImage);

                        if (ivHintImage != null) {
                            ivHintImage.setImageBitmap(resizedBitmap); // Pasang gambar hasil jepretan
                        }

                        if (btnRetake != null) {
                            btnRetake.setOnClickListener(v -> {
                                dialogHint.dismiss(); // Tutup dialog saat ini
                                // Simulasikan menekan tombol Capture lagi untuk mengulang!
                                findViewById(R.id.btnCapture).performClick();
                            });
                        }
                        // -------------------------------------------------

                        // Aksi jika tombol "Analisis" ditekan
                        btnAnalyze.setOnClickListener(v -> {
                            String hint = etHint.getText().toString();
                            String finalBase64 = bitmapToBase64(resizedBitmap);

                            showLoadingDialog();
                            analyzeFoodWithAI(resizedBitmap, finalBase64, hint);
                            dialogHint.dismiss();
                        });

                        // Aksi jika tombol "Lewati" ditekan
                        btnSkip.setOnClickListener(v -> {
                            String finalBase64 = bitmapToBase64(resizedBitmap);

                            showLoadingDialog();
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
        String apiKey = BuildConfig.API_KEY;
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash-lite", apiKey);
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
                        String estDetail = resultJson.optString("detail", "Detail tidak tersedia.");

                        runOnUiThread(() -> {
                            hideLoadingDialog(); // <-- MATIKAN LOADING
                            // BAWA imageBitmap KE FUNGSI KONFIRMASI BAWAH 👇
                            showConfirmDialog(estKalori, estProtein, estDetail, base64Image, imageBitmap);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        hideLoadingDialog(); // <-- MATIKAN LOADING SAAT ERROR
                        Toast.makeText(this, "AI gagal memproses gambar.", Toast.LENGTH_SHORT).show();
                    });
                }
            }, getMainExecutor());
        }
    }
    private void showConfirmDialog(String kalori, String protein, String detail, String base64Image, Bitmap foodBitmap) {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        EditText etEstCal = dialog.findViewById(R.id.etEstCal);
        EditText etEstPro = dialog.findViewById(R.id.etEstPro);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        TextView tvDetailAnalysis = dialog.findViewById(R.id.tvDetailAnalysis);
        ImageView ivConfirmImage = dialog.findViewById(R.id.ivConfirmImage);
        if (ivConfirmImage != null && foodBitmap != null) {
            ivConfirmImage.setImageBitmap(foodBitmap);
        }

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
        Button btnAddFromRecipe = dialog.findViewById(R.id.btnTambahDariRecipe);

        // Reset variabel setiap kali dialog dibuka
        manualImageBase64 = "";

        // Fungsi pilih file
        btnChooseFile.setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Pilih dari Galeri", android.widget.Toast.LENGTH_SHORT).show();
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
        });

        // Simpan Manual
        btnSave.setOnClickListener(v -> {
            String calStr = etCal.getText().toString();
            String proStr = etPro.getText().toString();

            if (calStr.isEmpty() || proStr.isEmpty()) {
                android.widget.Toast.makeText(this, "Kalori dan Protein wajib diisi!", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            int cal = Integer.parseInt(calStr);
            int pro = Integer.parseInt(proStr);

            // Simpan ke Database
            FoodRecord manualRecord = new FoodRecord(getCurrentDateString(), System.currentTimeMillis(), "Manual Input", cal, pro, manualImageBase64);
            AppDatabase.getInstance(this).foodDao().insertFood(manualRecord);

            // Refresh UI Piring & Layar
            updateDashboardUI();

            android.widget.Toast.makeText(this, "Calorite berhasil ditambah!", android.widget.Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // AKSI TAMBAH DARI RECIPE (BUKA DIALOG BARU)
        btnAddFromRecipe.setOnClickListener(v -> {
            dialog.dismiss(); // 1. Tutup dialog manual input terlebih dahulu
            showPickRecipeDialog(); // 2. Buka dialog pilih resep
        });

        // Sizing Dialog Manual Input
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.85);
            window.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.show(); // Tampilkan dialog manual
    }

    // BUAT FUNGSI TERPISAH UNTUK PICK RECIPE DIALOG
    private void showPickRecipeDialog() {
        Dialog pickDialog = new Dialog(this);
        pickDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pickDialog.setContentView(R.layout.dialog_pick_recipe);

        if (pickDialog.getWindow() != null) {
            pickDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        RecyclerView rvPickRecipe = pickDialog.findViewById(R.id.rvPickRecipeList);
        EditText etSearch = pickDialog.findViewById(R.id.etSearchRecipe);
        Button btnConfirm = pickDialog.findViewById(R.id.btnConfirmPickRecipe);

        // PROTEKSI: Cek apakah RecyclerView ditemukan di XML
        if (rvPickRecipe != null) {
            rvPickRecipe.setLayoutManager(new LinearLayoutManager(MainActivity.this));

            List<RecipeRecord> recipeList = AppDatabase.getInstance(MainActivity.this).recipeDao().getAllRecipes();
            PickRecipeAdapter adapter = new PickRecipeAdapter(MainActivity.this, recipeList);
            rvPickRecipe.setAdapter(adapter);

            // 1. Fitur Search Real-time
            if (etSearch != null) {
                etSearch.addTextChangedListener(new android.text.TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        adapter.filter(s.toString());
                    }
                    @Override public void afterTextChanged(android.text.Editable s) {}
                });
            }

            // 2. Aksi Tombol Confirm
            if (btnConfirm != null) {
                btnConfirm.setOnClickListener(v -> {
                    RecipeRecord selectedRecipe = adapter.getSelectedRecipe();

                    if (selectedRecipe == null) {
                        Toast.makeText(MainActivity.this, "Pilih salah satu resep terlebih dahulu!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Simpan ke food_records (Jurnal Harian)
                    String todayDate = getCurrentDateString();
                    FoodRecord newFood = new FoodRecord(
                            todayDate,
                            System.currentTimeMillis(),
                            selectedRecipe.recipeName,
                            selectedRecipe.calories,
                            selectedRecipe.protein,
                            selectedRecipe.imageBase64
                    );

                    AppDatabase.getInstance(MainActivity.this).foodDao().insertFood(newFood);
                    Toast.makeText(MainActivity.this, selectedRecipe.recipeName + " berhasil ditambahkan!", Toast.LENGTH_SHORT).show();

                    pickDialog.dismiss();
                    updateDashboardUI();
                });
            }
        } else {
            Toast.makeText(this, "Error: RecyclerView rvPickRecipeList tidak ditemukan di XML!", Toast.LENGTH_LONG).show();
        }

        // Ukuran Dialog
        Window window = pickDialog.getWindow();
        if (window != null) {
            android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.85);
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        pickDialog.show();
    }
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new Dialog(MainActivity.this);
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            loadingDialog.setContentView(R.layout.dialog_loading);
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            loadingDialog.setCancelable(false); // KUNCI LAYAR: User tidak bisa asal pencet di luar pop-up
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateDashboardUI();
    }
}