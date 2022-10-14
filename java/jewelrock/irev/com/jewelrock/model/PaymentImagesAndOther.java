package jewelrock.irev.com.jewelrock.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class PaymentImagesAndOther  extends RealmObject {

    @PrimaryKey
    @SerializedName("id")
    @Expose
    @Required
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("image_phone")
    @Expose
    private String imagePhone;
    @SerializedName("image_tablet")
    @Expose
    private String imageTablet;
    @SerializedName("type")
    @Expose
    private String paidType;

    private boolean isImageLoaded;

    public boolean isImageLoaded() {
        return isImageLoaded;
    }

    public PaymentImagesAndOther setImageLoaded(boolean imageLoaded) {
        isImageLoaded = imageLoaded;
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

    public String getImagePhone() {
        return imagePhone;
    }

    public void setImagePhone(String imagePhone) {
        this.imagePhone = imagePhone;
    }

    public String getImageTablet() { return imageTablet; }

    public void setImageTablet(String imageTablet) {
        this.imageTablet = imageTablet;
    }

    public String getImageTabletOrPhone(boolean isBigScreen) {
        if (isBigScreen) return getImageTablet();
        else  return  getImagePhone();
    }

    public String getPaidType() { return paidType; }

    public void setPaidType(String paidType) {
        this.paidType = paidType;
    }
}