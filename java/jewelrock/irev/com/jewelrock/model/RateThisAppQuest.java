package jewelrock.irev.com.jewelrock.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class RateThisAppQuest extends RealmObject{

    @PrimaryKey
    private Integer id;
    private int videoRunsCount;
    private int favoritesCount;
    private boolean isActive;

    public int getVideoRunsCount() {
        return videoRunsCount;
    }

    public RateThisAppQuest setVideoRunsCount(int videoRunsCount) {
        this.videoRunsCount = videoRunsCount;
        return this;
    }

    public int getFavoritesCount() {
        return favoritesCount;
    }

    public RateThisAppQuest setFavoritesCount(int favoritesCount) {
        this.favoritesCount = favoritesCount;
        return this;
    }

    public boolean isActive() {
        return isActive;
    }

    public RateThisAppQuest setActive(boolean active) {
        isActive = active;
        return this;
    }
}
