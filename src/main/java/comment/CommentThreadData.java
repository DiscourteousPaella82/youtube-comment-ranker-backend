package comment;

import java.util.List;

/**
 * <a href="https://developers.google.com/youtube/v3/docs/commentThreads">...</a>
 * Parsed YouTube Comment Thread Data object. Contains the topLevelComment object & list of replies
 * @param topLevelComment top level comment
 * @param commentReplies list of comments replies
 */
public record CommentThreadData(CommentData topLevelComment, List<CommentData> commentReplies) {
}
