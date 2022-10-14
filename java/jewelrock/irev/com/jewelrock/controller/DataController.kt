package jewelrock.irev.com.jewelrock.controller

import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import com.bumptech.glide.RequestManager
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.util.FileDownloadUtils
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.Sort
import jewelrock.irev.com.jewelrock.BaseRealmActivity
import jewelrock.irev.com.jewelrock.BuildConfig
import jewelrock.irev.com.jewelrock.api.Api
import jewelrock.irev.com.jewelrock.model.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.ExecutionException
import kotlin.collections.ArrayList


/**
 * Created by Юрий on 22.12.2016.
 */

object DataController {
    const val PLAYLIST_SONG = "Песни"
    const val SECTION_VIDEO_PROKAT = "Видеопрокат"
    const val SECTION_TATARSKY_MULTS = "Мультфильмы Татарского"
    const val SECTION_ALL_MULTS = "Все мультфильмы"
    const val SECTION_ALL_SUJETS = "Все сюжеты"
    const val SECTION_MULTS = "Мультфильмы"
    const val SECTION_TIME_LINE = "Time Line"
    const val SECTION_PERSONS = "Персоналии"
    const val SECTION_VIPUSKI = "Выпуски"

    fun updatePlaylists(url: String): Single<Boolean> {
        Api.get.setUrl(url)
        return Api.get.playlists
                .flatMap { playlists ->
                    val realm = Realm.getDefaultInstance()
                    val keyUpdate = saveOrUpdatePlaylists(playlists, realm)
                    createOrUpdateVipuskiPlaylists(realm)
                    deleteOldPlaylistsAndVideos(realm, url, keyUpdate)
                    Single.just(true)
                }
    }

    fun updatePlaylistsBySections(url: String): Single<Boolean> {
        Api.get.setUrl(url)
        return Api.get.playlistsBySections
                .flatMap { sections ->
                    val realm = Realm.getDefaultInstance()
                    val keyUpdate = saveOrUpdatePlaylistsBySections(sections, realm)
                    createOrUpdateVipuskiPlaylists(realm)
                    deleteOldPlaylistsAndVideos(realm, url, keyUpdate)
                    Single.just(true)
                }
    }

