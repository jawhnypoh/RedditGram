package com.example.redditimages.redditgram.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.redditimages.redditgram.DetailedImageActivity;
import com.example.redditimages.redditgram.R;
import com.example.redditimages.redditgram.SettingsActivity;
import com.example.redditimages.redditgram.Utils.DownloadImageTask;
import com.example.redditimages.redditgram.Utils.FeedFetchUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by jerrypeng on 3/11/18.
 */

public class FeedListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final String TAG = FeedListAdapter.class.getSimpleName();

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static Boolean isLoadingAdded = false;
    Context mContext;

    private ArrayList<FeedFetchUtils.PostItemData> mFeedListData;

    public FeedListAdapter(Context context) {
        mContext = context;
    }

    public void updateFeedData(ArrayList<FeedFetchUtils.PostItemData> feedListData) {
        if (mFeedListData == null) {
            mFeedListData = feedListData;
            notifyDataSetChanged();
        }
        else {
            for (int i = 0; i < feedListData.size(); i++) {
                mFeedListData.add(feedListData.get(i));
                notifyItemInserted(mFeedListData.size() - 1);
            }
        }
    }

    public void reloadFeedData(ArrayList<FeedFetchUtils.PostItemData> feedListData) {
        mFeedListData = feedListData;
        notifyDataSetChanged();
    }

    public void clearAllData() {
        mFeedListData.removeAll(mFeedListData);
        notifyDataSetChanged();
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        mFeedListData.add(new FeedFetchUtils.PostItemData());
        notifyItemInserted(mFeedListData.size() - 1);
    }
    public void removeLoadingFooter() {
        isLoadingAdded = false;
        Log.d(TAG, String.valueOf(mFeedListData.size()));
        mFeedListData.remove(mFeedListData.size() - 1);
        notifyItemRemoved(mFeedListData.size());
    }

    @Override
    public int getItemCount() {
        if (mFeedListData != null) {
            return mFeedListData.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == mFeedListData.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    /* TODO */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case ITEM:
                view = inflater.inflate(R.layout.post_item, parent, false);
                viewHolder = new PostItemViewHolder(view);
                break;
            case LOADING:
                view = inflater.inflate(R.layout.progress_item, parent, false);
                viewHolder = new LoadingViewHolder(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FeedFetchUtils.PostItemData item = mFeedListData.get(position);
        switch (getItemViewType(position)) {
            case ITEM:
                ((PostItemViewHolder)holder).bind(item);
                break;
            case LOADING:
                ((LoadingViewHolder)holder).mLoadingBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    public static String timeAgo(FeedFetchUtils.PostItemData postItemData) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("GMT"));

            Date postTime = postItemData.date_time;
            Date currentTime = new Date();

            long seconds = TimeUnit.MILLISECONDS.toSeconds(currentTime.getTime() - postTime.getTime());
            long minutes = TimeUnit.MILLISECONDS.toMinutes(currentTime.getTime() - postTime.getTime());
            long hours = TimeUnit.MILLISECONDS.toHours(currentTime.getTime() - postTime.getTime());
            long days = TimeUnit.MILLISECONDS.toDays(currentTime.getTime() - postTime.getTime());

            if (seconds < 60) {
                return " Just now ";
            }

            if (minutes < 60) {
                if (minutes == 1) {
                    return minutes + " minute ago ";
                }
                return minutes + " minutes ago ";
            } else if (hours < 24) {
                if (hours == 1) {
                    return hours + " hour ago ";
                }
                return hours + " hours ago ";
            } else {
                if (days == 1) {
                    return days + " day ago ";
                }
                return days + " days ago ";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "time unavailable ";
    }

    class PostItemViewHolder extends RecyclerView.ViewHolder {
        private TextView mPostItemSubredditTextView;
        private TextView mPostItemUserTextView;
        private TextView mPostItemDateTextView;
        private TextView mPostItemTitleTextView;
        private ImageView mPostItemImageView;
        private ImageView mPostItemBackground;
        public String imageUrl;

        public PostItemViewHolder(View itemView) {
            super(itemView);
            mPostItemSubredditTextView = (TextView) itemView.findViewById(R.id.tv_post_item_subreddit);
            mPostItemUserTextView = (TextView) itemView.findViewById(R.id.tv_post_user);
            mPostItemDateTextView = (TextView) itemView.findViewById(R.id.tv_post_date);
            mPostItemTitleTextView = (TextView)itemView.findViewById(R.id.tv_post_item_title);
            mPostItemImageView = (ImageView)itemView.findViewById(R.id.iv_post_item_image);
        }

        /* TODO: Currently only binds first image, maybe add onSwipeListener for multiple images */
        public void bind(FeedFetchUtils.PostItemData postItemData) {
            String postTime = timeAgo(postItemData);

            if (postItemData.imageUrls != null) {
                Bitmap b = Bitmap.createBitmap(postItemData.imageWidths.get(0), postItemData.imageHeights.get(0), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(b);
                Paint p = new Paint();
                p.setColor(Color.GRAY);
                p.setStyle(Paint.Style.FILL);
                c.drawRect(0, 0, postItemData.imageWidths.get(0), postItemData.imageHeights.get(0), p);
                mPostItemImageView.setImageBitmap(b);
            }

            Animation myFadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
            mPostItemImageView.startAnimation(myFadeInAnimation); //Set animation to your ImageView

            mPostItemUserTextView.setText("u/" + postItemData.author);
            mPostItemDateTextView.setText(postTime);
            mPostItemSubredditTextView.setText("r/" + postItemData.subreddit);
            mPostItemTitleTextView.setText(postItemData.title);

            if (postItemData.imageUrls != null) {
                imageUrl = postItemData.imageUrls.get(0);
                new DownloadImageTask(mPostItemImageView).execute(postItemData.imageUrls.get(0));
            }

            mPostItemImageView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent DetailedImageIntent = new Intent(v.getContext(), DetailedImageActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putString("image", imageUrl);
                    DetailedImageIntent.putExtras(mBundle);
                    v.getContext().startActivity(DetailedImageIntent);
                }
            });
        }
    }


    class LoadingViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar mLoadingBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            mLoadingBar = (ProgressBar) itemView.findViewById(R.id.load_more_progress);
        }
    }
}
