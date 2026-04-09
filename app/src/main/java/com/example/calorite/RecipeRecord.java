package com.example.calorite; // Sesuaikan dengan nama package-mu

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recipe_records")
public class RecipeRecord {

    @PrimaryKey(autoGenerate = true)
    public int id; // ID urut otomatis

    public String recipeName;
    public int calories;
    public int protein;
    public String recipeDetails;
    public String imageBase64; // Foto resep

    // Constructor
    public RecipeRecord(String recipeName, int calories, int protein, String recipeDetails, String imageBase64) {
        this.recipeName = recipeName;
        this.calories = calories;
        this.protein = protein;
        this.recipeDetails = recipeDetails;
        this.imageBase64 = imageBase64;
    }
}