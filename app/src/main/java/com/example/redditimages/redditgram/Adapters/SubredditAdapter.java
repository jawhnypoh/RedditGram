package com.example.redditimages.redditgram.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.redditimages.redditgram.R;
import com.example.redditimages.redditgram.Utils.SubredditSearchUtils;

import java.text.DateFormat;
import java.util.ArrayList;
/**
 * Created by Tam on 3/15/2018.
 */

public class SubredditAdapter extends RecyclerView.Adapter<SubredditAdapter.SubredditItemViewHolder> {
    private ArrayList<SubredditSearchUtils.SubredditItem> mSubredditItems;
    private OnSubredditItemClickListener mSubredditItemClickListener;
    private Context mContext;

    private static final String TAG = SubredditAdapter.class.getSimpleName();

    public interface OnSubredditItemClickListener {
        void onSubredditItemClick(String subredditName, boolean is_block);
        long deleteSubredditFromDB(String subredditName);
    }

    public SubredditAdapter(Context context, OnSubredditItemClickListener clickListener) {
        mContext = context;
        mSubredditItemClickListener = clickListener;
    }

    public void updateSubredditItems(ArrayList<SubredditSearchUtils.SubredditItem> subredditItems) {
        mSubredditItems = subredditItems;
        Log.d(TAG, "Update subreddit items successful");
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mSubredditItems != null) {
            return mSubredditItems.size();
        } else {
            return 0;
        }
    }

    @Override
    public SubredditItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.subreddit_item, parent, false);
        return new SubredditItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SubredditItemViewHolder holder, int position) {
        holder.bind(mSubredditItems.get(position), position);

    }

    class SubredditItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mSubredditName;
        ImageButton deleteButton;
        LinearLayout mContainer;
        private final DateFormat mDateFormatter = DateFormat.getDateTimeInstance();

        public SubredditItemViewHolder(View itemView) {
            super(itemView);
            mSubredditName = itemView.findViewById(R.id.subreddit_name);
            mContainer = (LinearLayout) itemView.findViewById(R.id.subreddit_item_container);
            itemView.setOnClickListener(this);
        }

        public void bind(SubredditSearchUtils.SubredditItem subredditItem , int position) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            mSubredditName.setText("r/" + subredditItem.name);
            if (subredditItem.is_blocked) {
                mContainer.setBackgroundColor(Color.rgb(200, 200, 200));
            } else {
                mContainer.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            deleteButton = itemView.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (mSubredditItemClickListener.deleteSubredditFromDB(mSubredditItems.get(getAdapterPosition()).name) != -1) {
                        Log.d(TAG, "Deleted item " + mSubredditItems.get(getAdapterPosition()));
                        mSubredditItems.remove(getAdapterPosition());  // remove the item from list
                        notifyItemRemoved(getAdapterPosition()); // notify the adapter about the removed item
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            SubredditSearchUtils.SubredditItem subredditItem = mSubredditItems.get(getAdapterPosition());
            mSubredditItemClickListener.onSubredditItemClick(subredditItem.name, subredditItem.is_blocked);
            subredditItem.is_blocked = !subredditItem.is_blocked;
            if (subredditItem.is_blocked) {
                mContainer.setBackgroundColor(Color.rgb(200, 200, 200));
            } else {
                mContainer.setBackgroundColor(Color.rgb(255, 255, 255));
            }
        }
    }
}
