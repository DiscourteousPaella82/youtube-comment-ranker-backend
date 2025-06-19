package video;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoClient {
    private static final String DEVELOPER_KEY = System.getenv("YOUTUBEDATAV3APIKEY");
    private YouTube youtube;
    
    public VideoClient(YouTube youtube){
        this.youtube = youtube;
    }

    public List<Video> getMostPopularVideos()
        throws IOException{
        YouTube youtubeService = youtube;

        List<String> part = new ArrayList<>();
        part.add("snippet");
        part.add("statistics");

        YouTube.Videos.List request = youtubeService.videos()
            .list(part);
        VideoListResponse response = request.setKey(DEVELOPER_KEY)
            .setChart("mostPopular")
            .setMaxResults(10L)
            .setRegionCode("US")
            .setFields("items.id,items.snippet.publishedAt,items.snippet.defaultAudioLanguage,items.statistics.commentCount")
            .execute();
        System.out.println(response);

        return null;
    }
}