package com.example.calorite;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface NoteDao {
    // Menyimpan atau menimpa catatan jika tanggalnya sama (Auto-save)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateNote(DailyNote note);

    // Mengambil catatan berdasarkan tanggal
    @Query("SELECT * FROM daily_notes WHERE dateString = :date LIMIT 1")
    DailyNote getNoteByDate(String date);
}