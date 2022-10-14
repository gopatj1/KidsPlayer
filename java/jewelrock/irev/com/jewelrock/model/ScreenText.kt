package jewelrock.irev.com.jewelrock.model

import com.google.gson.annotations.SerializedName

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import jewelrock.irev.com.jewelrock.utils.sha1

open class ScreenText() : RealmObject() {
    @PrimaryKey
    var id: String = ""
    var text: String = ""
    var screenName: String = ""
    var isShown: Boolean = false

    constructor(text: String, screenName: String) : this() {
        id =  (text + screenName).sha1()
        this.text = text
        this.screenName = screenName
    }
}