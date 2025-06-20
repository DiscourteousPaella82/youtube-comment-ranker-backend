package org.example;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import comment.CommentClient;
import comment.CommentThreadData;

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
                    System.out.println(commentThreadData.get(i));
                }

                for (CommentThreadData commentThreadData1 : commentThreadData) {
                    numberComments++;
                    if (commentThreadData1.commentReplies() != null)
                        numberComments += commentThreadData1.commentReplies().size();
                }

            }
            System.out.println("\n"+commentThreadData.size() + " comment threads captured.\n"
                + numberComments + " total comments captured." + "\n" + requestCount + " requests made.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}