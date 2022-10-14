package jewelrock.irev.com.jewelrock.controller;

import io.realm.Realm;
import io.realm.RealmResults;
import jewelrock.irev.com.jewelrock.model.Error;

/**
 * Created by Юрий on 28.02.2017.
 */
public class ErrorController {

    static void registerError(int type, String message, boolean removeAfterShow) {
        Realm realm = Realm.getDefaultInstance();
        Error error = realm.where(Error.class).equalTo("type", type).findFirst();
        if (error == null) {
            realm.beginTransaction();
            error = realm.createObject(Error.class, type);
            error.setMessage(message);
            error.setRemoveAfterShow(removeAfterShow);
            error.setShown(false);
            realm.copyToRealmOrUpdate(error);
            realm.commitTransaction();
        }
        realm.close();
    }

    public static void removeError(int type) {
        Realm realm = Realm.getDefaultInstance();
        Error error = realm.where(Error.class).equalTo("type", type).findFirst();
        if (error != null) {
            realm.beginTransaction();
            error.deleteFromRealm();
            realm.commitTransaction();
        }
        realm.close();
    }

    public static void setShownError(int type) {
        Realm realm = Realm.getDefaultInstance();
        Error error = realm.where(Error.class).equalTo("type", type).findFirst();
        if (error != null) {
            realm.beginTransaction();
            error.setShown(true);
            realm.commitTransaction();
        }
        realm.close();
    }

    public static void removeAllErrors() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Error> errors = realm.where(Error.class).findAll();
        realm.beginTransaction();
        errors.deleteAllFromRealm();
        realm.commitTransaction();
        realm.close();
    }

    public static RealmResults<Error> getErrors(Realm realm) {
        return realm.where(Error.class).equalTo("isShown", false).findAll();
    }
}
