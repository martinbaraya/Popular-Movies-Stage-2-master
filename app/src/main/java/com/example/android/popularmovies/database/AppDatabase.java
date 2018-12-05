package com.example.android.popularmovies.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

import com.example.android.popularmovies.MovieData;

@Database(entities = {MovieData.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
  private static final String TAG = AppDatabase.class.getSimpleName();
  private static final Object LOCK = new Object();
  private static final String DATABASE_NAME = "favorites";
  private static AppDatabase sInstance;

  public static AppDatabase getInstance(Context context) {
    if (sInstance == null) {
      synchronized (LOCK) {
        Log.d(TAG, "Creating new databse instance");
        sInstance = Room.databaseBuilder(context.getApplicationContext(),
            AppDatabase.class, AppDatabase.DATABASE_NAME)
            .build();
      }
    }
    Log.d(TAG, "Getting database instance");
    return sInstance;
  }

  public abstract com.example.android.popularmovies.database.FavoriteDao favoriteDao();
}
