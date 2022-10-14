package jewelrock.irev.com.jewelrock.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Playlist extends RealmObject{

    @SerializedName("id")
    @Expose
    @PrimaryKey
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("android_price")
    @Expose
    private Integer price;
    @SerializedName("android_purchase")
    @Expose
    private String purchase;
    @SerializedName("image_price")
    @Expose
    private String imagePrice;
    @SerializedName("playlist_video")
    @Expose
    private String playlistVideo;
    @SerializedName("playlistsong")
    @Expose
    private String playlistSong;
    @SerializedName("position")
    @Expose
    private Integer position;
    @SerializedName("font_color")
    @Expose
    private String fontColor;

    @SerializedName("videos")
    @Expose
    private RealmList<Video> videos;
    @SerializedName("songs")
    @Expose
    private RealmList<Video> songs;


    private long updateKey;
    private int sectionId;
    private String sectionName;
    private String search;

    public String getSearch() {
        return search;
    }

    public Playlist setSearch(String search) {
        this.search = search;
        return this;
    }

    public long getUpdateKey() {
        return updateKey;
    }

    public Playlist setUpdateKey(long updateKey) {
        this.updateKey = updateKey;
        return this;
    }

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getPurchase() {
        return purchase;
    }

    public void setPurchase(String purchase) {
        this.purchase = purchase;
    }

    public String getImagePrice() {
        return imagePrice;
    }

    public void setImagePrice(String imagePrice) {
        this.imagePrice = imagePrice;
    }

    public String getPlaylistVideo() {
        return playlistVideo;
    }

    public void setPlaylistVideo(String playlistVideo) {
        this.playlistVideo = playlistVideo;
    }

    public String getPlaylistSong() {
        return playlistSong;
    }

    public void setPlaylistSong(String playlistSong) {
        this.playlistSong = playlistSong;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public RealmList<Video> getVideos() {
        return videos;
    }

    public Playlist setVideos(RealmList<Video> videos) {
        this.videos = videos;
        return this;
    }

    public RealmList<Video> getSongs() {
        return songs;
    }

    public Playlist setSongs(RealmList<Video> songs) {
        this.songs = songs;
        return this;
    }

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
}
