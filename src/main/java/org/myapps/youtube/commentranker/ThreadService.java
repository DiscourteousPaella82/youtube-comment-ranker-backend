package org.myapps.youtube.commentranker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.google.api.services.youtube.YouTube;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles threads & futures for fetching comment lists functionality.
 */
public class ThreadService {
    private static Logger logger = LoggerFactory.getLogger(ThreadService.class);

    /**
     * The number of comments returned from all threads
     */
    private int commentCount;
    /**
     * The number of comment threads returned from all threads
     */
    private int commentThreadCount;
    /**
     * The number of requests made to the API
     */
    private int commentRequestCount;

    /**
     * Creates multiple threads to concurrently make requests and store CommentThreadData. Returns a list of CommentThreadData aggregated from all threads
     * @param youTube YouTube object
     * @param videoList List of Videos
     * @return List of CommentThreadData
     */
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
                callables.add(new CommentService(youTube, video.id()));
            }

            futureList = executor.invokeAll(callables);
            commentRequestCount += callables.size();

            for(Future<List<CommentThreadData>> future : futureList){
                try {
                    commentThreadDataList.addAll(future.get());
                    logger.debug("Comment thread added to CommentThreadData list");
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    logger.error("Thread " + Thread.currentThread().getId() 
                        + ":Error adding comment thread to list\u001B[0m");
                }
            }
        } catch (InterruptedException e) {
            logger.error("Thread error caught");
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

        logger.info("\u001B[34mNumber of comment requests made: " + commentRequestCount 
            + "\nNumber of commentThreads received: " + commentThreadCount + "\nNumber of comments received: " 
            + commentCount + "\nTime taken: " + timeElapsed + "ms \u001B[0m");

        return commentThreadDataList;
    }

    /**
     * @return Count of comments fetched across all threads
     */
    public int getCommentCount(){
        return commentCount;
    }

    /**
     * @return Count of comment threads fetched across all threads
     */
    public int getCommentThreadCount(){
        return commentThreadCount;
    }

    /**
     * @return Count of requests made across all threads
     */
    public int getCommentRequestCount(){
        return commentRequestCount;
    }
}