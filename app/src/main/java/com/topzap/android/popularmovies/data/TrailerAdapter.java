package com.topzap.android.popularmovies.data;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.topzap.android.popularmovies.R;

import java.util.ArrayList;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.ViewHolder> {

    private ArrayList<Trailer> mTrailers = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public TrailerAdapter(Context context, ArrayList<Trailer> trailers) {
        mTrailers = trailers;
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.recyclerview_trailer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Trailer currentTrailer = mTrailers.get(position);

        holder.mTrailerTextView.setText(currentTrailer.getName());
    }

    public void clear() {
        mTrailers.clear();
        notifyItemRangeRemoved(0, mTrailers.size());
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private void watchYoutubeVideo(String id){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            mContext.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            mContext.startActivity(webIntent);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView mTrailerImageView;
        final TextView mTrailerTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTrailerImageView = itemView.findViewById(R.id.trailer_play_icon);
            mTrailerTextView = itemView.findViewById(R.id.trailer_textview);
        }

        @Override
        public void onClick(View v) {

            Trailer currentTrailer = mTrailers.get(getAdapterPosition());
            watchYoutubeVideo(currentTrailer.getSource());
        }


    }
}
