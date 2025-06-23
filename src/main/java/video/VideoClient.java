package video;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            .setMaxResults(3L)
            .setRegionCode("US")
            .setFields("items.id,"
                +"items.snippet.publishedAt,"
                +"items.snippet.defaultAudioLanguage,"
                +"items.statistics.commentCount")
            .execute();

        List<Video> videoList = getVideos(response);

        System.out.println(response);

        return videoList;
    }

    private static List<Video> getVideos(VideoListResponse response) {
        List<Video> videoList = new ArrayList<>();

        for(int i = 0; i < response.getItems().size(); i++) {
            VideoSnippet videoSnippet = response.getItems().get(i).getSnippet();
            VideoStatistics videoStatistics = response.getItems().get(i).getStatistics();
            if ((Objects.equals(videoSnippet.getPublishedAt().toStringRfc3339().substring(0,10)
                , LocalDate.now().toString()) || (true)) && (videoStatistics.getCommentCount() != null)){

                    Video video = new Video(response.getItems().get(i).getId(),
                        videoSnippet.getPublishedAt(),
                        videoSnippet.getDefaultLanguage(),
                        videoStatistics.getCommentCount());

                    videoList.add(video);
            }
        }
        return videoList;
    }
}