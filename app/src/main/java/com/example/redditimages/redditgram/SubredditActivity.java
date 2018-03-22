package com.example.redditimages.redditgram;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.redditimages.redditgram.SubredditDB.SubredditContract;
import com.example.redditimages.redditgram.SubredditDB.SubredditDBHelper;
import com.example.redditimages.redditgram.Utils.SubredditSearchUtils;

import java.util.ArrayList;

public class SubredditActivity extends AppCompatActivity implements SubredditAdapter.OnSubredditItemClickListener{

    private SubredditAdapter mAdapter;
    private RecyclerView mSubredditListItemsRV;
    private SQLiteDatabase mDB;
    public ArrayList<String> subredditItems;

    private static final String TAG = SubredditActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit);

        mSubredditListItemsRV = (RecyclerView) findViewById(R.id.rv_subreddit_list);


        // set up adapter
        mAdapter = new SubredditAdapter(this, this);
        mSubredditListItemsRV.setAdapter(mAdapter);
        mSubredditListItemsRV.setLayoutManager(new LinearLayoutManager(this));
        mSubredditListItemsRV.setHasFixedSize(true);

        // connect to the database
        SubredditDBHelper dbHelper = new SubredditDBHelper(this);
        mDB = dbHelper.getWritableDatabase();


        SubredditSearchUtils.SubredditItem subredditItem = new SubredditSearchUtils.SubredditItem();
        subredditItem.name = "r/earthporn";
        subredditItem.category = "science";
        subredditItem.nsfw = false;

        addSubredditToDB(subredditItem);

        subredditItems = getAllSubredditsFromDB();
        mAdapter.updateSubredditItems(subredditItems);
    }



    //onclicklistener

    @Override
    public void onSubredditItemClick(String subredditItem) {
        // do nothing for now...
    }

    //add
    protected long addSubredditToDB(SubredditSearchUtils.SubredditItem subredditItem) {

        if (subredditItem != null) {
            if (!checkIAdded(subredditItem.name)) {
                ContentValues values = new ContentValues();
                values.put(SubredditContract.FollowingSubreddits.COLUMN_SUBREDDIT_NAME,
                        subredditItem.name);
                values.put(SubredditContract.FollowingSubreddits.COLUMN_CATEGORY,
                        subredditItem.category);
                values.put(SubredditContract.FollowingSubreddits.COLUMN_BLOCKED,
                        0);
                values.put(SubredditContract.FollowingSubreddits.COLUMN_NSFW,
                        subredditItem.nsfw);
                Log.d(TAG, "Adding to subreddit database successful.");
                return mDB.insert(SubredditContract.FollowingSubreddits.TABLE_NAME, null, values);
            }
        }
        Log.d(TAG, "Failed to add to subreddit database.");
        return -1;
    }

    public long deleteSubredditFromDB(String subredditName) {
        if (subredditName != null) {
            String sqlSelection = SubredditContract.FollowingSubreddits.COLUMN_SUBREDDIT_NAME + " = ?";
            String[] sqlSelectionArgs = {subredditName};
            return mDB.delete(SubredditContract.FollowingSubreddits.TABLE_NAME, sqlSelection, sqlSelectionArgs);
        } else {
            Log.d(TAG, "Failed to remove subreddit from database.");
            return -1;
        }
    }

    private boolean checkIAdded(String subredditName) {
        boolean isMarked = false;
        if (subredditName != null) {
            String sqlSelection =
                    SubredditContract.FollowingSubreddits.COLUMN_SUBREDDIT_NAME  + " = ?";
            String[] sqlSelectionArgs = { subredditName };
            Cursor cursor = mDB.query(
                    SubredditContract.FollowingSubreddits.TABLE_NAME,
                    null,
                    sqlSelection,
                    sqlSelectionArgs,
                    null,
                    null,
                    null
            );
            isMarked = cursor.getCount() > 0;
            cursor.close();
        }
        return isMarked;
    }

    private ArrayList<String> getAllSubredditsFromDB() {
        Cursor cursor = mDB.query(
                 SubredditContract.FollowingSubreddits.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                SubredditContract.FollowingSubreddits.COLUMN_SUBREDDIT_NAME + " ASC"
        );

        ArrayList<String> subredditResults = new ArrayList<>();
        while (cursor.moveToNext()) {
            String searchResult;
            searchResult= cursor.getString(
                    cursor.getColumnIndex(SubredditContract.FollowingSubreddits.COLUMN_SUBREDDIT_NAME)
            );
            subredditResults.add(searchResult);
        }
        cursor.close();
        return subredditResults;
    }



}