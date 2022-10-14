package jewelrock.irev.com.jewelrock;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.reactivex.disposables.Disposable;
import io.realm.Realm;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.controller.RateThisAppQuestController;
import jewelrock.irev.com.jewelrock.controller.UserSettingsController;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.subscribe.PaymentActivity;
import jewelrock.irev.com.jewelrock.ui.adapters.PlayerVideoAdapter;
import jewelrock.irev.com.jewelrock.utils.DialogUtils;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static jewelrock.irev.com.jewelrock.utils.Constants.ERALASH;
import static jewelrock.irev.com.jewelrock.utils.Constants.LES;

public class PlayerActivity extends BaseFabActivity {
    private static final String TAG = "PlayerActivity";
    private static final int CONTROL_HIDE_DELAY = 500; //ms
    private static final String BAU_BAY_VIDEO_URL = "file:///android_asset/Bau_bay.mp4";

    @BindView(R.id.sampleRecyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.videoView)
    SimpleExoPlayerView mVideoView;
    @BindView(R.id.toolbar)
    FrameLayout toolbar;
    @BindView(R.id.topPadding)
    View topPadding;
    @BindView(R.id.progress)
    ProgressBar progressBar;

    @BindView(R.id.prev)
    ImageView playerControlPrev;
    @BindView(R.id.next)
    ImageView playerControlNext;
    @BindView(R.id.videoTitle)
    TextView title;
    @BindDrawable(R.drawable.rectangle_invisible_16dp)
    Drawable rectangle;

    @BindView(R.id.replay_holder)
    FrameLayout replayLock;
    @BindView(R.id.btn_replay)
    ImageButton replayBtn;
    @BindView(R.id.btn_download)
    ImageButton downloadBtn;
    @BindView(R.id.replay_holder_back)
    ImageView holderBack;

    @BindView(R.id.btn_like)
    ImageView btnLike;
    @BindView(R.id.btn_dislike)
    ImageView btnDislike;
    private boolean isReplayLockActive = false;
    AlertDialog alertDialog;
    private boolean playError = false;
    private boolean isBauBayVideo = false;
    private int reqCode;
    private int resCode = 123456;
    private Disposable bannerCloseTimer;

    @BindView(R.id.info_holder)
    FrameLayout infoHolder;
    @BindView(R.id.info_holder_back)
    ImageView infoBack;

    @BindView(R.id.checkbox_checked)
    ImageView cbChecked;
    @BindView(R.id.checkbox_blank)
    ImageView cbBlank;

    @BindView(R.id.app_adv_frame_lay)
    FrameLayout appAdvFrameLay;
    @BindView(R.id.app_adv_image)
    ImageView appAdvImage;

    @BindView(R.id.video_rating_linLay)
    LinearLayout videoRatingLinLay;
    @BindView(R.id.video_name_anim)
    TextView videoNameAnim;
    @BindView(R.id.video_rating_anim)
    TextView videoRatingAnim;
    @BindView(R.id.video_rating_anim_new)
    TextView videoRatingAnimNew;
    @BindView(R.id.video_rating_anim_new_linLay)
    LinearLayout videoRatingAnimNewLinLay;
    @BindView(R.id.popular_star_anim)
    ImageView popularStarAnim;

    @OnClick(R.id.btn_ok)
    void okClick() {
        infoHolder.setVisibility(View.GONE);
        boolean checked = cbChecked.getVisibility() == View.VISIBLE;
        UserSettingsController.setShowLongTap(realm, checked ? 2 : 1);
        playVideoFromList(lastVideo, -1);
    }

