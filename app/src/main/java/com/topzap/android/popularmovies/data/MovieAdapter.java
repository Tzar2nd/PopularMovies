package com.topzap.android.popularmovies.data;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.topzap.android.popularmovies.MovieDetailActivity;
import com.topzap.android.popularmovies.R;

import java.util.ArrayList;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private ArrayList<Movie> mMovies;
    private LayoutInflater mInflater;
    private Context mContext;
    private static final String mIntentFlag = "MOVIE";

    public MovieAdapter(Context context, ArrayList<Movie> movies) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mMovies = movies;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflates the XML layout for each cell, without attaching to the parent view
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Using the Picasso library, bind the image to each cell
        Movie currentMovie = mMovies.get(position);
        Picasso.with(mContext).load(currentMovie.getPosterUrl()).into(holder.mMovieImageView);
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public void clear() {
        // Clear all items. Used when there is no internet connection.
        int size = mMovies.size();
        mMovies.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addAll(ArrayList<Movie> movies) {
        // All all movies into the movie ArrayList
        mMovies = movies;
    }

    // ViewHolder stores and recycles views as they scroll off and back on the screen
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView mMovieImageView;
        final TextView mMovieTextView;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mMovieImageView = itemView.findViewById(R.id.imageView_grid);
            mMovieTextView = itemView.findViewById(R.id.textview_grid);
        }

        @Override
        public void onClick(View view) {
            // Launch the MovieDetailActivity and serialize the current movie by using Parcelable
            Intent intent = new Intent(mContext, MovieDetailActivity.class);
            Movie currentMovie = mMovies.get(getAdapterPosition());

            intent.putExtra(mIntentFlag, currentMovie);

            mContext.startActivity(intent);
        }
    }
}
