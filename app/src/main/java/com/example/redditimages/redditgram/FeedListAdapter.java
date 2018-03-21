package com.example.redditimages.redditgram;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.redditimages.redditgram.Utils.DownloadImageTask;
import com.example.redditimages.redditgram.Utils.FeedFetchUtils;

import java.util.ArrayList;

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
        private TextView mPostItemTitleTextView;
        private ImageView mPostItemImageView;

        public PostItemViewHolder(View itemView) {
            super(itemView);
            mPostItemTitleTextView = (TextView)itemView.findViewById(R.id.tv_post_item_title);
            mPostItemImageView = (ImageView)itemView.findViewById(R.id.iv_post_item_image);
        }

        /* TODO: Currently only binds first image, maybe add onSwipeListener for multiple images */
        public void bind(FeedFetchUtils.PostItemData postItemData) {
            mPostItemTitleTextView.setText(postItemData.title);
            if (postItemData.imageUrls != null) {
                new DownloadImageTask(mPostItemImageView).execute(postItemData.imageUrls.get(0));
            }
        }
    }
}
