package ua.com.stwet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ua.com.stwet.tweetsdb.dao.DBHandler;
import ua.com.stwet.tweetsdb.objects.UserTweets;


public class TweetsActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    private final String twitterAPIKEY = "ttwu1CXhJHsZQJ9YNNodX4smn";
    private final String twitterAPISECRET = "wrVhqBEmulBAYNrf94BXUBryuGXnY2QFRerEsOGfvIuPAab7r1";

    private final String twitterAPIurl = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=";
    private final int tweets2Return = 10;

    private String twitterToken = null;
    private String jsonTokenStream = null;
    private String jsonFeed = null;
    private String tweetJSON = null;

    private ListView listTweets;
    private SwipeRefreshLayout swipeLayout;

    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweets);

        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        if (!(setUpView())) {
            new loadTwitterFeed().execute();
        }

        listTweets.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final String tweets = (String) parent.getAdapter().getItem(position);
                final boolean yesOrNo;

                AlertDialog.Builder b = new AlertDialog.Builder(TweetsActivity.this);
                b.setIcon(android.R.drawable.ic_dialog_alert);
                b.setMessage("Do you want share tweet to Facebook?");
                b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (Session.getActiveSession() != null && Session.getActiveSession().isOpened()) {
                            postStatusMessage(tweets);
                        } else {
                            Session session = Session.getActiveSession();
                            if (!session.isOpened() && !session.isClosed()) {
                                session.openForRead(new Session.OpenRequest(TweetsActivity.this)
                                        .setCallback(mFacebookCallback));
                            } else {
                                Session.openActiveSession(TweetsActivity.this, true, mFacebookCallback);
                            }
                        }
                    }
                });
                b.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                b.show();

                return true;

            }
        });
    }


//    // ========== FaceBook Sharing
//    /**
//     * Publish link in FaceBook
//     *
//     * @param name        - title of block
//     * @param caption     - text on bottom of block
//     * @param description - description of link (between title and caption)
//     * @param link        - http:// etc
//     * @param pictureLink - http:// etc - link on image in web
//     */
//    public final void facebookPublish(String name, String caption, String description, String link, String pictureLink) {
//        if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
//                FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
////Facebook-client is installed
//            FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
//                    .setName(name)
//                    .setCaption(caption)
//                    .setDescription(description)
//                    .setLink(link)
//                    .setPicture(pictureLink)
//                    .build();
//            uiHelper.trackPendingDialogCall(shareDialog.present());
//        } else {
////Facebook-client is not installed – use web-dialog
//            Bundle params = new Bundle();
//            params.putString("name", name);
//            params.putString("caption", caption);
//            params.putString("description", description);
//            params.putString("link", link);
//            params.putString("picture", pictureLink);
//            WebDialog feedDialog = new WebDialog.FeedDialogBuilder(this, Utility.getMetadataApplicationId(this), params)
//                    .setOnCompleteListener(new WebDialog.OnCompleteListener() {
//                        //Listener for web-dialog
//                        @Override
//                        public void onComplete(Bundle values, FacebookException error) {
//                            if (error == null) {
//                                final String postId = values.getString("post_id");
//                                if (postId != null) {
//                                    Toast.makeText(getApplicationContext(),
//                                            "Posted story, id: " + postId,
//                                            Toast.LENGTH_SHORT).show();
//                                } else {
//                                    Toast.makeText(getApplicationContext(),
//                                            "Publish cancelled",
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            } else if (error instanceof FacebookOperationCanceledException) {
//                                Toast.makeText(getApplicationContext(),
//                                        "Publish cancelled",
//                                        Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(getApplicationContext(),
//                                        "Error posting story",
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        }
//
//                    })
//                    .build();
//            feedDialog.show();
//        }
//    }


    private Session.StatusCallback mFacebookCallback = new Session.StatusCallback() {
        @Override
        public void
        call(final Session session, final SessionState state, final Exception exception) {

            if (state.isOpened()) {
                String facebookToken = session.getAccessToken();
                Request.newMeRequest(session, new Request.GraphUserCallback() {

                    @Override
                    public void onCompleted(GraphUser user, com.facebook.Response response) {

                    }
                }).executeAsync();
            }
        }
    };

    public void postStatusMessage(String message) {
        Request request = Request.newStatusUpdateRequest(
                Session.getActiveSession(), message,
                new Request.Callback() {
                    @Override
                    public void onCompleted(Response response) {
                        if (response.getError() == null)
                            Toast.makeText(TweetsActivity.this,
                                    "Status updated successfully",
                                    Toast.LENGTH_LONG).show();
                    }
                });
        request.executeAsync();
    }

