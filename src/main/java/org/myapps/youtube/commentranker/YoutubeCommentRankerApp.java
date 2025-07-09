package org.myapps.youtube.commentranker;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtube.YouTube;

/**
 * Main program method
 */
public class YoutubeCommentRankerApp{
    private static Logger logger = LoggerFactory.getLogger(YoutubeCommentRankerApp.class);

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        YouTube youTube = YoutubeService.getService();
        DatabaseService databaseFunctions = new DatabaseService();
        System.out.println("Hello");

        logger.debug("Debug log message");
        logger.info("Info log message");
        //logger.error("Error log message");
        
        String variable = "Hello John";
        logger.debug("Printing variable value {} ", variable);

        System.exit(1);

        String PORT = System.getenv("DATABASE_PORT");
        
        String DBNAME = System.getenv("DATABASE_NAME");

        String DBUSER = System.getenv("DATABASE_USER");
        
        String DBPASSWORD = System.getenv("DATABASE_PASSWORD");

        Connection connection = databaseFunctions.connectionToDb(PORT, DBNAME, DBUSER, DBPASSWORD); //creates connection to the database
        databaseFunctions.createCommentTable(connection);  //creates table if it doesn't exist already

        VideoService videoClient = new VideoService(youTube);
        List<Video> videoList;

        int totalRequestCount = 0;
        int totalCommentCount = 0;
        int totalRowsInserted = 0;
        int remainingQuota = 20;

        long start = System.currentTimeMillis();

        while(remainingQuota > 0){
            videoList = videoClient.getMostPopularVideos(); //gets List of Videos
            totalRequestCount += videoClient.getRequestCount();
            remainingQuota -= videoClient.getRequestCount();

            if(videoList.isEmpty()){    //if the returned List of Videos are empty, the program has critically failed to get anything and must exit
                logger.error("CRITICAL ERROR: Video list is empty. Aborting program");
                try{
                    connection.close();
                } catch (SQLException e){
                    logger.error("Error closing JDBC connection", e);
                }
                System.exit(1);
            }

            ThreadService threadClient = new ThreadService();
            List<CommentThreadData> commentThreadData = threadClient.requestCommentThreadData(youTube, videoList);  //Fetches CommentData via the ThreadClient
            totalRequestCount += threadClient.getCommentRequestCount();
            remainingQuota -= threadClient.getCommentRequestCount();
            totalCommentCount += threadClient.getCommentCount();

            try {
                databaseFunctions.insertIntoCommentTable(connection, commentThreadData);    //inserts CommentThreadData into the database
                totalRowsInserted += databaseFunctions.getNumberInsertions();
            } catch (SQLException e) {
                logger.error("Error running comment fetching threads", e);
                System.exit(1);
            }

            logger.debug("Requests made: {}\tRemaining Quota: {}", totalRequestCount, totalCommentCount);
        }


        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;

        logger.debug("\u001B[32m" + new Date() +" Finished fetching comments:\nTotal number of requests made: " + totalRequestCount + "\nTotal number of comments returned: " 
            + totalCommentCount + "\nTotal number of rows inserted: " + totalRowsInserted + "\nNumber of lost comments: " + (totalCommentCount - totalRowsInserted) + "\nTime taken: " 
            + timeElapsed + "ms\nFinal page token: " + videoClient.getNextPageToken() + "\u001B[0m");
            
        try{
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("\u001B[31mError closing connection\u001B[0m");
            System.exit(1);
        }
        System.exit(0);
    }
}