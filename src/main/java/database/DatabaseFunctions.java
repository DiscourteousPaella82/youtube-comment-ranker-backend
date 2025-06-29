package database;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import comment.CommentData;
import comment.CommentThreadData;

public class DatabaseFunctions {

    private String localDateParsed;
    private int numberInsertions;

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
                System.out.println("\u001B[32mConnection established!\u001B[0m");
            }
            else{
                System.out.println("\u001B[31mDatabase error:Failed to connect to database\u001B[0m");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return connection;
    }

    public void createTable(Connection connection){
        Statement statement;
        String query="CREATE TABLE IF NOT EXISTS comments" + localDateParsed + "(id SERIAL,"
            + "authorDisplayName VARCHAR(100),authorProfileImageURL VARCHAR(150),authorChannelUrl VARCHAR(200),"
            + "textOriginal VARCHAR(2500),videoId VARCHAR(12), parentId VARCHAR(50),likeRating INTEGER, PRIMARY KEY(id));";
        try {            
            statement = connection.createStatement();
            statement.executeUpdate(query);
            System.out.println("\u001B[32mTable comments" + localDateParsed + " created!\u001B[0m");
        } catch (Exception e) {
            System.out.println("\u001B[31mDatabase error: failed to create new table\u001B[0m");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void insertIntoCommentTable(Connection connection, List<CommentThreadData> commentThreadData) throws SQLException {
        PreparedStatement preparedStatement = null;

        numberInsertions = 0;
        String query = ("INSERT INTO comments" + localDateParsed +"("
            + " authorDisplayName, authorProfileImageURL, authorChannelUrl,"
            + " textOriginal, videoId, parentId, likeRating)"
            + "VALUES (?,?,?,?,?,?,?);");

        preparedStatement = connection.prepareStatement(query);

            for(int i = 0; i < commentThreadData.size(); i++){
                int attempts = 0;
                while(true){ 
                    try {  
                        CommentData topLevelCommentData = commentThreadData.get(i).topLevelComment();


                        preparedStatement.setString(1, topLevelCommentData.authorDisplayName());
                        preparedStatement.setString(2, topLevelCommentData.authorProfileImageURL());
                        preparedStatement.setString(3, topLevelCommentData.authorChannelUrl());
                        preparedStatement.setString(4, topLevelCommentData.textDisplay());
                        preparedStatement.setString(5, topLevelCommentData.videoId());
                        preparedStatement.setString(6, topLevelCommentData.parentId());
                        preparedStatement.setInt(7, Math.toIntExact(topLevelCommentData.likeRating()));
                        preparedStatement.addBatch();

                        if(!commentThreadData.get(i).commentReplies().isEmpty()){
                            for(int j = 0; j < commentThreadData.get(i).commentReplies().size(); j++){
                                numberInsertions++;
                                CommentData commentData = commentThreadData.get(i).commentReplies().get(j);

                                preparedStatement.setString(1, commentData.authorDisplayName());
                                preparedStatement.setString(2, commentData.authorProfileImageURL());
                                preparedStatement.setString(3, commentData.authorChannelUrl());
                                preparedStatement.setString(4, commentData.textDisplay());
                                preparedStatement.setString(5, commentData.videoId());
                                preparedStatement.setString(6, commentData.parentId());
                                preparedStatement.setInt(7, Math.toIntExact(commentData.likeRating()));
                                preparedStatement.addBatch();
                            }
                        }
                        
                        if(i % 100 == 0) preparedStatement.executeBatch();
                        numberInsertions++;
                        break;
                    } catch (BatchUpdateException e) {
                        if(attempts > 2){
                            System.out.println("\u001B[31mFailed to add batch within allowed attempt count. Skipping batch\u001B[0m");
                            e.printStackTrace();
                            break;
                        }
                        attempts++;
                        System.out.println("\u001B[31mError executing batch insert to database. Attempts remaining: " + (3-attempts) + "\u001B[0m");
                        e.printStackTrace();
                    }
                }
            }

            try{
                preparedStatement.executeBatch();
            } catch (BatchUpdateException e) {
                System.out.println("\u001B[31mError executing batch insert to database\u001B[0m");
                e.printStackTrace();
            }

            System.out.println("\u001B[32m" + numberInsertions + " rows added!\u001B[0m");
    }

    public int getNumberInsertions(){
        return numberInsertions;
    }
}
