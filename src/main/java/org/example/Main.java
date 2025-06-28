package org.example;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import comment.CommentThreadData;
import database.DatabaseFunctions;
import thread.ThreadClient;
import video.Video;
import video.VideoClient;
import youtube.YoutubeClient;

import com.google.api.services.youtube.YouTube;

public class Main{
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        YouTube youTube = YoutubeClient.getService();

        DatabaseFunctions databaseFunctions = new DatabaseFunctions();

        String PORT = System.getenv("DATABASE_PORT");
        
        String DBNAME = System.getenv("DATABASE_NAME");

        String DBUSER = System.getenv("DATABASE_USER");
        
        String DBPASSWORD = System.getenv("DATABASE_PASSWORD");

        Connection connection = databaseFunctions.connection_to_db(PORT, DBNAME, DBUSER, DBPASSWORD);
        databaseFunctions.createTable(connection);

        VideoClient videoClient = new VideoClient(youTube);
        List<Video> videoList = new ArrayList<Video>();

        int totalRequestCount = 0;
        int totalCommentCount = 0;
        int totalRowsInserted = 0;

        while(totalRequestCount < 50){
            while(true){
                try{
                    videoList = videoClient.getMostPopularVideos();
                    totalRequestCount = videoClient.getRequestCount();
                    break;
                } catch (IOException e){
                    System.out.println("\u001B[31mVideo list failed to fetch\u001B[0m");
                    System.exit(1); //Change this to a better handler when video list pagination is added
                }
            }

            if(videoList.size() == 0){
                System.out.println("\u001B[31mVideo list is empty. Aborting program\u001B[0m");
                try{
                    connection.close();
                } catch (SQLException e){
                    e.printStackTrace();
                }
                System.exit(1);
            }

            ThreadClient threadClient = new ThreadClient();
            List<CommentThreadData> commentThreadData = threadClient.requestCommentThreadData(youTube, videoList);
            totalRequestCount = threadClient.getCommentRequestCount();
            totalCommentCount = threadClient.getCommentCount();

            try {
                databaseFunctions.insertIntoCommentTable(connection, commentThreadData);

                break;
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("\u001B[31mError closing connection\u001B[0m");
                System.exit(1);
            }
        }

        System.out.println("\u001B[32m" + new Date() +" Finished fetching comments:\nTotal number of requests made: " + totalRequestCount + "\nTotal number of comments returned: " 
        + totalCommentCount + "\nTotal number of rows inserted: " + totalRowsInserted + "\nNumber of lost comments: " + (totalCommentCount - totalRowsInserted));
        
        try{
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("\u001B[31mError closing connection\u001B[0m");
            System.exit(1);
        }
        System.exit(0);
    }
}