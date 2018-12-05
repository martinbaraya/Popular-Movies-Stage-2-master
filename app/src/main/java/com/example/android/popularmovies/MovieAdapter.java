package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieHolder> {

    private static final String MOVIE_DATA = "MovieData";

    private List<MovieData> mMovies;
    Context mContext;

    public MovieAdapter(Context context, ArrayList<MovieData> movies) {
      mMovies = movies;
      mContext = context;
    }

    @NonNull
    @Override
    public MovieHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      View view = inflater.inflate(R.layout.list_item_movie, parent, false);

      RecyclerViewClickListener listener = new RecyclerViewClickListener() {
        @Override
        public void onClick(View view, int i) {
          Intent intent = new Intent(parent.getContext(), DetailActivity.class);
          intent.putExtra(MOVIE_DATA, mMovies.get(i));
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          mContext.startActivity(intent);
        }
      };

      return new MovieHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieHolder holder, int position) {
      Drawable placeholder = mContext.getResources().getDrawable(R.drawable.cinema);
      holder.bindDrawable(placeholder, mMovies.get(position));
    }

    @Override
    public int getItemCount() {
      return mMovies.size();
    }

    public void setMovies(List<MovieData> movies) {
      mMovies = movies;
    }


  public class MovieHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
    private final ImageButton mImageButton;
    private RecyclerViewClickListener mRecyclerViewClickListener;


    public MovieHolder(View itemView, RecyclerViewClickListener listener) {
      super(itemView);

      mImageButton = itemView.findViewById(R.id.list_item_movie_button);
      mRecyclerViewClickListener = listener;
      mImageButton.setOnClickListener(this);
    }

    public void bindDrawable(Drawable drawable, final MovieData movie) {


      Picasso.get()
          .load(movie.getSmallPosterUrl())
          .fit()
          .centerCrop()
          .placeholder(drawable)
          .error(drawable)
          .into(mImageButton);

    }

    @Override
    public void onClick(View v) {
      mRecyclerViewClickListener.onClick(v, getAdapterPosition());
    }
  }
}
