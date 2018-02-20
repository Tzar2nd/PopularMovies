package com.topzap.android.popularmovies.data;

// POJO for a Movie object that enforces parcelable so objects can be sent between activities

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {

    private String movieId;
    private String title;
    private String posterUrl;
    private String plot;
    private String userRating;
    private String releaseDate;

    public Movie(String movieId, String title, String posterUrl, String plot,
                 String userRating, String releaseDate) {
        this.movieId = movieId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.plot = plot;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() { return posterUrl; }

    public String getPlot() {
        return plot;
    }

    public String getUserRating() {
        return userRating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getMovieId() { return movieId; }

    private Movie(Parcel in) {
        movieId = in.readString();
        title = in.readString();
        posterUrl = in.readString();
        plot = in.readString();
        userRating = in.readString();
        releaseDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieId);
        dest.writeString(title);
        dest.writeString(posterUrl);
        dest.writeString(plot);
        dest.writeString(userRating);
        dest.writeString(releaseDate);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
