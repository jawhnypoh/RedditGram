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
import android.widget.Toast;

import com.example.redditimages.redditgram.Adapters.SearchListAdapter;
import com.example.redditimages.redditgram.SubredditDB.SubredditContract;
import com.example.redditimages.redditgram.SubredditDB.SubredditDBHelper;
import com.example.redditimages.redditgram.Utils.SearchLoader;
import com.example.redditimages.redditgram.Utils.SubredditSearchUtils;
import com.example.redditimages.redditgram.Utils.UrlJsonLoader;

import java.util.ArrayList;

/**
 * Created by jerrypeng on 3/20/18.
 */

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>, SearchListAdapter.OnSubredditAddListener,
        SearchListAdapter.SubredditChecker {

    public static final String TAG = SearchActivity.class.getSimpleName();
    private static final String SEARCH_URL_KEY = "searchSubredditURL";
    private static final int SEARCH_LOADER_ID = 0;

    private RecyclerView mSubredditItemRV;
    private SearchListAdapter mSearchListAdapter;
    private ProgressBar mLoadingIndicatorPB;
    private TextView mLoadingErrorMessageTV;
    private EditText mSearchBox;
    private ImageButton mSearchButton;
    private ImageButton mInstantAddSubredditButton;

    private SubredditDBHelper dbHelper;
    private SQLiteDatabase mDB;
    private Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_subreddit);

        mToast = null;

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
        dbHelper = new SubredditDBHelper(this);

        // Set up search bar
        mSearchButton = (ImageButton) findViewById(R.id.ib_search_subreddit_btn);
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

        mInstantAddSubredditButton = (ImageButton) findViewById(R.id.ib_instant_add_subreddit_btn);
        mInstantAddSubredditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subredditName = mSearchBox.getText().toString();
                if (!TextUtils.isEmpty(subredditName)) {
                    mSearchBox.setText("");
                    subredditName = stripPrefix(subredditName);
                    instantAddSubreddit(subredditName);
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

    String stripPrefix(String subredditName) {
        // strip prefix "/r" if exists
        if (subredditName.length() >= 2) {
            Log.d(TAG, subredditName.substring(0, 2));
            if (subredditName.substring(0, 2).equals("r/")) {
                return subredditName.substring(2);
            }
        }
        return subredditName;
    }

    void instantAddSubreddit(String subredditName) {
        if (!checkSubredditSaved(subredditName)) {
            // create new subredditItem object to add to DB
            SubredditSearchUtils.SubredditItem subredditItem = new SubredditSearchUtils.SubredditItem();
            subredditItem.name = subredditName;
            subredditItem.category = null;

            addSubredditToDB(subredditItem);
            toast("Subreddit " + subredditName + " saved!");
        } else {
            toast("Subreddit " + subredditName + " is already saved");
        }
    }

    void toast(String toastText) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, toastText, Toast.LENGTH_LONG);
        mToast.show();
    }

    /* Add Subreddit Button Click Listener */
    @Override
    public void onSubredditAdd(SubredditSearchUtils.SubredditItem subredditItem, ImageButton addSubredditButton) {
        if (!checkSubredditSaved(subredditItem.name)) {
            long status = addSubredditToDB(subredditItem);
            if (status == -1 ) Log.d(TAG, "IT IS NOT ADDED");
            addSubredditButton.setImageResource(R.drawable.ic_action_check);
            toast("Subreddit " + subredditItem.name + " saved!");
        } else {
            deleteSubredditFromDB(subredditItem.name);
            addSubredditButton.setImageResource(R.drawable.ic_action_add);
            toast("Subreddit " + subredditItem.name + " removed!");
        }

    }

    Boolean checkSubredditSaved(String subredditName) {
        Boolean isSaved = true;
        mDB = dbHelper.getWritableDatabase();
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
        mDB.close();
        return isSaved;
    }

    private long addSubredditToDB(SubredditSearchUtils.SubredditItem subredditItem) {
        if (subredditItem != null && subredditItem.name != null) {
            ContentValues row = new ContentValues();
            row.put(SubredditContract.SavedSubreddits.COLUMN_SUBREDDIT_NAME, subredditItem.name);
            row.put(SubredditContract.SavedSubreddits.COLUMN_CATEGORY, subredditItem.category);
            row.put(SubredditContract.SavedSubreddits.COLUMN_ICON_URL, subredditItem.icon_url);
            mDB = dbHelper.getWritableDatabase();
            long status = mDB.insert(SubredditContract.SavedSubreddits.TABLE_NAME, null, row);
            mDB.close();
            return status;
        } else {
            return -1;
        }
    }

    private long deleteSubredditFromDB(String subredditName) {
        if (subredditName != null) {
            String sqlSelection = SubredditContract.SavedSubreddits.COLUMN_SUBREDDIT_NAME + " = ?";
            String[] sqlSelectionArgs = {subredditName};
            mDB = dbHelper.getWritableDatabase();
            long status = mDB.delete(SubredditContract.SavedSubreddits.TABLE_NAME, sqlSelection, sqlSelectionArgs);
            mDB.close();
            return status;
        } else {
            Log.d(TAG, "Failed to remove subreddit from database.");
            return -1;
        }
    }


    /* Interface from adapter to check whether name exists in database to assign add/remove button */
    @Override
    public Boolean AdapterCheckIfExistInDB(String subredditName) {
        return checkSubredditSaved(subredditName);
    }


    /* Loader */
    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "Search loader onCreate");
        String subredditSearchUrl = null;
        if (args != null) {
            subredditSearchUrl = args.getString(SEARCH_URL_KEY);
        }
        return new SearchLoader(this, subredditSearchUrl);
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
