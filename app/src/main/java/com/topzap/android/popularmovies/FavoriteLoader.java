package com.topzap.android.popularmovies;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.topzap.android.popularmovies.data.Movie;
import com.topzap.android.popularmovies.data.MovieContract;

import java.util.ArrayList;

public class FavoriteLoader extends AsyncTaskLoader<ArrayList<Movie>> {

    private static final String TAG = FavoriteLoader.class.getSimpleName();

    private ArrayList<Movie> favoriteMovies = new ArrayList<>();

    public FavoriteLoader(Context context) {
        super(context);
        Log.d(TAG, "FavoriteLoader: favorites constructor called");
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
        Log.d(TAG, "onStartLoading: favorites");
    }

    @Override
    public ArrayList<Movie> loadInBackground() {
        Log.d(TAG, "loadInBackground: favorites");

        Cursor cursor = getContext().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        // Convert the cursor to an ArrayList for output to the Loader
        favoriteMovies = convertFavoritesCursorToArrayList(cursor);
        return favoriteMovies;
    }

    private ArrayList<Movie> convertFavoritesCursorToArrayList(Cursor cursor) {
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            String movieId = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE));
            String posterUrl = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_URL));
            String plot = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_PLOT));
            String userRating = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_USER_RATING));
            String releaseDate = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE));

            favoriteMovies.add(new Movie(movieId, title, posterUrl, plot, userRating, releaseDate));
            cursor.moveToNext();
        }

        return favoriteMovies;
    }
}
