package video;

import com.google.api.client.util.DateTime;

public record Video(String id, DateTime publishedAt, String defaultAudioLanguage, java.math.BigInteger commentCount) {
}