    @OnClick(R.id.cb_do_not_show)
    void dontShowClick() {
        if (cbChecked.getVisibility() == View.GONE) {
            cbChecked.setVisibility(View.VISIBLE);
            cbBlank.setVisibility(View.GONE);
        } else {
            cbChecked.setVisibility(View.GONE);
            cbBlank.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.videoClick)
    void onVideoClick() {
        mVideoView.setControllerShowTimeoutMs(-1);
        if (!isFullScreen) {
            hideControl();
            mVideoView.hideController();
        }
    }

    @OnLongClick(R.id.videoClick)
    boolean onVideoLongClick() {
        if (!isBauBayVideo) {
            mVideoView.setControllerShowTimeoutMs(-1);
            if (isFullScreen) {
                showControl();
                mVideoView.showController();
            }
        }
        return true;
    }

    @OnClick(R.id.btn_like)
    void onLikeClick() {
        if (DataController.INSTANCE.getFavoriteVideos(realm).size() >= 2 && !userSettings.isPaid()) {
            PaymentActivity.start(this, PaymentActivity.FROM_PLEER_LUBIMIE);
            return;
        }
        DataController.INSTANCE.setFavorite(this, adapter.getCurrentVideo(), true);
        updateUI();
        RateThisAppQuestController.addFavorite();
    }

    @OnClick(R.id.btn_dislike)
    void onDislikeClick() {
        DialogUtils.showDislikeDialog(this, adapter.getCurrentVideo(), dialogInterface -> updateUI());
    }

    @OnClick(R.id.btn_back)
    void onBackClick() {
        finish();
        showAd();
    }

    @OnClick(R.id.app_adv_btn_close)
    void onCloseClick() {
        if (appAdvFrameLay.getVisibility() == View.VISIBLE) {
            if (adapter.getCurrentPosition() != 3)
                playNext();
            else if (userSettings.isShowStreamingBauBay()) {
                isBauBayVideo = true;
                hideControl();
                play(BAU_BAY_VIDEO_URL);
            } else PlaylistsActivity.start(this, true, false);
            appAdvFrameLay.setVisibility(View.GONE);
            bannerCloseTimer.dispose();
        }
    }

    @OnClick(R.id.app_adv_image)
    void onAppAdvImageClick() {
        if (adapter.getCurrentVideo().getImage().contains("http://gs2")) {
            analyticsLogEvent("Баннер приложения", "Переход по баннеру Гора Самоцветов");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=jewelrock.irev.com.jewelrock")));
        }
        if (adapter.getCurrentVideo().getImage().contains("http://toe")) {
            analyticsLogEvent("Баннер приложения", "Переход по баннеру ТО \"Экран\"");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.irev.android.toe")));
        }
        if (adapter.getCurrentVideo().getImage().contains("http://tk")) {
            analyticsLogEvent("Баннер приложения", "Переход по баннеру Три Котенка");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.irev.android.threekittens")));
        }
        if (adapter.getCurrentVideo().getImage().contains("http://tsb")) {
            analyticsLogEvent("Баннер приложения", "Переход по баннеру Тайна Сухаревой Башни");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.irev.android.tsb")));
        }
        if (adapter.getCurrentVideo().getImage().contains("http://pkv")) {
            analyticsLogEvent("Баннер приложения", "Переход по баннеру Врунгель");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.irev.android.vrungel")));
        }
//        if (adapter.getCurrentVideo().getImage().contains("http://bis")) {
//            analyticsLogEvent("Баннер приложения", "Переход по баннеру Белка и Стрелка");
//            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.irev.android.bis")));
//        }
//        if (adapter.getCurrentVideo().getImage().contains("http://eralash")) {
//            analyticsLogEvent("Баннер приложения", "Переход по баннеру Ералаш");
//            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.irev.android.eralash")));
//        }
//        if (adapter.getCurrentVideo().getImage().contains("http://les")) {
//            analyticsLogEvent("Баннер приложения", "Переход по баннеру Лесные");
//            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.irev.android.les")));
//        }
    }

    @OnClick(R.id.btn_download)
    void downloadClick() {
        showLoadVideoDialog(lastVideo);
    }

    @OnClick(R.id.exo_play)
    void playClick() {
        if (playError) play(null);
        else  mVideoView.getPlayer().setPlayWhenReady(true);
    }

    @OnClick(R.id.btn_replay)
    void replayClick() {
        adapter.setCurrentPosition(adapter.getCurrentPosition(), userSettings.isPlayCycle());
    }

    private Handler mainHandler;
    private DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    private String lastUrl = null;

    private PlayerVideoAdapter adapter;

    private boolean isFavoriteList;
    private Video lastVideo;

    private boolean isFullScreen = true;
    private static boolean isStreaming = false;
    private static boolean isSong = false;
    private static boolean isRewarding = false;
    private static boolean isProkat = false;

    public static void startVideo(Activity activity, Video video) {
        Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra("playlistId", video.getPlaylist().getId());
        intent.putExtra("videoId", video.getId());
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);
        setupFabAndCatalogMenu();

        initToolbar();
        initPlayer();
        loadData();

        initPlayerControls();
        realm.addChangeListener(element -> updateUI());
    }

