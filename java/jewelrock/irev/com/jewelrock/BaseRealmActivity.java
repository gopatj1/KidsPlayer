package jewelrock.irev.com.jewelrock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Objects;

import butterknife.BindBool;
import io.realm.Realm;
import io.realm.RealmResults;
import jewelrock.irev.com.jewelrock.controller.ErrorController;
import jewelrock.irev.com.jewelrock.controller.InitSettingsController;
import jewelrock.irev.com.jewelrock.controller.UserSettingsController;
import jewelrock.irev.com.jewelrock.model.Error;
import jewelrock.irev.com.jewelrock.model.InitSettings;
import jewelrock.irev.com.jewelrock.model.UserSettings;
import jewelrock.irev.com.jewelrock.utils.DialogUtils;

public abstract class BaseRealmActivity extends AppCompatActivity {

    protected Realm realm;
    protected UserSettings userSettings;
    protected RealmResults<Error> errors;
    protected InitSettings initSettings;
    @BindBool(R.bool.isBigScreen)
    boolean isBigScreen;

    private FirebaseAnalytics mFirebaseAnalytics;
    private int bannerId = -1;
    public int animTime = 30000;

    public RewardedVideoAd mRewardedVideoAd;
    public boolean isRewarded = false;
    public int videoIdForRewarding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        initSettings = InitSettingsController.loadInitSettings(realm);
        userSettings = UserSettingsController.loadUserSettings(realm);
        errors = ErrorController.getErrors(realm);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Use an activity context to get the rewarded video instance.
        if (BuildConfig.HAS_REWARDING_AD) {
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
            mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                @Override
                public void onRewardedVideoAdLoaded() {
                }

                @Override
                public void onRewardedVideoAdOpened() {
                }

                @Override
                public void onRewardedVideoStarted() {
                    isRewarded = false;
                }

                @Override
                public void onRewardedVideoAdClosed() {
                    BaseRealmActivity.this.onRewardedVideoAdClosed();
                }

                @Override
                public void onRewarded(RewardItem rewardItem) {
                    isRewarded = true;
                }

                @Override
                public void onRewardedVideoAdLeftApplication() {
                }

                @Override
                public void onRewardedVideoAdFailedToLoad(int i) {
                }
            });
            mRewardedVideoAd.loadAd(BuildConfig.ADMOB_REWARD_AD_ID, new AdRequest.Builder().build());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setImmersiveMode();

        analyticsOpenScreen(getScreenName());
        errors.addChangeListener(element -> {
            if (element.size() > 0) {
                Error error = element.first();
                DialogUtils.alert(this, Objects.requireNonNull(error).getMessage(), dialogInterface -> {
                    try {
                        if (error.isRemoveAfterShow()) ErrorController.removeError(error.getType());
                        else ErrorController.setShownError(error.getType());
                    } catch (IllegalStateException ignored) {
                    }
                });
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setImmersiveMode();
        }
    }

    protected void setImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void analyticsOpenScreen(String screenName) {
        if (screenName == null) return;
        mFirebaseAnalytics.setCurrentScreen(this, screenName, null);
    }

    public void analyticsLogEvent(String category, String action) {
        analyticsLogEvent(category, action, null);
    }

    public void analyticsLogEvent(String category, String action, String label) {
        Bundle bundle = new Bundle();
        if (label != null) bundle.putString(FirebaseAnalytics.Param.ITEM_ID, label);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, action);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo connection = manager.getActiveNetworkInfo();
        return connection != null && connection.isConnectedOrConnecting();
    }

    @Override
    protected void onPause() {
        super.onPause();
        errors.removeAllChangeListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userSettings.removeAllChangeListeners();
        realm.removeAllChangeListeners();
        realm.close();
    }

    public abstract String getScreenName();

    public void playSound(int id) {
        if (userSettings.isSoundsOn()) {
            MediaPlayer mp = MediaPlayer.create(this, id);
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.start();
        }
    }

    public void playSoundByUrl(Uri uri) {
        try {
            MediaPlayer player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, uri);
            player.prepare();
            player.start();
        } catch(Exception e) {
            System.out.println(e.toString());
        }
    }

    public boolean showRewardedAd(int id){
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
            videoIdForRewarding = id;
            return true;
        }
        return false;
    }

    public void onRewardedVideoAdClosed(){
        if (isRewarded) {
            Intent intent = new Intent(getBaseContext(), PlayerActivity.class);
            intent.putExtra("playlistId", -5);
            intent.putExtra("videoId", videoIdForRewarding);
            intent.putExtra("isRewarding", true);
            startActivity(intent);
            finish();
        } else
            mRewardedVideoAd.loadAd(BuildConfig.ADMOB_REWARD_AD_ID, new AdRequest.Builder().build());
    }

    @SuppressLint("ClickableViewAccessibility")
    public ImageView addBanner(Context context) {
        ImageView imageView = new ImageView(context);
        if (!BuildConfig.HAS_FLY_BANNER_APP || userSettings.getBannerIsOpen() || (userSettings.getLastBannerTime() > System.currentTimeMillis() - animTime)) {
            imageView.setVisibility(View.GONE);
            return imageView;
        } if (userSettings.getLastBannerTime() <= System.currentTimeMillis() - animTime) {
            bannerId = userSettings.getLastBannerId() + 1;
            if (bannerId == 3) bannerId = 0;
        }
        UserSettingsController.setLastBannerTimeIDAndState(realm, System.currentTimeMillis(), bannerId, false);
        imageView.setImageDrawable(getResources().getDrawable(bannerId == 0 ? R.drawable.eralash_banner_1 : bannerId == 1 ? R.drawable.eralash_banner_2 : R.drawable.eralash_banner_3));
        if (isBigScreen) imageView.setLayoutParams(new ViewGroup.LayoutParams((int) (BaseFabActivity.displayMetrics.heightPixels / 1.6), (int) (BaseFabActivity.displayMetrics.heightPixels / 1.3)));
        else imageView.setLayoutParams(new ViewGroup.LayoutParams((int) (BaseFabActivity.displayMetrics.heightPixels / 2.5), BaseFabActivity.displayMetrics.heightPixels / 2));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setTranslationY(BaseFabActivity.displayMetrics.heightPixels);
        imageView.setTranslationX((float) (-BaseFabActivity.displayMetrics.heightPixels / 2.5));
        imageView.animate().translationY(-BaseFabActivity.displayMetrics.heightPixels / 2).translationX(BaseFabActivity.displayMetrics.widthPixels).setInterpolator(new LinearInterpolator())
                .withEndAction(() -> UserSettingsController.setLastBannerTimeIDAndState(realm, System.currentTimeMillis(), bannerId, false)).setDuration(animTime);
        final float[] firstTouchY = {0};
        imageView.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    firstTouchY[0] = motionEvent.getRawY();
                    view.animate().cancel();
                    break;
                case MotionEvent.ACTION_MOVE:
                    view.animate().translationY(motionEvent.getRawY() - view.getHeight() / 2).setDuration(0).start();
                    break;
                case MotionEvent.ACTION_UP:
                    if ((firstTouchY[0] > motionEvent.getRawY() - view.getHeight() / 10) && (firstTouchY[0] < motionEvent.getRawY() + view.getHeight() / 10)) {
                        playSound(R.raw.tap);
                        UserSettingsController.setLastBannerTimeIDAndState(realm, System.currentTimeMillis(), bannerId, true);
                        view.animate().translationY(firstTouchY[0] - view.getHeight() / 2).setDuration(100).start();
                        analyticsLogEvent("Баннер приложения", "Переход по баннеру Ералаш");
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.irev.android.eralash")));
                        removeBanner(view);
                        return true;
                    }
                    if ((firstTouchY[0] > motionEvent.getRawY() - view.getHeight() / 2) && (firstTouchY[0] < motionEvent.getRawY() + view.getHeight() / 2)) {
                        playSound(R.raw.scroll);
                        view.animate().translationY(firstTouchY[0] - view.getHeight() / 2).setDuration(100).withEndAction(() -> {
                            view.animate().cancel();
                            long animTimeOstatok = (long) ((BaseFabActivity.displayMetrics.widthPixels - view.getX()) * animTime) / BaseFabActivity.displayMetrics.widthPixels;
                            view.animate().translationY(-BaseFabActivity.displayMetrics.heightPixels / 2).translationX(BaseFabActivity.displayMetrics.widthPixels)
                                    .setInterpolator(new LinearInterpolator()).setDuration(animTimeOstatok);
                        }).start();
                        return true;
                    }
                    if (firstTouchY[0] < motionEvent.getRawY() - view.getHeight() / 1.999) {
                        UserSettingsController.setLastBannerTimeIDAndState(realm, System.currentTimeMillis(), bannerId, false);
                        view.animate().translationY(BaseFabActivity.displayMetrics.heightPixels * 2).setDuration(500)
                                .withEndAction(() -> removeBanner(view)).start();
                    } else if (firstTouchY[0] > motionEvent.getRawY() + view.getHeight() / 1.999) {
                        UserSettingsController.setLastBannerTimeIDAndState(realm, System.currentTimeMillis(), bannerId, false);
                        view.animate().translationY(BaseFabActivity.displayMetrics.heightPixels * -2).setDuration(500)
                                .withEndAction(() -> removeBanner(view)).start();
                    }
                    analyticsLogEvent("Баннер приложения", "Баннер Ералаша убран свайпом");
                    playSound(R.raw.scroll);
                    break;
                default:
                    return false;
            }
            return true;
        });
        return imageView;
    }

    public void removeBanner(View banner) {
        try {
            ((ViewGroup) banner.getParent()).removeView(banner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isCurrentApplicationId(String id) {
        return BuildConfig.APPLICATION_ID.equals(id);
    }
}
