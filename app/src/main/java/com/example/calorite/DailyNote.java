package com.example.calorite;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_notes")
public class DailyNote {
    @PrimaryKey
    @NonNull
    public String dateString; // Tanggal jadi kunci utama
    public String noteText;   // Isi catatan

    public DailyNote(@NonNull String dateString, String noteText) {
        this.dateString = dateString;
        this.noteText = noteText;
    }
}