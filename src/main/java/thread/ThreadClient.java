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
        
        List<Future<List<CommentThreadData>>> futureList = new ArrayList<Future<List<CommentThreadData>>>();
        List<Callable<List<CommentThreadData>>> callables = new ArrayList<Callable<List<CommentThreadData>>>();

        List<CommentThreadData> commentThreadDataList = new ArrayList<CommentThreadData>();

        try{
            for(Video video:videoList){
                callables.add(new CommentClient(youTube, video.id()));
            }

            futureList = executor.invokeAll(callables);
            commentRequestCount += callables.size();

            for(Future<List<CommentThreadData>> future : futureList){
                try {
                    commentThreadDataList.addAll(future.get());
                    System.out.println("\u001B[33m" + new Date() + "::\u001B[0m"+commentThreadDataList);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
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
            + "\nNumber of commentThreads received: " + commentThreadCount + "\n Number of comments received: " 
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