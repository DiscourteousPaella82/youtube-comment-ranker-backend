package org.myapps.youtube.commentranker;

import com.google.api.client.util.DateTime;

/**
 * Parsed Video object which contains only relevant fields
 * @param id YouTube's video ID
 * @param publishedAt publish date used for checking if the video is new as of the day of fetching
 * @param defaultAudioLanguage
 * @param commentCount Number of comments on the video. Use to check if the video has comments disabled
 */
public record Video(String id, DateTime publishedAt, String defaultAudioLanguage, java.math.BigInteger commentCount) {
}
