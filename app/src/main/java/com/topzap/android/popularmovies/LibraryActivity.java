package com.topzap.android.popularmovies;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.stetho.Stetho;
import com.topzap.android.popularmovies.data.Movie;
import com.topzap.android.popularmovies.data.MovieAdapter;
import com.topzap.android.popularmovies.data.MovieContract;
import com.topzap.android.popularmovies.utils.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;

public class LibraryActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<ArrayList<Movie>> {

    private static final String TAG = LibraryActivity.class.getSimpleName();

    // Loader for Network calls that returns an ArrayList of movies
    private static final int MOVIE_LOADER_ID = 1;

    // Loader for Favorites that returns a Cursor of favorite movies
    private static final int FAVORITE_LOADER_ID = 2;

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mErrorTextView;
    private MovieAdapter mMovieAdapter;
    private ArrayList<Movie> movies = new ArrayList<>();
    Spinner spinner;
    int spinnerPos;

    boolean userIsInteracting;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;

    private LoaderManager.LoaderCallbacks<Cursor> favoriteLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Cursor>() {

            ArrayList<Movie> favoriteMovies = new ArrayList<>();
                
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    Log.d(TAG, "onCreateLoader: created");
                    return new CursorLoader(LibraryActivity.this,
                            MovieContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                    if(favoriteMovies != null) {
                        favoriteMovies.clear();
                    }
                    
                    favoriteMovies = convertFavoritesCursorToArrayList(cursor);
                    mMovieAdapter.clear();

                    if (favoriteMovies != null) {
                        Log.d(TAG, "onLoadFinished: items found");
                        mMovieAdapter.addAll(favoriteMovies);
                        displayItemsFound();
                    } else {
                        Log.d(TAG, "onLoadFinished: items not found");
                        displayItemsNotFound();
                    }
                    mMovieAdapter.notifyDataSetChanged();
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    Log.d(TAG, "onLoaderReset: reset");
                    mRecyclerView.setAdapter(null);
                }

                private ArrayList<Movie> convertFavoritesCursorToArrayList(Cursor cursor) {
                    cursor.moveToFirst();

                    while(!cursor.isAfterLast()) {
                        String movieId = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_ID));
                        String title  = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE));
                        String posterUrl = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_URL));
                        String plot = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_PLOT));
                        String userRating = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_USER_RATING));
                        String releaseDate = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE));

                        favoriteMovies.add(new Movie(movieId, title, posterUrl, plot, userRating, releaseDate));
                        cursor.moveToNext();
                    }

                    return favoriteMovies;
                }
            };

    String mMovieFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        // Enable Stetho integration for inspecting my database
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());

        // Assign the progress bar and error text view
        mProgressBar = findViewById(R.id.progress_bar);
        mErrorTextView = findViewById(R.id.tv_library_error_message);

        // Set up RecyclerView and attach the movie adapter. Get the column numbers according to the screen orientation
        mRecyclerView = findViewById(R.id.recyclerview_movies);
        startAdapter();

    }

    private boolean checkInternetConnection() {
        // Check the status of the network connection
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo;

        // Get details on the current active default data network
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    private void displayItemsFound() {
        // Hide progress bar and error message if an item is found
        mProgressBar.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
    }

    private void displayItemsNotFound() {
        // Hide progress bar but show error message if no item is found e.g. no connection
        mProgressBar.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        getLoaderManager().destroyLoader(MOVIE_LOADER_ID);
        getLoaderManager().destroyLoader(FAVORITE_LOADER_ID);

        // Save the state of the recyclerview including scroll position
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }

    private void startAdapter() {
        int numberOfColumns = getResources().getInteger(R.integer.gallery_columns);
        mMovieAdapter = new MovieAdapter(this, movies);
        mRecyclerView.setAdapter(mMovieAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        mRecyclerView.setHasFixedSize(true);
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("Spinner", spinner.getSelectedItemPosition());

        Parcelable mListState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(KEY_RECYCLER_STATE, mListState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null) {
            spinnerPos = savedInstanceState.getInt("Spinner", 0);

            Parcelable mListState = savedInstanceState.getParcelable(KEY_RECYCLER_STATE);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mListState);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get the loaders set back up again
        initializeLoaders();

        // Adapter initialisation moved to onResume() as the adapter must be reinitialised every
        // time the user returns to this LibraryActivity
        startAdapter();

        if (spinner != null) {
            Log.d(TAG, "onResume: Setting spinner at " + spinner);
            spinner.setSelection(spinnerPos);
        }

        // Restore recyclerview state including scroll position
        if(mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the spinner drop down menu for the sort options and attach an adapter to it
        // that has default android menu item view but a custom initial view to enforce white text
        // on the appcompat toolbar
        Log.d(TAG, "onCreateOptionsMenu: Created");
        getMenuInflater().inflate(R.menu.library_menu, menu);

        MenuItem spinnerMenuItem = menu.findItem(R.id.menu_library_spinner);
        spinner = (Spinner) spinnerMenuItem.getActionView();

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this, R.array.movie_filter_array,
                        R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        AdapterView.OnItemSelectedListener spinnerSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: Initialised");

                if(!userIsInteracting) {
                    spinner.setSelection(spinnerPos);
                }

                String[] filterValue = getResources().getStringArray(R.array.movie_filter_value);
                mMovieFilter = filterValue[position];

                // Call to a routine to check which loader to refresh depending on filter options
                initializeLoaders();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spinner.setSelection(Adapter.NO_SELECTION, false); // Prevents triggering incorrectly on activity creation
        spinner.setOnItemSelectedListener(spinnerSelectedListener);
        
        String[] filterValue = getResources().getStringArray(R.array.movie_filter_value);
        mMovieFilter = filterValue[0];
        initializeLoaders();

        return true;
    }

    public void initializeLoaders() {
        Log.d(TAG, "initializeLoaders: MovieFilter = " + mMovieFilter);
        if (mMovieFilter != null) {
            // Check for an available internet connection before starting the LoaderManager
            if (checkInternetConnection()) {
                mErrorTextView.setVisibility(View.INVISIBLE);

                // Start or refresh the Popular / Top Rated Loader
                if (mMovieFilter.equals("popular") || mMovieFilter.equals("top_rated")) {
                    if(getLoaderManager().getLoader(MOVIE_LOADER_ID) == null) {
                        getLoaderManager().initLoader(MOVIE_LOADER_ID, null, LibraryActivity.this);
                    } else {
                        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, LibraryActivity.this);
                    }
                }
            } else {
                displayItemsNotFound();
            }

            // Start or Refresh the Favorites Loader (interrogates Cursor instead of Network)
            if (mMovieFilter.equals("favorites")) {
                if (getLoaderManager().getLoader(FAVORITE_LOADER_ID) == null) {
                    getLoaderManager().initLoader(FAVORITE_LOADER_ID, null, favoriteLoaderCallbacks);
                } else {
                    getLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, favoriteLoaderCallbacks);
                }
            }
        }
    }

    @Override
    public Loader<ArrayList<Movie>> onCreateLoader(int id, Bundle args) {
        // Build the TMDB url and begin a new MovieLoader
        Log.d(TAG, "onCreateLoader: Started");
        URL url = NetworkUtils.createUrl(mMovieFilter);
        return new MovieLoader(this, url.toString());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> movies) {
        // When finished check if the internet is enabled first and that movies has some items
        int id = loader.getId();
        Log.d(TAG, "onLoadFinished: Finished");

        if (id == MOVIE_LOADER_ID) {
            if (!checkInternetConnection()) {
                mMovieAdapter.clear();
                displayItemsNotFound();
            } else {
                if (movies != null) {
                    displayItemsFound();
                    mMovieAdapter.addAll(movies);
                    mMovieAdapter.notifyDataSetChanged();
                } else {
                    mMovieAdapter.clear();
                    displayItemsNotFound();
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mRecyclerView.setAdapter(null);
    }
}
