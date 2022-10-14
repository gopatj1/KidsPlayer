package jewelrock.irev.com.jewelrock.controller;

import io.reactivex.Single;
import io.realm.Realm;
import jewelrock.irev.com.jewelrock.BuildConfig;
import jewelrock.irev.com.jewelrock.model.Video;

/**
 * Created by Yuri Peremetov on 17.05.2017.
 */

public class DataLoader {

    public static Single<Boolean> updateMainData(String url){
        if (BuildConfig.HAS_PLAYLISTS) {
            if (BuildConfig.HAS_SECTIONS && url.equals(BuildConfig.BASE_URL))
                return DataController.INSTANCE.updatePlaylistsBySections(url);
            else
                return DataController.INSTANCE.updatePlaylists(url);
        }
        return DataController.INSTANCE.updateVideo(BuildConfig.BASE_URL, BuildConfig.SOLO_PLAYLIST_NAME, 1);
    }

    public static Single<Boolean> updateMainData(String url, String playlistName, int playlistId){
        return DataController.INSTANCE.updateVideo(url, playlistName, playlistId);
    }

    public static boolean hasLoadedData() {
        Realm realm = Realm.getDefaultInstance();
        boolean result = realm.where(Video.class).findFirst() != null;
        realm.close();
        return result;
    }

    public static boolean hasPlaylists(){
        return BuildConfig.HAS_PLAYLISTS;
    }

    public static Single<Boolean> updateSongMainData(){
        if (BuildConfig.HAS_SONG_PLAYLISTS)
            return DataController.INSTANCE.updateSongPlaylists();
        else return DataController.INSTANCE.updateSong();
    }
}
