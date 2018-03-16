package com.example.redditimages.redditgram;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.redditimages.redditgram.SubredditDB.SubredditContract;
import com.example.redditimages.redditgram.SubredditDB.SubredditDBHelper;

import java.util.ArrayList;

public class SubredditActivity extends AppCompatActivity implements SubredditAdapter.OnSubredditItemClickListener{

    private SubredditAdapter mAdapter;
    private RecyclerView mSubredditListItemsRV;
    private SQLiteDatabase mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit);

        mSubredditListItemsRV = (RecyclerView)findViewById(R.id.rv_subreddit_list);

        // set up adapter
        mAdapter = new SubredditAdapter(this, this);
        mSubredditListItemsRV.setAdapter(mAdapter);
        mSubredditListItemsRV.setLayoutManager(new LinearLayoutManager(this));
        mSubredditListItemsRV.setHasFixedSize(true);


        // connect to the database
        SubredditDBHelper dbHelper = new SubredditDBHelper(this);
        mDB = dbHelper.getWritableDatabase();

        getAllSubredditsFromDB();
    }

    //onclicklistener

    @Override
    public void onSubredditItemClick(String subredditItem) {
        // do nothing for now...
    }

    //add
/*
    protected void addSubreddit() {

    }

*/
    private ArrayList<String> getAllSubredditsFromDB() {
        Cursor cursor = mDB.query(
                SubredditContract.FollowingSubreddits.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                SubredditContract.FollowingSubreddits.SUBREDDIT_NAME + " ASC"
        );

        ArrayList<String> subredditResults = new ArrayList<>();
        while (cursor.moveToNext()) {
            String searchResult;
            searchResult= cursor.getString(
                    cursor.getColumnIndex(SubredditContract.FollowingSubreddits.SUBREDDIT_NAME)
            );
            subredditResults.add(searchResult);
        }
        cursor.close();
        return subredditResults;
    }


}