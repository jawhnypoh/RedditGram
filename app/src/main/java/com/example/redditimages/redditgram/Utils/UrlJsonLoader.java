package com.example.redditimages.redditgram.Utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jerrypeng on 3/15/18.
 */

public class UrlJsonLoader extends AsyncTaskLoader<ArrayList<String>> {

    private final static String TAG = UrlJsonLoader.class.getSimpleName();

    private ArrayList<String> mCachedJSON;
    public ArrayList<String> mUrls;
    private String mListSize;
    private ArrayList<String> SubredditFeedJSON;

    public UrlJsonLoader(Context context, Bundle subredditFeedURLs) {
        super(context);
        mUrls = new ArrayList<String>();
        SubredditFeedJSON = new ArrayList<String>();
        mListSize = subredditFeedURLs.getString("size");

        for (int i = 0; i < Integer.parseInt(mListSize); i++ ) {
            mUrls.add(subredditFeedURLs.getString(Integer.toString(i)));
            Log.d(TAG, "Adding URL: " + mUrls.get(i));
        }
    }

    @Override
    protected void onStartLoading() {
        if (mUrls != null) {
            if (mCachedJSON == null) {
                forceLoad();
            } else {
                Log.d(TAG, "using cached data");
                deliverResult(mCachedJSON);
            }
        }
    }

    @Nullable
    @Override
    public ArrayList<String> loadInBackground() {
        if (mUrls != null) {
            Log.d(TAG, "Network Call: " + mUrls);
            try {
                for (int i = 0; i< Integer.parseInt(mListSize); i++) {
                    SubredditFeedJSON.add(NetworkUtils.doHTTPGet(mUrls.get(i)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return SubredditFeedJSON;
    }

    @Override
    public void deliverResult(@Nullable ArrayList<String> data) {
        mCachedJSON = data;
        super.deliverResult(data);
    }
}
