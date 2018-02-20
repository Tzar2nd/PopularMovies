package com.topzap.android.popularmovies;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.topzap.android.popularmovies.data.Movie;
import com.topzap.android.popularmovies.data.MovieAdapter;
import com.topzap.android.popularmovies.utils.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;

public class LibraryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Movie>> {

    private static final String TAG = LibraryActivity.class.getSimpleName();

    // Loader for Network calls that returns an ArrayList of movies from network
    // and favorites from cursor (then conerted to an ArrayList)
    private static final int MOVIE_LOADER_ID = 1;
    private static final int FAVORITE_LOADER_ID = 2;

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mErrorTextView;
    private MovieAdapter mMovieAdapter;
    private ArrayList<Movie> movies = new ArrayList<>();
    Spinner spinner;
    int spinnerPos;
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
        int numberOfColumns = getResources().getInteger(R.integer.gallery_columns);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        mRecyclerView.setHasFixedSize(true);
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

    private void startAdapter() {
        Log.d(TAG, "startAdapter: Adapter refresh started");
        if (mMovieAdapter == null) {
            mMovieAdapter = new MovieAdapter(this, movies);
        }

        if (mMovieFilter != null && mMovieFilter.equals("favorites")) {
            mMovieAdapter.clear();
        }

        mRecyclerView.setAdapter(mMovieAdapter);
        mMovieAdapter.notifyDataSetChanged();

    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSaveInstanceState: Saving Instance State, spinner: " + spinner.getSelectedItemPosition());

        outState.putInt("Spinner", spinner.getSelectedItemPosition());
    }

    @Override
    protected void onResume() {
        super.onResume();

        startAdapter();

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            Log.d(TAG, "onRestoreInstanceState: Restore Instance State, spinner: "
                    + savedInstanceState.getInt("Spinner", 0));
            spinnerPos = savedInstanceState.getInt("Spinner", 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the spinner drop down menu for the sort options and attach an adapter to it
        // that has default android menu item view but a custom initial view to enforce white text
        // on the appcompat toolbar
        Log.d(TAG, "onCreateOptionsMenu: Started - " + spinnerPos);
        getMenuInflater().inflate(R.menu.library_menu, menu);

        MenuItem spinnerMenuItem = menu.findItem(R.id.menu_library_spinner);
        spinner = (Spinner) spinnerMenuItem.getActionView();

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this, R.array.movie_filter_array,
                        R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(spinnerPos);

        AdapterView.OnItemSelectedListener spinnerSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                spinnerPos = position;
                Log.d(TAG, "onItemSelected: Initialised");

                // Call to a routine to check which loader to refresh depending on filter options
                initializeLoaders();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinner.setSelection(spinnerPos, true);
        spinner.setOnItemSelectedListener(spinnerSelectedListener);

        String[] filterValue = getResources().getStringArray(R.array.movie_filter_value);
        mMovieFilter = filterValue[0];
        initializeLoaders();

        return true;
    }

    public void initializeLoaders() {
        String[] filterValue = getResources().getStringArray(R.array.movie_filter_value);
        mMovieFilter = filterValue[spinnerPos];

        Log.d(TAG, "initializeLoaders: MovieFilter = " + mMovieFilter);
        mErrorTextView.setVisibility(View.VISIBLE);

        if (mMovieFilter != null) {
            // Start or refresh the Popular / Top Rated Loader
            if (mMovieFilter.equals("popular") || mMovieFilter.equals("top_rated")) {
                if (getLoaderManager().getLoader(MOVIE_LOADER_ID) == null) {
                    Log.d(TAG, "initializeLoaders: MOVIE_LOADER_ID");
                    getLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
                } else {
                    Log.d(TAG, "restartingLoader: MOVIE_LOADER_ID");
                    getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
                }
            }
        }

        // Start or Refresh the Favorites Loader (interrogates Cursor instead of Network)
        if (mMovieFilter.equals("favorites")) {
            if (getLoaderManager().getLoader(FAVORITE_LOADER_ID) == null) {
                Log.d(TAG, "initializeLoaders: FAVORITE_LOADER_ID");
                getLoaderManager().initLoader(FAVORITE_LOADER_ID, null, this);
            } else {
                Log.d(TAG, "restartLoaders: FAVORITE_LOADER_ID");
                getLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, this);
            }
        }
    }

    @Override
    public Loader<ArrayList<Movie>> onCreateLoader(int id, Bundle args) {
        // Build the TMDB url and begin a new MovieLoader
        Log.d(TAG, "onCreateLoader: Movies: Started");
        URL url = NetworkUtils.createUrl(mMovieFilter);

        switch (id) {
            case MOVIE_LOADER_ID:
                return new MovieLoader(LibraryActivity.this, url.toString());

            case FAVORITE_LOADER_ID:
                return new FavoriteLoader(LibraryActivity.this);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> movies) {
        // When finished check if the internet is enabled first and that movies has some items
        int id = loader.getId();
        Log.d(TAG, "onLoadFinished: Movies: Finished: movies = " +
                movies.size() + " - " + loader.getId() + " - Spinner: " + spinnerPos);
        mMovieAdapter.clear();

        if (!checkInternetConnection()) {
            displayItemsNotFound();
        } else if (movies.size() > 0) {
            displayItemsFound();
            mMovieAdapter.addAll(movies);
            mMovieAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.d(TAG, "onLoaderReset: Movies: Loader Reset called");
        mRecyclerView.setAdapter(null);
    }
}


