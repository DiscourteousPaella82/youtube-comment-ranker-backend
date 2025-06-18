package comment;

import java.io.IOException;
import java.util.List;


import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThreadListResponse;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;

public class CommentClient {

    private static final String DEVELOPER_KEY = System.getenv("APIKEY");
    private static final String APPLICATION_NAME = "COMMENT FETCH APP";
    private static final GsonFactory GSON_FACTORY 
        = GsonFactory.getDefaultInstance();

    public static YouTube getService() throws GeneralSecurityException, IOException {

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        return new YouTube.Builder(httpTransport, GSON_FACTORY, null)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    public List<CommentThreadData> findCommentThreadByVideoId()
        throws GeneralSecurityException, IOException {
        YouTube youtubeService = getService();

        List<String> part = new ArrayList<>();
        part.add("snippet");
        part.add("replies");

        YouTube.CommentThreads.List request = youtubeService.commentThreads()
            .list(part);

        CommentThreadListResponse response = request.setKey(DEVELOPER_KEY)
            .setMaxResults(100L)
            .setOrder("relevance")
            .setVideoId("PziYflu8cB8")
            .execute();
        
        List<CommentThreadData> commentThreadList = new ArrayList<>();

        for (int i = 0; i < response.getItems().size(); i++) {
            CommentData topLevelComment = getTopLevelCommentData(response, i);

            List<CommentData> repliesList = null;
            if(response.getItems().get(i).getSnippet().getTotalReplyCount() != 0)
                repliesList = getCommentData(response, i);

            commentThreadList.add(new CommentThreadData(topLevelComment, repliesList));
        }

        return commentThreadList;
    }

    private static CommentData getTopLevelCommentData(CommentThreadListResponse response, int i) {
        try{
            CommentSnippet topLevelCommentSnippet = response.getItems().get(i).getSnippet().getTopLevelComment().getSnippet();
            return new CommentData(
                topLevelCommentSnippet.getAuthorDisplayName(),
                topLevelCommentSnippet.getAuthorProfileImageUrl(),
                topLevelCommentSnippet.getAuthorChannelUrl(),
                topLevelCommentSnippet.getTextDisplay(),
                topLevelCommentSnippet.getVideoId(),
                null,
                topLevelCommentSnippet.getLikeCount(),
                topLevelCommentSnippet.getPublishedAt()
            );
        } catch (Exception e) {
            System.out.println("Exception thrown while getting top level comment: " + e);
        }
        return null;
    }

    private static List<CommentData> getCommentData(CommentThreadListResponse response, int i) {
        try{
            List<com.google.api.services.youtube.model.Comment> replies = response.getItems().get(i).getReplies().getComments();
            List<CommentData> repliesList = new ArrayList<>();
            for (com.google.api.services.youtube.model.Comment reply : replies) {
                CommentSnippet replySnippet = reply.getSnippet();
                repliesList.add(
                    new CommentData(replySnippet.getAuthorDisplayName(), replySnippet.getAuthorProfileImageUrl(), replySnippet.getAuthorChannelUrl(),
                        replySnippet.getTextDisplay(), replySnippet.getVideoId(), replySnippet.getParentId(), replySnippet.getLikeCount(),
                        replySnippet.getPublishedAt()));
            }
            return repliesList;
        } catch (Exception e){
            System.out.println("Exception thrown while getting replies: " + e);
        }
        return Collections.emptyList();
    }
}
