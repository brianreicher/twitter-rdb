package api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;


/**
 * This program exercises the DPDatabaseAPI (MySQL implementation).
 * Notice that nothing other than the instantiation of the API shows us that
 * the underlying database is Relational, or MySQL.

 */
public class TwitterDriver {

    private static DPDatabaseAPI api = new DPDatabaseMysql();

	public static void getUserTimeline(Integer user_id){
		// get list of all potential users to pick from
		List<Integer> all_users = api.getAllUsers();
		double elapsed_time = 0;

		if(all_users.contains(user_id)){
			long start_time = System.nanoTime();
			long end_time = 0;
			while(Math.abs((end_time - start_time))/ 10e8 < 60){
				api.getTimeline(user_id);
				end_time = System.nanoTime();
			}
			elapsed_time = (end_time - start_time)/10e8;
		}
		else{
			System.out.println("USER_NOT_FOUND: " + user_id);
		}


		// RETURN METRICS FOR FETCHED TIMELINE
		System.out.println("Time Taken to Get Timeline (seconds): " + elapsed_time);
	}

	public static void getUserTimeline(){
		// get list of all potential users to pick from
		List<Integer> all_users = api.getAllUsers();
		int total_timelines_fetched = 0;
		// print?
		long start_time = System.nanoTime();
		long end_time = 0;
		while(Math.abs((end_time - start_time))/ 10e8 < 60){
			int random_user_id = all_users.get((int)(Math.random() * all_users.size()));
			api.getTimeline(random_user_id);
			total_timelines_fetched++;
			// end_time = System.nanoTime();
		}

		end_time = System.nanoTime();
		double elapsed_time = (end_time - start_time)/10e8;


		// RETURN METRICS FOR FETCHED TIMELINE
		System.out.println("Total Elapsed Time (seconds): " + elapsed_time);
        System.out.println("Total Timelines Fetched: " + total_timelines_fetched);
        System.out.println("Timelines per Second: " + total_timelines_fetched/elapsed_time);
	}

	public static void postAllTweets(){

		long start_time = System.nanoTime();

		// read CSV file containing tweets
		String csv_line;
		BufferedReader buff;
		int total_tweets_posted = 0; 
		try {
			buff = new BufferedReader(new FileReader("db/tweet.csv"));
			buff.readLine();
			while((csv_line = buff.readLine()) != null) {
				api.postTweet(formatTweetFromCSV(csv_line));
				total_tweets_posted++;
				System.out.println("Total Tweets Sent: " + total_tweets_posted);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		long end_time = System.nanoTime();

		
		double elapsed_time = (end_time-start_time)/10e8;

		// RETURN METRICS FOR ALL POSTED TWEETS
		System.out.println("Time Taken to Post Tweets (seconds): " + elapsed_time);
		System.out.println("Total Tweets Posted: " + total_tweets_posted);
		System.out.println("Tweets Posted per Minute: " + (60 * total_tweets_posted/elapsed_time));
		System.out.println("Tweets Posted per Second: " + total_tweets_posted/elapsed_time);
	}

	public static Tweet formatTweetFromCSV(String tweetline){
		List<String> tweet_values = new ArrayList<String>();

		try(Scanner tweetScanner = new Scanner(tweetline)){
			tweetScanner.useDelimiter(",");
			while (tweetScanner.hasNext()){
				tweet_values.add(tweetScanner.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Tweet(
						Integer.parseInt(tweet_values.get(0)),
						new Timestamp(System.currentTimeMillis()),
						tweet_values.get(1)
		);
	}

    public static void main(String[] args) throws Exception {

    	// Authenticate your access to the server.
		String url =  "jdbc:mysql://localhost:3306/twitter?serverTimezone=EST5EDT";
		String user = System.getenv("TWITTER_USERNAME");
		String password = System.getenv("TWITTER_PASSWORD");
		System.out.println(user + " " + password);
	
		api.authenticate(url, "root", "NYGiants9021#"); // DON'T HARDCODE PASSWORDS!
		
		// postAllTweets();
		getUserTimeline();

		api.closeConnection();

	}
}
