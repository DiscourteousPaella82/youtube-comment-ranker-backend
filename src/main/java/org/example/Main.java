package org.example;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import comment.CommentClient;
import comment.CommentThreadData;

import video.VideoClient;
import youtube.YoutubeClient;

import com.google.api.services.youtube.YouTube;


public class Main {
    public static void main(String[] args) throws GeneralSecurityException, IOException {

        YouTube youTube = YoutubeClient.getService();

        try {
            VideoClient videoClient = new VideoClient(youTube);

            videoClient.getMostPopularVideos();

            System.exit(0);

            CommentClient commentClient = new CommentClient(youTube);
            List<CommentThreadData> commentThreadData = commentClient.findCommentThreadByVideoId("PziYflu8cB8");

            for(int i = 0; i < commentThreadData.size(); i++){
                System.out.println(commentThreadData.get(i));
            }

            int numberComments = 0;
            for (CommentThreadData commentThreadData1 : commentThreadData) {
                numberComments++;
                if(commentThreadData1.commentReplies() != null)
                    numberComments += commentThreadData1.commentReplies().size();
            }

            System.out.println(commentThreadData.size() + " comment threads captured.\n" + numberComments + " total comments captured.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}