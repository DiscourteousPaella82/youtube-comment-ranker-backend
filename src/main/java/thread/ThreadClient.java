package thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.api.services.youtube.YouTube;

import comment.CommentClient;
import comment.CommentThreadData;
import video.Video;

public class ThreadClient {

    private int commentCount;
    private int commentThreadCount;
    private int commentRequestCount;

    public List<CommentThreadData> requestCommentThreadData(YouTube youTube,List<Video> videoList){
        commentCount = 0;
        commentRequestCount = 0;

        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(100);
        
        List<Future<List<CommentThreadData>>> futureList;
        List<Callable<List<CommentThreadData>>> callables = new ArrayList<>();

        List<CommentThreadData> commentThreadDataList = new ArrayList<>();

        try{
            for(Video video:videoList){
                callables.add(new CommentClient(youTube, video.id()));
            }

            futureList = executor.invokeAll(callables);
            commentRequestCount += callables.size();

            for(Future<List<CommentThreadData>> future : futureList){
                try {
                    commentThreadDataList.addAll(future.get());
                    System.out.println("\u001B[33m" + new Date() + ":: Comment thread added to comment list\u001B[0m");
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    System.out.println("\u001B[31m " + new Date() + ":: Thread " + Thread.currentThread().getId() 
                        + ":Error adding comment thread to list\u001B[0m");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        executor.shutdown();

        commentThreadCount = commentThreadDataList.size();

        commentCount += commentThreadCount;
        for(CommentThreadData commentThreadData: commentThreadDataList){
            commentCount += commentThreadData.commentReplies().size();
        }

        System.out.println("\u001B[34mNumber of comment requests made: " + commentRequestCount 
            + "\nNumber of commentThreads received: " + commentThreadCount + "\nNumber of comments received: " 
            + commentCount + "\nTime taken: " + timeElapsed + "ms \u001B[0m");

        return commentThreadDataList;
    }
    public int getCommentCount(){
        return commentCount;
    }
    
    public int getCommentThreadCount(){
        return commentThreadCount;
    }    
    
    public int getCommentRequestCount(){
        return commentRequestCount;
    }
}