package jewelrock.irev.com.jewelrock.controller;

import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;
import jewelrock.irev.com.jewelrock.model.InitSettings;
import jewelrock.irev.com.jewelrock.model.ScreenText;

/**
 * Created by Yuri Peremetov on 17.02.2018.
 */

public class InitSettingsController {

    public static InitSettings loadInitSettings(Realm realm) {
        InitSettings settings = realm.where(InitSettings.class).equalTo("id", 0).findFirst();
        if (settings == null) {
            realm.beginTransaction();
            InitSettings userSettings = realm.createObject(InitSettings.class, 0);
            userSettings.setTimerAdMob("1");
            userSettings.setReklamaAdmob("1");
            realm.copyToRealmOrUpdate(userSettings);
            realm.commitTransaction();
            settings = realm.where(InitSettings.class).equalTo("id", 0).findFirst();
        }
        return settings;
    }

    public static void saveInitSettings(Realm realm, InitSettings settings) {
        settings.setId(0);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static ScreenText getScreenTexts(Realm realm, String screenName) {
        ScreenText text = realm.where(ScreenText.class)
                .equalTo("screenName", screenName)
                .equalTo("isShown", false)
                .findFirst();
        if (text == null) {
            RealmResults<ScreenText> texts = realm.where(ScreenText.class)
                    .equalTo("screenName", screenName)
                    .findAll();
            realm.executeTransaction(r -> {
                for (ScreenText t : texts) {
                    t.setShown(false);
                    r.copyToRealmOrUpdate(t);
                }
            });
            text = realm.where(ScreenText.class)
                    .equalTo("screenName", screenName)
                    .findFirst();
        }
        realm.beginTransaction();
        try {
            Objects.requireNonNull(text).setShown(true);
            realm.copyToRealmOrUpdate(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        realm.commitTransaction();
        return text;
    }
}
