package jewelrock.irev.com.jewelrock.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Video extends RealmObject implements Comparable<Video>{
    @SerializedName("id")
    @Expose
    @PrimaryKey
    private Integer id;
    @SerializedName("id_json")
    @Expose
    private Integer idJSON;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("android_price")
    @Expose
    private Integer price;
    @SerializedName("android_purchase")
    @Expose
    private String purchase;
    @SerializedName("image_price")
    @Expose
    private String imagePrice;
    @SerializedName("video_likes")
    @Expose
    private Integer videoLikes;
    @SerializedName("youtube_link")
    @Expose
    private String youtubeLink;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("share_image")
    @Expose
    private String shareImage;
    @SerializedName("share_link")
    @Expose
    private String shareLink;
    @SerializedName("share_text")
    @Expose
    private String shareText;
    @SerializedName("video_preview")
    @Expose
    private String videoPreview;
    @SerializedName("video_preview_duration")
    @Expose
    private String videoPreviewDuration;
    @SerializedName("video_source")
    @Expose
    private String videoSource;
    @SerializedName("video_source_duration")
    @Expose
    private String videoSourceDuration;
    @SerializedName("video_compressed")
    @Expose
    private String videoCompressed;
    @SerializedName("video_compressed_duration")
    @Expose
    private String videoCompressedDuration;
    @SerializedName("position")
    @Expose
    private Integer position;

    @SerializedName("video_source_duration_sec")
    @Expose
    private Integer videoSourceDurationSec;
    @SerializedName("video_source_size")
    @Expose
    private Long videoSourceSize;

    @SerializedName("video_compressed_duration_sec")
    @Expose
    private Integer videoCompressedDurationSec;
    @SerializedName("video_compressed_size")
    @Expose
    private Long videoCompressedSize;
    @SerializedName("google_link")
    @Expose
    private String googleLink;
    @SerializedName("series")
    @Expose
    private String series;
    @SerializedName("video_current_application")
    @Expose
    private boolean videoOfCurrentApplication;


    //////////////////////// songs params ////////////////////////
    @SerializedName("song_likes")
    @Expose
    private Integer songLikes;
    @SerializedName("song_source")
    @Expose
    private String songSource;
    @SerializedName("song_source_duration")
    @Expose
    private String songSourceDuration;
    @SerializedName("song_source_duration_sec")
    @Expose
    private Integer songSourceDurationSec;
    @SerializedName("song_source_size")
    @Expose
    private Long songSourceSize;

    @SerializedName("song_preview")
    @Expose
    private String songPreview;
    @SerializedName("song_preview_duration")
    @Expose
    private String songPreviewDuration;

    @SerializedName("song_compressed")
    @Expose
    private String songCompressed;
    @SerializedName("song_compressed_duration")
    @Expose
    private String songCompressedDuration;
    @SerializedName("song_compressed_duration_sec")
    @Expose
    private Integer songCompressedDurationSec;
    @SerializedName("song_compressed_size")
    @Expose
    private Long songCompressedSize;

    private long updateKey;
    private boolean isFavorite;
    private long favoriteTime;
    private boolean isPurchase;

    private String previewFile;
    private String videoFile;
    private int videoLoadingProgress;
    private int quality;

    private Playlist playlist;
    private RealmList<Playlist> playlists = new RealmList<>();

    private String search;

    public String getSearch() {
        return search;
    }

    public Video setSearch(String search) {
        this.search = search;
        return this;
    }

    public Integer getVideoSourceDurationSec() {
        return videoSourceDurationSec;
    }

    public Video setVideoSourceDurationSec(Integer videoSourceDurationSec) {
        this.videoSourceDurationSec = videoSourceDurationSec;
        return this;
    }

    public Long getVideoSourceSize() {
        return videoSourceSize;
    }

    public Video setVideoSourceSize(Long videoSourceSize) {
        this.videoSourceSize = videoSourceSize;
        return this;
    }

    public Integer getVideoCompressedDurationSec() {
        return videoCompressedDurationSec;
    }

    public Video setVideoCompressedDurationSec(Integer videoCompressedDurationSec) {
        this.videoCompressedDurationSec = videoCompressedDurationSec;
        return this;
    }

    public Long getVideoCompressedSize() {
        return videoCompressedSize;
    }

    public Video setVideoCompressedSize(Long videoCompressedSize) {
        this.videoCompressedSize = videoCompressedSize;
        return this;
    }

    public Playlist getPlaylist(int index) {
        return playlists.get(index);
    }

    public Video addPlaylist(Playlist playlist) {
        this.playlists.add(playlist);
        return this;
    }

    public int getPlaylistCount() {
        return this.playlists.size();
    }

    public boolean isPlaylistEmpty() {
        return this.playlists.isEmpty();
    }

    public boolean containPlaylist(Playlist playlist) {
        return this.playlists.contains(playlist);
    }

    public Video setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        return this;
    }

    public Playlist getPlaylist() {
        return this.playlist;
    }

    public Playlist getPlaylistBySection(String name) {
        for (Playlist p: this.playlists)
            if (p.getSectionName().equals(name))
                return p;
        return null;
    }

    public long getFavoriteTime() {
        return favoriteTime;
    }

    public Video setFavoriteTime(long favoriteTime) {
        this.favoriteTime = favoriteTime;
        return this;
    }

    public int getQuality() {
        return quality;
    }

    public Video setQuality(int quality) {
        this.quality = quality;
        return this;
    }

    public int getVideoLoadingProgress() {
        return videoLoadingProgress;
    }

    public Video setVideoLoadingProgress(int videoLoadingProgress) {
        this.videoLoadingProgress = videoLoadingProgress;
        return this;
    }

    public String getPreviewFile() {
        return previewFile;
    }

    public Video setPreviewFile(String previewFile) {
        this.previewFile = previewFile;
        return this;
    }

    public String getVideoFile() {
        return videoFile;
    }

    public Video setVideoFile(String videoFile) {
        this.videoFile = videoFile;
        return this;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public Video setFavorite(boolean favorite) {
        isFavorite = favorite;
        return this;
    }

    public boolean isPurchase() {
        return isPurchase;
    }

    public Video setPurchase(boolean purchase) {
        isPurchase = purchase;
        return this;
    }

    public long getUpdateKey() {
        return updateKey;
    }

    public Video setUpdateKey(long updateKey) {
        this.updateKey = updateKey;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdJSON() {
        return idJSON;
    }

    public void setIdJSON(Integer idJSON) {
        this.idJSON = idJSON;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVideoLikes() {
        return videoLikes;
    }

    public void setVideoLikes(Integer videoLikes) {
        this.videoLikes = videoLikes;
    }

    public String getYoutubeLink() {
        return youtubeLink;
    }

    public void setYoutubeLink(String youtubeLink) {
        this.youtubeLink = youtubeLink;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getShareImage() {
        return shareImage;
    }

    public void setShareImage(String shareImage) {
        this.shareImage = shareImage;
    }

    public String getShareLink() {
        return shareLink;
    }

    public void setShareLink(String shareLink) {
        this.shareLink = shareLink;
    }

    public String getShareText() {
        return shareText;
    }

    public void setShareText(String shareText) {
        this.shareText = shareText;
    }

    public String getVideoPreview() {
        return getEncodedUrl(videoPreview);
    }

    public void setVideoPreview(String videoPreview) {
        this.videoPreview = videoPreview;
    }

    public String getVideoPreviewDuration() {
        return videoPreviewDuration;
    }

    public void setVideoPreviewDuration(String videoPreviewDuration) {
        this.videoPreviewDuration = videoPreviewDuration;
    }

    public String getVideoSource() {
        return getEncodedUrl(videoSource);
    }

    public void setVideoSource(String videoSource) {
        this.videoSource = videoSource;
    }

    public String getVideoSourceDuration() {
        return videoSourceDuration;
    }

    public void setVideoSourceDuration(String videoSourceDuration) {
        this.videoSourceDuration = videoSourceDuration;
    }

    public String getVideoCompressed() {
        return getEncodedUrl(videoCompressed);
    }

    private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
    @Nullable
    private String getEncodedUrl(String url) {
        return Uri.encode(url, ALLOWED_URI_CHARS);
    }

    public void setVideoCompressed(String videoCompressed) {
        this.videoCompressed = videoCompressed;
    }

    public String getVideoCompressedDuration() {
        return videoCompressedDuration;
    }

    public void setVideoCompressedDuration(String videoCompressedDuration) {
        this.videoCompressedDuration = videoCompressedDuration;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    //////////////////////// songs params ////////////////////////

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

    public Integer getSongLikes() {
        return songLikes;
    }

    public void setSongLikes(Integer songLikes) {
        this.songLikes = songLikes;
    }

    public String getSongSource() {
        return getEncodedUrl(songSource);
    }

    public void setSongSource(String songSource) {
        this.songSource = songSource;
    }

    public String getSongSourceDuration() {
        return songSourceDuration;
    }

    public void setSongSourceDuration(String songSourceDuration) {
        this.songSourceDuration = songSourceDuration;
    }

    public Integer getSongSourceDurationSec() {
        return songSourceDurationSec;
    }

    public Video setSongSourceDurationSec(Integer songSourceDurationSec) {
        this.songSourceDurationSec = songSourceDurationSec;
        return this;
    }

    public Long getSongSourceSize() {
        return songSourceSize;
    }

    public Video setSongSourceSize(Long songSourceSize) {
        this.songSourceSize = songSourceSize;
        return this;
    }

    public String getSongPreview() {
        return getEncodedUrl(songPreview);
    }

    public void setSongPreview(String songPreview) {
        this.songPreview = songPreview;
    }

    public String getSongPreviewDuration() {
        return songPreviewDuration;
    }

    public void setSongPreviewDuration(String songPreviewDuration) {
        this.songPreviewDuration = songPreviewDuration;
    }

    public String getSongCompressed() {
        return getEncodedUrl(songCompressed);
    }

    public void setSongCompressed(String songCompressed) {
        this.songCompressed = songCompressed;
    }

    public String getSongCompressedDuration() {
        return songCompressedDuration;
    }

    public void setSongCompressedDuration(String songCompressedDuration) {
        this.songCompressedDuration = songCompressedDuration;
    }

    public Integer getSongCompressedDurationSec() {
        return songCompressedDurationSec;
    }

    public Video setSongCompressedDurationSec(Integer songCompressedDurationSec) {
        this.songCompressedDurationSec = songCompressedDurationSec;
        return this;
    }

    public Long getSongCompressedSize() {
        return songCompressedSize;
    }

    public Video setSongCompressedSize(Long songCompressedSize) {
        this.songCompressedSize = songCompressedSize;
        return this;
    }

    public String getGoogleLink() {
        return googleLink;
    }

    public Video setGoogleLink(String googleLink) {
        this.googleLink = googleLink;
        return this;
    }

    public String getSeries() {
        return series;
    }

    public Video setSeries(String series) {
        this.series = series;
        return this;
    }

    public boolean getVideoOfCurrentApplication() {
        return videoOfCurrentApplication;
    }

    public Video setVideoOfCurrentApplication(boolean videoOfCurrentApplication) {
        this.videoOfCurrentApplication = videoOfCurrentApplication;
        return this;
    }

    @Override
    public int compareTo(@NonNull Video other) {
        if (id  > other.id) return 1;
        if (id < other.id) return -1;
        return 0;
    }

    public boolean isFree() {
        return "free".equals(type);
    }
}
