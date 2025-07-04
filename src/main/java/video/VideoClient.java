package video;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class VideoClient {
    private static final String DEVELOPER_KEY = System.getenv("YOUTUBEDATAV3APIKEY");
    private final YouTube youtube;
    private String nextPageToken;
    private int requestCount;
    
    public VideoClient(YouTube youtube){
        this.youtube = youtube;
        nextPageToken = null;
    }

    public List<Video> getMostPopularVideos() throws IOException{
        requestCount = 0;

        List<String> part = new ArrayList<>();  
        part.add("snippet");
        part.add("statistics");
        
        VideoListResponse response;
        
        int attempts = 0;
        System.out.println("\u001B[36m" + new Date() + "Attempting to get video list with nextPageToken: " + nextPageToken + "\u001B[0m");
        while(true){
            try{
                requestCount++;
                response = youtube.videos()
                    .list(part)
                    .setKey(DEVELOPER_KEY)
                    .setChart("mostPopular")
                    .setMaxResults(3L)
                    .setPageToken(nextPageToken)
                    .setRegionCode("US")
                    .setFields("items.id,"
                        +"items.snippet.publishedAt,"
                        +"items.snippet.defaultAudioLanguage,"
                        +"items.statistics.commentCount,"
                        +"nextPageToken")
                    .execute();
                
                break;
            } catch (IOException e) {
                if(attempts > 2){
                    System.out.println("\u001B[31mCritical Error executing trying to fetch videos. Exiting program\n");
                    e.printStackTrace();
                    System.exit(1);
                }
                attempts++;
                e.printStackTrace();
                System.out.println("\u001B[31mError executing trying to fetch videos.\nAttempts remaining: " + (3-attempts) + "\u001B[0m");
            }
        }
        nextPageToken = response.getNextPageToken();
        List<Video> videoList = getVideos(response);

        System.out.println(response);

        return videoList;
    }

    private static List<Video> getVideos(VideoListResponse response) {
        List<Video> videoList = new ArrayList<Video>();

        int size = response.getItems().size();
        for(int i = 0; i < size; i++) {
            try{
                VideoSnippet videoSnippet = response.getItems().get(i).getSnippet();
                VideoStatistics videoStatistics = response.getItems().get(i).getStatistics();
                if ((Objects.equals(videoSnippet.getPublishedAt().toStringRfc3339().substring(0,10)
                    , LocalDate.now().toString()) || (true)) && (videoStatistics.getCommentCount() != null)){   //remove (true) when ready

                        Video video = new Video(response.getItems().get(i).getId(),
                            videoSnippet.getPublishedAt(),
                            videoSnippet.getDefaultLanguage(),
                            videoStatistics.getCommentCount());

                        videoList.add(video);
            }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("\u001B[31mError adding video to list\u001B[0m");
            }
        }
        return videoList;
    }

    public int getRequestCount(){
        return requestCount;
    }

    public String getNextPageToken(){
        return nextPageToken;
    }
}