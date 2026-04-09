package com.example.calorite;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_records")
public class FoodRecord {

    @PrimaryKey(autoGenerate = true)
    public int id; // ID unik otomatis

    public String dateString; // Contoh: "02 Mar 2026" (untuk grouping tampilan UI)
    public long timestamp;    // Waktu spesifik dalam milidetik (untuk fitur auto-delete 7 hari)

    public String foodName;
    public int calories;
    public int protein;
    public String imageBase64; // Menyimpan foto dalam bentuk teks rahasia

    // Constructor (Biar gampang masukin data nanti)
    public FoodRecord(String dateString, long timestamp, String foodName, int calories, int protein, String imageBase64) {
        this.dateString = dateString;
        this.timestamp = timestamp;
        this.foodName = foodName;
        this.calories = calories;
        this.protein = protein;
        this.imageBase64 = imageBase64;
    }
}