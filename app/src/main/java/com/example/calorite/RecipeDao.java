package com.example.calorite;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface RecipeDao {

    // Menyimpan resep baru
    @Insert
    void insertRecipe(RecipeRecord recipe);

    // Mengambil semua resep dari yang paling baru ditambahkan (untuk ditampilkan di layar)
    @Query("SELECT * FROM recipe_records ORDER BY id DESC")
    List<RecipeRecord> getAllRecipes();

    @Query("SELECT * FROM recipe_records WHERE id = :recipeId LIMIT 1")
    RecipeRecord getRecipeById(int recipeId);

    // Menyimpan perubahan data (Edit)
    @androidx.room.Update
    void updateRecipe(RecipeRecord recipe);

    @androidx.room.Delete
    void deleteRecipe(RecipeRecord recipe);
}