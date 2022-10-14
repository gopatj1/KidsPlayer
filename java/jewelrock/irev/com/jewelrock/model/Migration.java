package jewelrock.irev.com.jewelrock.model;

import android.support.annotation.NonNull;

import java.util.Objects;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by Yuri Peremetov on 01.09.2017.
 */

public class Migration implements RealmMigration {
    @Override
    public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        if (oldVersion == 0) {
            schema.create("Motivator")
                    .addField("id", int.class, FieldAttribute.PRIMARY_KEY)
                    .addField("name", String.class)
                    .addField("videoId", int.class)
                    .addField("textMotivator", String.class)
                    .addField("position", int.class)
                    .addField("imagePhone", String.class)
                    .addField("imageTablet", String.class)
                    .addField("textInKey", String.class)
                    .addField("isShown", boolean.class)
                    .addField("isImageLoaded", boolean.class);

            oldVersion++;
        }

        if (oldVersion == 1) {
            Objects.requireNonNull(schema.get("Video")).addField("search", String.class)
                    .transform(obj -> obj.set("search", obj.getString("name").toLowerCase()));
            oldVersion++;
        }

        if (oldVersion == 2) {
            Objects.requireNonNull(schema.get("WelcomeScreen"))
                    .addField("screenType", String.class)
                    .addField("screenWeight", String.class)
                    .addField("url", String.class)
                    .addField("closeTime", String.class)
                    .addField("displayOrder", String.class)
                    .addField("audience", String.class)
                    .addField("lasting", String.class);
            oldVersion++;
        }

        if (oldVersion == 3) {
            Objects.requireNonNull(schema.get("Motivator"))
                    .addField("updateKey", long.class);
            oldVersion++;
        }

