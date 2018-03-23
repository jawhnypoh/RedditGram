package com.example.redditimages.redditgram.SubredditDB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Tam on 3/15/2018.
 */

public class SubredditDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME =
            "followingSubreddits.db";
    private static final int DATABASE_VERSION = 1;

    public SubredditDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FOLLOWING_SUBREDDITS_TABLE =
                "CREATE TABLE " + SubredditContract.SavedSubreddits.TABLE_NAME + " (" +
                        SubredditContract.SavedSubreddits._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SubredditContract.SavedSubreddits.COLUMN_SUBREDDIT_NAME + " TEXT NOT NULL, " +
                        SubredditContract.SavedSubreddits.COLUMN_CATEGORY + " TEXT, " +
                        SubredditContract.SavedSubreddits.COLUMN_ICON_URL + " TEXT, " +
                        SubredditContract.SavedSubreddits.COLUMN_BLOCKED + " INT DEFAULT 0 " +
                        ");";

        db.execSQL(SQL_CREATE_FOLLOWING_SUBREDDITS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SubredditContract.SavedSubreddits.TABLE_NAME);
        onCreate(db);
    }
}
