package com.topzap.android.popularmovies.utils;

import android.net.Uri;
import android.util.Log;

import com.topzap.android.popularmovies.BuildConfig;
import com.topzap.android.popularmovies.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public final class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getName();

    private static final String MOVIE_BASE_SCHEME = "https";
    private static final String MOVIE_BASE_URL = "api.themoviedb.org";
    private static final String MOVIE_API_VERSION = "3";
    private static final String MOVIE_BASE_CATEGORY = "movie";
    private static final String MOVIE_API_TAG = "api_key";

    // TODO: Udacity please enter your TMDB API key in your gradle.properties file
    private static final String MOVIE_API_KEY = BuildConfig.API_KEY;

    private static final String MOVIE_CHAR_SET = "UTF-8";

    private static final String MOVIE_IMAGE_URL_PREFIX = "http://image.tmdb.org/t/p/w185/";

    private String movieFilter;

    /**
     * Empty constructor so that this class cannot be called directly and only
     * its public methods used
     */
    private NetworkUtils() {
    }

    /**
     * Query the TMDB dataset and return a list of Movie objects
     */

    public static ArrayList<Movie> getMovieData(String stringUrl) {
        // Create URL object
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Perform a HTTP response to the URL and receive a JSON response in return
        String jsonResponse = null;

        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Error making the HTTP request", e);
        }

        // Extract the relevant fields from the JSON response and create a list of movies
        ArrayList<Movie> movies = extractMovieDataFromJSON(jsonResponse);

        return movies;
    }

    private static ArrayList<Movie> extractMovieDataFromJSON(String movieJSON) {
        ArrayList<Movie> movies = new ArrayList<>();

        // Set the defaults
        String movieId;
        String title;
        String posterUrl;
        String plot;
        String userRating;
        String releaseDate;

        /* Parse the JSON response. If there is a problem with the way the JSON is formatted then
        a JSON exception will be thrown
         */

        try {
            // Get the JSON Object from the String
            JSONObject jsonObject = new JSONObject(movieJSON);

            // Obtain the 'results' node as each individual movie is an array with that name
            JSONArray jsonMovies = jsonObject.getJSONArray("results");

            // Loop through each movie and add to the movies ArrayList
            for (int i = 0; i < jsonMovies.length(); i++) {
                JSONObject jsonMovie = jsonMovies.getJSONObject(i);

                movieId = jsonMovie.getString("id");
                title = jsonMovie.getString("title");
                posterUrl = jsonMovie.getString("poster_path");
                posterUrl = MOVIE_IMAGE_URL_PREFIX + posterUrl;     // Add default required full URL prefix

                plot = jsonMovie.getString("overview");
                userRating = jsonMovie.getString("vote_average");
                releaseDate = jsonMovie.getString("release_date");

                movies.add(new Movie(movieId, title, posterUrl, plot, userRating, releaseDate));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Problem with parsing the TMDB movie results");
        }
        return movies;
    }

    /**
     * Builds the URL and validates it by using Uri builder
     *
     * @param movieFilter Either "popular" or "top_rated" tag as per user shared preferences.
     * @return url
     */

    public static URL createUrl(String movieFilter) {
        Uri.Builder builder = new Uri.Builder();

        // Obtain the API key from the resources without passing through the context
        builder.scheme(MOVIE_BASE_SCHEME)
                .authority(MOVIE_BASE_URL)
                .appendPath(MOVIE_API_VERSION)
                .appendPath(MOVIE_BASE_CATEGORY)
                .appendPath(movieFilter)
                .appendQueryParameter(MOVIE_API_TAG, MOVIE_API_KEY);

        URL url = null;
        try {
            url = new URL(builder.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    /**
     * Make a HTTP request from the TMDB and return a String response, ready to be parsed
     */

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponseString = "";

        // If the url is null then return early
        if (url == null) {
            return jsonResponseString;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        // Create an HTTP connection, setting the timeouts and try to obtain a 200 OK connection
        // Return an InputStream from the connection
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                Log.d(TAG, "Connection successful (200)");
                inputStream = urlConnection.getInputStream();
                jsonResponseString = readFromStreamJSON(inputStream);
            } else {
                Log.e(TAG, "Unable to connect, error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem receiving TMDB query due to connection failure", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponseString;
    }

    /**
     * Convert the InputStream into a String which contains the whole JSON response from the server
     */

    private static String readFromStreamJSON(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream, Charset.forName(MOVIE_CHAR_SET));

            // BufferedReader takes an inputstream in chunks
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line);
                line = reader.readLine();
            }
        }
        String stringJSON = stringBuilder.toString();

        return stringJSON;
    }
}
