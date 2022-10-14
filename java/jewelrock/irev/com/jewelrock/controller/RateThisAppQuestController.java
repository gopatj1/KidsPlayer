package jewelrock.irev.com.jewelrock.controller;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import io.realm.Realm;
import jewelrock.irev.com.jewelrock.BaseRealmActivity;
import jewelrock.irev.com.jewelrock.BuildConfig;
import jewelrock.irev.com.jewelrock.model.RateThisAppQuest;
import jewelrock.irev.com.jewelrock.utils.DialogUtils;

/**
 * Created by Юрий on 28.02.2017.
 */

public class RateThisAppQuestController {

    private static RateThisAppQuest loadRateThisAppQuest(Realm realm) {
        RateThisAppQuest quest = realm.where(RateThisAppQuest.class).equalTo("id", 0).findFirst();
        if (quest == null) {
            realm.beginTransaction();
            RateThisAppQuest q = realm.createObject(RateThisAppQuest.class, 0);
            q.setActive(true);
            realm.copyToRealmOrUpdate(q);
            realm.commitTransaction();
            quest = realm.where(RateThisAppQuest.class).equalTo("id", 0).findFirst();
        }
        return quest;
    }

    public static void addVideoRun() {
        Realm realm = Realm.getDefaultInstance();
        if (UserSettingsController.loadUserSettings(realm).isPaid()) {
            RateThisAppQuest quest = loadRateThisAppQuest(realm);
            if (quest.isActive()) {
                realm.beginTransaction();
                quest.setVideoRunsCount(quest.getVideoRunsCount() + 1);
                realm.copyToRealmOrUpdate(quest);
                realm.commitTransaction();
            }
        }
        realm.close();
    }

    private static void complete() {
        Realm realm = Realm.getDefaultInstance();
        RateThisAppQuest quest = loadRateThisAppQuest(realm);
        realm.beginTransaction();
        quest.setActive(false);
        realm.copyToRealmOrUpdate(quest);
        realm.commitTransaction();
        realm.close();
    }

    public static void addFavorite() {
        Realm realm = Realm.getDefaultInstance();
        if (UserSettingsController.loadUserSettings(realm).isPaid()) {
            RateThisAppQuest quest = loadRateThisAppQuest(realm);
            if (quest.isActive()) {
                realm.beginTransaction();
                quest.setFavoritesCount(quest.getFavoritesCount() + 1);
                realm.copyToRealmOrUpdate(quest);
                realm.commitTransaction();
            }
        }
        realm.close();
    }

    private static boolean isQuestComplete() {
        boolean result = false;
        Realm realm = Realm.getDefaultInstance();
        if (UserSettingsController.loadUserSettings(realm).isPaid()) {
            RateThisAppQuest quest = loadRateThisAppQuest(realm);
            result = quest.getFavoritesCount() >= 3 && quest.getVideoRunsCount() >= 5 && quest.isActive();
        }
        realm.close();
        return result;
    }

    private static void reset() {
        Realm realm = Realm.getDefaultInstance();
        RateThisAppQuest quest = loadRateThisAppQuest(realm);
        realm.beginTransaction();
        quest.setFavoritesCount(0);
        quest.setVideoRunsCount(0);
        quest.setActive(true);
        realm.copyToRealmOrUpdate(quest);
        realm.commitTransaction();
        realm.close();
    }

    public static void checkQuest(final BaseRealmActivity activity){
        if (isQuestComplete()) showRateThisAppDialog(activity);
    }

    private static void showRateThisAppDialog(final BaseRealmActivity activity) {
        Configuration configuration = activity.getResources().getConfiguration();
        int screenWidthDp = configuration.smallestScreenWidthDp;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Оценить")
                .setMessage(BuildConfig.RATE_THIS_APP)
                .setCancelable(false)
                .setPositiveButton(screenWidthDp > 320 ? "Оставить отзыв" : "Оценить", (dialogInterface, i) -> {
                    final String appPackageName = activity.getPackageName();
                    try {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                    activity.analyticsLogEvent("Оценка приложения", "Оставить отзыв");
                    complete();
                })
                .setNeutralButton(screenWidthDp > 320 ? "Напомнить позже" : "Позже", (dialogInterface, i) -> {
                    activity.analyticsLogEvent("Оценка приложения", "Напомнить позже");
                    reset();
                })
                .setNegativeButton(screenWidthDp > 320 ? "Не напоминать" : "Никогда", (dialogInterface, i) -> {
                    activity.analyticsLogEvent("Оценка приложения", "Не напоминать");
                    complete();
                });
        AlertDialog dialog = builder.create();
        DialogUtils.showImmersiveDialog(dialog);
    }
}
