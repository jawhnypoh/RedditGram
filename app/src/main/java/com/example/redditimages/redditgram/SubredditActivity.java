package com.example.redditimages.redditgram;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.redditimages.redditgram.Adapters.SubredditAdapter;
import com.example.redditimages.redditgram.SubredditDB.SubredditContract;
import com.example.redditimages.redditgram.SubredditDB.SubredditDBHelper;
import com.example.redditimages.redditgram.Utils.SubredditSearchUtils;

import java.util.ArrayList;

public class SubredditActivity extends AppCompatActivity implements SubredditAdapter.OnSubredditItemClickListener {

    private static final String TAG = SubredditActivity.class.getSimpleName();

    private SubredditAdapter mAdapter;
    private RecyclerView mSubredditListItemsRV;
    private SQLiteDatabase mDB;
    private SubredditDBHelper dbHelper;
    public ArrayList<SubredditSearchUtils.SubredditItem> subredditItems;

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
        dbHelper = new SubredditDBHelper(this);
        subredditItems = getAllSubredditsFromDB();
        mAdapter.updateSubredditItems(subredditItems);
    }



    /* onclicklistener */
    @Override
    public void onSubredditItemClick(String subredditName, boolean isBlocked) {
        if (subredditName != null) {
            isBlocked = !isBlocked;
            String blocked= isBlocked ? "1" : "0";
            ContentValues row = new ContentValues();
            row.put(SubredditContract.SavedSubreddits.COLUMN_BLOCKED, blocked);

            String sqlSelection = SubredditContract.SavedSubreddits.COLUMN_SUBREDDIT_NAME + " = ?";
            String[] sqlSelectionArgs = { subredditName };

            mDB = dbHelper.getWritableDatabase();
            mDB.update(SubredditContract.SavedSubreddits.TABLE_NAME, row, sqlSelection, sqlSelectionArgs);
            mDB.close();
        }
    }

    @Override
    public long deleteSubredditFromDB(String subredditName) {
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


    public ArrayList<SubredditSearchUtils.SubredditItem> getAllSubredditsFromDB() {
        mDB = dbHelper.getWritableDatabase();
        Cursor cursor = mDB.query(
                 SubredditContract.SavedSubreddits.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                SubredditContract.SavedSubreddits.COLUMN_SUBREDDIT_NAME + " ASC"
        );

        ArrayList<SubredditSearchUtils.SubredditItem> subredditResults = new ArrayList<>();
        while (cursor.moveToNext()) {
            SubredditSearchUtils.SubredditItem item = new SubredditSearchUtils.SubredditItem();
            String blocked;
            item.name = cursor.getString(
                    cursor.getColumnIndex(SubredditContract.SavedSubreddits.COLUMN_SUBREDDIT_NAME)
            );
            item.category = cursor.getString(
                    cursor.getColumnIndex(SubredditContract.SavedSubreddits.COLUMN_CATEGORY)
            );
            item.icon_url = cursor.getString(
                    cursor.getColumnIndex(SubredditContract.SavedSubreddits.COLUMN_ICON_URL)
            );
            blocked = cursor.getString(
                    cursor.getColumnIndex(SubredditContract.SavedSubreddits.COLUMN_BLOCKED)
            );
            item.is_blocked = blocked.equals("1") ? true : false;
            subredditResults.add(item);
        }
        cursor.close();
        mDB.close();
        return subredditResults;
    }
}