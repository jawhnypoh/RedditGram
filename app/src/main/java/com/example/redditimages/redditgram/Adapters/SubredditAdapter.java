package com.example.redditimages.redditgram.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.redditimages.redditgram.R;

import java.text.DateFormat;
import java.util.ArrayList;
/**
 * Created by Tam on 3/15/2018.
 */

public class SubredditAdapter extends RecyclerView.Adapter<SubredditAdapter.SubredditItemViewHolder> {
    private ArrayList<String> mSubredditItems;
    private OnSubredditItemClickListener mSubredditItemClickListener;
    private Context mContext;

    private static final String TAG = SubredditAdapter.class.getSimpleName();

    public interface OnSubredditItemClickListener {
        void onSubredditItemClick(String subredditItem);
        long deleteSubredditFromDB(String subredditName);
    }

    public SubredditAdapter(Context context, OnSubredditItemClickListener clickListener) {
        mContext = context;
        mSubredditItemClickListener = clickListener;
    }

    public void updateSubredditItems(ArrayList<String> subredditItems) {
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
        private final DateFormat mDateFormatter = DateFormat.getDateTimeInstance();

        public SubredditItemViewHolder(View itemView) {
            super(itemView);
            mSubredditName= itemView.findViewById(R.id.subreddit_name);
            itemView.setOnClickListener(this);
        }

        public void bind(String subredditName, int position) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            mSubredditName.setText("r/" + subredditName);
            deleteButton = itemView.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (mSubredditItemClickListener.deleteSubredditFromDB(mSubredditItems.get(getAdapterPosition())) != -1) {

                        mSubredditItems.remove(getAdapterPosition());  // remove the item from list
                        Log.d(TAG, "Deleted item " + mSubredditItems.get(getAdapterPosition()));
                        notifyItemRemoved(getAdapterPosition()); // notify the adapter about the removed item
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            String subredditItem = mSubredditItems.get(getAdapterPosition());
            mSubredditItemClickListener.onSubredditItemClick(subredditItem);
        }
    }
}
