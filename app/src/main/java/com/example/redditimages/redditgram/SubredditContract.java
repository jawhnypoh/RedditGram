package com.example.redditimages.redditgram;

import android.provider.BaseColumns;

/**
 * Created by Tam on 3/15/2018.
 */

public class SubredditContract {
    private SubredditContract() {}

    public static class FollowingSubreddits implements BaseColumns {
        public static final String TABLE_NAME = "followingSubreddits";
        public static final String SUBREDDIT_NAME = "subredditName";
        public static final int IS_NSFW = 0;

    }
}