    fun updateInitSettings(): Single<Boolean> {
        return Api.get.initSettings
                .map { map ->
                    val initSettings = InitSettings()
                    initSettings.reklamaAdmob = map["reklama_admob"]
                    initSettings.timerAdMob = map["timer_AdMob"]
                    var keyText = "text01"
                    var keyPlace = "place_text01"
                    var i = 1
                    val realm = Realm.getDefaultInstance()
                    while (map.containsKey(keyText)) {
                        val text = map[keyText]
                        val places = map[keyPlace]
                        val split = places?.split(", ".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
                        realm.beginTransaction()
                        split
                                ?.map { ScreenText(text!!, it) }
                                ?.forEach { realm.copyToRealmOrUpdate(it) }
                        realm.commitTransaction()
                        i++
                        keyText = String.format("text%02d", i)
                        keyPlace = String.format("place_text%02d", i)
                    }
                    InitSettingsController.saveInitSettings(realm, initSettings)
                    realm.close()
                    true
                }
    }

    fun updateVideo(url: String, playlistName: String, playlistId: Int): Single<Boolean> {
        Api.get.setUrl(url)
        return Api.get.video
                .map { videos ->
                    val realm = Realm.getDefaultInstance()
                    val playlist = Playlist()
                    playlist.name = playlistName
                    playlist.id = playlistId
                    playlist.position = 1

                    val v = RealmList<Video>()
                    v.addAll(videos)
                    playlist.videos = v
                    val playlists = ArrayList<Playlist>()
                    playlists.add(playlist)
                    realm.use { realm ->
                        if (videos.size > 0) {
                            val keyUpdate = saveOrUpdatePlaylists(playlists, realm)
                            deleteOldPlaylistsAndVideos(realm, url, keyUpdate)
                        }
                    }
                    true
                }
    }

    fun updateSong(): Single<Boolean> {
        return Api.get.song
                .map { videos ->
                    val realm = Realm.getDefaultInstance()
                    val playlist = Playlist()
                    // custom name, id, position. Don't locate on server in json file
                    playlist.name = PLAYLIST_SONG
                    playlist.id = -3 // -1 favourite, -2 streaming, -4 popular
                    playlist.position = -1

                    val v = RealmList<Video>()
                    v.addAll(videos)
                    playlist.videos = v
                    realm.use { realm ->
                        if (videos.size > 0) {
                            val keyUpdate = saveOrUpdatePlaylistBySong(playlist, realm)
                            deleteOldSongs(realm, keyUpdate)
                        }
                    }
                    true
                }
    }

    fun updateSongPlaylists(): Single<Boolean> {
        return Api.get.songPlaylists
                .flatMap { songPlaylists ->
                    val realm = Realm.getDefaultInstance()
                    val keyUpdate = saveOrUpdatePlaylistBySongPlaylists(songPlaylists, realm)
                    deleteOldSongs(realm, keyUpdate)
                    Single.just(true)
                }
    }

    fun updateWelcomeScreens(glide: RequestManager, isBigScreen: Boolean): Single<Boolean> {
        Api.get.setUrl(BuildConfig.BASE_URL)
        return Api.get.welcomeScreens
                .map { welcomeScreens ->
                    var result = true

                    for (p in welcomeScreens) {
                        val future = glide
                                .load(if (isBigScreen) p.imageTablet else p.imagePhone)
                                .downloadOnly(500, 500)
                        try {
                            future.get()
                            Log.d("!!!", "WS loaded :" + if (isBigScreen) p.imageTablet else p.imagePhone)
                            p.isImageLoaded = true
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                            result = false
                            break
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                            result = false
                            break
                        }

                    }
                    val realm = Realm.getDefaultInstance()
                    saveOrUpdateWelcomeScreens(welcomeScreens, realm)
                    realm.close()
                    result
                }
    }

    fun updatePaymentImagesAndOther(glide: RequestManager, isBigScreen: Boolean): Single<Boolean> {
        return Api.get.paymentImagesAndOther
                .map { paymentImagesAndOther ->
                    var result = true

                    for (p in paymentImagesAndOther) {
                        val future = glide
                                .load(if (isBigScreen) p.imageTablet else p.imagePhone)
                                .downloadOnly(500, 500)
                        try {
                            future.get()
                            Log.d("!!!", "Payment Images And Other loaded :" + if (isBigScreen) p.imageTablet else p.imagePhone)
                            p.isImageLoaded = true
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                            result = false
                            break
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                            result = false
                            break
                        }

                    }
                    val realm = Realm.getDefaultInstance()
                    saveOrUpdatePaymentImagesAndOther(paymentImagesAndOther, realm)
                    realm.close()
                    result
                }
    }

    fun updateMotivators(glide: RequestManager, isBigScreen: Boolean): Single<Boolean> {
        return Api.get.motivators
                .map { motivators ->
                    var result = true

                    for (m in motivators) {
                        val future = glide
                                .load(if (isBigScreen) m.imageTablet else m.imagePhone)
                                .downloadOnly(500, 500)
                        try {
                            future.get()
                            Log.d("!!!", "Motivator loaded :" + if (isBigScreen) m.imageTablet else m.imagePhone)
                            m.isImageLoaded = true
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                            result = false
                            break
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                            result = false
                            break
                        }

                    }
                    saveOrUpdateMotivators(motivators)

                    result
                }
    }

    fun getPaymentImagesAndOther(realm: Realm): List<PaymentImagesAndOther> {
        return ArrayList(realm.where(PaymentImagesAndOther::class.java)
                .findAll()
                .sort("id", Sort.ASCENDING))
    }

    fun getUnshownWelcomeScreens(realm: Realm): List<WelcomeScreen> {
        return ArrayList(realm.where(WelcomeScreen::class.java)
                .notEqualTo("screenType", "advertising")
                .equalTo("isShown", false)
                .equalTo("isImageLoaded", true)
                .findAll()
                .sort("position", Sort.ASCENDING))
    }

    fun getHelpWelcomeScreens(realm: Realm): List<WelcomeScreen> {
        return ArrayList(realm.where(WelcomeScreen::class.java)
                .notEqualTo("screenType", "advertising")
                .equalTo("isImageLoaded", true)
                .findAll()
                .sort("position", Sort.ASCENDING))
    }

    fun getUnshownAdvertisingScreens(realm: Realm, isPaid: Boolean): List<WelcomeScreen> {
        val welcomeScreens = realm.where(WelcomeScreen::class.java)
                .equalTo("screenType", "advertising")
                .equalTo("isShown", false)
                .equalTo("displayOrder", "consistently")
                .equalTo("isImageLoaded", true)
                .beginGroup()
                .equalTo("audience", "all")
                .or()
                .equalTo("audience", if (isPaid) "bought" else "notbought")
                .endGroup()
                .findAll()
                .sort("position", Sort.ASCENDING)

        if (!welcomeScreens.isEmpty()) return realm.copyFromRealm(welcomeScreens)

        var rotationAdvert = getNotShownRotationAdvert(realm, isPaid)
        if (rotationAdvert.isEmpty()) {
            resetRotationAdvert(realm, isPaid)
            rotationAdvert = getNotShownRotationAdvert(realm, isPaid)
        }

        return realm.copyFromRealm(rotationAdvert)
    }

    private fun resetRotationAdvert(realm: Realm, isPaid: Boolean) {
        val allRotationAdvert = getAllRotationAdvert(realm, isPaid)
        for (ws in allRotationAdvert) {
            realm.executeTransaction { r ->
                ws.isShown = false
                r.copyToRealmOrUpdate(ws)
            }
        }
    }

    private fun getNotShownRotationAdvert(realm: Realm, isPaid: Boolean): RealmResults<WelcomeScreen> {
        return realm.where(WelcomeScreen::class.java)
                .equalTo("screenType", "advertising")
                .equalTo("isShown", false)
                .equalTo("displayOrder", "rotation")
                .equalTo("isImageLoaded", true)
                .beginGroup()
                .equalTo("audience", "all")
                .or()
                .equalTo("audience", if (isPaid) "bought" else "notbought")
                .endGroup()
                .findAll()
                .sort("screenWeight", Sort.ASCENDING)
    }

    private fun getAllRotationAdvert(realm: Realm, isPaid: Boolean): RealmResults<WelcomeScreen> {
        return realm.where(WelcomeScreen::class.java)
                .equalTo("screenType", "advertising")
                .equalTo("displayOrder", "rotation")
                .equalTo("isImageLoaded", true)
                .beginGroup()
                .equalTo("audience", "all")
                .or()
                .equalTo("audience", if (isPaid) "bought" else "notbought")
                .endGroup()
                .findAll()
                .sort("screenWeight", Sort.DESCENDING)
    }

    fun getUnshownMotivators(realm: Realm): RealmResults<Motivator> {
        return realm.where(Motivator::class.java)
                .equalTo("isShown", false)
                .equalTo("isImageLoaded", true)
                .findAll()
    }

    fun resetMotivators() {
        val realm = Realm.getDefaultInstance()
        val all = realm.where(Motivator::class.java).findAll()
        for (m in all) {
            realm.executeTransaction { r ->
                m.isShown = false
                r.copyToRealmOrUpdate(m)
            }
        }
        realm.close()
    }

    fun hasNotLoadedVideos(): Boolean {
        val realm = Realm.getDefaultInstance()
        val videoLoadingProgress = realm.where(Video::class.java).lessThan("videoLoadingProgress", 0).findAll().size > 0
        realm.close()
        return videoLoadingProgress
    }

    fun setWelcomeScreenShown(welcomeScreen: WelcomeScreen) {
        val realm = Realm.getDefaultInstance()
        val ws = realm.where(WelcomeScreen::class.java).equalTo("id", welcomeScreen.id).findFirst()
        realm.beginTransaction()
        ws!!.isShown = true
        realm.copyToRealmOrUpdate(ws)
        realm.commitTransaction()
        realm.close()
    }

    fun setMotivatorShown(motivator: Motivator, isShown: Boolean) {
        val realm = Realm.getDefaultInstance()
        val ws = realm.where(Motivator::class.java).equalTo("id", motivator.id).findFirst()
        realm.beginTransaction()
        ws!!.isShown = isShown
        realm.copyToRealmOrUpdate(ws)
        realm.commitTransaction()
        realm.close()
    }

    fun setWelcomeScreenError(id: Int) {
        val realm = Realm.getDefaultInstance()
        val ws = realm.where(WelcomeScreen::class.java).equalTo("id", id).findFirst()
        realm.beginTransaction()
        ws!!.isShown = false
        realm.copyToRealmOrUpdate(ws)
        realm.commitTransaction()
        realm.close()
    }

    fun startDownloadingPreviews() {
        val realm = Realm.getDefaultInstance()
        val videos = realm.where(Video::class.java).findAll()
        val videoDownloadListener = PreviewDownloadListener()

        val error = realm.where(Error::class.java).equalTo("type", Error.NO_DISK_SPACE_FOR_PREVIEW).findFirst()
        if (error != null) {
            Log.d("!!!", "No disk space for preview. Loading ignored.")
            realm.close()
            return
        }
        for (v in videos) {
            if (!isPreviewLoaded(v)) {
                val url = v.videoPreview
                if (FileDownloader.getImpl().getStatus(url, FileDownloadUtils.getDefaultSaveFilePath(url)) <= 0) {
                    FileDownloader.getImpl()
                            .create(v.videoPreview)
                            .setListener(videoDownloadListener)
                            .asInQueueTask()
                            .enqueue()
                }
            }
        }
        FileDownloader.getImpl().start(videoDownloadListener, true)
        realm.close()
    }

    fun restartDownloadingVideos() {
        val realm = Realm.getDefaultInstance()
        val videos = realm.where(Video::class.java)
                .greaterThanOrEqualTo("videoLoadingProgress", 0)
                .lessThan("videoLoadingProgress", 100)
                .notEqualTo("playlist.sectionName", SECTION_VIDEO_PROKAT)
                .notEqualTo("playlist.sectionName", SECTION_TATARSKY_MULTS)
                .findAll()
        startDownloadingVideos(videos, -2) // -2 will be ignored
        realm.close()
    }

    fun startDownloadingVideos(videos: List<Video>, quality: Int) {
        var quality = quality
        val videoDownloadListener = VideoDownloadListener()
        for (v in videos) {
            if (quality == -2)
                quality = v.quality // ignore quality param for started loading video (need retry loading)

            if (v.quality != quality) {
                cancelDownloadingVideo(v)
                removeDownloadedVideo(v)
            }
            val url = getLoadingVideoUrl(quality, v) ?: continue
            if (FileDownloader.getImpl().getStatus(url, FileDownloadUtils.getDefaultSaveFilePath(url)) <= 0) {
                val task = FileDownloader.getImpl().create(url).setTag(v.id)
                task.setCallbackProgressMinInterval(5000)
                        .setListener(videoDownloadListener)
                        .setAutoRetryTimes(1)
                        .asInQueueTask()
                        .enqueue()
                setVideoLoadingPercent(task, 0, quality)
            }
        }
        FileDownloader.getImpl().start(videoDownloadListener, false)
    }

    fun removeDownloadedVideos(videos: Collection<Video>) {
        val realm = Realm.getDefaultInstance()
        for (v in videos) {
            realm.beginTransaction()
            v.videoLoadingProgress = -1
            deleteFile(v.videoFile)
            v.videoFile = null
            v.quality = -1
            realm.commitTransaction()
        }
        realm.close()
    }

    private fun removeDownloadedVideo(v: Video) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        v.videoLoadingProgress = -1
        deleteFile(v.videoFile)
        v.videoFile = null
        v.quality = -1
        realm.commitTransaction()
        realm.close()
    }

