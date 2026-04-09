package com.example.calorite;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {FoodRecord.class, RecipeRecord.class, DailyNote.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract FoodDao foodDao();
    public abstract RecipeDao recipeDao();
    public abstract NoteDao noteDao();

    private static volatile AppDatabase INSTANCE;

    // Memastikan database hanya dibuat satu kali (Singleton) agar tidak boros memori
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "calorite_database")
                            .allowMainThreadQueries() // Boleh jalan di antarmuka utama (untuk versi belajar)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}