package ua.com.stwet.tweetsdb.objects;

public class UserTweets {

    private long id;
    private String userName;
    private String tweet;
    private String date;

    public UserTweets() {
    }

    public UserTweets(long id, String userName, String tweet, String date) {
        this.id = id;
        this.userName = userName;
        this.tweet = tweet;
        this.date = date;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTweet() {
        return tweet;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getId() {
        return id;
    }
}
