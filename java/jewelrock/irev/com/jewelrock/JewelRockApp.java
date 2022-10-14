package jewelrock.irev.com.jewelrock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import com.google.firebase.messaging.FirebaseMessaging;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import jewelrock.irev.com.jewelrock.model.Migration;

/**
 * Created by Юрий on 12.01.2017.
 */

public class JewelRockApp extends MultiDexApplication{
    @SuppressLint("StaticFieldLeak")
    private static Context sContext;


    public static Context getAppContext() {
        return sContext;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        MultiDex.install(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(16)
                .migration(new Migration())
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        FileDownloader.init(getApplicationContext());
        FileDownloadUtils.setDefaultSaveRootPath(getFilesDir().getAbsolutePath());

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        try { // add topic "all" for use notification by POST-request to firebase. Set in POST-body - to: "topic/all"
            FirebaseMessaging.getInstance().subscribeToTopic("all");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

}
