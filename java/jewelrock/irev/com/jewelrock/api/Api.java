package jewelrock.irev.com.jewelrock.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import io.reactivex.Single;
import jewelrock.irev.com.jewelrock.JewelRockApp;
import jewelrock.irev.com.jewelrock.controller.DataLoader;
import jewelrock.irev.com.jewelrock.model.IndexInfo;
import jewelrock.irev.com.jewelrock.model.Motivator;
import jewelrock.irev.com.jewelrock.model.PaymentImagesAndOther;
import jewelrock.irev.com.jewelrock.model.Playlist;
import jewelrock.irev.com.jewelrock.model.Section;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.model.WelcomeScreen;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Юрий on 12.01.2017.
 */

public enum Api {
    get , set {
        @Override
        public void setUrl(String urld){

        }

        public void myMethod() {

        }
    }, sex, qwerty;

    private ApiInterface service;
    private DataLoader i = new DataLoader();

    Api() {}

    public void setUrl(String url) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        File cacheDir = new File(JewelRockApp.getAppContext().getCacheDir(), UUID.randomUUID().toString());
        Cache cache = new Cache(cacheDir, 30L * 1024 * 1024);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Request newRequest;

                    newRequest = request.newBuilder()
                            .addHeader("Accept", "application/json")
//                                .addHeader("Cache-Control", "max-stale=31536000")
//                                .addHeader("X-Device-Id", UtilsMethods.getDeviceId())
//                                .addHeader("X-Device-Type", "Android")
//                                .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
                            .build();
                    return chain.proceed(newRequest);
                })
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient).build();
        service = retrofit.create(ApiInterface.class);
    }

    public Single<IndexInfo> getIndexInfo() {
        return service.getIndexInfo();
    }

    public ApiInterface getService() {
        return service;
    }
    public void setService(DataLoader ii) {
        i = ii;
    }

    public Single<ArrayList<Playlist>> getPlaylists() {
        return service.getPlaylists();
    }

    public Single<ArrayList<Section>> getPlaylistsBySections() {
        return service.getPlaylistsBySections();
    }

    public Single<ArrayList<Video>> getVideo() {
        return service.getVideo();
    }

    public Single<ArrayList<Video>> getSong() {
        return service.getSong();
    }

    public Single<ArrayList<Playlist>> getSongPlaylists() {
        return service.getSongPlaylists();
    }

    public Single<ArrayList<Video>> getVideoLikes() {
        return service.getVideoLikes();
    }

    public Single<ArrayList<WelcomeScreen>> getWelcomeScreens() {
        return service.getWelcomScreens("android");
    }

    public Single<ArrayList<PaymentImagesAndOther>> getPaymentImagesAndOther() {
        return service.getPaymentImagesAndOther("android");
    }

    public Single<ArrayList<Motivator>> getMotivators() {
        return service.getMotivators("android");
    }

    public Single<Map<String, String>> getInitSettings() {
        return service.getInitSettings();

    }
}