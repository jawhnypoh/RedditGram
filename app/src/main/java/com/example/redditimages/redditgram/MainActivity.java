package com.example.redditimages.redditgram;

import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.redditimages.redditgram.Utils.FeedFetchUtils;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{

    private static final String TAG = MainActivity.class.getSimpleName();
    private final static String FEED_URL_KEY = "subredditFeedURL";
    private final static int FEED_LOADER_ID = 0;

    private RecyclerView mFeedListItemsRV;
    private FeedListAdapter mFeedListAdapter;
    private ProgressBar mLoadingIndicatorPB;
    private TextView mLoadingErrorMessageTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize all Views
        mLoadingIndicatorPB = (ProgressBar)findViewById(R.id.pb_loading_indicator);
        mLoadingErrorMessageTV = (TextView)findViewById(R.id.tv_loading_error);
        mFeedListItemsRV = (RecyclerView)findViewById(R.id.rv_feed_list);

        // Set up Recycler view for the main activity feed
        mFeedListAdapter = new FeedListAdapter();
        mFeedListItemsRV.setAdapter(mFeedListAdapter);
        mFeedListItemsRV.setLayoutManager(new LinearLayoutManager(this));
        mFeedListItemsRV.setHasFixedSize(true);
        
        // Load The Feed
        loadFeed(true);

        getSupportLoaderManager().initLoader(FEED_LOADER_ID, null, this);
    }


    public void loadFeed(boolean initialLoad) {
        // Set the progress indicator as visible
        mLoadingIndicatorPB.setVisibility(View.VISIBLE);

        Bundle loaderArgs = new Bundle();
        String subredditUrl = FeedFetchUtils.buildFeedFetchURL("earthporn", 25, null, null);
        loaderArgs.putString(FEED_URL_KEY, subredditUrl);
        LoaderManager loaderManager = getSupportLoaderManager();

        if (initialLoad) {
            loaderManager.initLoader(FEED_LOADER_ID, loaderArgs, this);
        } else {
            loaderManager.restartLoader(FEED_LOADER_ID, loaderArgs, this);
        }
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "Loader onCreate");
        String subredditFeedUrl = null;
        if (args != null) {
            subredditFeedUrl = args.getString(FEED_URL_KEY);
        }
        return new FeedLoader(this, subredditFeedUrl);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        Log.d(TAG, "got forecast from loader");
        mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
        if (data != null) {
            mLoadingErrorMessageTV.setVisibility(View.INVISIBLE);
            mFeedListItemsRV.setVisibility(View.VISIBLE);
            FeedFetchUtils.SubredditFeedData subredditFeedData = FeedFetchUtils.parseFeedJSON(data);
            mFeedListAdapter.updateFeedData(subredditFeedData.allPostItemData);
        } else {
            mFeedListItemsRV.setVisibility(View.INVISIBLE);
            mLoadingErrorMessageTV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // Nothing ...
    }

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
            case R.id.action_subreddits:
                Intent subredditsIntent = new Intent(this, SubredditActivity.class);
                startActivity(subredditsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
