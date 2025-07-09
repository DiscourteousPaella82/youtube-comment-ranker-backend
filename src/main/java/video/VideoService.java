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

/**
 * Has functionality for returning Lists of Videos
 */
public class VideoService {
    /**
     * Google API key
     */
    private static final String DEVELOPER_KEY = System.getenv("YOUTUBEDATAV3APIKEY");
    /**
     * YouTube object provides access to YouTube
     */
    private final YouTube youtube;
    /**
     * Pagination token
     */
    private String nextPageToken;
    /**
     * Count of requests made
     */
    private int requestCount;
    
    public VideoService(YouTube youtube){
        this.youtube = youtube;
        nextPageToken = null;
    }

    /**
     * Requests the 'most relevant' videos given the page token.
     * @return List of Videos
     * @throws IOException
     */
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

    /**
     * @return Count of requests made
     */
    public int getRequestCount(){
        return requestCount;
    }

    /**
     * @return Next page token
     */
    public String getNextPageToken(){
        return nextPageToken;
    }

    /**
     * Assignment function for Video List from response
     * @param response Video GET response
     * @return
     */
    private static List<Video> getVideos(VideoListResponse response) {
        List<Video> videoList = new ArrayList<>();

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
}