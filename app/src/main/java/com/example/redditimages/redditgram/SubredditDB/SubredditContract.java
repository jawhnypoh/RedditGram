package com.example.redditimages.redditgram.SubredditDB;

import android.provider.BaseColumns;

/**
 * Created by Tam on 3/15/2018.
 */

public class SubredditContract {
    private SubredditContract() {}

    public static class SavedSubreddits implements BaseColumns {
        public static final String TABLE_NAME = "followingSubreddits";
        public static final String COLUMN_SUBREDDIT_NAME = "name";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_BLOCKED = "is_blocked";
        public static final String COLUMN_ICON_URL = "icon_url";
    }
}
