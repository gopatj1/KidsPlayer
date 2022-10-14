package jewelrock.irev.com.jewelrock.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Motivator extends RealmObject implements Comparable<Motivator> {

    @SerializedName("id")
    @PrimaryKey
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("videoid")
    private int videoId;

    @SerializedName("textmotivator")
    private String textMotivator;

    @SerializedName("sound")
    private String sound;

    @SerializedName("position")
    private int position;

    @SerializedName("image_phone")
    private String imagePhone;

    @SerializedName("image_tablet")
    private String imageTablet;

    @SerializedName("textinkey")
    private String textInKey;


    private boolean isShown;
    private boolean isImageLoaded;
    private long updateKey;

    public long getUpdateKey() {
        return updateKey;
    }

    public Motivator setUpdateKey(long updateKey) {
        this.updateKey = updateKey;
        return this;
    }

    public boolean isShown() {
        return isShown;
    }

    public Motivator setShown(boolean shown) {
        isShown = shown;
        return this;
    }

    public boolean isImageLoaded() {
        return isImageLoaded;
    }

    public Motivator setImageLoaded(boolean imageLoaded) {
        isImageLoaded = imageLoaded;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    public int getVideoId() {
        return videoId;
    }

    public void setTextMotivator(String textMotivator) {
        this.textMotivator = textMotivator;
    }

    public String getTextMotivator() {
        return textMotivator;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getSound() {
        return sound;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setImagePhone(String imagePhone) {
        this.imagePhone = imagePhone;
    }

    public String getImagePhone() {
        return imagePhone;
    }

    public void setImageTablet(String imageTablet) {
        this.imageTablet = imageTablet;
    }

    public String getImageTablet() {
        return imageTablet;
    }

    public void setTextInKey(String textInKey) {
        this.textInKey = textInKey;
    }

    public String getTextInKey() {
        return textInKey;
    }

    @Override
    public String toString() {
        return
                "Motivator{" +
                        "name = '" + name + '\'' +
                        ",videoId = '" + videoId + '\'' +
                        ",textMotivator = '" + textMotivator + '\'' +
                        ",id = '" + id + '\'' +
                        ",position = '" + position + '\'' +
                        ",image_phone = '" + imagePhone + '\'' +
                        ",image_tablet = '" + imageTablet + '\'' +
                        ",textInKey = '" + textInKey + '\'' +
                        ",sound = '" + sound + '\'' +
                        "}";
    }

    @Override
    public int compareTo(@NonNull Motivator other) {
        if (id  > other.id) return 1;
        if (id < other.id) return -1;
        return 0;
    }
}