//    public void postImage() {
//            Bitmap img = BitmapFactory.decodeResource(getResources(),
//                    R.drawable.ic_launcher);
//            Request uploadRequest = Request.newUploadPhotoRequest(
//                    Session.getActiveSession(), img, new Request.Callback() {
//                        @Override
//                        public void onCompleted(Response response) {
//                            Toast.makeText(TweetsActivity.this,
//                                    "Photo uploaded successfully",
//                                    Toast.LENGTH_LONG).show();
//                        }
//                    });
//            uploadRequest.executeAsync();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });
    }

    public Boolean setUpView() {
        String screenName = MainActivity.getNameUser();
        ArrayList<String> tweets = new ArrayList<String>();
        DBHandler db = new DBHandler(getApplicationContext());

        listTweets = (ListView) findViewById(R.id.listTweets);
        List<UserTweets> tweet = (List<UserTweets>) db.getUserTweets(screenName);
        if (!(tweet.isEmpty())) {
            for (UserTweets ut : tweet) {
                tweets.add("@" + screenName + " - " + ut.getDate() + "\n" + ut.getTweet() + "\n");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.simple_list_item_1, tweets);
            listTweets.setAdapter(adapter);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                new loadTwitterFeed().execute();
            }
        }, 5000);
    }

    protected class loadTwitterFeed extends AsyncTask<Void, Void, Void> {

        String screenName = MainActivity.getNameUser();
        String tweeterURL = twitterAPIurl + screenName
                + "&include_rts=1&count=" + tweets2Return;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient(
                        new BasicHttpParams());
                HttpPost httppost = new HttpPost(
                        "https://api.twitter.com/oauth2/token");

                String apiString = twitterAPIKEY + ":" + twitterAPISECRET;
                String authorization = "Basic "
                        + Base64.encodeToString(apiString.getBytes(),
                        Base64.NO_WRAP);

                httppost.setHeader("Authorization", authorization);
                httppost.setHeader("Content-Type",
                        "application/x-www-form-urlencoded;charset=UTF-8");
                httppost.setEntity(new StringEntity(
                        "grant_type=client_credentials"));

                InputStream inputStream = null;
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                JSONObject root = new JSONObject(sb.toString());
                twitterToken = root.getString("access_token");

                HttpGet httpget = new HttpGet(tweeterURL);
                httpget.setHeader("Authorization", "Bearer " + twitterToken);
                httpget.setHeader("Content-type", "application/json");

                response = httpclient.execute(httpget);
                entity = response.getEntity();
                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    jsonFeed = EntityUtils.toString(entity);
                } else {
                    onDestroy();
                    finish();
                }

            } catch (Exception e) {
                Log.e("loadTwitterToken",
                        "doInBackground Error:" + e.getMessage());
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (jsonFeed != null) {
                ArrayList<String> tweets = new ArrayList<String>();
                DBHandler db = new DBHandler(getApplicationContext());
                try {
                    StringBuilder sb = new StringBuilder();
                    JSONArray jsonArray = new JSONArray(jsonFeed);
                    for (int i = jsonArray.length() - 1; i >= 0; i--) {
                        JSONObject tweetObject = jsonArray.getJSONObject(i);
                        UserTweets uTweets = new UserTweets();
                        uTweets.setUserName(screenName);
                        uTweets.setTweet(tweetObject.get("text").toString());
                        uTweets.setDate(tweetObject.getString("created_at").toString());
                        db.searchTweet(uTweets);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                listTweets = (ListView) findViewById(R.id.listTweets);
                List<UserTweets> tweet = db.getUserTweets(screenName);
                for (UserTweets ut : tweet) {
                    tweets.add("@" + screenName + " - " + ut.getDate() + "\n" + ut.getTweet() + "\n");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.simple_list_item_1, tweets);
                listTweets.setAdapter(adapter);
            } else {
                Toast.makeText(TweetsActivity.this,
                        "User not found", Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tweets, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_update_tweets:
                onRefresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
