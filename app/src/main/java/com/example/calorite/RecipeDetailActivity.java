package com.example.calorite; // Sesuaikan packagenya

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;

public class RecipeDetailActivity extends AppCompatActivity {

    private int currentRecipeId;
    private RecipeRecord currentRecipe;

    // Variabel untuk Pop-up Edit
    private static final int REQUEST_EDIT_IMAGE = 10;
    private Bitmap newRecipeBitmap = null;
    private TextView tvFileNameGlobal;
    private String currentBase64Image = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail); // Pastikan nama XML-nya sesuai

        // 1. Ambil ID Resep yang dikirim dari Adapter
        currentRecipeId = getIntent().getIntExtra("RECIPE_ID", -1);

        if (currentRecipeId != -1) {
            loadRecipeData();
        } else {
            Toast.makeText(this, "Resep tidak ditemukan!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Tombol Kembali (Asumsi ID di XML: btnBack)
        // ImageView btnBack = findViewById(R.id.btnBack);
        // btnBack.setOnClickListener(v -> finish());

        // Tombol Edit (Asumsi ID di XML: btnEditRecipe)
        Button btnEdit = findViewById(R.id.btnEditRecipe);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> showEditDialog());
        }
    }

    // Fungsi memuat data dari Database ke Layar
    private void loadRecipeData() {
        currentRecipe = AppDatabase.getInstance(this).recipeDao().getRecipeById(currentRecipeId);

        if (currentRecipe != null) {
            TextView tvTitle = findViewById(R.id.tvRecipeName); // Sesuaikan ID XML
            TextView tvDetails = findViewById(R.id.tvDetailDesc);
            TextView tvPro = findViewById(R.id.tvDetailProtein);
            TextView tvCal = findViewById(R.id.tvDetailCal);
            ImageView ivImage = findViewById(R.id.ivDetailRecipeImage);

            if(tvTitle != null) tvTitle.setText(currentRecipe.recipeName);
            if(tvDetails != null) tvDetails.setText(currentRecipe.recipeDetails);
            if(tvPro != null) tvPro.setText("🥩 " + currentRecipe.protein + "g Protein");
            if(tvCal != null) tvCal.setText("🔥 " + currentRecipe.calories + " kcal");

            currentBase64Image = currentRecipe.imageBase64;
            if (currentBase64Image != null && !currentBase64Image.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(currentBase64Image, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if(ivImage != null) ivImage.setImageBitmap(decodedByte);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    // Fungsi Pop-up Edit
    private void showEditDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_recipe); // Kita pakai ulang layout Add Recipe!

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        TextView btnClose = dialog.findViewById(R.id.btnCloseAdd);
        btnClose.setOnClickListener(view -> dialog.dismiss());
        Button btnDeleteRecipe = dialog.findViewById(R.id.btnDeleteRecipe);
        if (btnDeleteRecipe != null) {
            btnDeleteRecipe.setVisibility(android.view.View.VISIBLE); // Munculkan tombol saat edit

            btnDeleteRecipe.setOnClickListener(v -> {
                // 1. Hapus dari database
                AppDatabase.getInstance(this).recipeDao().deleteRecipe(currentRecipe);

                Toast.makeText(this, "Resep dihapus!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                // 2. Tutup halaman detail dan kembali ke Dasbor utama
                finish();
            });
        }

        Button btnChooseFile = dialog.findViewById(R.id.btnChooseFile);
        tvFileNameGlobal = dialog.findViewById(R.id.tvFileName);
        Button btnConfirmRecipe = dialog.findViewById(R.id.btnConfirmRecipe);
        btnConfirmRecipe.setText("Save Changes"); // Ubah teks tombolnya jadi Save

        EditText etRecipeName = dialog.findViewById(R.id.etRecipeName);
        EditText etCal = dialog.findViewById(R.id.etCal);
        EditText etPro = dialog.findViewById(R.id.etPro);
        EditText etRecipeDetails = dialog.findViewById(R.id.etRecipeDetails);

        // --- PRE-FILL: Isi kotak input dengan data lama ---
        etRecipeName.setText(currentRecipe.recipeName);
        etCal.setText(String.valueOf(currentRecipe.calories));
        etPro.setText(String.valueOf(currentRecipe.protein));
        etRecipeDetails.setText(currentRecipe.recipeDetails);
        newRecipeBitmap = null; // Reset foto

        if (currentBase64Image != null && !currentBase64Image.isEmpty()) {
            tvFileNameGlobal.setText("Image Exists ✓");
            tvFileNameGlobal.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            tvFileNameGlobal.setText("No file chosen");
        }

        // Aksi Pilih Foto Baru
        btnChooseFile.setOnClickListener(view -> {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, REQUEST_EDIT_IMAGE);
        });

        // Aksi Simpan Perubahan
        btnConfirmRecipe.setOnClickListener(view -> {
            String name = etRecipeName.getText().toString();
            String calStr = etCal.getText().toString();
            String proStr = etPro.getText().toString();
            String details = etRecipeDetails.getText().toString();

            if(name.isEmpty() || calStr.isEmpty() || proStr.isEmpty()) {
                Toast.makeText(this, "Data tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update data di objek lama
            currentRecipe.recipeName = name;
            currentRecipe.calories = Integer.parseInt(calStr);
            currentRecipe.protein = Integer.parseInt(proStr);
            currentRecipe.recipeDetails = details;

            // Jika user memilih gambar baru, konversi ke Base64. Jika tidak, biarkan base64 yang lama.
            if (newRecipeBitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                newRecipeBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] imageBytes = baos.toByteArray();
                currentRecipe.imageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            }

            // SIMPAN KE DATABASE
            AppDatabase.getInstance(this).recipeDao().updateRecipe(currentRecipe);

            Toast.makeText(this, "Resep berhasil diupdate!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            // Refresh tampilan layar di belakangnya
            loadRecipeData();
        });

        dialog.show();
    }

    // Menangkap foto baru dari galeri saat Edit
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && requestCode == REQUEST_EDIT_IMAGE) {
            try {
                Uri selectedImageUri = data.getData();
                newRecipeBitmap = android.provider.MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                if (tvFileNameGlobal != null) {
                    tvFileNameGlobal.setText("New Image ✓");
                    tvFileNameGlobal.setTextColor(Color.parseColor("#4CAF50"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal mengambil foto", Toast.LENGTH_SHORT).show();
            }
        }
    }
}