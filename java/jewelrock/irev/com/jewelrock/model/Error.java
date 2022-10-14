package jewelrock.irev.com.jewelrock.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Error extends RealmObject{

    public static final int NO_DISK_SPACE = 1;
    public static final int NO_INTERNET_CONNECTION = 3;
    public static final int NO_DISK_SPACE_FOR_PREVIEW = 2;

    @PrimaryKey
    private Integer type;
    private String message;
    private boolean removeAfterShow;
    private boolean isShown;

    public boolean isShown() {
        return isShown;
    }

    public Error setShown(boolean shown) {
        isShown = shown;
        return this;
    }

    public boolean isRemoveAfterShow() {
        return removeAfterShow;
    }

    public Error setRemoveAfterShow(boolean removeAfterShow) {
        this.removeAfterShow = removeAfterShow;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public Error setType(Integer type) {
        this.type = type;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Error setMessage(String message) {
        this.message = message;
        return this;
    }
}
