package jewelrock.irev.com.jewelrock.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;


public class UserSettings extends RealmObject{

    @PrimaryKey
    private Integer id;
    private boolean isPaid;
    private String sku;
    private String purchaseType; //IabHelper.ITEM_TYPE_INAPP|IabHelper.ITEM_TYPE_SUBS\null
    private boolean isPlayCycle = false;
    private boolean soundsOn;
    private Integer needShowLongTapInfo;
    private long lastAdTime;
    private boolean showAd;
    private boolean showMotivator;
    private boolean showStreamingBauBay = true;
    private boolean showMotivatorBauBay22;
    private boolean showVideoSplash = true;
    private boolean showAutoScrollSeekBar = true;
    private int lastStreamingDate;
    private int lastStreamingTime;
    private long lastBannerTime = 0;
    private int lastBannerId = -1;
    private boolean bannerIsOpen = false;
    private RealmList<Integer> lastStreamingVideoId = new RealmList<>();
    private String lastFragmentName = "allMults";

    public boolean isShowMotivator() {
        return showMotivator;
    }

    public UserSettings setShowMotivator(boolean showMotivator) {
        this.showMotivator = showMotivator;
        return this;
    }

    public boolean isShowStreamingBauBay() {
        return showStreamingBauBay;
    }

    public boolean isShowMotivatorBauBay22() {
        return showMotivatorBauBay22;
    }

    public UserSettings setShowStreamingBauBay(boolean showStreamingBauBay) {
        this.showStreamingBauBay = showStreamingBauBay;
        return this;
    }

    public UserSettings setShowMotivatorBauBay22(boolean showMotivatorBauBay22) {
        this.showMotivatorBauBay22 = showMotivatorBauBay22;
        return this;
    }

    public boolean isVideoSplash() {
        return showVideoSplash;
    }

    public UserSettings setVideoSplash(boolean showVideoSplash) {
        this.showVideoSplash = showVideoSplash;
        return this;
    }

    public boolean isAutoScrollSeekBar() {
        return showAutoScrollSeekBar;
    }

    public UserSettings setAutoScrollSeekBar(boolean showAutoScrollSeekBar) {
        this.showAutoScrollSeekBar = showAutoScrollSeekBar;
        return this;
    }

    public boolean isStreamingWasAlready() {
        return  (lastStreamingDate == Integer.parseInt(new SimpleDateFormat("ddMMyyyy").format((new Date()))) &&
                lastStreamingTime + 73000 > Integer.parseInt(new SimpleDateFormat("HHmmss").format((new Date()))));
    }

    public UserSettings setLastBannerTime(long time) {
        lastBannerTime = time;
        return this;
    }

    public long getLastBannerTime() {
        return lastBannerTime;
    }

    public UserSettings setLastBannerId(int id) {
        lastBannerId = id;
        return this;
    }

    public int getLastBannerId() {
        return lastBannerId;
    }

    public boolean getBannerIsOpen() {
        return bannerIsOpen;
    }

    public UserSettings setBannerShowState(boolean open) {
        bannerIsOpen = open;
        return this;
    }

    public RealmList<Integer> getLastStreamingVideoId() {
        return lastStreamingVideoId;
    }

    public UserSettings setLastStreamingVideoId(RealmResults<Video> streamingVideo) {
        this.lastStreamingVideoId.clear();
        for (int i = 0; i < 4; i++)
            this.lastStreamingVideoId.add(streamingVideo.get(i).getId());
        return this;
    }

    public UserSettings setLastStreamingDate() {
        this.lastStreamingDate = Integer.parseInt(new SimpleDateFormat("ddMMyyyy").format((new Date())));
        this.lastStreamingTime = Integer.parseInt(new SimpleDateFormat("HHmmss").format((new Date())));
        return this;
    }

    public Integer isNeedShowLongTapInfo() {
        return needShowLongTapInfo == null ? 0 : needShowLongTapInfo;
    }

    public UserSettings setNeedShowLongTapInfo(Integer needShowLongTapInfo) {
        this.needShowLongTapInfo = needShowLongTapInfo;
        return this;
    }

    public boolean isSoundsOn() {
        return soundsOn;
    }

    public UserSettings setSoundsOn(boolean soundsOn) {
        this.soundsOn = soundsOn;
        return this;
    }

    public UserSettings() {
    }

    public UserSettings(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public UserSettings setId(Integer id) {
        this.id = id;
        return this;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public UserSettings setPaid(boolean paid) {
        isPaid = paid;
        return this;
    }

    public UserSettings setSku(String sku, String type){
        this.sku = sku;
        purchaseType = type;
        return this;
    }

    public UserSettings setPlayCycle(boolean playCycle){
        this.isPlayCycle = playCycle;
        return this;
    }

    public boolean isPlayCycle(){
        return isPlayCycle;
    }

    public long getLastAdTime() {
        return lastAdTime;
    }

    public UserSettings setLastAdTime(long lastAdTime) {
        this.lastAdTime = lastAdTime;
        return this;
    }

    public boolean isShowAd() {
        return showAd;
    }

    public UserSettings setShowAd(boolean showAd) {
        this.showAd = showAd;
        return this;
    }

    public String getLastFragmentName() {
        return lastFragmentName;
    }

    public UserSettings setLastFragmentName(String lastFragmentName) {
        this.lastFragmentName = lastFragmentName;
        return this;
    }
}
