package com.example.android.popularmovies;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.popularmovies.database.AppDatabase;

public class DetailViewModel extends ViewModel {
  private LiveData<MovieData> favorite;

  public DetailViewModel(AppDatabase db, int id) {
    favorite = db.favoriteDao().loadMovieEntry(id);
  }

  public LiveData<MovieData> getFavorite() {
    return favorite;
  }


}
