package com.example.redditimages.redditgram.Adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.redditimages.redditgram.R;
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

public class FeedListAdapter extends RecyclerView.Adapter<FeedListAdapter.PostItemViewHolder> {

    private static final String TAG = FeedListAdapter.class.getSimpleName();

    private ArrayList<FeedFetchUtils.PostItemData> mFeedListData;

    public void updateFeedData(ArrayList<FeedFetchUtils.PostItemData> feedListData) {
        mFeedListData = feedListData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mFeedListData != null) {
            return mFeedListData.size();
        } else {
            return 0;
        }
    }

    /* TODO */
    @Override
    public PostItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.post_item, parent, false);
        return new PostItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostItemViewHolder holder, int position) {
        holder.bind(mFeedListData.get(position));
    }

    class PostItemViewHolder extends RecyclerView.ViewHolder {
        private TextView mPostItemSubredditTextView;
        private TextView mPostItemUserTextView;
        private TextView mPostItemDateTextView;
        private TextView mPostItemTitleTextView;
        private ImageView mPostItemImageView;

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

            mPostItemUserTextView.setText(postItemData.author);
            mPostItemDateTextView.setText(postTime);
            mPostItemSubredditTextView.setText("r/" + postItemData.subreddit);
            mPostItemTitleTextView.setText(postItemData.title);
            if (postItemData.imageUrls != null) {
                new DownloadImageTask(mPostItemImageView).execute(postItemData.imageUrls.get(0));
            }
        }
    }

    public static String timeAgo(FeedFetchUtils.PostItemData postItemData) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("GMT"));

            Date postTime = postItemData.date_time;
            Date currentTime = new Date();

            long seconds= TimeUnit.MILLISECONDS.toSeconds(currentTime.getTime() - postTime.getTime());
            long minutes=TimeUnit.MILLISECONDS.toMinutes(currentTime.getTime() - postTime.getTime());
            long hours=TimeUnit.MILLISECONDS.toHours(currentTime.getTime() - postTime.getTime());
            long days=TimeUnit.MILLISECONDS.toDays(currentTime.getTime() - postTime.getTime());

            if(seconds < 60) {
                return " Just now ";
            }

            if(minutes < 60) {
                if(minutes == 1) {
                    return minutes + " minute ago ";
                }
                return minutes + " minutes ago ";
            }

            else if(hours < 24) {
                if(hours == 1) {
                    return hours + " hour ago ";
                }
                return hours + " hours ago ";
            }

            else {
                if(days == 1) {
                    return  days + " day ago ";
                }
                return days + " days ago ";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "time unavailable ";
    }
}
