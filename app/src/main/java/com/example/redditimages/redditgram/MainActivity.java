package com.example.redditimages.redditgram;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.redditimages.redditgram.Adapters.FeedListAdapter;
import com.example.redditimages.redditgram.SubredditDB.SubredditContract;
import com.example.redditimages.redditgram.SubredditDB.SubredditDBHelper;
import com.example.redditimages.redditgram.Utils.FeedFetchUtils;
import com.example.redditimages.redditgram.Utils.InfiniteScrollListener;
import com.example.redditimages.redditgram.Utils.UrlJsonLoader;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<String>>{

    private static final String TAG = MainActivity.class.getSimpleName();
    private int FeedURLKey = 0;
    private final static int FEED_LOADER_ID = 0;

    private RecyclerView mFeedListItemsRV;
    private FeedListAdapter mFeedListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager linearLayoutManager;
    private TextView mLoadingErrorMessageTV;
    private TextView mOverlayTV;

    public ArrayList<String> subredditURLs;
    public ArrayList<String> subredditItems;

    private SQLiteDatabase mDB;
    private ArrayList<FeedFetchUtils.SubredditFeedData> mSubredditFeedData;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize all Views
        mLoadingErrorMessageTV = (TextView)findViewById(R.id.tv_loading_error);
        mFeedListItemsRV = (RecyclerView)findViewById(R.id.rv_feed_list);
        mOverlayTV = (TextView)findViewById(R.id.tv_overlay);

        mOverlayTV.setVisibility(View.INVISIBLE);

        // Set up Recycler view for the main activity feed
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_refresh_feed);
        linearLayoutManager = new LinearLayoutManager(this);
        mFeedListAdapter = new FeedListAdapter();
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

        // Swipe refresh
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLoading) {
                    mFeedListAdapter.clearAllData();
                    loadFeed(true);
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        // Set up database
        SubredditDBHelper dbHelper = new SubredditDBHelper(this);
        mDB = dbHelper.getWritableDatabase();
        subredditItems = getAllSubredditsFromDB();

        // Load The Feed
        loadFeed(true);

        getSupportLoaderManager().initLoader(FEED_LOADER_ID, null, this);
    }

    public ArrayList<String> getAllSubredditsFromDB() {
        Cursor cursor = mDB.query(
                SubredditContract.SavedSubreddits.TABLE_NAME,
                null,
                null,
                null,
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
        return subredditResults;
    }

    public void loadFeed(boolean initialLoad) {

        Bundle loaderArgs = new Bundle();
        subredditURLs = new ArrayList<String>();
        String after = null;

        if (subredditItems != null) {
            // put all the urls to loaderArgs
            FeedURLKey = 0;
            for (int i=0; i<subredditItems.size(); i++) {
                after = null;
                if (!initialLoad) {
                    after = mSubredditFeedData.get(i).after;
                }
                subredditURLs.add(FeedFetchUtils.buildFeedFetchURL(subredditItems.get(i), 2, after, null));
                loaderArgs.putString(Integer.toString(FeedURLKey), subredditURLs.get(i));
                FeedURLKey++;
            }
            // put size to loaderArgs
            loaderArgs.putString("size", Integer.toString(FeedURLKey));

            // Initiate loader
            LoaderManager loaderManager = getSupportLoaderManager();
            if (initialLoad) {
                loaderManager.initLoader(FEED_LOADER_ID, loaderArgs, this);
            } else {
                loaderManager.restartLoader(FEED_LOADER_ID, loaderArgs, this);
            }
        }
        else {
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
    public void onLoadFinished(Loader<ArrayList<String>> loader,  ArrayList<String> subredditURLs) {

        Log.d(TAG, "got Reddit post data from loader");
        mSubredditFeedData = new ArrayList<>();
        ArrayList<FeedFetchUtils.PostItemData> allSubredditFeedData = new ArrayList<>();

        if (subredditURLs != null) {
            for (int i = 0; i < FeedURLKey; i++) {

                mSubredditFeedData.add(FeedFetchUtils.parseFeedJSON(subredditURLs.get(i)));
                Log.d(TAG, "DATA FOR " + mSubredditFeedData.get(i).allPostItemData.get(0).subreddit + " IS " + subredditURLs.get(i));
                for (int j = 0; j < mSubredditFeedData.get(i).allPostItemData.size(); j++) {
                    allSubredditFeedData.add(mSubredditFeedData.get(i).allPostItemData.get(j));
                }
            }
            Log.d(TAG, "Fetching DONE");

            mLoadingErrorMessageTV.setVisibility(View.INVISIBLE);
            mFeedListItemsRV.setVisibility(View.VISIBLE);

            if (isLoading) {
                mFeedListAdapter.removeLoadingFooter();
                isLoading = false;
            }

            // add each item in each subreddit feed data to one array list
            mFeedListAdapter.updateFeedData(allSubredditFeedData);

            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            mFeedListItemsRV.setVisibility(View.INVISIBLE);
            mLoadingErrorMessageTV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String>> loader) {
        // Nothing ...
    }
}

