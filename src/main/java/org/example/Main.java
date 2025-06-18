package org.example;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import comment.CommentClient;
import comment.CommentThreadData;

public class Main {
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        CommentClient commentClient = new CommentClient();
        List<CommentThreadData> commentThreadData = commentClient.findCommentThreadByVideoId();

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
    }
}