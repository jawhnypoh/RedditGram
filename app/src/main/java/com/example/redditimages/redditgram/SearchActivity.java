package com.example.redditimages.redditgram;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.redditimages.redditgram.SubredditDB.SubredditContract;
import com.example.redditimages.redditgram.SubredditDB.SubredditDBHelper;
import com.example.redditimages.redditgram.Utils.SubredditSearchUtils;

import java.util.ArrayList;

/**
 * Created by jerrypeng on 3/20/18.
 */

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>, SearchListAdapter.OnSubredditAddListener{

    public static final String TAG = SearchActivity.class.getSimpleName();
    private static final String SEARCH_URL_KEY = "searchSubredditURL";
    private static final int SEARCH_LOADER_ID = 0;

    private RecyclerView mSubredditItemRV;
    private SearchListAdapter mSearchListAdapter;
    private ProgressBar mLoadingIndicatorPB;
    private TextView mLoadingErrorMessageTV;
    private EditText mSearchBox;
    private ImageButton mSearchButton;

    private SQLiteDatabase mDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_subreddit);

        // Set up the search bar
        mSearchBox = (EditText) findViewById(R.id.et_search_box);
        mSearchButton = (ImageButton) findViewById(R.id.ib_search_subreddit_btn);

        // Set up the recycler view
        mSubredditItemRV = (RecyclerView) findViewById(R.id.rv_search_subreddit_list);
        mLoadingIndicatorPB = (ProgressBar) findViewById(R.id.pb_search_loading_indicator);
        mLoadingErrorMessageTV = (TextView) findViewById(R.id.tv_search_loading_error);

        // Set up the adapter
        mSearchListAdapter = new SearchListAdapter(this, this);
        mSubredditItemRV.setAdapter(mSearchListAdapter);
        mSubredditItemRV.setLayoutManager(new LinearLayoutManager(this));
        mSubredditItemRV.setHasFixedSize(true);

        // Set up the db
        SubredditDBHelper dbHelper = new SubredditDBHelper(this);
        mDB = dbHelper.getWritableDatabase();

        // Set up search bar
        ImageButton mSearchButton = (ImageButton)findViewById(R.id.ib_search_subreddit_btn);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = mSearchBox.getText().toString();
                Log.d(TAG, searchQuery);
                if (!TextUtils.isEmpty(searchQuery)) {
                    searchSubreddit(searchQuery);
                }
            }
        });

        getSupportLoaderManager().initLoader(SEARCH_LOADER_ID, null, this);
    }

    private void searchSubreddit(String searchQuery) {

        // Set the progress indicator as visible
        mLoadingIndicatorPB.setVisibility(View.VISIBLE);

        // Create url string
        Bundle loaderArgs = new Bundle();
        String searchSubredditUrl = SubredditSearchUtils.buildSubredditSearchURL(searchQuery, "25", "relevancy");
        loaderArgs.putString(SEARCH_URL_KEY, searchSubredditUrl);
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.restartLoader(SEARCH_LOADER_ID, loaderArgs, this);
    }


    /* Add Subreddit Button Click Listener */
    @Override
    public void onSubredditAdd(SubredditSearchUtils.SubredditItem subredditItem) {
        if (!checkSubredditSaved(subredditItem.name)) {
            addSubredditToDB(subredditItem);
            Log.d(TAG, subredditItem.name + " Added!");
        } else {
            Log.d(TAG, subredditItem.name + " duplicate!");
        }

    }

    Boolean checkSubredditSaved(String subredditName) {
        Boolean isSaved = true;

        if (subredditName != null) {
            String sqlSelection = SubredditContract.SavedSubreddits.COLUMN_SUBREDDIT_NAME + " = ?";
            String[] sqlSelectionArgs = { subredditName };
            Cursor cursor = mDB.query(
                    SubredditContract.SavedSubreddits.TABLE_NAME,
                    null,
                    sqlSelection,
                    sqlSelectionArgs,
                    null,
                    null,
                    null
            );
            isSaved = cursor.getCount() > 0;
            cursor.close();
        }
        return isSaved;
    }

    private long addSubredditToDB(SubredditSearchUtils.SubredditItem subredditItem) {
        if (subredditItem != null && subredditItem.name != null) {
            ContentValues row = new ContentValues();
            row.put(SubredditContract.SavedSubreddits.COLUMN_SUBREDDIT_NAME, subredditItem.name);
            row.put(SubredditContract.SavedSubreddits.COLUMN_CATEGORY, subredditItem.category);
            return mDB.insert(SubredditContract.SavedSubreddits.TABLE_NAME, null, row);
        } else {
            return -1;
        }
    }


    /* Loader */
    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "Search loader onCreate");
        String subredditSearchUrl = null;
        if (args != null) {
            subredditSearchUrl = args.getString(SEARCH_URL_KEY);
        }
        return new UrlJsonLoader(this, subredditSearchUrl);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        Log.d(TAG, "got search subreddit data from loader");
        mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
        if (data != null) {
            mLoadingErrorMessageTV.setVisibility(View.INVISIBLE);
            mSubredditItemRV.setVisibility(View.VISIBLE);
            ArrayList<SubredditSearchUtils.SubredditItem> subredditSearchData = SubredditSearchUtils.parseSubredditSearchJSON(data);
            mSearchListAdapter.updateSubredditSearchData(subredditSearchData);
        } else {
            mSubredditItemRV.setVisibility(View.INVISIBLE);
            mLoadingErrorMessageTV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // Nothing ...
    }
}
