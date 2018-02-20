package com.topzap.android.popularmovies;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.topzap.android.popularmovies.data.Movie;
import com.topzap.android.popularmovies.utils.NetworkUtils;

import java.util.ArrayList;

public class MovieLoader extends AsyncTaskLoader<ArrayList<Movie>> {

    private static final String TAG = MovieLoader.class.getName();

    // Query URL
    private String url;

    public MovieLoader(Context context, String url) {
        super(context);
        this.url = url;
        Log.d(TAG, "Movie constructor called");
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
        Log.d(TAG, "onStartLoading called");
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
        return movies;
    }
}
