package jewelrock.irev.com.jewelrock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
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
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.gms.ads.MobileAds;

import java.util.concurrent.TimeUnit;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.controller.DataLoader;
import jewelrock.irev.com.jewelrock.controller.ErrorController;
import jewelrock.irev.com.jewelrock.controller.UserSettingsController;
import jewelrock.irev.com.jewelrock.subscribe.PaymentActivity;
import jewelrock.irev.com.jewelrock.utils.DialogUtils;

import static jewelrock.irev.com.jewelrock.utils.Constants.GS;
import static jewelrock.irev.com.jewelrock.utils.Constants.LES;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends BaseRealmActivity {
    public final static int PAYMENT_ACTIVITY_REQUEST_CODE = 35445;
    @BindView(R.id.imageSwitcher)
    ImageSwitcher imageSwitcher;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindBool(R.bool.isBigScreen)
    boolean isBigScreen;
    @BindView(R.id.videoView)
    SimpleExoPlayerView mVideoView;
    boolean isSplashEnd = false;
    boolean wasOnActivityResult = false;
    boolean canSkipSplashVideo = false;
    private DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    private Handler mainHandler = new Handler();

    @OnClick(R.id.videoClick)
    void onVideoClick() {
        if (mVideoView.getVisibility() == View.VISIBLE) {
            if (!canSkipSplashVideo) return;
            startActivity(new Intent(SplashActivity.this, PlaylistsActivity.class));
            mVideoView.getPlayer().stop();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        MobileAds.initialize(this, BuildConfig.ADMOB_APP_ID);

        ErrorController.removeAllErrors();

        if (userSettings.isNeedShowLongTapInfo() != 2) {
            UserSettingsController.setShowLongTap(realm, 0);
        }

        initImageAnimation();
        loadData();
    }

    boolean isFirstRun = true;

    @SuppressLint("CheckResult")
    private void loadData() {
        Single<Boolean> updatePlaylistsMainURL = DataLoader.updateMainData(BuildConfig.BASE_URL);
        Single<Boolean> updateSong; if (BuildConfig.HAS_SONG_BUTTON) updateSong = DataLoader.updateSongMainData();
        else updateSong = DataController.INSTANCE.updateWelcomeScreens(Glide.with(getApplicationContext()), isBigScreen);
        Single<Boolean> updateInitSettings = DataController.INSTANCE.updateInitSettings();
        Single<Boolean> updateWelcomeScreens = DataController.INSTANCE.updateWelcomeScreens(Glide.with(getApplicationContext()), isBigScreen);
        Single<Boolean> updatePaymentImagesAndOther = DataController.INSTANCE.updatePaymentImagesAndOther(Glide.with(getApplicationContext()), isBigScreen);
        Single<Boolean> updateMotivators = DataController.INSTANCE.updateMotivators(Glide.with(getApplicationContext()), isBigScreen);

        Single.zip(updatePlaylistsMainURL, updateMotivators, updatePaymentImagesAndOther, updateInitSettings, updateSong, updateWelcomeScreens,
                (aBoolean, aBoolean1, aBoolean2, aBoolean3, aBoolean4, isWelcomeLoaded) -> isWelcomeLoaded)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::startNextZip, throwable -> {
                    throwable.printStackTrace();
                    startNextZip(false);
                });

        isFirstRun = false;
    }

    @SuppressLint("CheckResult")
    private void startNextZip(Boolean isWelcomeScreensLoaded) {
        canSkipSplashVideo = true;
        if (BuildConfig.HAS_STREAMING_BUTTON) {
            Single<Boolean> updatePlaylistsURL0 = DataLoader.updateMainData(BuildConfig.URL_ARRAY[0]);
            Single<Boolean> updatePlaylistsURL1 = DataLoader.updateMainData(BuildConfig.URL_ARRAY[1]);
            Single<Boolean> updatePlaylistsTsb = DataLoader.updateMainData(BuildConfig.URL_ARRAY[2], "tsb", 100);
            Single<Boolean> updatePlaylistsVrungel = DataLoader.updateMainData(BuildConfig.URL_ARRAY[3], "vrungel", 101);
//            Single<Boolean> updatePlaylistsURL4 = DataLoader.updateMainData(BuildConfig.URL_ARRAY[4]);
//            Single<Boolean> updatePlaylistsURL5 = DataLoader.updateMainData(BuildConfig.URL_ARRAY[5]);
            Single<Boolean> updateWelcomeScreens = DataController.INSTANCE.updateWelcomeScreens(Glide.with(getApplicationContext()), isBigScreen);
            Single.zip(updatePlaylistsURL0, updatePlaylistsURL1, updatePlaylistsTsb, updatePlaylistsVrungel, updateWelcomeScreens,
                    (aBoolean, aBoolean1, aBoolean2, aBoolean3, isWelcomeLoaded) -> isWelcomeLoaded)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::startNext, throwable -> {
                        throwable.printStackTrace();
                        startNext(false);
                    });
        } else {
            Single<Boolean> updateWelcomeScreens = DataController.INSTANCE.updateWelcomeScreens(Glide.with(getApplicationContext()), isBigScreen);
            Single.zip(updateWelcomeScreens, updateWelcomeScreens,
                    (Boolean, isWelcomeLoaded) -> isWelcomeLoaded)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::startNext, throwable -> {
                        throwable.printStackTrace();
                        startNext(false);
                    });
        }
    }

    @SuppressLint("CheckResult")
    private void startNext(Boolean isWelcomeScreensLoaded) {
        if (!DataLoader.hasLoadedData()) {
            DialogUtils.showNoInternetDialog(this, buttonId -> {
                switch (buttonId) {
                    case 1:
                        loadData();
                        break;
                    case 2:
                        finish();
                        break;
                }
            });
            return;
        }
        DataController.INSTANCE.preloadImages(Glide.with(getApplicationContext()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> Log.d("!!!", "Image preloaded: " + s));
        PaymentActivity.start(SplashActivity.this, true, PAYMENT_ACTIVITY_REQUEST_CODE, PaymentActivity.FROM_GLAVNI_EKRAN);
    }

    @SuppressLint("CheckResult")
    private void initImageAnimation() {

        mVideoView.setVisibility(View.GONE);
        Animation animationOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        Animation animationIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);

        imageSwitcher.setOutAnimation(animationOut);
        imageSwitcher.setInAnimation(animationIn);
        Observable.timer(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    // les после изображения компании сразу открывает велкам экраны или PlaylistActivity,
                    // остальные - сначала показывают изображение приложения или превью-видео
                    if (isCurrentApplicationId(LES)) return;

                    if (BuildConfig.VIDEO_SPLASH) {
                        if (UserSettingsController.loadUserSettings(realm).isVideoSplash())
                            playVideo();
                        else {
                            startActivity(new Intent(SplashActivity.this, PlaylistsActivity.class));
                            finish();
                            return;
                        }
                        return;
                    }
                    imageSwitcher.showNext();
                    if (isCurrentApplicationId(GS))
                        progressBar.setVisibility(View.VISIBLE);
                });
    }

    private void playVideo() {
        mVideoView.setVisibility(View.VISIBLE);
        String path = "file:///android_asset/splash.mp4";
        LoadControl loadControl = new DefaultLoadControl();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        mVideoView.setPlayer(player);
        mVideoView.setUseController(false);
        mVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, "splash");
        // This is the MediaSource representing the media to be played.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse(path),
                dataSourceFactory, extractorsFactory, mainHandler, error -> Log.e("!!", "onLoadError"));

        // Prepare the player with the source.
        mVideoView.getPlayer().prepare(videoSource);
        mVideoView.getPlayer().setPlayWhenReady(true);
        player.setPlayWhenReady(true);
        player.addListener(new ExoPlayer.EventListener() {
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

                if (playbackState == ExoPlayer.STATE_READY)
                    imageSwitcher.animate().alpha(0).setDuration(500).withEndAction(() -> imageSwitcher.setVisibility(View.GONE));

                if (playbackState == ExoPlayer.STATE_ENDED && playWhenReady) {
                    Log.d("!!!", "the end!");
                    isSplashEnd = true;
                }
                if (playbackState == ExoPlayer.STATE_ENDED && playWhenReady && wasOnActivityResult) {
                    Log.d("!!!", "the end!");
                    startActivity(new Intent(SplashActivity.this, PlaylistsActivity.class));
                    finish();
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
            }

            @Override
            public void onPositionDiscontinuity() {
            }
        });

    }

    @SuppressLint("InlinedApi")
    @Override
    protected void onResume() {
        super.onResume();
        imageSwitcher.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public String getScreenName() {
        return "Сплэш";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_ACTIVITY_REQUEST_CODE && !BuildConfig.VIDEO_SPLASH) {
            startActivity(new Intent(SplashActivity.this, PlaylistsActivity.class));
            finish();
        }
        if (requestCode == PAYMENT_ACTIVITY_REQUEST_CODE && BuildConfig.VIDEO_SPLASH) {
            wasOnActivityResult = true;
        }
        if (requestCode == PAYMENT_ACTIVITY_REQUEST_CODE && BuildConfig.VIDEO_SPLASH && isSplashEnd) {
            startActivity(new Intent(SplashActivity.this, PlaylistsActivity.class));
            finish();
        }
    }
}