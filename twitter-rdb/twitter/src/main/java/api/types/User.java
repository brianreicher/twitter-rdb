package api.types;

/*
 * Type Class for a User object 
 */
public class User {

    private int user_ID; // unique user identifier
    private int follows_id; // id of a user that the user follows


    public User(int user_ID, int follows_id) {
        this.user_ID = user_ID;
        this.follows_id = follows_id;
    }

    @Override
    public String toString() {
        return "User{" +
                ", user_ID='" + user_ID + '\'' +
                ", follows_id='" + follows_id +  '\'' +
                '}';
    }

    public int getUserID() {
        return user_ID;
    }

    public void setUserID(int user_ID){
        this.user_ID = user_ID;
    }

    public int getFollowsID() {
        return follows_id;
    }

    public void setFollowsID(int follows_id) {
        this.follows_id = follows_id;
    }    
}
