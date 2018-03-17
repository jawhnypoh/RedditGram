package com.example.redditimages.redditgram;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.redditimages.redditgram.Utils.NetworkUtils;

import java.io.IOException;

/**
 * Created by jerrypeng on 3/15/18.
 */

public class FeedLoader extends AsyncTaskLoader<String> {

    private final static String TAG = FeedLoader.class.getSimpleName();

    private String mCachedSubredditFeedJSON;
    private String mSubredditFeedUrl;

    public FeedLoader(Context context, String subredditFeedlURL) {
        super(context);
        mSubredditFeedUrl = subredditFeedlURL;
    }

    @Override
    protected void onStartLoading() {
        if (mSubredditFeedUrl != null) {
            if (mCachedSubredditFeedJSON != null) {
                Log.d(TAG, "using cached data");
                deliverResult(mCachedSubredditFeedJSON);
            } else {
                forceLoad();
            }
        }
    }

    @Nullable
    @Override
    public String loadInBackground() {
        String SubredditFeedJSON = null;
        if (mSubredditFeedUrl != null) {
            Log.d(TAG, "Network Call: " + mSubredditFeedUrl);
            try {
                SubredditFeedJSON = NetworkUtils.doHTTPGet(mSubredditFeedUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return SubredditFeedJSON;
    }

    @Override
    public void deliverResult(@Nullable String data) {
        mCachedSubredditFeedJSON = data;
        super.deliverResult(data);
    }
}
