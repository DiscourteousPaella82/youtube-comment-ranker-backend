package comment;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.Date;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThreadListResponse;

public class CommentClient implements Callable<List<CommentThreadData>>{

    private static final String DEVELOPER_KEY = System.getenv("YOUTUBEDATAV3APIKEY");
    private String videoId;
    private final YouTube youTube;

    public CommentClient(YouTube youTube, String videoId){
        this.youTube = youTube;
        this.videoId = videoId;
    }

    private List<CommentThreadData> findCommentThreadByVideoId()
        throws IOException {
        System.out.println("\u001B[36m" + new Date() + ":: Thread " + Thread.currentThread().getId() 
        + ": Attempting to get comment thread data for video with id: " + videoId + "\u001B[0m");

        List<String> part = new ArrayList<>();
        part.add("snippet");
        part.add("replies");

        YouTube.CommentThreads.List request = youTube.commentThreads()
            .list(part);

        List<CommentThreadData> commentThreadList = new ArrayList<>();

        try{
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

    private List<CommentData> getCommentRepliesData(CommentThreadListResponse response, int index) {
        List<com.google.api.services.youtube.model.Comment> replies = response.getItems().get(index).getReplies().getComments();
        List<CommentData> repliesList = new ArrayList<>();
        
        for (com.google.api.services.youtube.model.Comment reply : replies) {
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

    public List<CommentThreadData> call() throws IOException{
        List<CommentThreadData> commentThreadDataList = null;
        try{
            commentThreadDataList = findCommentThreadByVideoId();
        } catch (IOException e){
            e.printStackTrace();
            System.out.println("\u001B[31m " + new Date() + " ::Thread " + Thread.currentThread().getId() 
                + ":IO Exception thrown\tVideo Id: " + videoId + "\u001B[0m");
        }
        return commentThreadDataList;
    }
}
