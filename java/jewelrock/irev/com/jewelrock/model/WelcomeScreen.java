package jewelrock.irev.com.jewelrock.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Юрий on 12.01.2017.
 */

public class WelcomeScreen extends RealmObject{

    @PrimaryKey
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("position")
    @Expose
    private Integer position;
    @SerializedName("image_phone")
    @Expose
    private String imagePhone;
    @SerializedName("background_color_phone")
    @Expose
    private String backgroundColorPhone;
    @SerializedName("image_tablet")
    @Expose
    private String imageTablet;
    @SerializedName("background_color_tablet")
    @Expose
    private String backgroundColorTablet;

    // для рекламы
    @SerializedName("screen_type")
    @Expose
    private String screenType;

    @SerializedName("screen_weight")
    @Expose
    private String screenWeight;

    @SerializedName("url")
    @Expose
    private String url;

    @SerializedName("close_time")
    @Expose
    private String closeTime;

    @SerializedName("display_order")
    @Expose
    private String displayOrder;

    @SerializedName("audience")
    @Expose
    private String audience;

    @SerializedName("lasting")
    @Expose
    private String lasting;

    // для локальных операций
    private boolean isShown;
    private boolean isImageLoaded;

    public boolean isImageLoaded() {
        return isImageLoaded;
    }

    public WelcomeScreen setImageLoaded(boolean imageLoaded) {
        isImageLoaded = imageLoaded;
        return this;
    }

    public boolean isShown() {
        return isShown;
    }

    public WelcomeScreen setShown(boolean shown) {
        isShown = shown;
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

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getImagePhone() {
        return imagePhone;
    }

    public void setImagePhone(String imagePhone) {
        this.imagePhone = imagePhone;
    }

    public String getBackgroundColorPhone() {
        return backgroundColorPhone;
    }

    public void setBackgroundColorPhone(String backgroundColorPhone) {
        this.backgroundColorPhone = backgroundColorPhone;
    }

    public String getImageTablet() {
        return imageTablet;
    }

    public void setImageTablet(String imageTablet) {
        this.imageTablet = imageTablet;
    }

    public String getBackgroundColorTablet() {
        return backgroundColorTablet;
    }

    public void setBackgroundColorTablet(String backgroundColorTablet) {
        this.backgroundColorTablet = backgroundColorTablet;
    }

    public String getScreenType() {
        return screenType;
    }

    public WelcomeScreen setScreenType(String screenType) {
        this.screenType = screenType;
        return this;
    }

    public String getScreenWeight() {
        return screenWeight;
    }

    public WelcomeScreen setScreenWeight(String screenWeight) {
        this.screenWeight = screenWeight;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public WelcomeScreen setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public WelcomeScreen setCloseTime(String closeTime) {
        this.closeTime = closeTime;
        return this;
    }

    public String getDisplayOrder() {
        return displayOrder;
    }

    public WelcomeScreen setDisplayOrder(String displayOrder) {
        this.displayOrder = displayOrder;
        return this;
    }

    public String getAudience() {
        return audience;
    }

    public WelcomeScreen setAudience(String audience) {
        this.audience = audience;
        return this;
    }

    public String getLasting() {
        return lasting;
    }

    public WelcomeScreen setLasting(String lasting) {
        this.lasting = lasting;
        return this;
    }
}
