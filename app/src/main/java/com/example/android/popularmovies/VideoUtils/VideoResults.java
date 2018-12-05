package com.example.android.popularmovies.VideoUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VideoResults {

  // This is the array of videos returned in the retrofit call of DetailActivity.java

  @SerializedName("results")
  @Expose
  private List<Video> mResults = null;

  public List<Video> getVideoList() {
    return mResults;
  }

  public interface VideoAcquiredListener {
    void onVideosAcquired(List<Video> videos);
  }

}
