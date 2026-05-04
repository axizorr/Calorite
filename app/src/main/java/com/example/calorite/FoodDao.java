package com.example.calorite;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FoodDao {

    // 1. Menambah makanan baru
    @Insert
    void insertFood(FoodRecord food);

    // 2. Mengambil semua makanan di tanggal tertentu (untuk Histori Detail)
    @Query("SELECT * FROM food_records WHERE dateString = :date")
    List<FoodRecord> getFoodsByDate(String date);

    // 3. Menghitung total kalori di tanggal tertentu (untuk Dashboard)
    @Query("SELECT SUM(calories) FROM food_records WHERE dateString = :date")
    int getTotalCaloriesByDate(String date);

    // 4. FITUR SAKTI: Menghapus data yang lebih tua dari X waktu (7 Hari)
    @Query("DELETE FROM food_records WHERE timestamp < :timeThreshold")
    void deleteOlderThan(long timeThreshold);

    // Mengambil daftar tanggal unik (tidak kembar) dari yang paling baru ke paling lama
    @Query("SELECT DISTINCT dateString FROM food_records ORDER BY timestamp DESC")
    List<String> getUniqueDates();

    // Mengambil 1 gambar terakhir berdasarkan tanggal
    // Mengambil gambar terakhir di hari itu YANG BUKAN string kosong
    @Query("SELECT imageBase64 FROM food_records WHERE dateString = :date AND imageBase64 != '' ORDER BY id DESC LIMIT 1")
    String getLastImageByDate(String date);

    @androidx.room.Delete
    void deleteFood(FoodRecord food);

    @Query("SELECT SUM(protein) FROM food_records WHERE dateString = :date")
    int getTotalProteinByDate(String date);
}