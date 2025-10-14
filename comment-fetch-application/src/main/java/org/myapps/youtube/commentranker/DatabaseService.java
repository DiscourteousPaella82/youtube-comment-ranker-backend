package org.myapps.youtube.commentranker;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides functionality for interacting with a local PostGreSQL database
 */
public class DatabaseService {
    private static Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    /**
     * Row insertion count
     */
    private int numberInsertions;

    /**
     * Creates a connection to the database
     * @param port Database host port
     * @param dbname Database name
     * @param username Database username
     * @param password Database password
     * @return Connected Connection object
     */
    public Connection connectionToDb(String port, String dbname, String username, String password){
        Connection connection;

        try{
            Class.forName("org.postgresql.Driver");
            logger.debug("Attempting to create connection with database");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:" + port + "/" + dbname,username,password);
            if(connection!= null){
                logger.info("Connection established");
            }
            else{
                logger.error("Database error:Failed to connect to database");
                System.exit(1);
            }
        } catch (Exception e) {
            logger.error("Error thrown attempting to create database connection");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return connection;
    }

    /**
     * Creates comment table if it doesn't exist
     * @param connection
     */
    public void createCommentTable(Connection connection){
        Statement statement;
        String query="CREATE TABLE IF NOT EXISTS comments(id SERIAL,"
            + "authorDisplayName VARCHAR(100),authorProfileImageURL VARCHAR(150),authorChannelUrl VARCHAR(200),"
            + "textOriginal VARCHAR(2500),videoId VARCHAR(12), parentId VARCHAR(50),likeRating INTEGER, publishedDate DATE DEFAULT CURRENT_DATE, PRIMARY KEY(id));";
        try {            
            statement = connection.createStatement();
            statement.executeUpdate(query);
            logger.info("Table comments created or already exists");
        } catch (Exception e) {
            logger.error("Database error: failed to create new table");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Inserts CommentData into the comment table in batches of 100.
     * @param connection
     * @param commentThreadData List of CommentThreadData
     * @throws SQLException
     */
    public void insertIntoCommentTable(Connection connection, List<CommentThreadData> commentThreadData) throws SQLException {
        PreparedStatement preparedStatement = null;

        numberInsertions = 0;
        String query = ("INSERT INTO comments("
            + " authorDisplayName, authorProfileImageURL, authorChannelUrl,"
            + " textOriginal, videoId, parentId, likeRating)"
            + "VALUES (?,?,?,?,?,?,?);");

        preparedStatement = connection.prepareStatement(query);

        int size = commentThreadData.size();
        for(int i = 0; i < size; i++){
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
                        logger.warn("Failed to add batch within allowed attempt count. Skipping batch");
                        e.printStackTrace();
                        break;
                    }
                    attempts++;
                    logger.warn("Error executing batch insert to database. Attempts remaining: " + (3-attempts));
                    e.printStackTrace();
                }
            }
        }

        try{
            preparedStatement.executeBatch();
        } catch (BatchUpdateException e) {
            logger.error("Error executing batch insert to database");
            e.printStackTrace();
        }

        logger.debug(numberInsertions + " rows added!");
    }

    /**
     * @return number of row insertions
     */
    public int getNumberInsertions(){
        return numberInsertions;
    }
}
