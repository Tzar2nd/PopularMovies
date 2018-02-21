package com.topzap.android.popularmovies.data;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.topzap.android.popularmovies.R;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private ArrayList<Review> mReviews = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context mContext;

    public ReviewAdapter(Context context, ArrayList<Review> reviews) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mReviews = reviews;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the XML layout for each review
        View view = mInflater.inflate(R.layout.recyclerview_review_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Review currentReview = mReviews.get(position);

        holder.mAuthorTextView.setText(currentReview.getAuthor());
        holder.mContentTextView.setText(currentReview.getContent());
    }

    public void addAll(ArrayList<Review> reviews) {
        mReviews = reviews;
    }

    public void clear() {
        mReviews.clear();
        notifyItemRangeRemoved(0, mReviews.size());
    }

    @Override
    public int getItemCount() {
        return mReviews.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView mAuthorTextView;
        final TextView mContentTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mAuthorTextView = itemView.findViewById(R.id.review_author);
            mContentTextView = itemView.findViewById(R.id.review_content);
        }

        @Override
        public void onClick(View v) {
            // TODO: Display the full review in a dialog box
            Review currentReview = mReviews.get(getAdapterPosition());

            Dialog dialog = new Dialog(mContext, R.style.Theme_AppCompat_Light_Dialog_Alert) {
                @Override
                public boolean onTouchEvent(MotionEvent event) {
                    // Tap anywhere to close dialog.
                    this.dismiss();
                    return true;
                }
            };

            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(mContext.getResources().getColor(R.color.colorReviewPopup)));

            dialog.setContentView(R.layout.activity_movie_detail_review_popup);
            TextView reviewText = dialog.findViewById(R.id.review_popup_textview);
            reviewText.setText(currentReview.getContent());
            dialog.show();

        }
    }
}
