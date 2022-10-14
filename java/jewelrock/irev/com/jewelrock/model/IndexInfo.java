package jewelrock.irev.com.jewelrock.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class IndexInfo extends RealmObject{

    @SerializedName("playlists")
    @Expose
    private RealmList<Playlist> playlists = null;
    @SerializedName("banners")
    @Expose
    private RealmList<Banner> banners = null;
    @SerializedName("welcome_screens")
    @Expose
    private RealmList<WelcomeScreen> welcomeScreens = null;

    public RealmList<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(RealmList<Playlist> playlists) {
        this.playlists = playlists;
    }

    public RealmList<Banner> getBanners() {
        return banners;
    }

    public void setBanners(RealmList<Banner> banners) {
        this.banners = banners;
    }

    public RealmList<WelcomeScreen> getWelcomeScreens() {
        return welcomeScreens;
    }

    public void setWelcomeScreens(RealmList<WelcomeScreen> welcomeScreens) {
        this.welcomeScreens = welcomeScreens;
    }
}