        if (oldVersion == 4) {
            Objects.requireNonNull(schema.get("UserSettings"))
                    .addField("lastAdTime", long.class)
                    .addField("showAd", boolean.class)
                    .addField("showMotivator", boolean.class)
                    .transform(obj -> {
                        obj.setBoolean("showAd", true);
                        obj.setBoolean("showMotivator", true);
                        obj.setLong("lastAdTime", 0);
                    });
            schema.create("InitSettings")
                    .addField("id", int.class, FieldAttribute.PRIMARY_KEY)
                    .addField("timerAdMob", String.class)
                    .addField("reklamaAdmob", String.class)
            ;
            schema.create("ScreenText")
                    .addField("id", String.class, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED)
                    .addField("text", String.class, FieldAttribute.REQUIRED)
                    .addField("screenName", String.class, FieldAttribute.REQUIRED)
            ;
            oldVersion++;
        }
        if (oldVersion == 5) {
            Objects.requireNonNull(schema.get("ScreenText"))
                    .addField("isShown", boolean.class, FieldAttribute.REQUIRED)
                    .transform(obj -> obj.setBoolean("isShown", false));
            oldVersion++;
        }
        if (oldVersion == 6) {
            Objects.requireNonNull(schema.get("Video"))
                    .addField("songSource", String.class)
                    .addField("songSourceDuration", String.class)
                    .addField("songSourceDurationSec", Integer.class)
                    .addField("songSourceSize", Long.class)
            ;
            Objects.requireNonNull(schema.get("UserSettings"))
                    .addField("showStreamingBauBay", boolean.class)
                    .addField("lastStreamingDate", int.class)
                    .addField("lastStreamingTime", int.class)
                    .addField("lastStreamingVideoId", int.class)
            ;
            oldVersion++;
        }
        if (oldVersion == 7) {
            Objects.requireNonNull(schema.get("Video"))
                    .addField("songCompressed", String.class)
                    .addField("songCompressedDuration", String.class)
                    .addField("songCompressedDurationSec", Integer.class)
                    .addField("videoCompressedDurationSec", Integer.class)
                    .addField("songCompressedSize", Long.class)
                    .addField("songPreview", String.class)
                    .addField("songPreviewDuration", String.class)
                    .addRealmListField("playlists", Objects.requireNonNull(schema.get("Playlist")))
            ;
            Objects.requireNonNull(schema.get("UserSettings"))
                    .addField("lastFragmentName", String.class)
                    .transform(obj -> obj.setString("lastFragmentName", "allMults"))
            ;
            oldVersion++;
        }
        if (oldVersion == 8) {
            Objects.requireNonNull(schema.get("UserSettings"))
                    .removeField("lastStreamingVideoId")
                    .addRealmListField("lastStreamingVideoId", Integer.class)
                    .transform(obj -> {
                        obj.setBoolean("showStreamingBauBay", true);
                        obj.setInt("lastStreamingDate", 0);
                    })
            ;
            oldVersion++;
        }
        if (oldVersion == 9) {
            Objects.requireNonNull(schema.get("UserSettings"))
                .addField("showMotivatorBauBay22", boolean.class)
                .transform(obj -> obj.setBoolean("showMotivatorBauBay22", false));
            oldVersion++;
        }
        if (oldVersion == 10) {
            Objects.requireNonNull(schema.get("UserSettings"))
                    .addField("showVideoSplash", boolean.class)
                    .addField("showAutoScrollSeekBar", boolean.class)
                    .transform(obj -> {
                        obj.setBoolean("showVideoSplash", true);
                        obj.setBoolean("showAutoScrollSeekBar", true);
                    });
            Objects.requireNonNull(schema.get("Video"))
                    .addField("idJSON", Integer.class)
                    .addField("videoLikes", Integer.class)
            ;
            Objects.requireNonNull(schema.get("Playlist"))
                    .addField("sectionId", int.class)
                    .addField("sectionName", String.class)
                    .addField("fontColor", String.class)
            ;
            schema.create("Section")
                    .addField("id", Integer.class, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED)
                    .addField("name", String.class)
                    .addRealmListField("playlists", Objects.requireNonNull(schema.get("Playlist")))
            ;
            schema.create("PaymentImagesAndOther")
                    .addField("id", Integer.class, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED)
                    .addField("name", String.class)
                    .addField("imagePhone", String.class)
                    .addField("imageTablet", String.class)
                    .addField("paidType", String.class)
                    .addField("isImageLoaded", boolean.class)
            ;
            oldVersion++;
        }
        if (oldVersion == 11) {
            Objects.requireNonNull(schema.get("Playlist"))
                    .addField("playlistSong", String.class)
                    .addRealmListField("songs", Objects.requireNonNull(schema.get("Video")))
            ;
            Objects.requireNonNull(schema.get("Video"))
                    .addField("songLikes", Integer.class)
            ;
            oldVersion++;
        }
        if (oldVersion == 12) {
            Objects.requireNonNull(schema.get("Playlist"))
                    .addField("price", Integer.class)
                    .addField("purchase", String.class)
                    .addField("imagePrice", String.class)
            ;
            Objects.requireNonNull(schema.get("Video"))
                    .addField("price", Integer.class)
                    .addField("purchase", String.class)
                    .addField("imagePrice", String.class)
                    .addField("isPurchase", boolean.class)
                    .addField("googleLink", String.class)
                    .addField("videoOfCurrentApplication", boolean.class)
            ;
            oldVersion++;
        }
        if (oldVersion == 13) {
            Objects.requireNonNull(schema.get("Motivator"))
                    .addField("sound", String.class)
            ;
            Objects.requireNonNull(schema.get("UserSettings"))
                    .addField("lastBannerTime", long.class)
                    .addField("lastBannerId", int.class)
                    .addField("bannerIsOpen", boolean.class)
            ;
            oldVersion++;
        }
        if (oldVersion == 14) {
            Objects.requireNonNull(schema.get("Playlist"))
                    .addField("search", String.class)
            ;
            schema.create("Notification")
                    .addField("id", Integer.class, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED)
                    .addField("titleName", String.class)
                    .addField("text", String.class)
                    .addField("time", Long.class)
                    .addField("isRead", boolean.class)
            ;
            oldVersion++;
        }
        if (oldVersion == 15) {
            Objects.requireNonNull(schema.get("Video"))
                    .addField("series", String.class)
            ;
            oldVersion++;
        }
    }
}