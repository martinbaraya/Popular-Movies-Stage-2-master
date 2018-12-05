package com.example.android.popularmovies;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.android.popularmovies.database.AppDatabase;

public class DetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {
  private final AppDatabase mDb;
  private final int mId;

  public DetailViewModelFactory(AppDatabase db, int id) {
    mDb = db;
    mId = id;
  }

  @Override
  public <T extends ViewModel> T create(Class<T> modelClass) {
    //noinspection unchecked
    return (T) new DetailViewModel(mDb, mId);
  }
}
