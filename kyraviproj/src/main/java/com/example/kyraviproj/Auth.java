package com.example.kyraviproj;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
// import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
// import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;
// import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
// import se.michaelthelin.spotify.requests.data.personalization.GetUsersTopArtistsAndTracksRequest;
// import se.michaelthelin.spotify.requests.data.personalization.interfaces.IArtistTrackModelObject;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
// import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
// import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

public class Auth {
    private static final String clientId = "0b61847e2765488190ebd3bf56476ecd";
    private static final String clientSecret = "79fe6f79891248e7a4d467e655288fe5";
    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8080/index.html");
    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .setRedirectUri(redirectUri)
        .build(); //gives the spotifyApi object of type SpotifyApi attributes
    public static final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
        .scope("user-top-read")
        .build(); //adds the scope value now, and uses the attributes of the spotifyApi object to make a request
    public static void authorizationCodeUri_Sync() {
        final URI uri = authorizationCodeUriRequest.execute(); //after using the method, the request will execute returning the auth URL
        System.out.println("\nPlease click this link to authenticate: " + uri.toString()); //prints out so user clicks
        startLocalServer();   
    }
    private static String code = "";
     public static void startLocalServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            final String[] authCode = new String[1]; // Store the authorization code
            server.createContext("/index.html", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    // Parse the query parameters
                    String query = exchange.getRequestURI().getQuery();
                    Map<String, String> params = parseQuery(query);
                    // Retrieve the authorization code
                    if (params.containsKey("code")) {
                        authCode[0] = params.get("code");
                        String response = "Authorization successful! You can close this tab.";
                        exchange.sendResponseHeaders(200, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                        server.stop(1); // Stop the server after handling the request
                    } else {
                        String response = "Authorization failed!";
                        exchange.sendResponseHeaders(400, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                }
            });
            server.start();
            System.out.println("Server started at http://localhost:8080/callback. Waiting for authorization...");
            while (authCode[0] == null) {
                Thread.sleep(100); // Wait until the code is captured
            }
            code = authCode[0];

        } catch (Exception e) {
            e.printStackTrace();
        }
        tokens();
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> queryPairs = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            queryPairs.put(keyValue[0], keyValue[1]);
        }
        return queryPairs;
    }
    //Exchange code for Access and Refresh Tokens
    public static void tokens() {
        try {
            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
            AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();
            spotifyApi.setAccessToken(credentials.getAccessToken());
            spotifyApi.setRefreshToken(credentials.getRefreshToken()); 
            // System.out.println("Access Token " + credentials.getAccessToken());
            // System.out.println("Refresh Token " + credentials.getRefreshToken());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static void getUsersTopAlbums_Sync(String s, int n) { //Spotify doesn't have a direct thing for this, so we can go through the top songs and attribute it to the albums based on frequency
        try {
            ArrayList<AlbumEntry> albumList = new ArrayList<>();
            int offset = 0;
            boolean moreTracks = true;
            System.out.println("Top Albums:");
            while (moreTracks) {
                GetUsersTopTracksRequest topTracksRequest = spotifyApi.getUsersTopTracks().limit(50).offset(offset).time_range(s).build();
                Paging<Track> trackPaging = topTracksRequest.execute();
                Track[] tracks = trackPaging.getItems();
                if (tracks.length == 0) {
                    moreTracks = false;
                    break;
                }
                for (Track track: tracks) {
                    AlbumSimplified album = track.getAlbum();
                    String albumName = album.getName();

                    String artistNames = ""; //all the artists added to a string
                    ArtistSimplified[] albumArtists = album.getArtists();
                    for (int j = 0; j < albumArtists.length; j++) {
                        artistNames += albumArtists[j].getName();
                        if (j != albumArtists.length -1) {
                            artistNames += ", ";
                        }
                    }

                    boolean found = false;
                    for (AlbumEntry a : albumList) {
                        if (a.name.equals(albumName)) {
                            a.incrementCount();
                            found = true;;
                            break;
                        }
                    }
                    if (!found) {
                        albumList.add(new AlbumEntry(albumName, artistNames));
                    }
                }
                offset += 50;
            } 
            albumList.sort((a, b) -> Integer.compare(b.count, a.count));
            int ct = Math.min(n, albumList.size());
            for (int i = 0; i < ct; i++) { //Print
                AlbumEntry entry = albumList.get(i);
                System.out.println(entry.name + " by " + entry.artists + " - " + entry.count + " songs listened to");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static void getListofCurrentUsersPlaylists_Sync(int a) {
        try {
            GetListOfCurrentUsersPlaylistsRequest playlistRequest = spotifyApi.getListOfCurrentUsersPlaylists().limit(a).build();
            Paging<PlaylistSimplified> playlistEasyPaging = playlistRequest.execute();
            PlaylistSimplified[] playlists = playlistEasyPaging.getItems();
            System.out.println("Your most recent playlists here: ");
            for (PlaylistSimplified p : playlists) {
                System.out.println(p.getName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    } 
    public static void getUsersTopArtists_Sync(String s, int a) {
        try {
            GetUsersTopArtistsRequest artistRequest = spotifyApi.getUsersTopArtists().time_range(s).limit(a).build();
            Paging<Artist> artistPaging = artistRequest.execute();
            System.out.println("Top Artists:");
            for (Artist artist : artistPaging.getItems()) {
                System.out.println(artist.getName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static void getUsersTopTracks_Sync(String s, int a) {
        try {
            GetUsersTopTracksRequest tracksRequest = spotifyApi.getUsersTopTracks().time_range(s).limit(a).build();
            Paging<Track> tracksPaging = tracksRequest.execute();
            System.out.println("Top Tracks:");
            Track[] trackList = tracksPaging.getItems();
            for (int i = 0; i < trackList.length; i++) {
                System.out.print(trackList[i].getName() + " by ");
                ArtistSimplified[] trackArtists = trackList[i].getArtists();
                for (int j = 0; j < trackArtists.length; j++) {
                    System.out.print(trackArtists[j].getName());
                    if (j != trackArtists.length -1) {
                        System.out.print(", ");
                    }
                }
                System.out.println();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static String getUser() {
        try {
            User user = spotifyApi.getCurrentUsersProfile().build().execute();
            return user.getDisplayName();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "No user detected.";
    }
}   
