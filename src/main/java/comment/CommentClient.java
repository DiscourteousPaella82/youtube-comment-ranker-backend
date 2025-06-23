package comment;

import java.io.IOException;
import java.util.List;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThreadListResponse;

import java.util.ArrayList;
import java.util.Collections;

public class CommentClient {

    private static final String DEVELOPER_KEY = System.getenv("YOUTUBEDATAV3APIKEY");
    private String videoId;
    private YouTube youTube;

    public CommentClient(YouTube youTube){
        this.youTube = youTube;
    }

    public List<CommentThreadData> findCommentThreadByVideoId(String videoId)
        throws IOException {
        this.videoId = videoId;
        System.out.println("Attempting to get comment thread data for video with id: " + videoId);
        YouTube youTubeService = youTube;

        List<String> part = new ArrayList<>();
        part.add("snippet");
        part.add("replies");

        YouTube.CommentThreads.List request = youTubeService.commentThreads()
            .list(part);

        List<CommentThreadData> commentThreadList = new ArrayList<>();

        try{
            CommentThreadListResponse response = request.setKey(DEVELOPER_KEY)
                .setMaxResults(10L)
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


            for (int i = 0; i < response.getItems().size(); i++) {
                CommentData topLevelComment = getTopLevelCommentData(response, i);

                List<CommentData> repliesList = new ArrayList<>();
                if(response.getItems().get(i).getSnippet().getTotalReplyCount() != 0)
                    repliesList = getCommentData(response, i);

                commentThreadList.add(new CommentThreadData(topLevelComment, repliesList));

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return commentThreadList;
    }

    private CommentData getTopLevelCommentData(CommentThreadListResponse response, int i) {
        try{
            CommentSnippet topLevelCommentSnippet = response.getItems().get(i).getSnippet().getTopLevelComment().getSnippet();
            return new CommentData(
                topLevelCommentSnippet.getAuthorDisplayName(),
                topLevelCommentSnippet.getAuthorProfileImageUrl(),
                topLevelCommentSnippet.getAuthorChannelUrl(),
                topLevelCommentSnippet.getTextDisplay(),
                videoId,
                null,
                topLevelCommentSnippet.getLikeCount(),
                topLevelCommentSnippet.getPublishedAt()
            );
        } catch (Exception e) {
            System.out.println("Exception thrown while getting top level comment: " + e);
        }
        return null;
    }

    private List<CommentData> getCommentData(CommentThreadListResponse response, int i) {
        try{
            List<com.google.api.services.youtube.model.Comment> replies = response.getItems().get(i).getReplies().getComments();
            List<CommentData> repliesList = new ArrayList<>();
            for (com.google.api.services.youtube.model.Comment reply : replies) {
                CommentSnippet replySnippet = reply.getSnippet();
                repliesList.add(
                    new CommentData(replySnippet.getAuthorDisplayName(), 
                        replySnippet.getAuthorProfileImageUrl(), 
                        replySnippet.getAuthorChannelUrl(),
                        replySnippet.getTextDisplay(),
                        videoId,
                        replySnippet.getParentId(), 
                        replySnippet.getLikeCount(),
                        replySnippet.getPublishedAt()));
            }
            return repliesList;
        } catch (Exception e){
            System.out.println("Exception thrown while getting replies: " + e);
        }
        return Collections.emptyList();
    }
}
