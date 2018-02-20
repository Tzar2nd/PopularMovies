package com.topzap.android.popularmovies;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.topzap.android.popularmovies.data.Movie;
import com.topzap.android.popularmovies.utils.NetworkUtils;

import java.util.ArrayList;

public class MovieLoader extends AsyncTaskLoader<ArrayList<Movie>> {

    private static final String TAG = MovieLoader.class.getSimpleName();

    boolean isStarted;

    // Query URL
    private String url;

    public MovieLoader(Context context, String url) {
        super(context);
        isStarted = true;
        this.url = url;
        Log.d(TAG, "Movie constructor called");
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading called");

        // Avoid a forceLoad() if onStartLoading is triggered by the activity lifecycle not on
        // the user on a back button from a detail activity to the main activity.
        if (isStarted) {
            Log.d(TAG, "onStartLoading: forceLoad() called");
            forceLoad();
        }
    }

    /**
     * Obtain the actual movie data from the background thread
     *
     * @return movies a list of all movies
     */

    @Override
    public ArrayList<Movie> loadInBackground() {
        Log.d(TAG, "loadinBackground called");
        if(this.url == null) {
            return null;
        }

        ArrayList<Movie> movies = NetworkUtils.getMovieData(url);
        isStarted = false;  // Don't force any unnecesasry lifecycle loads without the constructor being called
        return movies;
    }
}
