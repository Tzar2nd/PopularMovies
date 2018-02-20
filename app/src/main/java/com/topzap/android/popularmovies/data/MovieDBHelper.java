package com.topzap.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.topzap.android.popularmovies.data.MovieContract.MovieEntry;

public class MovieDBHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = MovieDBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 2;

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create the database and favorites table
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " +
                MovieEntry.TABLE_FAVORITES + "(" +
                MovieEntry.COLUMN_ID + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_PLOT + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_URL + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_USER_RATING + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    // Upgrade the database when the version number is changed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " +
                newVersion + ". OLD DATA WILL BE DESTROYED");

        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_FAVORITES);

        onCreate(db);
    }
}
