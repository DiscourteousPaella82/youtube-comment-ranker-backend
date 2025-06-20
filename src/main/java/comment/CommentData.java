package comment;

import com.google.api.client.util.DateTime;

/**
 * <a href="https://developers.google.com/youtube/v3/docs/comments">...</a>
 *
 * @param authorDisplayName The display name of the user who posted the comment.
 * @param authorProfileImageURL The URL for the avatar of the user who posted the comment.
 * @param authorChannelUrl Gets channel url.
 * @param textOriginal The comment's text. The text can be retrieved in either plain text or HTML. (The comments.list and commentThreads.list methods both support a textFormat parameter, which specifies the chosen text format.) Even the plain text may differ from the original comment text. For example, it may replace video links with video titles.
 * @param likeRating Count of likes the comment has.
 * @param videoId The id of the video,
 * @param parentId The id of the top level comment.
 * @param publishedAt [datetime] The date and time when the comment was originally published. The value is specified in ISO 8601 format.
 */

public record CommentData(String authorDisplayName, String authorProfileImageURL, String authorChannelUrl, String textOriginal, String videoId, String parentId ,Long likeRating, DateTime publishedAt) {
}