    fun cancelDownloadingVideo(task: BaseDownloadTask) {
        val realm = Realm.getDefaultInstance()
        val videos = realm.where(Video::class.java)
                .equalTo("videoSource", task.url)
                .or()
                .equalTo("videoCompressed", task.url)
                .findAll()
        cancelDownloadingVideos(videos)
        realm.close()
    }

    fun cancelDownloadingVideo(video: Video) {
        val videos = ArrayList<Video>()
        videos.add(video)
        cancelDownloadingVideos(videos)
    }

    fun cancelDownloadingVideos(videos: Collection<Video>) {
        for (v in videos) {
            val url = getLoadingVideoUrl(v) ?: continue
            val task = FileDownloader.getImpl().create(url)
            FileDownloader.getImpl().pause(FileDownloadUtils.generateId(url, FileDownloadUtils.getDefaultSaveFilePath(url)))
            setVideoLoadingPercent(task, -1, -1)
            deleteFile(FileDownloadUtils.getTempPath(FileDownloadUtils.getDefaultSaveFilePath(url)))
        }
    }

    private fun getLoadingVideoUrl(v: Video): String? {
        val quality = v.quality
        return if (quality < 0) null else getLoadingVideoUrl(quality, v)
    }

    private fun getLoadingVideoUrl(quality: Int, v: Video): String? {
        if (quality < 0 || quality > 1) throw RuntimeException("Video quality must be 0 or 1")
        return if (quality == 1) v.videoSource else v.videoCompressed
    }

    @JvmOverloads
    fun setVideoLoadingPercent(task: BaseDownloadTask, percent: Int, quality: Int = -2) {
        val realm = Realm.getDefaultInstance()
        val videos = realm.where(Video::class.java)
                .equalTo("videoSource", task.url)
                .or()
                .equalTo("videoCompressed", task.url)
                .findAll()
        realm.beginTransaction()
        for (v in videos) {
            v.videoLoadingProgress = percent
            if (quality != -2) v.quality = quality
        }
        realm.commitTransaction()
        realm.close()
    }

    fun setVideoLoaded(task: BaseDownloadTask) {
        val realm = Realm.getDefaultInstance()
        val videos = realm.where(Video::class.java)
                .equalTo("videoSource", task.url)
                .or()
                .equalTo("videoCompressed", task.url)
                .findAll()
        realm.beginTransaction()
        for (v in videos) {
            v.videoFile = task.targetFilePath
            v.videoLoadingProgress = 100
        }
        realm.commitTransaction()
        realm.close()
    }

    fun setPreviewLoaded(task: BaseDownloadTask) {
        val realm = Realm.getDefaultInstance()
        val videos = realm.where(Video::class.java)
                .equalTo("videoPreview", task.url)
                .findAll()
        realm.beginTransaction()
        for (v in videos) {
            v.previewFile = task.targetFilePath
        }
        realm.commitTransaction()
        realm.close()
    }

    fun isPreviewLoaded(video: Video): Boolean {
        val file = File(FileDownloadUtils.getDefaultSaveFilePath(video.videoPreview))
        return file.exists()
    }

    fun isVideoSDLoaded(video: Video): Boolean {
        val file = File(FileDownloadUtils.getDefaultSaveFilePath(video.videoCompressed))
        return file.exists()
    }

    fun isVideoHDLoaded(video: Video): Boolean {
        val file = File(FileDownloadUtils.getDefaultSaveFilePath(video.videoSource))
        return file.exists()
    }

    @JvmOverloads
    fun getMainPlaylists(realm: Realm, sortBy: String = "position", text: String = ""): RealmResults<Playlist> {
        val query = realm.where(Playlist::class.java)
                .contains("image", BuildConfig.BASE_URL)
                .greaterThanOrEqualTo("id", 0) // not include songs playlists which have negative id
        if (BuildConfig.HAS_SECTIONS)
                query.beginGroup().equalTo("sectionName", SECTION_ALL_SUJETS)
                .or().equalTo("sectionName", SECTION_ALL_MULTS)
                .or().equalTo("sectionName", SECTION_MULTS).endGroup()
        if (!TextUtils.isEmpty(text)) query.contains("search", text.toLowerCase())
        return query.findAll().sort(sortBy)
    }

    fun getPlaylistsTimeline(realm: Realm): RealmResults<Playlist> {
        return realm.where(Playlist::class.java)
                .equalTo("sectionName", SECTION_TIME_LINE)
                .findAll().sort("position")
    }

