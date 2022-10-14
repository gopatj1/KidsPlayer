package jewelrock.irev.com.jewelrock.api;

import java.util.ArrayList;
import java.util.Map;

import io.reactivex.Single;
import jewelrock.irev.com.jewelrock.model.Banner;
import jewelrock.irev.com.jewelrock.model.IndexInfo;
import jewelrock.irev.com.jewelrock.model.Motivator;
import jewelrock.irev.com.jewelrock.model.PaymentImagesAndOther;
import jewelrock.irev.com.jewelrock.model.Playlist;
import jewelrock.irev.com.jewelrock.model.Section;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.model.WelcomeScreen;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;


/**
 * Created by Юрий on 12.01.2017.
 */

public interface ApiInterface {

    @Headers("Cache-Control: no-cache")
    @GET("/api/index")
    Single<IndexInfo> getIndexInfo();

    @Headers("Cache-Control: no-cache")
    @GET("/api/playlist")
    Single<ArrayList<Playlist>> getPlaylists();

    @Headers("Cache-Control: no-cache")
    @GET("/api/section")
    Single<ArrayList<Section>> getPlaylistsBySections();

    @Headers("Cache-Control: no-cache")
    @GET("/api/video")
    Single<ArrayList<Video>> getVideo();

    @Headers("Cache-Control: no-cache")
    @GET("/api/song")
    Single<ArrayList<Video>> getSong();

    @Headers("Cache-Control: no-cache")
    @GET("/api/playlistsong")
    Single<ArrayList<Playlist>> getSongPlaylists();

    @Headers("Cache-Control: no-cache")
    @GET("/api/video/likes")
    Single<ArrayList<Video>> getVideoLikes();

    @Headers("Cache-Control: no-cache")
    @GET("/api/playlist/{id}")
    Single<Playlist> getPlaylist(@Path("id") int id);

    @Headers("Cache-Control: no-cache")
    @GET("/api/banner")
    Single<ArrayList<Banner>> getBanners();

    @Headers("Cache-Control: no-cache")
    @GET("/api/welcomescreen")
    Single<ArrayList<WelcomeScreen>> getWelcomScreens(@Query("os") String resourceName);

    @Headers("Cache-Control: no-cache")
    @GET("/api/image")
    Single<ArrayList<PaymentImagesAndOther>> getPaymentImagesAndOther(@Query("os") String resourceName);

    @Headers("Cache-Control: no-cache")
    @GET("/api/motivator")
    Single<ArrayList<Motivator>> getMotivators(@Query("os") String resourceName);

    @Headers("Cache-Control: no-cache")
    @GET("/api/settings")
    Single<Map<String,String>> getInitSettings();

}
