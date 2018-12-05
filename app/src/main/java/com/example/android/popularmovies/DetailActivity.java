package com.example.android.popularmovies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.stetho.Stetho;

import com.example.android.popularmovies.ReviewUtils.Review;
import com.example.android.popularmovies.ReviewUtils.ReviewAdapter;
import com.example.android.popularmovies.ReviewUtils.ReviewResults;
import com.example.android.popularmovies.VideoUtils.Video;
import com.example.android.popularmovies.VideoUtils.VideoAdapter;
import com.example.android.popularmovies.VideoUtils.VideoResults;
import com.example.android.popularmovies.database.AppDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity implements
    VideoResults.VideoAcquiredListener,
    ReviewResults.ReviewsAcquiredListener {

  private static final String TAG = "DetailActivity";
  private static final String MOVIE_DATA = "MovieData";
  private static final String FAVORITE = "favorite";

  private AppDatabase mDb;

  private TextView mReleaseDateTextView;
  private TextView mSynopsisTextView;
  private RatingBar mRatingBar;
  private ImageView mImageView;
  private MovieData mMovie;

  private TextView mVideoTextView;
  private TextView mReviewTextView;
  private RecyclerView mVideoRecyclerView;
  private RecyclerView mReviewRecyclerView;

  private ScrollView mScrollView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);

    mDb = AppDatabase.getInstance(getApplicationContext());

    mReleaseDateTextView = findViewById(R.id.releaseDataTextView);

    mSynopsisTextView = findViewById(R.id.synopsisTextView);
    mSynopsisTextView.setMovementMethod(new ScrollingMovementMethod());

    mRatingBar = findViewById(R.id.ratingBar);
    mRatingBar.setIsIndicator(true);
    mRatingBar.setNumStars(5);
    mRatingBar.setStepSize((float) 0.1);

    mImageView = findViewById(R.id.imageView);
    mVideoTextView = findViewById(R.id.video_label_textview);
    mReviewTextView = findViewById(R.id.review_label_textview);
    mVideoRecyclerView = findViewById(R.id.video_recycler_view);
    mReviewRecyclerView = findViewById(R.id.review_recycler_view);
    mScrollView = findViewById(R.id.scrollView);

    Stetho.initializeWithDefaults(this);

    Bundle data = getIntent().getExtras();

    if (data != null && data.containsKey(MOVIE_DATA)) {
      mMovie = data.getParcelable(MOVIE_DATA);
      setTitle(mMovie.getTitle());
      mReleaseDateTextView.setText(mMovie.getReleaseDate());
      mSynopsisTextView.setText(mMovie.getOverview());
      mRatingBar.setRating((float) mMovie.getVoteAverage() / (float) 2.0);

      Picasso.get()
          .load(mMovie.getSmallPosterUrl())
          .fit()
          .centerCrop()
          .placeholder(R.drawable.cinema)
          .error(R.drawable.cinema)
          .into(mImageView);
      Log.i(TAG, String.valueOf((float) mMovie.getVoteAverage()));

      setupVideoRecycler(mMovie.getId());
      setupReviewRecycler(mMovie.getId());

      if (mMovie.isFavorite() == 0) {

        DetailViewModelFactory factory = new DetailViewModelFactory(mDb, mMovie.getId());
        final DetailViewModel viewModel = ViewModelProviders.of(this, factory)
            .get(DetailViewModel.class);

        viewModel.getFavorite().observe(this, new Observer<MovieData>() {
          @Override
          public void onChanged(@Nullable MovieData favoriteEntry) {
            if (viewModel.getFavorite().getValue() != null) {
              mMovie.setFavorite(1);
            }
          }
        });

      }
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(FAVORITE, mMovie.isFavorite() == 1);
  }

  @Override
  protected void onRestoreInstanceState(final Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);

    if (savedInstanceState.getBoolean(FAVORITE)) {
      mMovie.setFavorite(1);
    } else {
      mMovie.setFavorite(0);
    }

  }

  public void setupVideoRecycler(int id) {
    Call<VideoResults> call = NetworkUtils.buildVideoCall(id);

    callVideos(call);
    LinearLayoutManager videoLayoutManager = new LinearLayoutManager(
        this, LinearLayoutManager.VERTICAL, false);

    mVideoRecyclerView.setLayoutManager(videoLayoutManager);
    RecyclerView.ItemDecoration itemDecoration = new
        DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
    mVideoRecyclerView.addItemDecoration(itemDecoration);

  }

  public void setupReviewRecycler(int id) {
    Call<ReviewResults> reviewCall = NetworkUtils.buildReviewCall(mMovie.getId());
    callReviews(reviewCall);

    LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(
        this, LinearLayoutManager.VERTICAL, false);

    mReviewRecyclerView.setLayoutManager(reviewLayoutManager);
    RecyclerView.ItemDecoration itemDecoration = new
        DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
    mReviewRecyclerView.addItemDecoration(itemDecoration);

  }

  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_detail, menu);

    // If the movie is a favorite, color it appropriately.
    if (mMovie.isFavorite() == 1) {
      colorIconRed(menu.getItem(0));
    } else {
      colorIconWhite(menu.getItem(0));
    }

    return true;
  }


  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_item_favorite:
        changeIconColor(item);
        addMovieToDb();
        return true;
      default:
        return false;
    }
  }

  public void addMovieToDb() {

    final MovieData favoriteEntry = new MovieData(
        mMovie.getTitle(),
        mMovie.getReleaseDate(),
        mMovie.getVoteAverage(),
        mMovie.getPosterPath(),
        mMovie.getOverview(),
        mMovie.getId());

    if (mMovie.isFavorite() == 1) {
      AppExecutors.getInstance().diskIO().execute(new Runnable() {
        @Override
        public void run() {
          mDb.favoriteDao().insertFavorite(favoriteEntry);
        }
      });
    } else {
      AppExecutors.getInstance().diskIO().execute(new Runnable() {
        @Override
        public void run() {
          mDb.favoriteDao().deleteFavorite(favoriteEntry);
        }
      });
    }
  }

  public void changeIconColor(MenuItem item) {


    if (mMovie.isFavorite() == 0) {
      colorIconRed(item);
      mMovie.setFavorite(1);
    } else {
      colorIconWhite(item);
      mMovie.setFavorite(0);
    }
  }


  //Get videos from the video API asynchronously.

  public void callVideos(Call<VideoResults> call) {
    call.enqueue(new Callback<VideoResults>() {
      @Override
      public void onResponse(Call<VideoResults> call, Response<VideoResults> response) {
        if (response.message().contentEquals("OK")) {
          Log.i(TAG, response.body().getVideoList().toString());
          onVideosAcquired(response.body().getVideoList());
        } else {
          Log.e(TAG, response.message());
        }
      }

      @Override
      public void onFailure(Call<VideoResults> call, Throwable t) {
        Log.e(TAG, t.getMessage());
      }
    });
  }

  public void callReviews(Call<ReviewResults> call) {
    call.enqueue(new Callback<ReviewResults>() {
      @Override
      public void onResponse(Call<ReviewResults> call, Response<ReviewResults> response) {
        if (response.message().contentEquals("OK")) {
          Log.i(TAG, response.body().getReviewList().toString());
          onReviewsAcquired(response.body().getReviewList());
        } else Log.e(TAG, response.message());
      }

      @Override
      public void onFailure(Call<ReviewResults> call, Throwable t) {
        Log.e(TAG, t.getMessage());
      }
    });
  }


  /* Show the videos if you have them. */

  @Override
  public void onVideosAcquired(List<Video> videos) {
    if (!videos.isEmpty()) {
      mVideoTextView.setVisibility(View.VISIBLE);
      mVideoRecyclerView.setAdapter(new VideoAdapter(videos));
    }
  }

  public void onReviewsAcquired(List<Review> reviews) {
    if (!reviews.isEmpty()) {
      mReviewTextView.setVisibility(View.VISIBLE);
      mReviewRecyclerView.setAdapter(new ReviewAdapter(reviews));
    }
  }

  public void colorIconRed(MenuItem item) {
    Drawable icon = item.getIcon();
    Drawable newIcon = icon.mutate();
    DrawableCompat.setTint(newIcon, getResources().getColor(R.color.colorAccent));
    DrawableCompat.setTintMode(newIcon, PorterDuff.Mode.SRC_IN);
    item.setIcon(newIcon);
  }

  public void colorIconWhite(MenuItem item) {
    Drawable icon = item.getIcon();
    Drawable newIcon = icon.mutate();
    DrawableCompat.setTint(newIcon, getResources().getColor(R.color.white));
    DrawableCompat.setTintMode(newIcon, PorterDuff.Mode.SRC_IN);
    item.setIcon(newIcon);
  }

}