    fun getPlaylistsProkat(realm: Realm): RealmResults<Playlist> {
        return realm.where(Playlist::class.java)
                .equalTo("sectionName", SECTION_VIDEO_PROKAT)
                .or()
                .equalTo("sectionName", SECTION_TATARSKY_MULTS)
                .findAll().sort("position")
    }

    fun getLoadingVideos(realm: Realm): RealmResults<Video> {
        return realm.where(Video::class.java).greaterThanOrEqualTo("videoLoadingProgress", 0).findAll()
    }

    fun getPlaylist(realm: Realm, id: Int?): Playlist? {
        return realm.where(Playlist::class.java).equalTo("id", id).findFirst()
    }

    fun getVideoFromPlayList(realm: Realm, id: Int?): RealmResults<Video> {
        val queryPlaylist = realm.where(Playlist::class.java)
                .equalTo("id", id)
                .findFirst()
        return queryPlaylist!!.videos.sort("position")
    }

    fun getVideoForMotivator(id: Int): Video? {
        val realm = Realm.getDefaultInstance()
        val video = realm.where(Video::class.java).equalTo("idJSON", id).findFirst()
        realm.close()
        return video
    }

    @JvmOverloads
    fun getMainVideos(realm: Realm, sortBy: String = "position", text: String = ""): RealmResults<Video>? {
        val query = realm.where(Video::class.java)
                .contains("image", BuildConfig.BASE_URL)
                .greaterThanOrEqualTo("id", 0) // not include songs which have negative id
                .notEqualTo("playlist.sectionName", SECTION_VIDEO_PROKAT)
                .notEqualTo("playlist.sectionName", SECTION_TATARSKY_MULTS)
        if (!TextUtils.isEmpty(text)) query.contains("search", text.toLowerCase())
        return query.findAll().sort(sortBy)
    }

    fun getVideoByID(realm: Realm, id: Int): Video? {
        return realm.where(Video::class.java).equalTo("id", id).findFirst()
    }

    @JvmOverloads
    fun getVideoFromPrevPlayLists(realm: Realm, id: Int?, isSongs: Boolean = false): RealmList<Video> {
        val prevPlaylist = realm.where(Playlist::class.java)
                .lessThan("position", getPlaylist(realm, id)!!.position)
                .contains("image", BuildConfig.BASE_URL)
            if (isSongs) {
                prevPlaylist.lessThan("id", 0)
            } else {
                prevPlaylist.notEqualTo("sectionName", SECTION_VIDEO_PROKAT)
                prevPlaylist.notEqualTo("sectionName", SECTION_TATARSKY_MULTS)
            }
        val allPrevVideos = RealmList<Video>()
        for (p in prevPlaylist.findAll().sort("position")) {
            p.videos.sort("position")
            allPrevVideos.addAll(p.videos)
        }
        return allPrevVideos
    }

    @JvmOverloads
    fun getVideoFromNextPlayLists(realm: Realm, id: Int?, isSongs: Boolean = false): RealmList<Video> {
        val nextPlaylist = realm.where(Playlist::class.java)
                .greaterThan("position", getPlaylist(realm, id)!!.position)
                .contains("image", BuildConfig.BASE_URL)
        if (isSongs) {
            nextPlaylist.lessThan("id", 0)
        } else {
            nextPlaylist.notEqualTo("sectionName", SECTION_VIDEO_PROKAT)
            nextPlaylist.notEqualTo("sectionName", SECTION_TATARSKY_MULTS)
        }
        val allNextVideos = RealmList<Video>()
        for (p in nextPlaylist.findAll().sort("position")) {
            p.videos.sort("position")
            allNextVideos.addAll(p.videos)
        }
        return allNextVideos
    }

    @JvmOverloads
    fun getFavoriteVideos(realm: Realm, sortBy: String = "favoriteTime", text: String = ""): RealmResults<Video> {
        val query = realm.where(Video::class.java)
                .contains("image", BuildConfig.BASE_URL)
                .equalTo("isFavorite", true)
        if (!TextUtils.isEmpty(text)) query.contains("search", text.toLowerCase())
        return if (TextUtils.isEmpty(text)) query.findAll().sort(sortBy, Sort.DESCENDING)
               else query.findAll().sort(sortBy)
    }

    fun getPopularVideos(realm: Realm): RealmResults<Video>? {
        return realm.where(Video::class.java)
                .isNotNull("idJSON")
                .contains("image", BuildConfig.BASE_URL)
                // not include songs playlists which have negative id, end videos from other sections
                .notEqualTo("playlist.sectionName", SECTION_VIDEO_PROKAT)
                .notEqualTo("playlist.sectionName", SECTION_TATARSKY_MULTS)
                .notEqualTo("playlist.sectionName", SECTION_PERSONS)
                .notEqualTo("playlist.sectionName", SECTION_VIPUSKI)
                .greaterThanOrEqualTo("id", 0)
                .findAllSorted("videoLikes", Sort.DESCENDING)
    }

    fun getProkatVideos(realm: Realm): RealmResults<Video>? {
        return realm.where(Video::class.java)
                .equalTo("playlist.sectionName", SECTION_VIDEO_PROKAT)
                .or()
                .equalTo("playlist.sectionName", SECTION_TATARSKY_MULTS)
                .findAllSorted("videoLikes", Sort.DESCENDING)
    }

    @JvmOverloads
    fun getSongVideos(realm: Realm, sortBy: String = "position", text: String = ""): RealmResults<Video>? {
        val query = realm.where(Video::class.java)
                .contains("image", BuildConfig.BASE_URL)
                .lessThan("id", 0) // songs playlists which have negative id
        if (!TextUtils.isEmpty(text)) query.contains("search", text.toLowerCase())
        return query.findAll().sort(sortBy)
    }

    @JvmOverloads
    fun getSongPlaylists(realm: Realm, sortBy: String = "position", text: String = ""): RealmResults<Playlist>? {
        val query = realm.where(Playlist::class.java)
                .lessThan("id", 0) // songs playlists which have negative id
                .contains("image", BuildConfig.BASE_URL)
        if (!TextUtils.isEmpty(text)) query.contains("search", text.toLowerCase())
        return query.findAll().sort(sortBy)
    }

    @JvmOverloads
    fun getVipuskiPlaylists(realm: Realm, text: String = ""): RealmResults<Playlist>? {
        val query = realm.where(Playlist::class.java)
                .equalTo("sectionName", SECTION_VIPUSKI)
        if (!TextUtils.isEmpty(text)) query.contains("search", text.toLowerCase())
        return query.findAll().sort("position")
    }

    @JvmOverloads
    fun getPersonsPlaylists(realm: Realm, sortBy: String = "position", text: String = ""): RealmResults<Playlist>? {
        val query = realm.where(Playlist::class.java)
                .equalTo("sectionName", SECTION_PERSONS)
        if (!TextUtils.isEmpty(text)) query.contains("search", text.toLowerCase())
        return query.findAll().sort(sortBy)
    }

