package com.example.calorite;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class SavedRecipesActivity extends AppCompatActivity {

    private static final int REQUEST_RECIPE_IMAGE_PICK = 300;
    private RecyclerView rvSavedRecipes;
    private RecipeAdapter recipeAdapter;
    private List<RecipeRecord> recipeList;
    private Bitmap selectedRecipeBitmap = null;
    private TextView tvFileNameGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_recipes);

        ImageView btnBack = findViewById(R.id.btnBackSavedRecipes);
        btnBack.setOnClickListener(v -> finish());

        rvSavedRecipes = findViewById(R.id.rvVerticalSavedRecipes);
        rvSavedRecipes.setLayoutManager(new LinearLayoutManager(this));

        Button btnAddRecipe = findViewById(R.id.btnAddRecipeSaved);
        btnAddRecipe.setOnClickListener(v -> showAddRecipeDialog());

        EditText etSearch = findViewById(R.id.etSearchSavedRecipes);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipes(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadRecipes();
    }

    private void loadRecipes() {
        recipeList = AppDatabase.getInstance(this).recipeDao().getAllRecipes();
        recipeAdapter = new RecipeAdapter(this, recipeList);
        rvSavedRecipes.setAdapter(recipeAdapter);
    }

    private void filterRecipes(String query) {
        if (recipeList == null) return;
        java.util.List<RecipeRecord> filteredList = new java.util.ArrayList<>();
        for (RecipeRecord item : recipeList) {
            if (item.recipeName != null && item.recipeName.toLowerCase().contains(query.toLowerCase().trim())) {
                filteredList.add(item);
            }
        }
        recipeAdapter = new RecipeAdapter(this, filteredList);
        rvSavedRecipes.setAdapter(recipeAdapter);
    }

    private void showAddRecipeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_recipe);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        Button btnDeleteRecipe = dialog.findViewById(R.id.btnDeleteRecipe);
        if (btnDeleteRecipe != null) btnDeleteRecipe.setVisibility(android.view.View.GONE);

        TextView btnClose = dialog.findViewById(R.id.btnCloseAdd);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        Button btnChooseFile = dialog.findViewById(R.id.btnChooseFile);
        tvFileNameGlobal = dialog.findViewById(R.id.tvFileName);
        Button btnConfirmRecipe = dialog.findViewById(R.id.btnConfirmRecipe);

        EditText etRecipeName = dialog.findViewById(R.id.etRecipeName);
        EditText etCal = dialog.findViewById(R.id.etCal);
        EditText etPro = dialog.findViewById(R.id.etPro);
        EditText etRecipeDetails = dialog.findViewById(R.id.etRecipeDetails);

        selectedRecipeBitmap = null;
        if (tvFileNameGlobal != null) tvFileNameGlobal.setText("No file chosen");

        btnChooseFile.setOnClickListener(v -> {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, REQUEST_RECIPE_IMAGE_PICK);
        });

        btnConfirmRecipe.setOnClickListener(v -> {
            String name = etRecipeName.getText().toString();
            String calStr = etCal.getText().toString();
            String proStr = etPro.getText().toString();
            String details = etRecipeDetails.getText().toString();

            if (name.isEmpty() || calStr.isEmpty() || proStr.isEmpty()) {
                Toast.makeText(this, "Mohon isi nama, kalori, dan protein!", Toast.LENGTH_SHORT).show();
                return;
            }

            String base64Image = "";
            if (selectedRecipeBitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedRecipeBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
            }

            RecipeRecord newRecipe = new RecipeRecord(name, Integer.parseInt(calStr), Integer.parseInt(proStr), details, base64Image);
            AppDatabase.getInstance(this).recipeDao().insertRecipe(newRecipe);

            Toast.makeText(this, "Resep " + name + " berhasil disimpan!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadRecipes();
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && requestCode == REQUEST_RECIPE_IMAGE_PICK) {
            try {
                Uri selectedImageUri = data.getData();
                selectedRecipeBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                if (tvFileNameGlobal != null) {
                    tvFileNameGlobal.setText("Image Selected ✓");
                    tvFileNameGlobal.setTextColor(Color.parseColor("#4CAF50"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes();
    }
}