package jewelrock.irev.com.jewelrock.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Section extends RealmObject {

    @SerializedName("id")
    @Expose
    @PrimaryKey
    @Required
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("playlists")
    @Expose
    private RealmList<Playlist> playlists;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public RealmList<Playlist> getPlaylists() {
        return playlists;
    }

    public Section setPlaylists(RealmList<Playlist> playlists) {
        this.playlists = playlists;
        return this;
    }
}