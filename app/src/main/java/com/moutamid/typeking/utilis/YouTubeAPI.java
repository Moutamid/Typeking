package com.moutamid.typeking.utilis;

import android.os.StrictMode;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class YouTubeAPI {
    private static final String APPLICATION_NAME = "Typeking";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static final List<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/youtube.force-ssl");
    GoogleTokenResponse tokenResponse = new GoogleTokenResponse();
    private GoogleSignInAccount account;
    private String apiKey;

    public YouTubeAPI(GoogleSignInAccount account, String apiKey) {
        this.account = account;
        this.apiKey = apiKey;
    }

    public boolean hasLikedVideo(String videoId) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Build the YouTube client
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets("890062472575-0arbap2mpcqmfnn1hdq61l7pngeshg8f.apps.googleusercontent.com",
                        "AIzaSyBcgg_QpZ2O60W4UhMd28XbBclcQb8oCR4")
                .build()
                .setFromTokenResponse(tokenResponse);

        //GoogleCredential credential = GoogleCredential.fromTokenResponse(HTTP_TRANSPORT, JSON_FACTORY, account.getGoogleSignInAccount().getServerAuthCode());
        YouTube youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Execute the API request to check if the video has been liked by the authenticated user
        YouTube.Videos.List request = null;
        VideoListResponse response = null;
        try {
            request = youtube.videos().list(videoId)
                    .setMyRating("like")
                    .setId(videoId)
                    .setKey(apiKey);

        } catch (IOException e) {
            Log.d("Checking124", "That : " + e.getMessage());
        }

        try {
            if (request != null) {
                response = request.execute();
            } else {
                Log.d("Checking124", "e.getMessage()");
            }
        } catch (IOException e) {
            Log.d("Checking124", "This : " + e.getMessage());
        }


        // Check if the video is in the response
        return response.getItems().size()>0;
    }

}
