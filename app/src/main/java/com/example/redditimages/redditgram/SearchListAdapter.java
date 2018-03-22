package com.example.redditimages.redditgram;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.redditimages.redditgram.Utils.FeedFetchUtils;
import com.example.redditimages.redditgram.Utils.SubredditSearchUtils;

import java.util.ArrayList;

/**
 * Created by jerrypeng on 3/20/18.
 */

public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.SubredditItemViewHolder> {

    private static final String TAG = SearchListAdapter.class.getSimpleName();

    private ArrayList<SubredditSearchUtils.SubredditItem> mSearchListData;
    private Context mContext;
    private OnSubredditAddListener mSubredditAddListener;

    public SearchListAdapter(Context context, OnSubredditAddListener subredditAddListener) {
        mContext = context;
        mSubredditAddListener = subredditAddListener;
    }

    public interface OnSubredditAddListener {
        void onSubredditAdd(SubredditSearchUtils.SubredditItem subredditItem);
    }

    public void updateSubredditSearchData(ArrayList<SubredditSearchUtils.SubredditItem> searchListData) {
        mSearchListData = searchListData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mSearchListData != null) {
            return mSearchListData.size();
        } else {
            return 0;
        }
    }

    @Override
    public SearchListAdapter.SubredditItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.search_subreddit_item, parent, false);
        return new SearchListAdapter.SubredditItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchListAdapter.SubredditItemViewHolder holder, int position) {
        holder.bind(mSearchListData.get(position));
    }

    class SubredditItemViewHolder extends RecyclerView.ViewHolder {

        private TextView mSearchSubredditNameTextView;
        private TextView mSearchSubredditCategoryTextView;
        private ImageButton mSearchSubredditAddImageButton;

        public SubredditItemViewHolder(View itemView) {
            super(itemView);
            mSearchSubredditNameTextView = (TextView) itemView.findViewById(R.id.tv_search_subreddit_name);
            mSearchSubredditCategoryTextView = (TextView) itemView.findViewById(R.id.tv_search_subreddit_category);
            mSearchSubredditAddImageButton = (ImageButton) itemView.findViewById(R.id.ib_add_subreddit_button);

            mSearchSubredditAddImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SubredditSearchUtils.SubredditItem subredditItem = mSearchListData.get(getAdapterPosition());
                    mSubredditAddListener.onSubredditAdd(subredditItem);
                }
            });
        }

        public void bind(SubredditSearchUtils.SubredditItem subredditItem) {
            mSearchSubredditNameTextView.setText(subredditItem.name);
            mSearchSubredditCategoryTextView.setText(subredditItem.category);
        }
    }
}
