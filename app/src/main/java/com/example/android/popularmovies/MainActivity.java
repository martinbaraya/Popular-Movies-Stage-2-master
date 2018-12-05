package com.example.android.popularmovies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.android.popularmovies.database.AppDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  private static final String SORT_ORDER = "SortOrder";
  private static final String FAVORITE = "favorite";
  private static final String RECYCLER_POSITION = "RecyclerViewPosition";

  private AppDatabase mDb;

  private SharedPreferences mSharedPreferences;

  private String mSortOrder;

  private com.example.android.popularmovies.MovieAdapter mMovieAdapter;
  private RecyclerView mRecyclerView;
  private Parcelable recyclerPosition;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    mRecyclerView = findViewById(R.id.recycler_view);


    if (getResources().getConfiguration().orientation ==
        Configuration.ORIENTATION_PORTRAIT) {


      mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    } else {
      mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
    }

    mMovieAdapter = new com.example.android.popularmovies.MovieAdapter(getApplicationContext(), new ArrayList<com.example.android.popularmovies.MovieData>());
    mRecyclerView.setAdapter(mMovieAdapter);

    mSortOrder = mSharedPreferences.getString(SORT_ORDER, com.example.android.popularmovies.NetworkUtils.POPULAR);

    mDb = AppDatabase.getInstance(getApplicationContext());

    setupViewModel();


  }



  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(RECYCLER_POSITION,
        mRecyclerView.getLayoutManager().onSaveInstanceState());
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    if (savedInstanceState != null && savedInstanceState.containsKey(RECYCLER_POSITION)) {
      recyclerPosition = savedInstanceState.getParcelable(RECYCLER_POSITION);
    }
  }


  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_main, menu);

    MenuItem popular = menu.getItem(0);
    MenuItem top_rated = menu.getItem(1);
    MenuItem favorite = menu.getItem(2);


    // Check the correct radio button in the menu.
    switch (mSortOrder) {
      case FAVORITE:
        favorite.setChecked(true);
        break;
      case com.example.android.popularmovies.NetworkUtils.TOP_RATED:
        top_rated.setChecked(true);
        break;
      case com.example.android.popularmovies.NetworkUtils.POPULAR:
        popular.setChecked(true);
        break;
      default:
        popular.setChecked(true);
        break;
    }

    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    SharedPreferences.Editor editor = mSharedPreferences.edit();
    item.setChecked(true);

    switch (item.getItemId()) {
      case R.id.menu_item_popular:
        mSortOrder = com.example.android.popularmovies.NetworkUtils.POPULAR;
        break;
      case R.id.menu_item_top_rated:
        mSortOrder = com.example.android.popularmovies.NetworkUtils.TOP_RATED;
        break;
      case R.id.menu_item_favorite:
        mSortOrder = FAVORITE;
        break;
      default:
        return false;
    }

    editor.putString(SORT_ORDER, mSortOrder);
    editor.apply();
    setupViewModel();
    return true;
  }


  @Override
  protected void onResume() {
    super.onResume();

    if (mSortOrder.contentEquals(FAVORITE)) {
      setupViewModel();
    }
  }

  public void setupViewModel() {

    com.example.android.popularmovies.MainViewModel viewModel = ViewModelProviders.of(this).get(com.example.android.popularmovies.MainViewModel.class);


    viewModel.getFavoriteMovies().observe(this, new Observer<List<com.example.android.popularmovies.MovieData>>() {
      @Override
      public void onChanged(@Nullable List<com.example.android.popularmovies.MovieData> favoriteEntries) {
        Log.d(TAG, "Receiving changes from LiveData");

        if (mSortOrder.contentEquals(FAVORITE)) {
          List<com.example.android.popularmovies.MovieData> movieList = new ArrayList<com.example.android.popularmovies.MovieData>();

          if (favoriteEntries != null) {
            for (com.example.android.popularmovies.MovieData fave : favoriteEntries) {
              fave.setFavorite(1);
            }
            setAdapter(favoriteEntries);
          }
        }
      }
    });

    viewModel.getTopRatedMovies().observe(this, new Observer<List<com.example.android.popularmovies.MovieData>>() {
      @Override
      public void onChanged(@Nullable List<com.example.android.popularmovies.MovieData> movieData) {
        if (movieData != null && mSortOrder.contentEquals(com.example.android.popularmovies.NetworkUtils.TOP_RATED)) {
          setAdapter(movieData);
        }
      }
    });

    viewModel.getPopularMovies().observe(this, new Observer<List<com.example.android.popularmovies.MovieData>>() {
      @Override
      public void onChanged(@Nullable List<com.example.android.popularmovies.MovieData> movieData) {
        if (movieData != null && mSortOrder.contentEquals(com.example.android.popularmovies.NetworkUtils.POPULAR)) {
          setAdapter(movieData);
        }
      }
    });
  }

  public void setAdapter(List<com.example.android.popularmovies.MovieData> movies) {
    mMovieAdapter.setMovies(movies);
    mMovieAdapter.notifyDataSetChanged();
    if (recyclerPosition != null) {
      mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerPosition);
    }
  }

}
