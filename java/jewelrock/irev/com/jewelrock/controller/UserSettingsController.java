package jewelrock.irev.com.jewelrock.controller;

import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;
import jewelrock.irev.com.jewelrock.model.UserSettings;
import jewelrock.irev.com.jewelrock.model.Video;

/**
 * Created by Юрий on 28.02.2017.
 */
public class UserSettingsController {
    public static void setUserPaid(boolean isPaid) {
        Realm realm = Realm.getDefaultInstance();
        UserSettings settings = realm.where(UserSettings.class).equalTo("id", 0).findFirst();
        realm.beginTransaction();
        Objects.requireNonNull(settings).setPaid(isPaid);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
        realm.close();
    }

    public static void setSoundOn(boolean isOn) {
        Realm realm = Realm.getDefaultInstance();
        UserSettings settings = realm.where(UserSettings.class).equalTo("id", 0).findFirst();
        realm.beginTransaction();
        Objects.requireNonNull(settings).setSoundsOn(isOn);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
        realm.close();
    }

    public static void setUserPurchase(String sku, String type) {
        Realm realm = Realm.getDefaultInstance();
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setSku(sku, type);
        settings.setPaid(true);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
        realm.close();
    }

    public static UserSettings loadUserSettings(Realm realm) {
        UserSettings settings = realm.where(UserSettings.class).equalTo("id", 0).findFirst();
        if (settings == null) {
            realm.beginTransaction();
            UserSettings userSettings = realm.createObject(UserSettings.class, 0);
            userSettings.setSoundsOn(true);
            userSettings.setShowAd(true);
            userSettings.setShowMotivator(true);
//            userSettings.setShowStreamingBauBay(true);
//            userSettings.setShowMotivatorBauBay22(true);
//            userSettings.setVideoSplash(true);
            userSettings.setPlayCycle(false);
            userSettings.setLastAdTime(0);
            realm.copyToRealmOrUpdate(userSettings);
            realm.commitTransaction();
            settings = realm.where(UserSettings.class).equalTo("id", 0).findFirst();
        }
        return settings;
    }

    public static void setStartOver(Realm realm, boolean isStartOver) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setPlayCycle(isStartOver);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setShowLongTap(Realm realm, Integer needShow) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setNeedShowLongTapInfo(needShow);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setAdShowTime(Realm realm, long time) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setLastAdTime(time);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setShowAd(Realm realm, boolean show) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setShowAd(show);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setShowMotivator(Realm realm, boolean show) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setShowMotivator(show);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setShowStreamingBauBay(Realm realm, boolean show) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setShowStreamingBauBay(show);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setShowMotivatorBauBay22(Realm realm, boolean show) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setShowMotivatorBauBay22(show);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setLastStreamingDate(Realm realm) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setLastStreamingDate();
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setLastStreamingVideoId(Realm realm, RealmResults<Video> streamingVideo) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setLastStreamingVideoId(streamingVideo);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setLastFragmentName(Realm realm, String lastFragmentName) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setLastFragmentName(lastFragmentName);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setLastBannerTimeIDAndState(Realm realm, long time, int lastBannerId, boolean open) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setLastBannerTime(time);
        settings.setLastBannerId(lastBannerId);
        settings.setBannerShowState(open);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setVideoSplash(Realm realm, boolean show) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setVideoSplash(show);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setAutoScrollSeekBar(Realm realm, boolean show) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setAutoScrollSeekBar(show);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }
}
