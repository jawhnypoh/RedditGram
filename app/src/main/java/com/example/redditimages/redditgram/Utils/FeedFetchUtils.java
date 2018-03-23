package com.example.redditimages.redditgram.Utils;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by jerrypeng on 3/11/18.
 */

public class FeedFetchUtils {

    private static final String TAG = FeedFetchUtils.class.getSimpleName();

    private final static String REDDIT_POST_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    final static String REDDIT_BASE_URL = "https://www.reddit.com/";
    final static String REDDIT_RAW_JSON_PARAM = "raw_json";
    final static String REDDIT_RAW_JSON_VALUE = "1";
    final static String REDDIT_LIMIT_PARAM = "limit";
    final static String REDDIT_AFTER_ID_PARAM = "after";
    final static String REDDIT_BEFORE_ID_PARAM = "before";

    public static class PostItemData implements Serializable {
        public ArrayList<Integer> imageWidths;
        public ArrayList<Integer> imageHeights;
        public ArrayList<String> imageUrls;
        public ArrayList<String> sourceImageUrls;
        public String subreddit;
        public String title;
        public String author;
        public String post_hint;
        public String whitelist_status;
        public String name;
        public int ups;
        public Date date_time;
    }

    public static class SubredditFeedData implements Serializable {
        public ArrayList<PostItemData> allPostItemData;
        public String after;
        public String before;
        public String whitelistStatus;
    }

    public static String buildFeedFetchURL(String subreddit, int limit, String after_id, String before_id) {
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


    public static SubredditFeedData parseFeedJSON (String subredditFetchJSON) {
        try {
            SubredditFeedData mSubredditFeedData = new SubredditFeedData();
            JSONObject feedFromSubredditJSON = new JSONObject(subredditFetchJSON);
            JSONObject allDataJSON = feedFromSubredditJSON.getJSONObject("data");

            // Get current feed data
            mSubredditFeedData.after = allDataJSON.getString("after");
            mSubredditFeedData.before = allDataJSON.getString("before");
            mSubredditFeedData.whitelistStatus = allDataJSON.getString("whitelist_status");
            mSubredditFeedData.allPostItemData = new ArrayList<>();

            // Get all items and store as postItemData, add it to allPostItemData's allPostItemData
            JSONArray allPostItemDataJSON = allDataJSON.getJSONArray("children");

            for(int i = 0; i < allPostItemDataJSON.length(); i++) {

                PostItemData mPostItemData = new PostItemData();

                // Fill information about each post
                JSONObject postItemDataJSON = allPostItemDataJSON.getJSONObject(i).getJSONObject("data");
                mPostItemData = fillPostData(mPostItemData, postItemDataJSON);
                // Fine the optimal image resolution + url and fill in the info for each image
                if (postItemDataJSON.has("preview") && postItemDataJSON.has("post_hint")) {
                    // post_hint: self is filtered
                    if (!postItemDataJSON.getString("post_hint").equals("self")) {
                        JSONArray postItemImageDataJSON = postItemDataJSON.getJSONObject("preview").getJSONArray("images");
                        fillPostImageData(mPostItemData, postItemImageDataJSON);
                        mSubredditFeedData.allPostItemData.add(mPostItemData);
                    }
                }
            }
            return mSubredditFeedData;

        } catch (JSONException e) {
            return null;
        }
    }

    private static PostItemData fillPostData(PostItemData mPostItemData, JSONObject postItemDataJSON) {
        try {
            SimpleDateFormat dateParser = new SimpleDateFormat(REDDIT_POST_DATE_FORMAT);
            dateParser.setTimeZone(TimeZone.getTimeZone(REDDIT_POST_DATE_FORMAT));

            mPostItemData.subreddit = postItemDataJSON.getString("subreddit");
            mPostItemData.title = postItemDataJSON.getString("title");
            mPostItemData.author = postItemDataJSON.getString("author");
            mPostItemData.whitelist_status = postItemDataJSON.getString("whitelist_status");
            mPostItemData.name = postItemDataJSON.getString("name");
            mPostItemData.ups = Integer.parseInt(postItemDataJSON.getString("ups"));

            if (postItemDataJSON.has("post_hint")) {
                mPostItemData.post_hint = postItemDataJSON.getString("post_hint");
            } else {
                mPostItemData.post_hint = null;
            }

            long date = Double.valueOf(postItemDataJSON.getString("created")).longValue()*1000;
            mPostItemData.date_time = new Date(date);


            return mPostItemData;

        } catch (JSONException e) {
            return null;
        }
    }

    private static PostItemData fillPostImageData(PostItemData mPostItemData, JSONArray postItemImageDataJSON) {
        try {
            mPostItemData.sourceImageUrls = new ArrayList<>();
            mPostItemData.imageUrls = new ArrayList<>();
            mPostItemData.imageWidths = new ArrayList<>();
            mPostItemData.imageHeights = new ArrayList<>();

            for (int i = 0; i < postItemImageDataJSON.length(); i++) {
                JSONObject postItemImageItemJSON = postItemImageDataJSON.getJSONObject(i);

                String sourceImageUrl = postItemImageItemJSON.getJSONObject("source").getString("url");
                mPostItemData.sourceImageUrls.add(sourceImageUrl);
                int sourceImageWidth = Integer.parseInt(postItemImageItemJSON.getJSONObject("source").getString("width"));
                int sourceImageHeight = Integer.parseInt(postItemImageItemJSON.getJSONObject("source").getString("height"));

                if (sourceImageWidth < 1080) {
                    // If source image width lower than 1080px, fetch the source image
                    mPostItemData.imageUrls.add(sourceImageUrl);
                    mPostItemData.imageWidths.add(sourceImageWidth);
                    mPostItemData.imageHeights.add(sourceImageHeight);

                } else {
                    // Get the optimal resolution data from the JSON
                    JSONObject optimalImageItemJSON = getOptimalImageJSON(postItemImageItemJSON);

                    String imageUrl = optimalImageItemJSON.getString("url");
                    String imageWidth = optimalImageItemJSON.getString("width");
                    String imageHeight = optimalImageItemJSON.getString("height");

                    mPostItemData.imageUrls.add(imageUrl);
                    mPostItemData.imageWidths.add(Integer.parseInt(imageWidth));
                    mPostItemData.imageHeights.add(Integer.parseInt(imageHeight));
                }
            }
            return mPostItemData;

        } catch (JSONException e) {
            return null;
        }
    }

    private static JSONObject getOptimalImageJSON(JSONObject postItemImageItemJSON) {
        try {
            JSONArray imageResolutions = postItemImageItemJSON.getJSONArray("resolutions");
            int numberOfResolutions = imageResolutions.length();
            JSONObject optimalImageItemJSON = imageResolutions.getJSONObject(numberOfResolutions - 1);

            if (numberOfResolutions > 1) {
                for (int i = numberOfResolutions - 2; i >= 0; i--) {
                    int width = Integer.parseInt(imageResolutions.getJSONObject(i).getString("width"));
                    if (width < 1080) {
                        optimalImageItemJSON = imageResolutions.getJSONObject(i + 1);
                        break;
                    }
                }
            }

            return optimalImageItemJSON;

        } catch (JSONException e) {
            return null;
        }
    }
}
