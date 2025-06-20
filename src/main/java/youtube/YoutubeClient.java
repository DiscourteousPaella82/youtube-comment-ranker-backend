package youtube;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;

public final class YoutubeClient {
    private static final String APPLICATION_NAME = "COMMENT FETCH APP";
    private static final GsonFactory GSON_FACTORY = GsonFactory.getDefaultInstance();

    public static YouTube getService() throws GeneralSecurityException, IOException {

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        return new YouTube.Builder(httpTransport, GSON_FACTORY, null)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }
}
