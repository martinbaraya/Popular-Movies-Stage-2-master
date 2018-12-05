package com.example.android.popularmovies.VideoUtils;

//This is the recyclerview to show the videos in the DetailActivity.

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.R;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder> {

  private List<Video> mVideos;

  public VideoAdapter(List<Video> videos) {
    mVideos = videos;
  }

  @NonNull
  @Override
  public VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    final Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);

    View videoView = inflater.inflate(R.layout.list_item_video, parent, false);


    com.example.android.popularmovies.VideoUtils.VideoViewClickListener listener = new com.example.android.popularmovies.VideoUtils.VideoViewClickListener() {
      @Override
      public void onClick(View view, int i) {
        String key = mVideos.get(i).getKey();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.youtube.com/watch?v=" + key));
        context.startActivity(intent);
      }
    };

    VideoHolder videoHolder = new VideoHolder(videoView, listener);
    return videoHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull VideoHolder holder, int position) {
    //Get the data position.
    Video video = mVideos.get(position);

    //Set the views based on the data.
    holder.mVideoTextView.setText(video.getName());

  }

  @Override
  public int getItemCount() {
    return mVideos.size();
  }



  public class VideoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView mVideoTextView;
    private com.example.android.popularmovies.VideoUtils.VideoViewClickListener mVideoViewClickListener;

    public VideoHolder(View itemView, com.example.android.popularmovies.VideoUtils.VideoViewClickListener listener) {
      super(itemView);
      mVideoTextView = itemView.findViewById(R.id.list_item_video_textview);
      mVideoViewClickListener = listener;
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      mVideoViewClickListener.onClick(v, getAdapterPosition());
    }
  }
}