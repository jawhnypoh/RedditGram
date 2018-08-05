package com.example.redditimages.redditgram;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.redditimages.redditgram.Adapters.FeedListAdapter;
import com.example.redditimages.redditgram.SubredditDB.SubredditContract;
import com.example.redditimages.redditgram.SubredditDB.SubredditDBHelper;
import com.example.redditimages.redditgram.Utils.FeedFetchUtils;
import com.example.redditimages.redditgram.Utils.FeedProcessingUtils;
import com.example.redditimages.redditgram.Utils.InfiniteScrollListener;
import com.example.redditimages.redditgram.Utils.UrlJsonLoader;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<String>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private int FeedURLKey = 0;
    private final static int FEED_LOADER_ID = 0;

    private RecyclerView mFeedListItemsRV;
    private FeedListAdapter mFeedListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager linearLayoutManager;
    private TextView mLoadingErrorMessageTV;

    private HashMap<String, String> mAfterTable;
    public ArrayList<String> subredditItems;

    private SQLiteDatabase mDB;
    private SubredditDBHelper dbHelper;

    private boolean isLoading = false;
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize all Views
        mLoadingErrorMessageTV = (TextView)findViewById(R.id.tv_loading_error);
        mFeedListItemsRV = (RecyclerView)findViewById(R.id.rv_feed_list);

        // Set up Recycler view for the main activity feed
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_refresh_feed);
        linearLayoutManager = new LinearLayoutManager(this);
        mFeedListAdapter = new FeedListAdapter(this);
        mFeedListItemsRV.setAdapter(mFeedListAdapter);
        mFeedListItemsRV.setLayoutManager(linearLayoutManager);
        mFeedListItemsRV.setHasFixedSize(true);

        // Infinite scroll
        mFeedListItemsRV.addOnScrollListener(new InfiniteScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                mFeedListAdapter.addLoadingFooter();
                isLoading = true;
                loadFeed(false);
            }
            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        // Set up database
        dbHelper = new SubredditDBHelper(this);

        // Swipe refresh
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLoading) {
                    refresh();
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        // Set up subreddit/after hash table
        mAfterTable = new HashMap<>();

        // Load The Feed
        loadFeed(true);

        // Set up SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public ArrayList<String> getAllSubredditsFromDB() {
        mDB = dbHelper.getWritableDatabase();
        String sqlSelection = SubredditContract.SavedSubreddits.COLUMN_BLOCKED + "=?";
        String[] sqlSelectionArg = {"0"};
        Cursor cursor = mDB.query(
                SubredditContract.SavedSubreddits.TABLE_NAME,
                null,
                sqlSelection,
                sqlSelectionArg,
                null,
                null,
                SubredditContract.SavedSubreddits.COLUMN_SUBREDDIT_NAME + " ASC"
        );

        ArrayList<String> subredditResults = new ArrayList<>();
        while (cursor.moveToNext()) {
            String searchResult;
            searchResult= cursor.getString(
                    cursor.getColumnIndex(SubredditContract.SavedSubreddits.COLUMN_SUBREDDIT_NAME)
            );
            subredditResults.add(searchResult);
        }
        cursor.close();
        mDB.close();
        return subredditResults;
    }

    public void refresh() {
        isRefreshing = true;
        // Clear all data
        mFeedListAdapter.clearAllData();
        mAfterTable = new HashMap<>();
        loadFeed(true);
    }

    public void loadFeed(boolean initialLoad) {

        Bundle loaderArgs = new Bundle();
        String after, subredditName, subredditUrl;

        mDB = dbHelper.getWritableDatabase();
        subredditItems = getAllSubredditsFromDB();
        mDB.close();

        if (subredditItems.size() != 0) {

            // put all the urls to loaderArgs
            FeedURLKey = 0;
            for (int i = 0; i < subredditItems.size(); i++) {
                after = null;
                if (!initialLoad) {
                    subredditName = subredditItems.get(i);
                    if (mAfterTable.containsKey(subredditName)) {
                        after = mAfterTable.get(subredditName);
                    }
                }
                subredditUrl = FeedFetchUtils.buildFeedFetchURL(subredditItems.get(i), 10, after, null);
                loaderArgs.putString(Integer.toString(FeedURLKey), subredditUrl);
                FeedURLKey++;
            }

            // put size to loaderArgs
            loaderArgs.putString("size", Integer.toString(FeedURLKey));

            // Initiate loader
            LoaderManager loaderManager = getSupportLoaderManager();
            if (initialLoad && !isRefreshing) {
                loaderManager.initLoader(FEED_LOADER_ID, loaderArgs, this);
            } else {
                loaderManager.restartLoader(FEED_LOADER_ID, loaderArgs, this);
            }
        }
        else {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(MainActivity.this, "No subreddits to fetch!",
                    Toast.LENGTH_LONG).show();
        }
    }


    /* Option Menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.action_search:
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
                return true;

            case R.id.action_subreddits:
                Intent subredditsIntent = new Intent(this, SubredditActivity.class);
                startActivity(subredditsIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Loader */
    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "Loader onCreate");
        return new UrlJsonLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<String>> loader,  ArrayList<String> subredditFeedJSON) {
        Log.d(TAG, "got reddit post data from loader");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        ArrayList<FeedFetchUtils.PostItemData> newSubredditFeedData = FeedProcessingUtils.processSubredditFeedJSON(subredditFeedJSON, mAfterTable);
        Log.d(TAG, "Fetching DONE");

        if (isLoading) {
            mFeedListAdapter.removeLoadingFooter();
            isLoading = false;
        }

        FeedProcessingUtils.sortRedditFeedData(sharedPreferences, newSubredditFeedData, this);
        FeedProcessingUtils.filter_nsfw(newSubredditFeedData, this);

        // add each item in each subreddit feed data to one array list
        if (isRefreshing) {
            mFeedListAdapter.reloadFeedData(newSubredditFeedData);
            mSwipeRefreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
        else {
            mFeedListAdapter.updateFeedData(newSubredditFeedData);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String>> loader) {
        // Nothing ...
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_in_nsfw")) {
            refresh();
        } else if (key.equals("pref_sorting")) {
            refresh();
        }
    }
}

