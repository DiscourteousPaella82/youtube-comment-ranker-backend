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
            + "authorDisplayName VARCHAR(100),authorProfileImageURL VARCHAR(150),authorChannelUrl VARCHAR(150),"
            + "textOriginal VARCHAR(2500),videoId VARCHAR(12), parentId VARCHAR(50),likeRating INTEGER, PRIMARY KEY(id));";
        try {            
            statement = connection.createStatement();
            statement.executeUpdate(query);
            System.out.println("Table comments" + localDateParsed + " created!");
        } catch (Exception e) {
            System.out.println("ERROR: failed to create new table");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void insertIntoCommentTable(Connection connection, List<CommentThreadData> commentThreadData) throws SQLException {
        PreparedStatement preparedStatement = null;

        int numberInsertions = 0;
        String query = ("INSERT INTO comments" + localDateParsed +"("
            + " authorDisplayName, authorProfileImageURL, authorChannelUrl,"
            + " textOriginal, videoId, parentId, likeRating)"
            + "VALUES (?,?,?,?,?,?,?);");

        preparedStatement = connection.prepareStatement(query);

            for(int i = 0; i < commentThreadData.size(); i++){
                try {   
                numberInsertions++;
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

                } catch (BatchUpdateException e) {
                    System.out.println("\u001B[31mError executing batch insert to database\u001B[0m");
                    e.printStackTrace();
                }
            }

            try{
                preparedStatement.executeBatch();
            } catch (BatchUpdateException e) {
                System.out.println("\u001B[31mError executing batch insert to database\u001B[0m");
                e.printStackTrace();
            }

            System.out.println(numberInsertions + " rows added!");
    }
}
