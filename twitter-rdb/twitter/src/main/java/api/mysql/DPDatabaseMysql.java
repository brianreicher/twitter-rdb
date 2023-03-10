package api.mysql;

import java.sql.*;
import java.util.*;

import api.DPDatabaseAPI;
import api.types.Tweet;
import api.types.User;
import database.DBUtils;

/*
 * implementation of the MySQL Twitter API.
 */
public class DPDatabaseMysql implements DPDatabaseAPI {


    // dbu utilization
    DBUtils dbu;

    /**
     * Authenticate a MySQL connection string with enviornment variables
     * @param url Connection url
     * @param user Username, defined by enviornment variables
     * @param password Password, defined by enviornment variables
     */
    public void authenticate(String url, String user, String password) {
        // set the dbu connection string
        dbu = new DBUtils(url, user, password);
    }


    /**
     * Close the connection when application finishes
     */
    @Override
    public void closeConnection() { dbu.closeConnection(); }


    /**
     * Post one tweet
     * @param t The tweet
     */
    @Override
    public void postTweet(Tweet t) {
        
        // SQL statment
        String sql = "INSERT INTO tweets (user_id, tweet_text) VALUES" +
                "('"+t.getUserID()+"','"+t.getTweetText()+"')";
        // execute the statement
        dbu.insertOneRecord(sql);
    }


    /**
     * Post a follower/followee from a CSV stream
     * @param user The user's unique identifiers
     */
    public void postFollow(User user){
        // SQL statement
        String sql = "INSERT INTO follows (user_id, follows_id) VALUES" +
        "('"+user.getUserID()+"','"+user.getFollowsID()+"')";
        // execute the statement
        dbu.insertOneRecord(sql);
    }

    /**
     * Attempted batch implementation for posting tweets
     * @param twlist A list of tweets
     */
    public void postTweets(List<Tweet> twlist) {
        // SQL statement
        String sql = "INSERT INTO tweets (user_id, tweet_text) VALUES (?,?)";

        try {
            // connect, set prepareStatement and batch tweets in the list
            Connection con = dbu.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            con.setAutoCommit(false);

            // iterate through the list of tweets
            for (Tweet t : twlist) {
                // set the PreparedStatement parameters
                pstmt.setInt(1, t.getUserID());
                pstmt.setString(2, t.getTweetText());
                pstmt.addBatch();
            }
            // execute the batch
            pstmt.executeBatch();

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }  
    }

    /**
     * Get a given user's home timeline
     * @param userId The user's unique identifier
     * @return A list of tweets
     */
    @Override
    public List<Tweet> getTimeline(Integer userID) {
        // SQL statement, limiting to 10 tweets per user timeline
        String sql = "SELECT tweets.tweet_id as tweet_id, follows.follows_id as user_id, tweets.tweet_ts as tweet_ts, tweets.tweet_text as tweet_text " +
                     "FROM tweets JOIN follows on tweets.user_id = follows.follows_id " +
                     "WHERE follows.user_id = " + userID +" LIMIT 10;";

        // initialize the list of tweets, representing a timelie
        List<Tweet> timeline = new ArrayList<Tweet>();

        try {
            // get connection and initialize statement
            Connection con = dbu.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // if moving the cursor forward in the results position produces an output, initialize a Tweet object with the data and add it to the timeline
            while (rs.next()){
                timeline.add(new Tweet(rs.getInt("tweet_id"), rs.getInt("user_id"), rs.getTimestamp("tweet_ts"), rs.getString("tweet_text")));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        // return the fetched timeline
        return timeline;

    }

    /**
     * Tweets posted by a given userID
     * @param userId The user's unique identifier
     * @return A list of tweets
     */
    @Override
    public List<Tweet> getTweets(Integer userID) {
        // SQL statement -- tweets posted by a given user
        String sql = "SELECT *"+
                    "FROM tweets" +
                    "where follows_id = " + userID + ";";

        // list of tweets
        List<Tweet> tweets = new ArrayList<Tweet>();

        try {
        // get connection and initialize statement
            Connection con = dbu.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){
                tweets.add(new Tweet(rs.getInt("tweet_id"), rs.getInt("user_id"), rs.getTimestamp("tweet_ts"), rs.getString("tweet_text")));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return tweets;
    }

    /**
     * Who is following a given user
     * @param userId The user's unique identifier
     * @return A list of userIDs
     */
    @Override
    public Set<Integer> getFollowers(Integer userID) {
        // SQL statement -- who is following a given user
        String sql = "SELECT follows_id"+
                     "FROM follows" +
                     "where user_id = " + userID + ";";

        //  list of follower_ids
        List<Integer> followers = new ArrayList<Integer>();

        try {
            // get connection and initialize statement
            Connection con = dbu.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){
                followers.add(rs.getInt("follows_id"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        // utlize set to eliminate duplicates in the follower_ids
        return new HashSet<>(followers);    
    }

    /**
     * Who a given user user is following
     * @param userId The user's unique identifier
     * @return A list of userIDs
     */
    @Override
    public Set<Integer> getFollowees(Integer userID) {
        // SQL statement -- who a given user is following
        String sql = "SELECT user_id"+
                     "FROM follows" +
                     "where follows_id = " + userID + ";";

        //  list of followee IDs
        List<Integer> followees = new ArrayList<Integer>();

        try {
        // get connection and initialize statement
            Connection con = dbu.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){
                followees.add(rs.getInt("user_id"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        // utlize set to eliminate duplicates in the followee IDs
        return new HashSet<>(followees);   
    }

    /**
     * Fetch all users in the database
     * @return A list of all of the users
     */
    @Override
    public List<Integer> getAllUsers() {
        // SQL statement, utilizing UNIONS to fetch all unique user_ids (followers & users)
        String sql = "SELECT user_id FROM tweets UNION SELECT user_id FROM follows UNION select follows_id FROM follows";

        // 
        List<Integer> users = new ArrayList<Integer>();

        try {
        // get connection and initialize statement
            Connection con = dbu.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){
                users.add(rs.getInt("user_id"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        // return all total users across the database, makes it simplistic to grab random users from a list
        return users;
    }
}