    @Override
    protected void onDestroy() {
        mVideoView.getPlayer().stop();
        super.onDestroy();
    }

    @Override
    public String getScreenName() {
        return "Плеер";
    }

    private Boolean playState = null;
    private Subscription updateScreenTimer;
    private long lastActionUpTime = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d("!!!", "Screen touched " + ev.getAction());
        lastActionUpTime = System.currentTimeMillis();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onPause() {
        super.onPause();
        playState = mVideoView.getPlayer().getPlayWhenReady();
        mVideoView.getPlayer().setPlayWhenReady(false);
        hideReplayLock();
        if (alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();
        if (updateScreenTimer != null && !updateScreenTimer.isUnsubscribed())
            updateScreenTimer.unsubscribe();
        if (bannerCloseTimer != null && !bannerCloseTimer.isDisposed())
            bannerCloseTimer.dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playState != null) {
            mVideoView.getPlayer().setPlayWhenReady(playState);
        }
        showPrevNextButtons();
        updateUI();

        lastActionUpTime = System.currentTimeMillis();
        updateScreenTimer = Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tick -> {
                    if (lastActionUpTime + 5 * 1000 < System.currentTimeMillis()
                            && mVideoView.getPlayer().getPlayWhenReady()) hidePlaylist();
                });
    }

    private void initPlayerControls() {
        playerControlNext.setOnClickListener(view -> adapter.setNext(userSettings.isPlayCycle()));
        playerControlPrev.setOnClickListener(view -> adapter.setPrev(userSettings.isPlayCycle()));
    }

    private void updateToolbar() {
        boolean isFavorite = false;
        boolean canShowFavoriteButton = false;
        if (adapter != null) {
            Video currentVideo = adapter.getCurrentVideo();
            if (currentVideo != null) {
                isFavorite = currentVideo.isFavorite();
                canShowFavoriteButton = !isFavoriteList && (userSettings.isPaid()
                        || currentVideo.isFree()) && !isStreaming && !isRewarding && !isProkat;
            }
        }
        btnLike.setVisibility(!isFavorite && canShowFavoriteButton ? View.VISIBLE : View.GONE);
        btnDislike.setVisibility(isFavorite && canShowFavoriteButton ? View.VISIBLE : View.GONE);
    }

    private void updateUI() {
        updateToolbar();
        if (adapter != null) adapter.setPaid(userSettings.isPaid()).setSong(isSong).setStreaming(isStreaming)
                .setRewarding(isRewarding).setProkat(isProkat).notifyDataSetChanged();
        setImmersiveMode();
    }

    private void initToolbar() {
        title.setText(R.string.app_name);
        appAdvFrameLay.setVisibility(View.GONE);
    }

    private void initPlayer() {
        menuButton.setVisibility(View.GONE);
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        mainHandler = new Handler();
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        LoadControl loadControl = new DefaultLoadControl();

        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        mVideoView.setPlayer(player);
        mVideoView.setUseController(false);

        mVideoView.setControllerShowTimeoutMs(-1);
        mVideoView.hideController();
        mVideoView.setControllerVisibilityListener(visibility -> {
            if (visibility == View.GONE) {
                hideControl();
            }
        });
        player.setPlayWhenReady(true);

        mVideoView.getPlayer().addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                progressBar.setVisibility(playbackState == ExoPlayer.STATE_BUFFERING ? View.VISIBLE : View.GONE);
                if (playbackState == ExoPlayer.STATE_ENDED && playWhenReady) {
                    Log.d("!!!", "the end!");
                    onVideoEnd();
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                playError = true;
                if (!isConnected()) {
                    DialogUtils.showCantLoadVideoDialog(PlayerActivity.this, buttonId -> {
                        if (buttonId == 1) play(null);
                    });
                } else {
                    DialogUtils.alert(PlayerActivity.this, "Не удалось воспроизвести видео.");
                }
                mVideoView.getPlayer().setPlayWhenReady(false);
                showControl();
            }

            @Override
            public void onPositionDiscontinuity() {
            }
        });
    }

    private void hideControl() {
        if (isFinishing()) return;
        hidePlaylist();
        toolbar.setVisibility(View.GONE);
        playerControlNext.setVisibility(View.GONE);
        playerControlPrev.setVisibility(View.GONE);
        menuButton.setVisibility(View.GONE);
    }

    private void showControl() {
        toolbar.postDelayed(() -> {
            showPlaylist();
            toolbar.setVisibility(View.VISIBLE);
            showPrevNextButtons();
            menuButton.setVisibility(View.VISIBLE);
        }, 10);
    }

    private void showPrevNextButtons() {
        playerControlPrev.setVisibility((!userSettings.isPlayCycle() && !adapter.hasPrev()) || isFullScreen ? View.GONE : View.VISIBLE);
        playerControlNext.setVisibility((!userSettings.isPlayCycle() && !adapter.hasNext()) || isFullScreen ? View.GONE : View.VISIBLE);
    }

    private void initAdapter(List<Video> episodes) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                LinearLayoutManager.HORIZONTAL);
        dividerItemDecoration.setDrawable(rectangle);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter = new PlayerVideoAdapter(episodes);
        adapter.setOnItemClick(this::playVideoFromList);
        recyclerView.setAdapter(adapter);
    }

    private void playVideoFromList(Video video, int pos) {
        if (video == null) {
            finish();
            return;
        }
        if (pos > -1) recyclerView.scrollToPosition(pos);
        if (userSettings.isNeedShowLongTapInfo() == 0) {
            showInfoLock(video);
            return;
        }
        selectSourceAndPlay(video);
        showPrevNextButtons();

        updateUI();
    }

    private void onVideoEnd() {
        if (isRewarding && !isCurrentApplicationId(LES)) {
            onBackPressed();
            finish();
            return;
        }

        if (isProkat) {
            playNext();
            return;
        }

        if (isStreaming) {

            if (adapter.getCurrentVideo().getImage().contains("http://toe")) {
                analyticsLogEvent("Баннер приложения", "Показ баннера ТО \"Экран\"");
                appAdvImage.setImageDrawable(getResources().getDrawable(R.drawable.app_adv_to));
            } else if (adapter.getCurrentVideo().getImage().contains("http://tk")) {
                analyticsLogEvent("Баннер приложения", "Показ баннера Три котенка");
                appAdvImage.setImageDrawable(getResources().getDrawable(R.drawable.app_adv_3cats));
            } else if (adapter.getCurrentVideo().getImage().contains("http://tsb")) {
                analyticsLogEvent("Баннер приложения", "Показ баннера Тайна Сухаревой Башни");
                appAdvImage.setImageDrawable(getResources().getDrawable(R.drawable.app_adv_tsb));
            } else if (adapter.getCurrentVideo().getImage().contains("http://pkv")) {
                analyticsLogEvent("Баннер приложения", "Показ баннера Врунгель");
                appAdvImage.setImageDrawable(getResources().getDrawable(R.drawable.app_adv_vrungel));
            } else if (adapter.getCurrentVideo().getImage().contains("http://gs2")) {
                analyticsLogEvent("Баннер приложения", "Показ баннера Гора Самоцветов");
                appAdvImage.setImageDrawable(getResources().getDrawable(R.drawable.app_adv_gs));
            }
//                analyticsLogEvent("Баннер приложения", "Показ баннера Белка и Стрелка");
//                appAdvImage.setImageDrawable(getResources().getDrawable(R.drawable.app_adv_bis));
//            }
//                analyticsLogEvent("Баннер приложения", "Показ баннера Ералаш");
//                appAdvImage.setImageDrawable(getResources().getDrawable(R.drawable.app_adv_eralash));
//            }
//                analyticsLogEvent("Баннер приложения", "Показ баннера Лесные");
//                appAdvImage.setImageDrawable(getResources().getDrawable(R.drawable.app_adv_les));
//            }

            if (adapter.getCurrentVideo().getImage().contains(BuildConfig.BASE_URL) && !userSettings.isPaid() && !isBauBayVideo) {
                if (resCode != PaymentActivity.RESULT_CANCELED)
                    PaymentActivity.start(this, 8, 400, PaymentActivity.FROM_PLEER);
                resCode = 123456;
                return;
            } else if (!adapter.getCurrentVideo().getImage().contains(BuildConfig.BASE_URL) && !isBauBayVideo && reqCode != 400) {
                playSound(R.raw.cloud);
                appAdvFrameLay.setVisibility(View.VISIBLE);
                bannerCloseTimer = io.reactivex.Observable.timer(5, TimeUnit.SECONDS)
                        .subscribeOn(io.reactivex.schedulers.Schedulers.newThread())
                        .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                        .subscribe(aLong -> onCloseClick());
            } else if (reqCode == 400)  {
                reqCode = 0;
                return;
            }

            if (isBauBayVideo) {
                isBauBayVideo = false;
                mVideoView.getPlayer().stop();
                PlaylistsActivity.start(this, true, false);
                finish();
                return;
            }

            if (adapter.getCurrentPosition() == 3 && !userSettings.isShowStreamingBauBay() &&
                    adapter.getCurrentVideo().getImage().contains(BuildConfig.BASE_URL)) {
                PlaylistsActivity.start(this, true, false);
                return;
            }

            if (adapter.getCurrentPosition() == 3 && userSettings.isShowStreamingBauBay() &&
                    adapter.getCurrentVideo().getImage().contains(BuildConfig.BASE_URL)) {
                isBauBayVideo = true;
                hideControl();
                play(BAU_BAY_VIDEO_URL);
                return;
            }

            if (adapter.getCurrentPosition() != 3 && userSettings.isPaid() &&
                    adapter.getCurrentVideo().getImage().contains(BuildConfig.BASE_URL)) {
                playNext();
                return;
            }

            return;
        }

        isBauBayVideo = false;
        if (!userSettings.isPaid()) {
            mVideoView.getPlayer().stop();
            if (!adapter.getCurrentVideo().isFree() && BuildConfig.HAS_REWARDING_AD && adapter.getCurrentVideo().getVideoPreview() == null) {
                Intent intent = new Intent(PlayerActivity.this, PaymentActivity.class);
                intent.putExtra("videoId", adapter.getCurrentVideo().getId());
                intent.putExtra("isRewarding", true);
                intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FOR_REWARDING_VIDEO);
                startActivity(intent);
                finish();
            } else if (isSong) PaymentActivity.start(this, 8, 400, PaymentActivity.FROM_PLEER_SONGS);
            else PaymentActivity.start(this, 8, 400,
                    isFavoriteList ? PaymentActivity.FROM_PLEER_LUBIMIE : PaymentActivity.FROM_PLEER);
        } else {
            showReplayLock();
        }
    }

    private void showReplayLock() {
        replayLock.setVisibility(!isStreaming && !isRewarding ? View.VISIBLE : View.GONE);
        title.setAlpha(1);
        playerControlPrev.setVisibility((!userSettings.isPlayCycle() && !adapter.hasPrev()) || isStreaming || isRewarding ? View.GONE : View.VISIBLE);
        playerControlNext.setVisibility((!userSettings.isPlayCycle() && !adapter.hasNext()) || isStreaming || isRewarding ? View.GONE : View.VISIBLE);
        GlideApp.with(this)
                .load(lastVideo.getImage())
                .into(holderBack);
        downloadBtn.setVisibility(lastVideo.getVideoLoadingProgress() >= 0  || isStreaming  || isRewarding ? View.GONE : View.VISIBLE);
        isReplayLockActive = true;
        replayLock.postDelayed(() -> {
            if (!isFinishing() && isReplayLockActive) {
                playNext();
            }
        }, 5000);
    }

    private void hideReplayLock() {
        isReplayLockActive = false;
        replayLock.setVisibility(View.GONE);
        if (isFullScreen) {
            playerControlNext.setVisibility(View.GONE);
            playerControlPrev.setVisibility(View.GONE);
            title.setAlpha(0);
        }
    }

    @SuppressLint("SetTextI18n")
    private void selectSourceAndPlay(Video video) {
        hideReplayLock();
        lastActionUpTime = System.currentTimeMillis();
        DataController.INSTANCE.setVideoLikes(video, false, realm);
        if (((userSettings.isPaid() || video.isFree()) && !isProkat  && !(video.getVideoOfCurrentApplication())
                || getIntent().getBooleanExtra("isStreaming", false)
                || getIntent().getBooleanExtra("isRewarding", false)
                || (video.isPurchase() || (video.getVideoOfCurrentApplication() && userSettings.isPaid())))) {
            title.setText(video.getName().replace("\\n", " "));
            playFullVideo(video);
        } else {
            title.setText(video.getName().replace("\\n", " ") + " — Трейлер");
            playPreview(video);
        }
        if (isFullScreen) {
            title.setAlpha(1);
            title.postDelayed(() -> {
                if (isFullScreen) title.animate().alpha(0).start();
            }, 5000);
        }
        if (BuildConfig.HAS_POPULAR_BUTTON && video.getId() >= 0 && video.getImage().contains(BuildConfig.BASE_URL))
            try {
                if (!video.getPlaylist().getSectionName().equals("Видеопрокат") && !video.getPlaylist().getSectionName().equals("Мультфильмы Татарского"))
                    animRatingNumberAndStar(video);
            } catch (Exception e) {
                animRatingNumberAndStar(video);
                e.printStackTrace();
            }
    }

    private void animRatingNumberAndStar(Video video) {
        title.setVisibility(View.GONE);
        videoRatingLinLay.setVisibility(View.VISIBLE);
        videoNameAnim.setText(video.getName().replace("\\n", " "));
        videoRatingAnim.setText(String.valueOf(video.getVideoLikes() - 1));
        videoRatingAnimNew.setText(String.valueOf(video.getVideoLikes()));
        videoRatingAnim.animate().translationY(0).setDuration(5);
        videoRatingLinLay.animate().translationY(0).setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        playSound(R.raw.anim_rating);
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        popularStarAnim.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.popular_star_rating_anim));
                        videoRatingAnim.animate().translationY(-videoRatingAnim.getHeight()).setDuration(500).setStartDelay(700);
                        videoRatingAnimNewLinLay.animate().translationY(0).setDuration(500).setStartDelay(700);
                        videoRatingLinLay.animate().translationY(-videoRatingLinLay.getHeight()).setDuration(500).setStartDelay(2500)
                                .setListener(new AnimatorListenerAdapter() {
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        videoRatingAnim.animate().translationY(0).setDuration(5);
                                        videoRatingAnimNewLinLay.animate().translationY(videoRatingAnim.getHeight()).setDuration(5);
                                    }
                                });
                    }
                });
    }

    @Override
    protected void onMenuOpen() {
        super.onMenuOpen();
        mVideoView.getPlayer().setPlayWhenReady(false);
    }

    private void playFullVideo(Video video) {
        lastVideo = video;
        if (!TextUtils.isEmpty(video.getVideoFile())) {
            play(video.getVideoFile());
            analyticsLogEvent("Мультфильмы", "Просмотр мультфильма с устройства",
                    video.getName().replace("\\", "").replace("n", " "));
        } else {
            play(video.getVideoCompressed() != null ? video.getVideoCompressed() : video.getVideoSource());
            analyticsLogEvent("Мультфильмы", "Просмотр мультфильма с интернета",
                    video.getName().replace("\\", "").replace("n", " "));
        }
        RateThisAppQuestController.addVideoRun();
    }

    private void showLoadVideoDialog(Video video) {
        hideReplayLock();
        alertDialog = DialogUtils.showDownloadDialog(PlayerActivity.this, video, quality -> {
            if (!isDestroyed() && quality != -2) playNext();
        });
    }

    private void playPreview(Video video) {
        lastVideo = video;
        analyticsLogEvent("Мультфильмы", "Просмотр видео-превью",
                video.getName().replace("\\", "").replace("n", " "));
        if (!TextUtils.isEmpty(video.getPreviewFile())) {
            Log.d("!!!", "Play Cache Preview " + video.getPreviewFile());
            play(video.getPreviewFile());
        } else {
            Log.d("!!!", "Play online Preview " + video.getVideoPreview());
            play(video.getVideoPreview());
        }
    }

    private void loadData() {
        int playlistId = getIntent().getIntExtra("playlistId", 0);
        List<Video> videos;
        boolean isSongPlaylist = getIntent().getBooleanExtra("isSongPlaylist", false);
        if (playlistId == -1 && !isSongPlaylist) { // favourite
            videos = DataController.INSTANCE.getFavoriteVideos(realm);
            isFavoriteList = true;
            isStreaming = false;
            isSong = false;
            isRewarding = false;
            isProkat = false;
        } else if (playlistId == -2 && !isSongPlaylist) { // streaming
            videos = new ArrayList<>();
            for (int i = getIntent().getIntExtra("videoPos", 0); i < 4; i++)
                if (!userSettings.getLastStreamingVideoId().isEmpty())
                    videos.add(DataController.INSTANCE.getStreamingVideos(realm).get(i));
            for (int i = 0; i < getIntent().getIntExtra("videoPos", 0); i++)
                if (!userSettings.getLastStreamingVideoId().isEmpty())
                    videos.add(DataController.INSTANCE.getStreamingVideos(realm).get(i));
            isFavoriteList = false;
            isStreaming = true;
            isSong = false;
            isRewarding = false;
            isProkat = false;
        } else if (playlistId == -3 && !isSongPlaylist) { // song
            videos = DataController.INSTANCE.getSongVideos(realm);
            isFavoriteList = false;
            isStreaming = false;
            isSong = true;
            isRewarding = false;
            isProkat = false;
        } else if (playlistId == -4 && !isSongPlaylist) { // popular
            videos = new ArrayList<>(Objects.requireNonNull(DataController.INSTANCE.getPopularVideos(realm)));
            while (videos.size() > 20)
                videos.remove(20);
            isFavoriteList = false;
            isStreaming = false;
            isSong = false;
            isRewarding = false;
            isProkat = false;
        } else if (playlistId == -5 && !isSongPlaylist) { // rewarding
            videos = new ArrayList<>();
            videos.add(DataController.INSTANCE.getVideoByID(realm, PlayerActivity.this.getIntent().getIntExtra("videoId", 0)));
            isFavoriteList = false;
            isStreaming = false;
            isSong = false;
            isRewarding = true;
            isProkat = false;
        } else if (playlistId == -6 && !isSongPlaylist) { // prokat
            videos = new ArrayList<>(Objects.requireNonNull(DataController.INSTANCE.getProkatVideos(realm)));
            isFavoriteList = false;
            isStreaming = false;
            isSong = false;
            isRewarding = false;
            isProkat = true;
        } else if (playlistId < 0 && isSongPlaylist) { // song playlist
            videos = new ArrayList<>(DataController.INSTANCE.getVideoFromPlayList(realm, playlistId));
            // eralash в плеере отображет только ролики данного плейлитста, а остальные приложения - ролики всех плейлистов
            if (!isCurrentApplicationId(ERALASH)) {
                videos.addAll(DataController.INSTANCE.getVideoFromNextPlayLists(realm, playlistId, true));
                videos.addAll(DataController.INSTANCE.getVideoFromPrevPlayLists(realm, playlistId, true));
            }
            isFavoriteList = false;
            isStreaming = false;
            isSong = true;
            isRewarding = false;
            isProkat = false;
        } else {
            videos = new ArrayList<>();
            if (BuildConfig.HAS_PLAYLISTS) {
                videos.addAll(DataController.INSTANCE.getVideoFromPlayList(realm, playlistId));
                // eralash в плеере отображет только ролики данного плейлитста, а остальные приложения - ролики всех плейлистов
                if (!isCurrentApplicationId(ERALASH)) {
                    videos.addAll(DataController.INSTANCE.getVideoFromNextPlayLists(realm, playlistId));
                    videos.addAll(DataController.INSTANCE.getVideoFromPrevPlayLists(realm, playlistId));
                }
            } else {
                videos.addAll(Objects.requireNonNull(DataController.INSTANCE.getMainVideos(realm)));
            }
            isFavoriteList = false;
            isStreaming = false;
            isSong = false;
            isRewarding = false;
            isProkat = false;
        }
        for (int i = Objects.requireNonNull(videos).size() - 1; i >= 0; i--) {
            Video video = videos.get(i);
            try {
                if (Objects.requireNonNull(video).getPlaylist().getSectionName().equals("Видеопрокат"))
                    if ((video.isPurchase() || (video.getVideoOfCurrentApplication()
                            && UserSettingsController.loadUserSettings(Realm.getDefaultInstance()).isPaid()))
                            && !(video.getGoogleLink() != null
                                && video.getGoogleLink().contains("play.google.com")))
                        continue;
                    else videos.remove(video);
                if (video.getPlaylist().getSectionName().equals("Мультфильмы Татарского") && video.isPurchase()) continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!userSettings.isPaid() && !Objects.requireNonNull(video).isFree() && video.getVideoPreview() == null && !isStreaming && !isRewarding) {
                videos.remove(video);
            }
        }
        initAdapter(videos);
        int videoId = getIntent().getIntExtra("videoId", 0);

        int i = 0;
        for (Video v : videos) {
            if (v.getId() == videoId) break;
            i++;
        }
        adapter.setCurrentPosition(i, userSettings.isPlayCycle());

        updateToolbar();
    }

    private void showInfoLock(Video video) {

        infoHolder.setVisibility(userSettings.isNeedShowLongTapInfo() == 0 ? View.VISIBLE : View.GONE);
        lastVideo = video;
        // eralash в обучающем окне перед просмотром, отображает свою специальную картинку, а все остальные - превью данного видео
        if (!isCurrentApplicationId(ERALASH))
            GlideApp.with(this).load(video.getImage()).into(infoBack);
        else GlideApp.with(this).load(getResources().getDrawable(R.drawable.attantion_video)).into(infoBack);
    }

    private void play(String url) {
        if (TextUtils.isEmpty(url) && TextUtils.isEmpty(lastUrl)) return;
        if (TextUtils.isEmpty(url)) url = lastUrl;
        lastUrl = url;
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter);
        // This is the MediaSource representing the media to be played.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse(url),
                dataSourceFactory, extractorsFactory, mainHandler, error -> Log.e(TAG, "onLoadError"));
        // Prepare the player with the source.
        mVideoView.getPlayer().prepare(videoSource);
        mVideoView.getPlayer().setPlayWhenReady(true);
        playError = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        resCode = resultCode;
        if (requestCode == 400) {
            reqCode = requestCode;
            if (isStreaming && adapter.getCurrentPosition() == 3 && userSettings.isShowStreamingBauBay()) {
                isBauBayVideo = true;
                hideControl();
                play(BAU_BAY_VIDEO_URL);
            } else playNext();
        }
    }

    private void playNext() {
        if (!adapter.setNext(userSettings.isPlayCycle())) finish();
    }

    private void showPlaylist() {
        if (!isFullScreen) return;
        isFullScreen = false;
        AnimatorSet set = new AnimatorSet();
        ValueAnimator playlistAnimator = ValueAnimator.ofInt(0, getPlaylistHeight());
        playlistAnimator.setDuration(CONTROL_HIDE_DELAY);
        playlistAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        playlistAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
            params.height = (Integer) animation.getAnimatedValue();
            recyclerView.setLayoutParams(params);
        });

        playlistAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                recyclerView.animate().alpha(1).withStartAction(() -> title.animate().alpha(1));
            }
        });

        ValueAnimator paddingAnimator = ValueAnimator.ofInt(0, getResources().getDimensionPixelSize(R.dimen.player_top_padding));
        paddingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        paddingAnimator.setDuration(CONTROL_HIDE_DELAY);
        paddingAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = topPadding.getLayoutParams();
            params.height = (Integer) animation.getAnimatedValue();
            topPadding.setLayoutParams(params);
        });

        set.playTogether(playlistAnimator, paddingAnimator);
        set.start();
        mVideoView.setUseController(true);
        mVideoView.setControllerShowTimeoutMs(-1);
        mVideoView.showController();
    }

    private void hidePlaylist() {
        if (isFullScreen) return;
        isFullScreen = true;
        recyclerView.animate().alpha(0).withStartAction(() -> title.animate().alpha(0)).withEndAction(() -> {
            AnimatorSet set = new AnimatorSet();
            ValueAnimator playlistAnimator = ValueAnimator.ofInt(getPlaylistHeight(), 0);
            playlistAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            playlistAnimator.setDuration(CONTROL_HIDE_DELAY);
            playlistAnimator.addUpdateListener(animation -> {
                ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                params.height = (Integer) animation.getAnimatedValue();
                recyclerView.setLayoutParams(params);
            });
            playlistAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    recyclerView.animate().alpha(0);
                }
            });
            ValueAnimator paddingAnimator = ValueAnimator.ofInt(getResources().getDimensionPixelSize(R.dimen.player_top_padding), 0);
            paddingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            paddingAnimator.setDuration(CONTROL_HIDE_DELAY);
            paddingAnimator.addUpdateListener(animation -> {
                ViewGroup.LayoutParams params = topPadding.getLayoutParams();
                params.height = (Integer) animation.getAnimatedValue();
                topPadding.setLayoutParams(params);
            });
            set.playTogether(playlistAnimator, paddingAnimator);
            set.start();
        });
        mVideoView.setUseController(false);
        mVideoView.getPlayer().setPlayWhenReady(true);
    }

    private int getPlaylistHeight() {
        return getResources().getDimensionPixelSize(R.dimen.player_card_height) + getResources().getDimensionPixelSize(R.dimen.padding_quarter) * 4 + getResources().getDimensionPixelSize(R.dimen.player_top_padding);
    }

    @Override
    public void onBackPressed() {
        if (appAdvFrameLay.getVisibility() == View.VISIBLE && isStreaming) {
            onCloseClick();
            return;
        }
        if (isBauBayVideo) return;
        super.onBackPressed();
        showAd();
    }
}
