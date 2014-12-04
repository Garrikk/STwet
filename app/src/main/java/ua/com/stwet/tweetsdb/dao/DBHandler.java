package ua.com.stwet.tweetsdb.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ua.com.stwet.tweetsdb.objects.UserTweets;

public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "userTweets.db";
    private static final String TABLE_NAME = "tweets";
    private static final String KEY_ID = "id";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_TWEET = "tweet";
    private static final String KEY_DATE = "date";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_USER_NAME + " TEXT,"
                + KEY_TWEET + " TEXT," + KEY_DATE + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean searchTweet(UserTweets uTweets) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY_USER_NAME + " LIKE " + "'" + uTweets.getUserName() + "'" +
                " AND " + KEY_TWEET + " LIKE " + "'" + uTweets.getTweet() + "'" +
                " AND " + KEY_DATE + " LIKE " + "'" + uTweets.getDate() + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (!((cursor != null) && (cursor.getCount() > 0))) {
            addTweet(uTweets);
            return true;
        }
        db.close();
        return false;
    }

    public void addTweet(UserTweets uTweets) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, uTweets.getUserName());
        values.put(KEY_TWEET, uTweets.getTweet());
        values.put(KEY_DATE, String.valueOf(uTweets.getDate()));

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<UserTweets> getUserTweets(String uName) {

        List<UserTweets> tweetList = new ArrayList<UserTweets>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY_USER_NAME + " LIKE " + "'" + uName + "'"
                + " ORDER BY " + KEY_ID + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                UserTweets uTweets = null;
                uTweets = new UserTweets(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1), cursor.getString(2), twitterHumanFriendlyDate(cursor.getString(3)));
                tweetList.add(uTweets);
            } while (cursor.moveToNext());
        }

        db.close();
        return tweetList;
    }

    public List<UserTweets> getAllContacts() {
        List<UserTweets> tweetList = new ArrayList<UserTweets>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                UserTweets uTweets = null;
                uTweets = new UserTweets(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1), cursor.getString(2), twitterHumanFriendlyDate(cursor.getString(3)));
                tweetList.add(uTweets);
            } while (cursor.moveToNext());
        }

        db.close();
        return tweetList;
    }

    public void deleteTweets(String uName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_NAME, KEY_ID + " = ?", new String[]{uName});
        db.close();
    }

    public static String twitterHumanFriendlyDate(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
        dateFormat.setLenient(false);
        Date created = null;
        try {
            created = dateFormat.parse(dateStr);
        } catch (Exception e) {
            return null;
        }

        Date today = new Date();
        Long duration = today.getTime() - created.getTime();

        int second = 1000;
        int minute = second * 60;
        int hour = minute * 60;
        int day = hour * 24;

        if (duration < second * 7) {
            return "right now";
        }

        if (duration < minute) {
            int n = (int) Math.floor(duration / second);
            return n + " seconds ago";
        }

        if (duration < minute * 2) {
            return "about 1 minute ago";
        }

        if (duration < hour) {
            int n = (int) Math.floor(duration / minute);
            return n + " minutes ago";
        }

        if (duration < hour * 2) {
            return "about 1 hour ago";
        }

        if (duration < day) {
            int n = (int) Math.floor(duration / hour);
            return n + " hours ago";
        }
        if (duration > day && duration < day * 2) {
            return "yesterday";
        }

        if (duration < day * 365) {
            int n = (int) Math.floor(duration / day);
            return n + " days ago";
        } else {
            return "over a year ago";
        }
    }

}
