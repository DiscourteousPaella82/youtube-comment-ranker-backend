package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import comment.CommentData;
import comment.CommentThreadData;

public class DatabaseFunctions {

    String localDateParsed;

    public DatabaseFunctions(){
        localDateParsed = LocalDate.now().toString().replace("-", "");
        System.out.println("Date: " + localDateParsed);
    }

    public Connection connection_to_db(String port, String dbname, String username, String password){
        Connection connection = null;

        try{
            Class.forName("org.postgresql.Driver");
            System.out.println("Port: " + port + "\nUsername: " + username + "\nPassword: " + password);
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:" + port + "/" + dbname,username,password);
            if(connection!= null){
                System.out.println("Connection established!");
            }
            else{
                System.out.println("Failed to connect to database");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public void createTable(Connection connection){
        Statement statement;
        String query="CREATE TABLE IF NOT EXISTS comments" + localDateParsed + "(id SERIAL,"
            + "authorDisplayName VARCHAR(100),authorProfileImageURL VARCHAR(150),authorChannelUrl VARCHAR(100),"
            + "textOriginal VARCHAR(2500),videoId VARCHAR(12), parentId VARCHAR(50),likeRating INTEGER, PRIMARY KEY(id));";
        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);
            System.out.println("Table comments" + localDateParsed + " created!");
        } catch (Exception e) {
            System.out.println("ERROR: failed to create new table");
            throw new RuntimeException(e);
        }
    }

    public void insertIntoCommentTable(Connection connection, List<CommentThreadData> commentThreadData) throws SQLException {
        try {
            Statement statement = connection.createStatement();
            for(int i = 0; i < commentThreadData.size(); i++){
                CommentData topLevelCommentData = commentThreadData.get(i).topLevelComment();
                
                statement.addBatch("INSERT INTO comments" + localDateParsed + "("
                    + " authorDisplayName, authorProfileImageURL, authorChannelUrl,"
                    + " textOriginal, videoId, parentId, likeRating)"
                    + "VALUES ('" + topLevelCommentData.authorDisplayName() + "','" + topLevelCommentData.authorProfileImageURL() + "','"
                    + topLevelCommentData.authorChannelUrl() + "','" + topLevelCommentData.textDisplay() + "','" + topLevelCommentData.videoId()
                    + "','" + topLevelCommentData.parentId() + "'," + topLevelCommentData.likeRating().toString() + ");");

                if(!commentThreadData.get(i).commentReplies().isEmpty()){
                    for(int j = 0; j < commentThreadData.get(i).commentReplies().size(); j++){
                        CommentData commentData = commentThreadData.get(i).commentReplies().get(j);

                        statement.addBatch("INSERT INTO comments" + localDateParsed + "("
                            + "authorDisplayName, authorProfileImageURL, authorChannelUrl, "
                            + "textOriginal, videoId, parentId, likeRating)"
                            + "VALUES ('" + commentData.authorDisplayName() + "','" + commentData.authorProfileImageURL() + "','"
                            + commentData.authorChannelUrl() + "','" + commentData.textDisplay() + "','" + commentData.videoId()
                            + "','" + commentData.parentId() + "'," + commentData.likeRating().toString() + ");");
                    }
                }
            }
            statement.executeBatch();

            System.out.println("Rows added!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
