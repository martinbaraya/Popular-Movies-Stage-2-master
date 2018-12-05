package com.example.android.popularmovies;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.popularmovies.database.AppDatabase;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends AndroidViewModel {
  private LiveData<List<MovieData>> favoriteMovies;
  private MutableLiveData<List<MovieData>> popularMovies = new MutableLiveData<>();
  private MutableLiveData<List<MovieData>> topRatedMovies = new MutableLiveData<>();

  private String TAG = "MainViewModel";

  public MainViewModel(@NonNull Application application) {
    super(application);
    AppDatabase appDatabase = AppDatabase.getInstance(this.getApplication());
    favoriteMovies = appDatabase.favoriteDao().loadAllFavorites();

    Call<MovieAPIResults> call = NetworkUtils.buildAPICall(NetworkUtils.POPULAR);
    call.enqueue(new Callback<MovieAPIResults>() {

      @Override
      public void onResponse(Call<MovieAPIResults> call, Response<MovieAPIResults> response) {

        if (response.message().contentEquals("OK")) {
          popularMovies.setValue(response.body().getMovies());
        } else {
          Log.e(TAG, "Something unexpected happened to our request: " + response.message());
        }
      }

      @Override
      public void onFailure(Call<MovieAPIResults> call, Throwable t) {
        Log.e(TAG, t.getMessage());
      }
    });

    Call<MovieAPIResults> call2 = NetworkUtils.buildAPICall(NetworkUtils.TOP_RATED);
    call2.enqueue(new Callback<MovieAPIResults>() {

      @Override
      public void onResponse(Call<MovieAPIResults> call, Response<MovieAPIResults> response) {

        if (response.message().contentEquals("OK")) {
          topRatedMovies.setValue(response.body().getMovies());
        } else {
          Log.e(TAG, "Something unexpected happened to our request: " + response.message());
        }
      }

      @Override
      public void onFailure(Call<MovieAPIResults> call, Throwable t) {
        Log.e(TAG, t.getMessage());
      }
    });

  }

  public LiveData<List<MovieData>> getFavoriteMovies() {
    return favoriteMovies;
  }

  public LiveData<List<MovieData>> getPopularMovies() {
    return popularMovies;
  }

  public LiveData<List<MovieData>> getTopRatedMovies() {
    return topRatedMovies;
  }


}
