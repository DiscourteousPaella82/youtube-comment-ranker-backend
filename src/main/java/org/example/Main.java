package org.example;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import comment.CommentClient;
import comment.CommentThreadData;
import database.DatabaseFunctions;
import video.Video;
import video.VideoClient;
import youtube.YoutubeClient;

import com.google.api.services.youtube.YouTube;


public class Main {
    public static void main(String[] args) throws GeneralSecurityException, IOException {

        YouTube youTube = YoutubeClient.getService();

        int numberComments = 0;
        int requestCount = 0;

        try {    
            DatabaseFunctions databaseFunctions = new DatabaseFunctions();

            Connection connection = databaseFunctions.connection_to_db("5431", "youtubeComments", "postgres", "postgres");
            databaseFunctions.createTable(connection);
            
            VideoClient videoClient = new VideoClient(youTube);

            requestCount ++;
            List<Video> videoList = videoClient.getMostPopularVideos();



            CommentClient commentClient = new CommentClient(youTube);

            List<CommentThreadData> commentThreadData = new ArrayList<>();

            for(int i = 0; i < videoList.size(); i++) {

                requestCount ++;
                commentThreadData.addAll(commentClient.findCommentThreadByVideoId(
                    videoList.get(i).id()));
                
                for (int j = 0; j < commentThreadData.size(); j++) {
                    System.out.println(commentThreadData.get(j));
                }

                for (CommentThreadData commentThreadData1 : commentThreadData) {
                    numberComments++;
                    if (commentThreadData1.commentReplies() != null)
                        numberComments += commentThreadData1.commentReplies().size();
                }

            }
            System.out.println("\n"+commentThreadData.size() + " comment threads captured.\n"
                + numberComments + " total comments captured." + "\n" + requestCount + " requests made.");

            databaseFunctions.insertIntoCommentTable(connection, commentThreadData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        

    }
}