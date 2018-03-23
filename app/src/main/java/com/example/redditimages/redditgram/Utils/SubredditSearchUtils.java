package com.example.redditimages.redditgram.Utils;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by jerrypeng on 3/16/18.
 */

public class SubredditSearchUtils {

    private static final String TAG = SubredditSearchUtils.class.getSimpleName();

    final static String SEARCH_BASE_URL = "https://www.reddit.com/subreddits/search";
    final static String SEARCH_QUERY_PARAM = "q";
    final static String SEARCH_RAW_JSON_PARAM = "raw_json";
    final static String SEARCH_RAW_JSON_VALUE = "1";
    final static String SEARCH_LIMIT_PARAM = "limit";
    final static String SEARCH_SORT_PARAM = "sort";

    public static class SubredditItem implements Serializable {
        public String name;
        public String category;
        public String icon_url;
        public boolean is_blocked;
    }

    public static String buildSubredditSearchURL(String query, String limit, String sort) {
        return Uri.parse(SEARCH_BASE_URL).buildUpon()
                .appendPath(".json")
                .appendQueryParameter(SEARCH_QUERY_PARAM, query)
                .appendQueryParameter(SEARCH_RAW_JSON_PARAM, SEARCH_RAW_JSON_VALUE)
                .appendQueryParameter(SEARCH_LIMIT_PARAM, limit)
                .appendQueryParameter(SEARCH_SORT_PARAM, sort)
                .build()
                .toString();
    }

    public static ArrayList<SubredditItem> parseSubredditSearchJSON (String subredditSearchJSON) {
        try {
            ArrayList<SubredditItem> subredditList = new ArrayList<>();
            JSONObject subredditListDataJSON = new JSONObject(subredditSearchJSON);
            JSONArray subredditListJSON = subredditListDataJSON.getJSONObject("data").getJSONArray("children");
            for (int i = 0; i < subredditListJSON.length(); i++) {
                JSONObject subredditItemJSON = subredditListJSON.getJSONObject(i).getJSONObject("data");
                SubredditItem subredditItem = new SubredditItem();
                subredditItem.name = subredditItemJSON.getString("display_name");
                subredditItem.category = subredditItemJSON.getString("audience_target");
                if (subredditItemJSON.has("icon_url")) {
                    subredditItem.icon_url = subredditItemJSON.getString("icon_url);");
                }
                subredditItem.is_blocked = false;
                subredditList.add(subredditItem);
            }
            return subredditList;
        } catch (JSONException e) {
            return null;
        }
    }
}
