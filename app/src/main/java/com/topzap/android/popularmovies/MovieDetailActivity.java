package com.topzap.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.LoaderManager;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.topzap.android.popularmovies.data.Movie;
import com.topzap.android.popularmovies.data.MovieContract;

public class MovieDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private String TAG = MovieDetailActivity.class.getSimpleName();

    private static final String mIntentFlag = "MOVIE";
    private static final int CURSOR_LOADER_ID_QUERY = 5;

    private String movieId;
    private Menu menu;
    private boolean favorite = false;

    Movie currentMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Bundle mMovieData = getIntent().getExtras();

        ImageView posterImageView = findViewById(R.id.movie_detail_image);
        TextView titleTextView = findViewById(R.id.title_body);
        TextView releaseDateTextView = findViewById(R.id.release_date_body);
        TextView userRatingTextView = findViewById(R.id.user_rating_body);
        TextView moviePlotTextView = findViewById(R.id.plot_summary_body);

        if (mMovieData != null) {
            // If there is movie data then get parcelable data from the movie data passed in
            currentMovie = mMovieData.getParcelable(mIntentFlag);

            assert currentMovie != null; // Remove warning on possible null from getPosterUrl
            Picasso.with(this).load(currentMovie.getPosterUrl()).into(posterImageView);

            movieId = currentMovie.getMovieId();
            titleTextView.setText(currentMovie.getTitle());
            releaseDateTextView.setText(currentMovie.getReleaseDate());
            userRatingTextView.setText(currentMovie.getUserRating());
            moviePlotTextView.setText(currentMovie.getPlot());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.detail_menu, this.menu);
        getSupportLoaderManager().initLoader(CURSOR_LOADER_ID_QUERY, null, this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_favorite: {
                if(!favorite) {
                    insertFavoriteMovie();
                } else {
                    deleteFavoriteMovie();
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void insertFavoriteMovie() {
        // Create new empty ContentValues object and insert a new favorite via a ContentResolver
        ContentValues contentValues = new ContentValues();

        // Put the favorite movie data into the ContentValues
        contentValues.put(MovieContract.MovieEntry._ID, movieId);
        contentValues.put(MovieContract.MovieEntry.COLUMN_TITLE, currentMovie.getTitle());
        contentValues.put(MovieContract.MovieEntry.COLUMN_PLOT, currentMovie.getPlot());
        contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_URL, currentMovie.getPosterUrl());
        contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, currentMovie.getReleaseDate());
        contentValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, currentMovie.getUserRating());

        // Insert the content values via a ContentResolver
        Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);

        // Check if the URI has successfully inserted a favorite
        if (uri != null) {
            Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
            favorite = true;
            changeFavoriteIcon(favorite);
        } else {
            Toast.makeText(this, "URI not recognised: " + uri.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteFavoriteMovie() {
        ContentValues contentValues = new ContentValues();

        Uri deleteUri = Uri.withAppendedPath(MovieContract.MovieEntry.CONTENT_URI, movieId);

        // COMPLETED (2) Delete a single row of data using a ContentResolver
        getContentResolver().delete(deleteUri, null, null);

        // COMPLETED (3) Restart the loader to re-query for all tasks after a deletion
        getSupportLoaderManager().restartLoader(CURSOR_LOADER_ID_QUERY, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri favoriteUri = Uri.withAppendedPath(MovieContract.MovieEntry.CONTENT_URI, movieId);

        Log.d(TAG, "onCreateLoader favoriteUri: " + favoriteUri);

        String[] projection = {
                MovieContract.MovieEntry.COLUMN_ID,
                MovieContract.MovieEntry.COLUMN_TITLE};

        return new CursorLoader(this,
                favoriteUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null) {
            Log.d(TAG, "NO DATA FOUND");
        } else {

            // Set the favorite icon and boolean to Yes or No depending on whether it is found
            // in the database.
            if (cursor.moveToFirst()) {
                {
                    Log.d(TAG, "FOUND AS FAVORITE: " + cursor.getString(cursor.getColumnIndex("title")));
                    favorite = true;
                    changeFavoriteIcon(favorite);
                }
            } else {
                Log.d(TAG, "NO FAVORITE FOUND");
                favorite = false;
                changeFavoriteIcon(favorite);
            }
        }

    }

    public void changeFavoriteIcon(boolean favorited) {
        // Set the menuItem to favorite and favorite boolean to true
        if (favorited) {
            MenuItem favoriteItem = menu.findItem(R.id.menu_favorite);
            favoriteItem.setIcon(getResources().getDrawable(R.drawable.ic_favorite_black_24dp));
        } else {
            MenuItem favoriteItem = menu.findItem(R.id.menu_favorite);
            favoriteItem.setIcon(getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
