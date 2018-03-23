package com.example.redditimages.redditgram;

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

import java.util.ArrayList;

public class SubredditActivity extends AppCompatActivity implements SubredditAdapter.OnSubredditItemClickListener{

    private static final String TAG = SubredditActivity.class.getSimpleName();

    private SubredditAdapter mAdapter;
    private RecyclerView mSubredditListItemsRV;
    private SQLiteDatabase mDB;
    public ArrayList<String> subredditItems;

    public interface GetSubreddits {
        ArrayList<String> getAllSubredditsFromDB();
    }

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

        subredditItems = getAllSubredditsFromDB();
        mAdapter.updateSubredditItems(subredditItems);
    }



    /* onclicklistener */

    @Override
    public void onSubredditItemClick(String subredditItem) {
        // do nothing for now...
    }

    public long deleteSubredditFromDB(String subredditName) {
        if (subredditName != null) {
            String sqlSelection = SubredditContract.SavedSubreddits.COLUMN_SUBREDDIT_NAME + " = ?";
            String[] sqlSelectionArgs = {subredditName};
            return mDB.delete(SubredditContract.SavedSubreddits.TABLE_NAME, sqlSelection, sqlSelectionArgs);
        } else {
            Log.d(TAG, "Failed to remove subreddit from database.");
            return -1;
        }
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
}