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

    private String mCachedSubRedditFeedJSON;
    private String mSubRedditFeedUrl;

    public FeedLoader(Context context, String subRedditFeedlURL) {
        super(context);
        mSubRedditFeedUrl = subRedditFeedlURL;
    }

    @Override
    protected void onStartLoading() {
        if (mSubRedditFeedUrl != null) {
            if (mCachedSubRedditFeedJSON != null) {
                Log.d(TAG, "using cached data");
                deliverResult(mCachedSubRedditFeedJSON);
            } else {
                forceLoad();
            }
        }
    }

    @Nullable
    @Override
    public String loadInBackground() {
        String SubredditFeedJSON = null;
        if (mSubRedditFeedUrl != null) {
            Log.d(TAG, "Network Call: " + mSubRedditFeedUrl);
            try {
                SubredditFeedJSON = NetworkUtils.doHTTPGet(mSubRedditFeedUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return SubredditFeedJSON;
    }

    @Override
    public void deliverResult(@Nullable String data) {
        mCachedSubRedditFeedJSON = data;
        super.deliverResult(data);
    }
}
