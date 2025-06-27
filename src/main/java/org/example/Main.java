package org.example;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
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

        try {    
            DatabaseFunctions databaseFunctions = new DatabaseFunctions();

            Connection connection = databaseFunctions.connection_to_db("5431", "youtubeComments", "postgres", "postgres");
            databaseFunctions.createTable(connection);
            
            VideoClient videoClient = new VideoClient(youTube);

            List<Video> videoList = videoClient.getMostPopularVideos();

            ThreadClient threadClient = new ThreadClient();
            List<CommentThreadData> commentThreadData = threadClient.requestCommentThreadData(youTube, videoList);

            databaseFunctions.insertIntoCommentTable(connection, commentThreadData);
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        

    }
}