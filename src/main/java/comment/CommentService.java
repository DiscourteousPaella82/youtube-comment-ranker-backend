package comment;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.Date;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThreadListResponse;

/**
 * Contains functionality for fetching lists of CommentThreads
 */
public class CommentService implements Callable<List<CommentThreadData>>{

    /**
     * Google API key
     */
    private static final String DEVELOPER_KEY = System.getenv("YOUTUBEDATAV3APIKEY");
    /**
     * Video ID which comments are being fetched from
     */
    private String videoId;
    /**
     * YouTube object provides access to YouTube
     */
    private final YouTube youTube;

    public CommentService(YouTube youTube, String videoId){
        this.youTube = youTube;
        this.videoId = videoId;
    }

    /**
     * Callable method for threads
     * @return List of CommentThreadData
     * @throws IOException
     */
    public List<CommentThreadData> call() throws IOException{
        List<CommentThreadData> commentThreadDataList = null;
        commentThreadDataList = findCommentThreadByVideoId();
        return commentThreadDataList;
    }

    /**
     * Gets a hardcoded amount of maximum amount comments(100 max) from video with videoId.
     * @return List of CommentThreadData
     */
    private List<CommentThreadData> findCommentThreadByVideoId() {
        System.out.println("\u001B[36m" + new Date() + ":: Thread " + Thread.currentThread().getId() 
        + ": Attempting to get comment thread data for video with id: " + videoId + "\u001B[0m");

        List<String> part = new ArrayList<>();
        part.add("snippet");
        part.add("replies");

        List<CommentThreadData> commentThreadList = new ArrayList<>();

        try{
            YouTube.CommentThreads.List request = youTube.commentThreads()
                .list(part);

            CommentThreadListResponse response = request.setKey(DEVELOPER_KEY)
                .setMaxResults(100L)
                .setOrder("relevance")
                .setVideoId(videoId)
                .setFields("items.snippet.videoId,"
                    + "items.snippet.topLevelComment.snippet.likeCount,"
                    + "items.snippet.topLevelComment.snippet.publishedAt,"
                    + "items.snippet.topLevelComment.snippet.authorDisplayName,"
                    + "items.snippet.topLevelComment.snippet.textDisplay,"
                    + "items.snippet.totalReplyCount,"
                    + "items.snippet.topLevelComment.snippet.authorProfileImageUrl,"
                    + "items.snippet.topLevelComment.snippet.authorChannelUrl,"
                    + "items.replies.comments.snippet.textDisplay,"
                    + "items.replies.comments.snippet.authorDisplayName,"
                    + "items.replies.comments.snippet.authorProfileImageUrl,"
                    + "items.replies.comments.snippet.authorProfileImageUrl,"
                    + "items.replies.comments.snippet.authorChannelUrl,"
                    + "items.replies.comments.snippet.parentId,"
                    + "items.replies.comments.snippet.likeCount,"
                    + "items.replies.comments.snippet.publishedAt")
                .execute();

            int size = response.getItems().size();
            for (int i = 0; i < size; i++) {
                CommentData topLevelComment = getTopLevelCommentData(response, i);

                List<CommentData> repliesList = new ArrayList<>(); 
                
                try{
                    if(response.getItems().get(i).getSnippet().getTotalReplyCount() != 0L)
                        repliesList = getCommentRepliesData(response, i);
                } catch (Exception e){
                    System.out.println("\u001B[31m " + new Date() + ":: Thread " + Thread.currentThread().getId() 
                    + ":Error reading replies\u001B[0m");
                }

                commentThreadList.add(new CommentThreadData(topLevelComment, repliesList));

            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("\u001B[31m " + new Date() + ":: Thread " + Thread.currentThread().getId() 
                + ":Error adding comment to list\u001B[0m");
        }
        return commentThreadList;
    }

    /**
     * Assigns top level comment data
     * @param response Request response object
     * @param index index of item in the response
     * @return CommentData for the topLevelComment
     */
    private CommentData getTopLevelCommentData(CommentThreadListResponse response, int index) {
        try{
            CommentSnippet topLevelCommentSnippet = response.getItems().get(index).getSnippet().getTopLevelComment().getSnippet();
            return new CommentData(
                (topLevelCommentSnippet.getAuthorDisplayName().isEmpty()) ? null : topLevelCommentSnippet.getAuthorDisplayName(),
                topLevelCommentSnippet.getAuthorProfileImageUrl(),
                topLevelCommentSnippet.getAuthorChannelUrl(),
                (topLevelCommentSnippet.getTextDisplay().length() < 2499) ? topLevelCommentSnippet.getTextDisplay() : 
                    topLevelCommentSnippet.getTextDisplay().substring(0, 2499),
                videoId,
                null,
                topLevelCommentSnippet.getLikeCount(),
                topLevelCommentSnippet.getPublishedAt()
            );
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\u001B[31m " + new Date() + ":: Thread " + Thread.currentThread().getId() 
                + ":Error adding top level comment to list\u001B[0m");
        }
        return null;
    }

    /**
     * Assigns reply list
     * @param response Request response object
     * @param index index of item in the response
     * @return List of CommentData
     */
    private List<CommentData> getCommentRepliesData(CommentThreadListResponse response, int index) {
        List<Comment> replies = response.getItems().get(index).getReplies().getComments();
        List<CommentData> repliesList = new ArrayList<>();
        
        for (Comment reply : replies) {
            try{
            CommentSnippet replySnippet = reply.getSnippet();
            repliesList.add(
                new CommentData(
                    (replySnippet.getAuthorDisplayName().isEmpty()) ? null : replySnippet.getAuthorDisplayName(),
                    replySnippet.getAuthorProfileImageUrl(), 
                    replySnippet.getAuthorChannelUrl(),
                    (replySnippet.getTextDisplay().length() < 2499) ? replySnippet.getTextDisplay() : 
                        replySnippet.getTextDisplay().substring(0, 2499),
                    videoId,
                    replySnippet.getParentId(), 
                    replySnippet.getLikeCount(),
                    replySnippet.getPublishedAt()));
        
            } catch (Exception e){
            e.printStackTrace();
            System.out.println("\u001B[31m " + new Date() + ":: Thread " + Thread.currentThread().getId() 
                + ":Error adding comment reply to list\u001B[0m");
            }
        }
        
        return (!replies.isEmpty()) ? repliesList : Collections.emptyList();
    }
}