    fun setStreamingVideosById(realm: Realm, id: RealmList<Int>): RealmResults<Video> {
        return realm.where(Video::class.java)
                .equalTo("id", id[0])
                .or().equalTo("id", id[1])
                .or().equalTo("id", id[2])
                .or().equalTo("id", id[3])
                .findAll()
    }

    fun setRandomStreamingVideos(realm: Realm): RealmResults<Video> {
        val userSettings = UserSettingsController.loadUserSettings(realm)
        val indexStreamingVideos: ArrayList<Int> = ArrayList()
        val urlVideos: ArrayList<String> = ArrayList()
        var allVideos: RealmResults<Video>

        if (!userSettings.isPaid) {
            allVideos = realm.where(Video::class.java)
                    .greaterThan("id", -1)
                    .contains("image", BuildConfig.BASE_URL)
                    .isNull("price")
                    .findAll()
                    .sort("id", Sort.ASCENDING)
            for (i in 0 until allVideos.size)
                indexStreamingVideos.add(allVideos[i]!!.id)
            while (true) {
                var isUniqueId = true
                indexStreamingVideos.shuffle()
                for (i in 0..3)
                    for (m in 0..3) {
                        try {
                            if (indexStreamingVideos[i] == userSettings.lastStreamingVideoId[m])
                                isUniqueId = false
                        } catch (e: Exception) {
                        }
                    }
                if (isUniqueId) break
            }
        } else {
            for (url in BuildConfig.URL_ARRAY) {
                if (BuildConfig.BASE_URL == "http://bis.irev.ru" &&
                        (url == "http://tk.irev.ru" || url == "http://tsb.irev.ru")) continue
                urlVideos.add(url)
            }
            urlVideos.add(BuildConfig.BASE_URL)
            urlVideos.shuffle()

            for (i in 0..3) {
                allVideos = realm.where(Video::class.java)
                        .greaterThan("id", -1)
                        .contains("image", urlVideos[i])
                        .isNull("price")
                        .findAll()
                        .sort("id", Sort.ASCENDING)
                indexStreamingVideos.add(allVideos[Random().nextInt(allVideos.size)]!!.id)
                try {
                    for (m in 0..3) {
                        while (indexStreamingVideos[i] == userSettings.lastStreamingVideoId[m]) {
                            indexStreamingVideos.remove(i)
                            indexStreamingVideos.add(allVideos[Random().nextInt(allVideos.size)]!!.id)
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }

        return realm.where(Video::class.java)
                .equalTo("id", indexStreamingVideos[0])
                .or().equalTo("id", indexStreamingVideos[1])
                .or().equalTo("id", indexStreamingVideos[2])
                .or().equalTo("id", indexStreamingVideos[3])
                .findAll()
    }

    @JvmOverloads
    fun getStreamingVideos(realm: Realm, sortBy: String = "", text: String = ""): RealmResults<Video> {
        val userSettings = UserSettingsController.loadUserSettings(realm)
        val query = realm.where(Video::class.java).beginGroup()
                .equalTo("id", userSettings.lastStreamingVideoId[0])
                .or().equalTo("id", userSettings.lastStreamingVideoId[1])
                .or().equalTo("id", userSettings.lastStreamingVideoId[2])
                .or().equalTo("id", userSettings.lastStreamingVideoId[3])
                .endGroup()
        if (!TextUtils.isEmpty(text)) query.contains("search", text.toLowerCase())
        return if (TextUtils.isEmpty(text)) query.findAll()
               else query.findAll().sort(sortBy)
    }

    fun setFavorite(activity: BaseRealmActivity, video: Video, isFavorite: Boolean) {
        activity.analyticsLogEvent("Мультфильмы", if (isFavorite) "Добавление в любимые" else "Удаление из любимых",
                video.name.replace("\\", "").replace("n", " "))
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        val v = realm.where(Video::class.java).equalTo("id", video.id).findFirst()
        v!!.isFavorite = isFavorite
        val favoriteTime = System.currentTimeMillis()
        v.favoriteTime = favoriteTime
        realm.commitTransaction()
        realm.close()
    }

    fun setPurchase(video: Video, isPurchase: Boolean) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        val v = realm.where(Video::class.java).equalTo("id", video.id).findFirst()
        v!!.isPurchase = isPurchase
        realm.commitTransaction()
        realm.close()
    }

    fun setPurchaseToAllProkatVideos(isPurchase: Boolean) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        val p = realm.where(Playlist::class.java).equalTo("sectionName", SECTION_TATARSKY_MULTS).findFirst()
        for (video in p!!.videos)
            video!!.isPurchase = isPurchase
        realm.commitTransaction()
        realm.close()
    }

    fun setVideoLikes(video: Video, isManyLikes: Boolean, realm: Realm) {
        if (!BuildConfig.HAS_POPULAR_BUTTON) return
        val idJSON = video.idJSON
        object : AsyncTask<Void, String, String>() {
            override fun doInBackground(vararg voids: Void): String {
                try {
                    if (!isManyLikes) doGet(BuildConfig.BASE_URL + "/api/video/setlikes/$idJSON")
                        else doGet(BuildConfig.BASE_URL + "/api/video/setlikesmany/$idJSON")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return ""
            }
        }.execute()
        realm.beginTransaction()
        try {
            if (!isManyLikes) video.videoLikes++
            else video.videoLikes += 3
            realm.copyToRealm(video)
        } catch (e: Exception) {}
        realm.commitTransaction()
    }


    @Throws(Exception::class)
    fun doGet(url: String) {
        val obj = URL(url)
        val connection = obj.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.inputStream
        return
    }

    fun preloadImages(glide: RequestManager): Observable<String> {
        return Observable.create { subscriber ->
            val realm = Realm.getDefaultInstance()
            val playlists = realm.where(Playlist::class.java).findAll()
            for (p in playlists) {
                val future = glide
                        .load(p.image)
                        .downloadOnly(500, 500)
                try {
                    future.get()
                    subscriber.onNext(p.image)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }

            }
            val videos = realm.where(Video::class.java).findAll()
            for (v in videos) {
                val future = glide
                        .load(v.image)
                        .downloadOnly(660, 496)
                try {
                    future.get()
                    subscriber.onNext(v.image)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }

            }
            realm.close()
            subscriber.onComplete()
        }
    }

    fun addNotification(notification: Map<String, String>) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        var customId = 0
        var n = realm.where(Notification::class.java).equalTo("id", customId).findFirst()
        while (n != null) {
            customId++
            n = realm.where(Notification::class.java).equalTo("id", customId).findFirst()
        }
        if (n == null) n = realm.createObject(Notification::class.java, customId)
        n!!.titleName = notification["title"]
        n.text = notification["text"]
        n.time = System.currentTimeMillis()
        n.isRead = false
        realm.copyToRealmOrUpdate(n)
        realm.commitTransaction()
        realm.close()
    }

    fun getNotifications(): RealmResults<Notification>? {
        val realm = Realm.getDefaultInstance()
        return realm.where(Notification::class.java).findAll().sort("time", Sort.DESCENDING)
    }

    fun deleteOldNotifications() {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        for (n in realm.where(Notification::class.java).findAll())
            if (n.time < System.currentTimeMillis() - 2592000000) // 30 days in ms
                n.deleteFromRealm()
        realm.commitTransaction()
        realm.close()
    }

    fun countOfNonReadNotifications(): Int {
        val realm = Realm.getDefaultInstance()
        return try {
            realm.where(Notification::class.java).equalTo("isRead", false).findAll().size
        } catch (e: Exception) {0}
    }

    fun setAllNotificationRead() {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        for (n in realm.where(Notification::class.java).findAll())
            n.isRead = true
        realm.commitTransaction()
        realm.close()
    }

    // Private methods
    // *********************************************************************************************

    private fun deleteOldPlaylistsAndVideos(realm: Realm, url: String, key: Long) {
        realm.beginTransaction()
        val resultOldPlaylists = realm.where(Playlist::class.java)
                .notEqualTo("updateKey", key)
                .greaterThanOrEqualTo("id", 0)
                .contains("image", url)
                .findAll()
        resultOldPlaylists.deleteAllFromRealm()

        val resultsOldVideo = realm.where(Video::class.java)
                .notEqualTo("updateKey", key)
                .greaterThanOrEqualTo("id", 0)
                .contains("image", url)
                .findAll()
        // удалить файлы скачанных видео
        for (v in resultsOldVideo) deleteVideoFiles(v)
        resultsOldVideo.deleteAllFromRealm()
        realm.commitTransaction()
    }

    private fun deleteOldSongs(realm: Realm, key: Long) {
        realm.beginTransaction()
        val resultOldPlaylists = realm.where(Playlist::class.java)
                .notEqualTo("updateKey", key)
                .lessThan("id", 0)
                .findAll()
        resultOldPlaylists.deleteAllFromRealm()

        val resultsOldVideo = realm.where(Video::class.java)
                .notEqualTo("updateKey", key)
                .lessThan("id", 0)
                .findAll()
        // удалить файлы скачанных видео
        for (v in resultsOldVideo) deleteVideoFiles(v)
        resultsOldVideo.deleteAllFromRealm()
        realm.commitTransaction()
    }

    private fun deleteVideoFiles(v: Video) {
        deleteFile(FileDownloadUtils.getDefaultSaveFilePath(v.videoPreview))
        deleteFile(FileDownloadUtils.getDefaultSaveFilePath(v.videoCompressed))
        deleteFile(FileDownloadUtils.getDefaultSaveFilePath(v.videoSource))
    }

    private fun deleteFile(fileName: String?) {
        if (fileName == null) return
        val file = File(fileName)
        if (file.exists()) file.delete()
    }

    private fun saveOrUpdatePlaylists(playlists: List<Playlist>, realm: Realm): Long {
        val key = System.currentTimeMillis()
        var customVideoId = 0
        for (playlist in playlists) {
            realm.beginTransaction()
            var customIdPlaylistScale = 500
            if (playlist.image != null && playlist.image.contains(BuildConfig.BASE_URL))
                customIdPlaylistScale = 0
            var p = realm.where(Playlist::class.java).equalTo("id", playlist.id + customIdPlaylistScale).findFirst()
            if (p == null) p = realm.createObject(Playlist::class.java, playlist.id + customIdPlaylistScale)
            p!!.name = playlist.name
            p.image = playlist.image
            p.position = playlist.position
            p.playlistVideo = playlist.playlistVideo
            p.fontColor = playlist.fontColor
            p.search = playlist.name.toLowerCase()
            p.updateKey = key

            if (playlist.videos != null) {
                val videos = RealmList<Video>()
                for (video in playlist.videos) {
                    var v = if (video.image.contains(BuildConfig.BASE_URL))
                                realm.where(Video::class.java)
                                    .equalTo("id", customVideoId).or()
                                    .equalTo("idJSON", video.id).findFirst()
                            else realm.where(Video::class.java).equalTo("id", customVideoId).findFirst()
                    if (v == null) {
                        v = realm.createObject(Video::class.java, customVideoId)
                        v.videoLoadingProgress = -1
                        v.quality = -1
                        customVideoId++
                    } else {
                        val videosOld = realm.where(Video::class.java).findAll()
                        while (true) {
                            if (customVideoId > videosOld.size - 1 ||
                                    (video.name == videosOld[customVideoId]!!.name &&
                                            video.image == videosOld[customVideoId]!!.image)) {
                                 v = if (video.image.contains(BuildConfig.BASE_URL))
                                    realm.where(Video::class.java)
                                        .equalTo("id", customVideoId).or()
                                        .equalTo("idJSON", video.id).findFirst()
                                else realm.where(Video::class.java).equalTo("id", customVideoId).findFirst()
                                if (v == null) {
                                    v = realm.createObject(Video::class.java, customVideoId)
                                    v.videoLoadingProgress = -1
                                    v.quality = -1
                                }
                                customVideoId++
                                break
                            } else customVideoId++
                        }
                    }
                    v!!.updateKey = key
                    if (video.image != null && video.image.contains(BuildConfig.BASE_URL))
                        v.idJSON = video.id
                    else v.idJSON = null
                    v.videoLikes = video.videoLikes
                    v.position = video.position
                    v.image = video.image
                    v.name = video.name
                    v.type = video.type
                    v.shareImage = video.shareImage
                    v.shareLink = video.shareLink
                    v.shareText = video.shareText
                    v.videoCompressed = video.videoCompressed
                    v.videoCompressedDuration = video.videoCompressedDuration
                    v.videoPreview = video.videoPreview
                    v.videoPreviewDuration = video.videoPreviewDuration
                    v.videoSource = video.videoSource
                    v.videoSourceDuration = video.videoSourceDuration
                    v.youtubeLink = video.youtubeLink
                    v.videoSourceDurationSec = video.videoSourceDurationSec
                    v.videoCompressedDurationSec = video.videoCompressedDurationSec
                    v.videoSourceSize = video.videoSourceSize
                    v.videoCompressedSize = video.videoCompressedSize
                    v.googleLink = video.googleLink
                    v.series = video.series
                    v.videoOfCurrentApplication = video.videoOfCurrentApplication
                    v.search = video.name.toLowerCase()
                    if (!v.containPlaylist(p))
                        v.addPlaylist(p)
                    v.playlist = v.getPlaylist(0)
                    videos.add(v)
                }
                p.videos = videos
            }
            realm.copyToRealmOrUpdate(p)
            realm.commitTransaction()
        }
        return key
    }

    private fun saveOrUpdatePlaylistsBySections(sections: List<Section>, realm: Realm): Long {
        val key = System.currentTimeMillis()
        var customVideoId = 0
        for (section in sections) {
            if (section.playlists != null) {
                for (playlist in section.playlists) {
                    realm.beginTransaction()
                    var customIdPlaylistScale = 500
                    if (playlist.image != null && playlist.image.contains(BuildConfig.BASE_URL))
                        customIdPlaylistScale = 0
                    var p = realm.where(Playlist::class.java).equalTo("id", playlist.id + customIdPlaylistScale).findFirst()
                    if (p == null) p = realm.createObject(Playlist::class.java, playlist.id + customIdPlaylistScale)
                    p!!.name = playlist.name
                    p.image = playlist.image
                    p.price = playlist.price
                    p.purchase = playlist.purchase
                    p.imagePrice = playlist.imagePrice
                    p.position = playlist.position
                    p.playlistVideo = playlist.playlistVideo
                    p.fontColor = playlist.fontColor
                    p.sectionId = section.id
                    p.sectionName = section.name
                    p.search = playlist.name.toLowerCase()
                    p.updateKey = key

                    if (playlist.videos != null) {
                        val videos = RealmList<Video>()
                        for (video in playlist.videos) {
                            var v = if (video.image.contains(BuildConfig.BASE_URL))
                                    realm.where(Video::class.java)
                                        .equalTo("id", customVideoId).or()
                                        .equalTo("idJSON", video.id).findFirst()
                            else realm.where(Video::class.java).equalTo("id", customVideoId).findFirst()
                            if (v == null) {
                                v = realm.createObject(Video::class.java, customVideoId)
                                v.videoLoadingProgress = -1
                                v.quality = -1
                                customVideoId++
                            } else {
                                val videosOld = realm.where(Video::class.java).findAll()
                                while (true) {
                                    if (customVideoId > videosOld.size - 1 ||
                                            (video.name == videosOld[customVideoId]!!.name &&
                                                    video.image == videosOld[customVideoId]!!.image)) {
                                        v = if (video.image.contains(BuildConfig.BASE_URL))
                                            realm.where(Video::class.java)
                                                .equalTo("id", customVideoId).or()
                                                .equalTo("idJSON", video.id).findFirst()
                                        else realm.where(Video::class.java).equalTo("id", customVideoId).findFirst()
                                        if (v == null) {
                                            v = realm.createObject(Video::class.java, customVideoId)
                                            v.videoLoadingProgress = -1
                                            v.quality = -1
                                        }
                                        customVideoId++
                                        break
                                    } else customVideoId++
                                }
                            }
                            v!!.updateKey = key
                            if (video.image != null && video.image.contains(BuildConfig.BASE_URL))
                                v.idJSON = video.id
                            else v.idJSON = null
                            v.videoLikes = video.videoLikes
                            v.position = video.position
                            v.image = video.image
                            v.price = video.price
                            v.purchase = video.purchase
                            v.imagePrice = video.imagePrice
                            v.name = video.name
                            v.type = video.type
                            v.shareImage = video.shareImage
                            v.shareLink = video.shareLink
                            v.shareText = video.shareText
                            v.videoCompressed = video.videoCompressed
                            v.videoCompressedDuration = video.videoCompressedDuration
                            v.videoPreview = video.videoPreview
                            v.videoPreviewDuration = video.videoPreviewDuration
                            v.videoSource = video.videoSource
                            v.videoSourceDuration = video.videoSourceDuration
                            v.youtubeLink = video.youtubeLink
                            v.videoSourceDurationSec = video.videoSourceDurationSec
                            v.videoCompressedDurationSec = video.videoCompressedDurationSec
                            v.videoSourceSize = video.videoSourceSize
                            v.videoCompressedSize = video.videoCompressedSize
                            v.googleLink = video.googleLink
                            v.series = video.series
                            v.videoOfCurrentApplication = video.videoOfCurrentApplication
                            v.search = video.name.toLowerCase()
                            if (!v.containPlaylist(p))
                                v.addPlaylist(p)
                            v.playlist = v.getPlaylist(0)
                            videos.add(v)
                        }
                        p.videos = videos
                        realm.copyToRealmOrUpdate(p)
                        realm.commitTransaction()
                    }
                }
            }
        }
        return key
    }

    private fun createOrUpdateVipuskiPlaylists(realm: Realm) {
        for (v in realm.where(Video::class.java).contains("image", BuildConfig.BASE_URL).findAll())
            if (v.series != null) {
                realm.beginTransaction()
                var pl = realm.where(Playlist::class.java).equalTo("name", v.series).findFirst()
                if (pl == null) pl = realm.createObject(Playlist::class.java,
                        realm.where(Playlist::class.java).findAll().sort("id").last()!!.id + 1)
                pl!!.name = v.series
                pl.position = v.series.split("№")[1].toInt() // get the number from name like "Выпуск №10"
                pl.sectionName = SECTION_VIPUSKI
                pl.image = "image"
                pl.updateKey = v.updateKey
                pl.search = pl.name.toLowerCase()
                if (!pl.videos.contains(v)) pl.videos.add(v)
                if (!v.containPlaylist(pl))
                    v.addPlaylist(pl)
                realm.copyToRealmOrUpdate(pl)
                realm.commitTransaction()
            }
    }

    private fun saveOrUpdatePlaylistBySong(playlist: Playlist, realm: Realm): Long {
        val key = System.currentTimeMillis()
        realm.beginTransaction()
        var p = realm.where(Playlist::class.java).equalTo("id", playlist.id).findFirst()
        if (p == null) p = realm.createObject(Playlist::class.java, playlist.id)
        p!!.name = playlist.name
        p.image = playlist.image
        p.position = playlist.position
        p.fontColor = playlist.fontColor
        p.search = playlist.name.toLowerCase()
        p.updateKey = key

        if (playlist.videos != null) {
            val videos = RealmList<Video>()
            for (video in playlist.videos) {
                // use negative song id because video has the same rate positive id
                var v = realm.where(Video::class.java).equalTo("id", (video.id * -1) - 1).findFirst()
                if (v == null) {
                    v = realm.createObject(Video::class.java, (video.id * -1) - 1)
                    v!!.videoLoadingProgress = -1
                    v.quality = -1
                }
                v.updateKey = key
                if (video.image != null && video.image.contains(BuildConfig.BASE_URL))
                    v.idJSON = video.id
                else v.idJSON = null
                v.videoLikes = video.songLikes
                v.position = video.position
                v.image = video.image
                v.name = video.name
                v.type = video.type
                v.shareImage = video.shareImage
                v.shareLink = video.shareLink
                v.shareText = video.shareText
                v.videoCompressed = video.songCompressed
                v.videoCompressedDuration = video.songCompressedDuration
                v.videoPreview = video.songPreview
                v.videoPreviewDuration = video.songPreviewDuration
                v.videoSource = video.songSource
                v.videoSourceDuration = video.songSourceDuration
                v.youtubeLink = video.youtubeLink
                v.videoSourceDurationSec = video.songSourceDurationSec
                v.videoCompressedDurationSec = video.songCompressedDurationSec
                v.videoSourceSize = video.songSourceSize
                v.videoCompressedSize = video.songCompressedSize
                v.googleLink = video.googleLink
                v.series = video.series
                v.videoOfCurrentApplication = video.videoOfCurrentApplication
                v.search = video.name.toLowerCase()
                if (!v.containPlaylist(p))
                    v.addPlaylist(p)
                v.playlist = v.getPlaylist(0)
                videos.add(v)
            }
            p.videos = videos
            realm.copyToRealmOrUpdate(p)
            realm.commitTransaction()
        }
        return key
    }

    private fun saveOrUpdatePlaylistBySongPlaylists(songPlaylists: List<Playlist>, realm: Realm): Long {
        val key = System.currentTimeMillis()
        for (songPlaylist in songPlaylists) {
            realm.beginTransaction()
            var sp = realm.where(Playlist::class.java).equalTo("id", (songPlaylist.id * -1) -1).findFirst()
            if (sp == null) sp = realm.createObject(Playlist::class.java, (songPlaylist.id * -1) - 1)
            sp!!.name = songPlaylist.name
            sp.image = songPlaylist.image
            sp.position = songPlaylist.position
            sp.playlistVideo = songPlaylist.playlistSong
            sp.fontColor = songPlaylist.fontColor
            sp.search = songPlaylist.name.toLowerCase()
            sp.updateKey = key

            if (songPlaylist.songs != null) {
                val videos = RealmList<Video>()
                for (video in songPlaylist.songs) {
                    // use negative song id because video has the same rate positive id
                    var v = realm.where(Video::class.java).equalTo("id", (video.id * -1) - 1).findFirst()
                    if (v == null) {
                        v = realm.createObject(Video::class.java, (video.id * -1) - 1)
                        v!!.videoLoadingProgress = -1
                        v.quality = -1
                    }
                    v.updateKey = key
                    if (video.image != null && video.image.contains(BuildConfig.BASE_URL))
                        v.idJSON = video.id
                    else v.idJSON = null
                    v.videoLikes = video.songLikes
                    v.position = video.position
                    v.image = video.image
                    v.name = video.name
                    v.type = video.type
                    v.shareImage = video.shareImage
                    v.shareLink = video.shareLink
                    v.shareText = video.shareText
                    v.videoCompressed = video.songCompressed
                    v.videoCompressedDuration = video.songCompressedDuration
                    v.videoPreview = video.songPreview
                    v.videoPreviewDuration = video.songPreviewDuration
                    v.videoSource = video.songSource
                    v.videoSourceDuration = video.songSourceDuration
                    v.youtubeLink = video.youtubeLink
                    v.videoSourceDurationSec = video.songSourceDurationSec
                    v.videoCompressedDurationSec = video.songCompressedDurationSec
                    v.videoSourceSize = video.songSourceSize
                    v.videoCompressedSize = video.songCompressedSize
                    v.googleLink = video.googleLink
                    v.series = video.series
                    v.videoOfCurrentApplication = video.videoOfCurrentApplication
                    v.search = video.name.toLowerCase()
                    if (!v.containPlaylist(sp))
                        v.addPlaylist(sp)
                    v.playlist = v.getPlaylist(0)
                    videos.add(v)
                }
                sp.videos = videos
            }
            realm.copyToRealmOrUpdate(sp)
            realm.commitTransaction()
        }
        return key
    }

    private fun saveOrUpdateWelcomeScreens(welcomeScreens: List<WelcomeScreen>?, realm: Realm) {
        for (welcomeScreen in welcomeScreens!!) {
            realm.beginTransaction()
            var ws = realm.where(WelcomeScreen::class.java).equalTo("id", welcomeScreen.id).findFirst()
            if (ws == null) ws = realm.createObject(WelcomeScreen::class.java, welcomeScreen.id)
            ws!!.name = welcomeScreen.name
            ws.backgroundColorPhone = welcomeScreen.backgroundColorPhone
            ws.backgroundColorTablet = welcomeScreen.backgroundColorTablet
            ws.imagePhone = welcomeScreen.imagePhone
            ws.imageTablet = welcomeScreen.imageTablet
            ws.position = welcomeScreen.position
            ws.isImageLoaded = welcomeScreen.isImageLoaded

            ws.screenType = welcomeScreen.screenType
            ws.screenWeight = welcomeScreen.screenWeight
            ws.url = welcomeScreen.url
            ws.closeTime = welcomeScreen.closeTime
            ws.displayOrder = welcomeScreen.displayOrder
            ws.audience = welcomeScreen.audience
            ws.lasting = welcomeScreen.lasting

            realm.copyToRealmOrUpdate(ws)
            realm.commitTransaction()
        }
    }

    private fun saveOrUpdatePaymentImagesAndOther(paymentImagesAndOther: List<PaymentImagesAndOther>?, realm: Realm) {
        for (paymentImageAndOther in paymentImagesAndOther!!) {
            realm.beginTransaction()
            var pi = realm.where(PaymentImagesAndOther::class.java).equalTo("id", paymentImageAndOther.id).findFirst()
            if (pi == null) pi = realm.createObject(PaymentImagesAndOther::class.java, paymentImageAndOther.id)
            pi!!.name = paymentImageAndOther.name
            pi.imagePhone = paymentImageAndOther.imagePhone
            pi.imageTablet = paymentImageAndOther.imageTablet
            pi.isImageLoaded = paymentImageAndOther.isImageLoaded
            pi.paidType = paymentImageAndOther.paidType
            realm.copyToRealmOrUpdate(pi)
            realm.commitTransaction()
        }
    }

    private fun saveOrUpdateMotivators(motivators: List<Motivator>?) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        val key = System.currentTimeMillis()
        for (motivator in motivators!!) {
            var m = realm.where(Motivator::class.java).equalTo("id", motivator.id).findFirst()
            if (m == null) m = realm.createObject(Motivator::class.java, motivator.id)
            m!!.name = motivator.name
            m.videoId = motivator.videoId
            m.textMotivator = motivator.textMotivator
            m.sound = motivator.sound
            m.imagePhone = motivator.imagePhone
            m.imageTablet = motivator.imageTablet
            m.position = motivator.position
            m.textInKey = motivator.textInKey
            m.isImageLoaded = motivator.isImageLoaded
            m.updateKey = key
            realm.copyToRealmOrUpdate(m)
        }
        realm.where(Motivator::class.java).notEqualTo("updateKey", key).findAll().deleteAllFromRealm()
        realm.commitTransaction()
        realm.close()
    }
}
