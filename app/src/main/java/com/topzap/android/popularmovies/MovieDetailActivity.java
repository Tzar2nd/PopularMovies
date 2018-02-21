package com.topzap.android.popularmovies;

import android.annotation.SuppressLint;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.content.Loader;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.LoaderManager;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.topzap.android.popularmovies.data.Movie;
import com.topzap.android.popularmovies.data.MovieContract;
import com.topzap.android.popularmovies.data.Review;
import com.topzap.android.popularmovies.utils.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;

public class MovieDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<ArrayList<Movie>> {

    private String TAG = MovieDetailActivity.class.getSimpleName();

    private static final String mIntentFlag = "MOVIE";
    private static final int FAVORITE_LOADER_ID = 2;
    private String movieId;
    private Menu menu;
    private boolean favorite = false;

    TextView movieReviewsTextView;

    ArrayList<Review> reviews = new ArrayList<>();
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
        movieReviewsTextView = findViewById(R.id.movie_reviews);

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

        // Launch Async task to obtain
        new getReviewsTask().execute(movieId);
    }

    /**
     * Async Task for obtaining reviews.
     */

    @SuppressLint("StaticFieldLeak")
    public class getReviewsTask extends AsyncTask<String, Void, ArrayList<Review>> {

        private final String TAG = MovieDetailActivity.class.getClass().getSimpleName();

        @Override
        protected ArrayList<Review> doInBackground(String... movieId) {
            ArrayList<Review> reviewResult = new ArrayList<>();

            URL reviewUrl = NetworkUtils.createReviewUrl(movieId[0]);
            reviewResult.addAll(NetworkUtils.getReviewData(reviewUrl.toString()));

            Log.d(TAG, "doInBackground: reviewsSize " + reviewResult.size());

            return reviewResult;
        }

        @Override
        protected void onPostExecute(ArrayList<Review> reviewResult) {
            reviews.addAll(reviewResult);
            for (Review review: reviews) {
                Log.d(TAG, "onPostExecute: " + review.getAuthor());

                movieReviewsTextView.append(review.getAuthor() + "\n\n");
                movieReviewsTextView.append(review.getContent() + "\n\n");
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.detail_menu, this.menu);
        getLoaderManager().initLoader(FAVORITE_LOADER_ID, null, this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_favorite: {
                if (!favorite) {
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
            Toast.makeText(getBaseContext(), R.string.favorite_added, Toast.LENGTH_SHORT).show();
            favorite = true;
            showFavoriteIcon(favorite);
        }

        getLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, this);
    }

    public void deleteFavoriteMovie() {
        Uri deleteUri = Uri.withAppendedPath(MovieContract.MovieEntry.CONTENT_URI, movieId);

        // COMPLETED (2) Delete a single row of data using a ContentResolver
        getContentResolver().delete(deleteUri, null, null);

        // COMPLETED (3) Restart the loader to re-query for all tasks after a deletion
        getLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, this);
    }

    @Override
    public Loader<ArrayList<Movie>> onCreateLoader(int id, Bundle args) {
        Uri favoriteUri = Uri.withAppendedPath(MovieContract.MovieEntry.CONTENT_URI, movieId);

        Log.d(TAG, "onCreateLoader favoriteUri: " + favoriteUri);

        return new FavoriteLoader(MovieDetailActivity.this, favoriteUri);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> movies) {

        if (movies.size() < 1) {
            Log.d(TAG, "NO FAVORITE DATA FOUND");
            favorite = false;
            showFavoriteIcon(favorite);
        } else {

            // Set the favorite icon and boolean to Yes or No depending on whether it is found
            // in the database.
            if (movies.size() > 0) {
                for(Movie currentMovie : movies) {
                    Log.d(TAG, "FOUND AS FAVORITE: " + currentMovie.getTitle());
                }
                favorite = true;
                showFavoriteIcon(favorite);
            } else {
                favorite = false;
                Log.d(TAG, "NOT A FAVORITE");
                showFavoriteIcon(favorite);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Movie>> loader) {
    }

    public void showFavoriteIcon(boolean isFavorite) {
        favorite = isFavorite;
        // Set the menuItem to favorite and favorite boolean to true
        if (favorite) {
            MenuItem favoriteItem = menu.findItem(R.id.menu_favorite);
            favoriteItem.setIcon(getResources().getDrawable(R.drawable.ic_favorite_black_24dp));
        } else {
            MenuItem favoriteItem = menu.findItem(R.id.menu_favorite);
            favoriteItem.setIcon(getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
        }
    }
}
