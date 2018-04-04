package com.example.redditimages.redditgram.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.example.redditimages.redditgram.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by jerrypeng on 4/4/18.
 */

public class FeedProcessingUtils {

    private static final String TAG = FeedFetchUtils.class.getSimpleName();

    /* Utility function for processing subredditFeedJSON into individual post data */
    public static ArrayList<FeedFetchUtils.PostItemData> processSubredditFeedJSON(ArrayList<String> subredditFeedJSON, HashMap<String, String> mAfterTable) {

        ArrayList<FeedFetchUtils.PostItemData> feedData = new ArrayList<>();

        for (int i = 0; i < subredditFeedJSON.size(); i++) {
            // for each subreddit, parse posts data and add it to array. Add after id to after hash table
            FeedFetchUtils.SubredditFeedData subredditFeedData = FeedFetchUtils.parseFeedJSON(subredditFeedJSON.get(i));

            if (subredditFeedData != null && subredditFeedData.allPostItemData != null) {

                for (int j = 0; j < subredditFeedData.allPostItemData.size(); j++) {
                    feedData.add(subredditFeedData.allPostItemData.get(j));
                }

                // Get subreddit name and put after id to hash table
                if (subredditFeedData.allPostItemData.size() > 0) {
                    mAfterTable.put(subredditFeedData.allPostItemData.get(0).subreddit, subredditFeedData.after);
                }
            }
        }
        return feedData;
    }

    /* Utility function for filtering nsfw posts */
    public static void filter_nsfw(ArrayList<FeedFetchUtils.PostItemData> feedData, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean show_nsfw = sharedPreferences.getBoolean(
                context.getResources().getString(R.string.pref_in_nsfw_key),
                false
        );

        // If now show nsfw post, filter them
        String mPostHint;
        if (!show_nsfw) {
            for (int i = feedData.size() - 1; i >= 0; i--) {
                mPostHint = feedData.get(i).whitelist_status;
                if (mPostHint.contains("nsfw")) {
                    feedData.remove(i);
                }
            }
        }
    }


    /* Utility classes/functions for sorting posts */
    private static class sortByUpvotes implements Comparator<FeedFetchUtils.PostItemData> {
        public int compare(FeedFetchUtils.PostItemData a, FeedFetchUtils.PostItemData b) {
            return b.ups - a.ups;
        }
    }

    private static class sortByDate implements Comparator<FeedFetchUtils.PostItemData> {
        public int compare(FeedFetchUtils.PostItemData a, FeedFetchUtils.PostItemData b) {
            long aDateTime = a.date_time.getTime();
            long bDateTime = b.date_time.getTime();
            return (int) (bDateTime - aDateTime);
        }
    }

    public static void sortRedditFeedData(SharedPreferences sharedPreferences, ArrayList<FeedFetchUtils.PostItemData> feedData, Context context) {
        String sortMethod = sharedPreferences.getString(
                context.getResources().getString(R.string.pref_sorting_key),
                context.getResources().getString(R.string.pref_sorting_hot_value)
        );

        if(sortMethod.equals("hot")) {
            Collections.sort(feedData, new sortByUpvotes());
            for(int i=0; i<feedData.size(); i++) {
            }
        }
        else if(sortMethod.equals("new")){
            Collections.sort(feedData, new sortByDate());
            for(int i=0; i<feedData.size(); i++) {
            }
        }
    }
}
