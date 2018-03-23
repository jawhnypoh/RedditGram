package com.example.redditimages.redditgram.Utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;

/**
 * Created by jerrypeng on 3/15/18.
 */

public class SearchLoader extends AsyncTaskLoader<String> {

    private final static String TAG = UrlJsonLoader.class.getSimpleName();

    private String mCachedJSON;
    private String mUrl;

    public SearchLoader(Context context, String subredditFeedlURL) {
        super(context);
        mUrl = subredditFeedlURL;
    }

    @Override
    protected void onStartLoading() {
        if (mUrl != null) {
            if (mCachedJSON != null) {
                Log.d(TAG, "using cached data");
                deliverResult(mCachedJSON);
            } else {
                forceLoad();
            }
        }
    }

    @Nullable
    @Override
    public String loadInBackground() {
        String SubredditFeedJSON = null;
        if (mUrl != null) {
            Log.d(TAG, "Network Call: " + mUrl);
            try {
                SubredditFeedJSON = NetworkUtils.doHTTPGet(mUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return SubredditFeedJSON;
    }

    @Override
    public void deliverResult(@Nullable String data) {
        mCachedJSON = data;
        super.deliverResult(data);
    }
}
