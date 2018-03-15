package com.example.redditimages.redditgram.Utils;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by jerrypeng on 3/11/18.
 */

public class FeedFetchUtils {

    public static final String EXTRA_RESULT = "RedditFeed.Result";

    final static String REDDIT_BASE_URL = "https://www.reddit.com/";
    final static String REDDIT_RAW_JSON_PARAM = "raw_json";
    final static String REDDIT_RAW_JSON_VALUE = "1";
    final static String REDDIT_LIMIT_PARAM = "limit";
    final static String REDDIT_AFTER_ID_PARAM = "after";
    final static String REDDIT_BEFORE_ID_PARAM = "before";

    public static class PostItemData implements Serializable {
        public ArrayList<int> imageWidth;
        public ArrayList<int> imageHeight;
        public ArrayList<String> imageUrl;
        public ArrayList<String> sourceImageUrl;
        public String subreddit;
        public String title;
        public String post_hint;
        public String whitelist_status;
        public String name;
        public String date_time;
    }

    public static class SubredditData implements Serializable {
        public ArrayList<PostItemData> allPostItemData;
        public String after;
        public String before;
        public String whitelistStatus;
    }

    public static String buildForecastSearchURL(String subreddit, int limit, String after_id, String before_id) {
        // Url Parameter: raw_json=1, limit max:100 min:1
        String urlStr = Uri.parse(REDDIT_BASE_URL).buildUpon()
                .appendPath("r")
                .appendPath(subreddit)
                .appendPath(".json")
                .appendQueryParameter(REDDIT_RAW_JSON_PARAM, "1")
                .appendQueryParameter(REDDIT_LIMIT_PARAM, String.valueOf(Math.max(Math.min(limit, 100), 1)))
                .build()
                .toString();

        // If after_id or before id, then append to the query
        if (after_id != null) {
            urlStr = Uri.parse(urlStr).buildUpon()
                    .appendQueryParameter(REDDIT_AFTER_ID_PARAM, after_id)
                    .build()
                    .toString();
        } else if (before_id != null) {
            urlStr = Uri.parse(urlStr).buildUpon()
                    .appendQueryParameter(REDDIT_BEFORE_ID_PARAM, before_id)
                    .build()
                    .toString();
        }
        return urlStr;
    }


    public static SubredditData parsePostFeedJSON (String subredditFetchJSON) {
        try {
            SubredditData mSubredditData = new SubredditData();
            JSONObject feedFromSubredditObj = new JSONObject(subredditFetchJSON);
            JSONObject allData = feedFromSubredditObj.getJSONObject("data");

            // Get current feed data
            mSubredditData.after = allData.getString("after");
            mSubredditData.before = allData.getString("before");
            mSubredditData.whitelistStatus = allData.getString("whitelist_status");

            // Get all items and store as postItemData, add it to allPostItemData's allPostItemData
            JSONArray allPostItemDataJSON = allData.getJSONArray("children");

            for(int i = 0; i < allPostItemDataJSON.length(); i++) {
                PostItemData mPostItemData = new PostItemData();
                JSONObject postItemDataJSON = allPostItemDataJSON.getJSONObject(i).getJSONObject("data");
                mPostItemData = fillPostData(mPostItemData, postItemDataJSON);
                JSONArray postItemImageDataJSON = postItemDataJSON.getJSONObject("previews").getJSONArray("images");
                fillPostImageData(mPostItemData, postItemImageDataJSON);
                mSubredditData.allPostItemData.add(mPostItemData);
            }

            return mSubredditData;
        } catch (JSONException e) {
            return null;
        }
    }

    public ArrayList<int> imageWidth;
    public ArrayList<int> imageHeight;
    public ArrayList<String> imageUrl;
    public ArrayList<String> sourceImageUrl;
    public String subreddit;
    public String title;
    public String post_hint;
    public String whitelist_status;
    public String name;
    public String date_time;

    private static PostItemData fillPostData(PostItemData mPostItemData, JSONObject postItemDataJSON) {
        try {
            mPostItemData.subreddit = postItemDataJSON.getString("subreddit");
            mPostItemData.title = postItemDataJSON.getString("title");
            mPostItemData.post_hint = postItemDataJSON.getString("post_hint");
            mPostItemData.whitelist_status = postItemDataJSON.getString("whitelist_status");
            mPostItemData.name = postItemDataJSON.getString("name");
            mPostItemData.date_time = postItemDataJSON.getString("date_time");
        } catch (JSONException e) {
            return null;
        }
    }

    private static PostItemData fillPostImageData(PostItemData mPostItemdata, JSONArray postItemImageDataJSON) {
        try {

        } catch (JSONException e) {
            return null;
        }
    }
}
