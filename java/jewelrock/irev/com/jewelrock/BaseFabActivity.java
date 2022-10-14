package jewelrock.irev.com.jewelrock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Objects;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.OnClick;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.controller.RateThisAppQuestController;
import jewelrock.irev.com.jewelrock.controller.UserSettingsController;
import jewelrock.irev.com.jewelrock.settings.SettingsActivity;
import jewelrock.irev.com.jewelrock.ui.welcome.WelcomeActivity;

public abstract class BaseFabActivity extends BaseRealmActivity {
    @BindView(R.id.menu)
    ImageView menuButton;
    @BindView(R.id.fab1)
    View fab1;
    @BindView(R.id.fab2)
    View fab2;
    @BindView(R.id.fab3)
    View fab3;
    @BindView(R.id.fab0)
    View fab0;
    @BindView(R.id.notification_linlay)
    View notificationLinLay;
    @BindView(R.id.menu_bg)
    View menuBg;
    @BindView(R.id.all_nabors_linlay)
    View allNaborsLinLay;
    @BindView(R.id.all_nabors_icon)
    View allNaborsIcon;
    @BindView(R.id.all_nabors_text)
    TextView allNaborsText;
    @BindView(R.id.all_mults_linlay)
    View allMultsLinLay;
    @BindView(R.id.all_mults_icon)
    View allMultsIcon;
    @BindView(R.id.all_mults_text)
    TextView allMultsText;
    @BindView(R.id.all_favourites_linlay)
    View allFavouritesLinLay;
    @BindView(R.id.all_favourites_icon)
    View allFavouritesIcon;
    @BindView(R.id.all_favourites_text)
    TextView allFavouritesText;
    @BindView(R.id.all_streaming_linlay)
    View allStreamingLinLay;
    @BindView(R.id.all_streaming_icon)
    View allStreamingIcon;
    @BindView(R.id.all_streaming_text)
    TextView allStreamingText;
    @BindView(R.id.all_songs_linlay)
    View allSongsLinLay;
    @BindView(R.id.all_songs_icon)
    View allSongsIcon;
    @BindView(R.id.all_songs_text)
    TextView allSongsText;
    @BindView(R.id.vipuski_linlay)
    View allVipuskiLinLay;
    @BindView(R.id.vipuski_icon)
    View allVipuskiIcon;
    @BindView(R.id.vipuski_text)
    TextView allVipuskiText;
    @BindView(R.id.persons_linlay)
    View allPersonsLinLay;
    @BindView(R.id.persons_icon)
    View allPersonsIcon;
    @BindView(R.id.persons_text)
    TextView allPersonsText;
    @BindView(R.id.menu_bg_catalog)
    View menuBgCatalog;

    @BindView(R.id.menu_response)
    ImageView fabResponse;

    @BindView(R.id.menu_placement)
    ImageView fabPlacement;
    @BindView(R.id.menu_settings)
    ImageView fabSettings;
    @BindView(R.id.menu_help)
    ImageView fabHelp;
    @BindView(R.id.icon_notification)
    ImageView notificationIcon;
    @BindView(R.id.notification_text_menu)
    TextView notificationText;
    Drawable btnLock;
    @BindView(R.id.close_catalog)
    ImageButton closeCatalog;
    @BindView(R.id.close_menu)
    ImageButton btnClose;

    @OnClick(R.id.close_catalog)
    void closeCatalog() {
        playSound(R.raw.tap);
        closeCatalogMenu();
    }

    @OnClick(R.id.close_menu)
    void closeMenu() {
        playSound(R.raw.tap);
        closeFABMenu();
    }

    @BindBool(R.bool.isBigScreen)
    boolean isBigScreen;
    public boolean isFABOpen;
    public boolean isCatalogOpen;

    private InterstitialAd interstitialAd;
    public static DisplayMetrics displayMetrics;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isConnected()) return;
            Log.d("!!!", "Internet connected");
            DataController.INSTANCE.restartDownloadingVideos();
        }
    };

    public boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo connection = manager.getActiveNetworkInfo();
        return connection != null && connection.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(BuildConfig.ADMOB_AD_ID);
        interstitialAd.loadAd(getAdRequest());

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                interstitialAd.loadAd(getAdRequest());
                UserSettingsController.setAdShowTime(realm, System.currentTimeMillis());
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.d("BaseFabActivity", "Failed Load AD Code: " + i);
            }
        });

        displayMetrics = getResources().getDisplayMetrics();
    }

    @NonNull
    private AdRequest getAdRequest() {
        AdRequest.Builder builder = new AdRequest.Builder();
        return builder.build();
    }

    protected void showAd(){
        long now = System.currentTimeMillis();
        int delta;
        try {
            delta = Integer.parseInt(initSettings.getTimerAdMob());
        } catch (NumberFormatException e) {
            delta = 1;
            e.printStackTrace();
        }
        boolean needShowAd = now > userSettings.getLastAdTime() + delta * 60 * 1000
                && "1".equals(initSettings.getReklamaAdmob());
        if (interstitialAd.isLoaded() && needShowAd && userSettings.isShowAd()) {
            interstitialAd.show();
        } else {
            Log.d("BaseFabActivity", "The interstitial wasn't loaded yet.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
        RateThisAppQuestController.checkQuest(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        if (isFABOpen) {
            closeFABMenu();
            return;
        }
        super.onBackPressed();
    }

    @OnClick(R.id.menu)
    void onParentalControlClick() {
        playSound(R.raw.tap);
        onMenuOpen();
        showAd();
        if (BuildConfig.HAS_PARENTAL_CONTROL) {
            Fragment old = getSupportFragmentManager().findFragmentByTag(ParentalControlsFragment.class.getName());
            if (old != null) return;
            ParentalControlsFragment f = ParentalControlsFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentParentalControls, f, ParentalControlsFragment.class.getName())
                    .addToBackStack(ParentalControlsFragment.class.getName())
                    .commit();
        } else showMenu();
    }

    protected void setupFabAndCatalogMenu() {
        btnLock = menuButton.getDrawable();
        fabResponse.setOnClickListener(view -> {
            playSound(R.raw.tap);
            final String appPackageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });
        fabPlacement.setOnClickListener(view -> {
            playSound(R.raw.tap);
            startActivity(new Intent(this, VideoSavedActivity.class));
        });
        fabSettings.setOnClickListener(view -> {
            playSound(R.raw.tap);
            SettingsActivity.Companion.start(BaseFabActivity.this);
        });
        fabHelp.setOnClickListener(view -> {
            playSound(R.raw.tap);
            WelcomeActivity.start(this, true);
        });
        notificationIcon.setOnClickListener(view -> {
            playSound(R.raw.tap);
            NotificationActivity.start(this);
        });

        fab0.setVisibility(View.GONE);
        fab1.setVisibility(View.GONE);
        fab2.setVisibility(View.GONE);
        fab3.setVisibility(View.GONE);
        notificationLinLay.setVisibility(View.GONE);
        btnClose.setVisibility(View.GONE);
        menuBg.setVisibility(View.GONE);
        menuBg.setAlpha(0);

        // catalog menu
        allNaborsLinLay.setVisibility(View.GONE);
        allMultsLinLay.setVisibility(View.GONE);
        allFavouritesLinLay.setVisibility(View.GONE);
        allStreamingLinLay.setVisibility(View.GONE);
        allSongsLinLay.setVisibility(View.GONE);
        allVipuskiLinLay.setVisibility(View.GONE);
        allPersonsLinLay.setVisibility(View.GONE);
        closeCatalog.setVisibility(View.GONE);
        menuBgCatalog.setVisibility(View.GONE);
        menuBgCatalog.setAlpha(0);

        allNaborsIcon.setOnClickListener(view -> {
            AllVideosActivity.start(this, "playlistsFragment");
            closeCatalogMenu();
            playSound(R.raw.tap);
        });
        allMultsIcon.setOnClickListener(view -> {
            AllVideosActivity.start(this, "videoFragment");
            closeCatalogMenu();
            playSound(R.raw.tap);
        });
        allFavouritesIcon.setOnClickListener(view -> {
            AllVideosActivity.start(this, "favoritesFragment");
            closeCatalogMenu();
            playSound(R.raw.tap);
        });
        allStreamingIcon.setOnClickListener(view -> {
            if (StreamingFragment.isStreamingTime()) AllVideosActivity.start(this, "streamingFragment");
            else return;
            playSound(R.raw.tap);
            closeCatalogMenu();
        });
        allSongsIcon.setOnClickListener(view -> {
            AllVideosActivity.start(this, "songFragment");
            closeCatalogMenu();
            playSound(R.raw.tap);
        });
        allVipuskiIcon.setOnClickListener(view -> {
            AllVideosActivity.start(this, "vipuskiPlaylistsFragment");
            closeCatalogMenu();
            playSound(R.raw.tap);
        });
        allPersonsIcon.setOnClickListener(view -> {
            AllVideosActivity.start(this, "personsPlaylistsFragment");
            closeCatalogMenu();
            playSound(R.raw.tap);
        });

        setSpannableText(allNaborsText, "playlistsFragment");
        setSpannableText(allMultsText, "videoFragment");
        setSpannableText(allFavouritesText, "favoritesFragment");
        setSpannableText(allStreamingText, "streamingFragment");
        setSpannableText(allSongsText, "songFragment");
        setSpannableText(allVipuskiText, "vipuskiPlaylistsFragment");
        setSpannableText(allPersonsText, "personsPlaylistsFragment");
    }

    private void showFABMenu() {
        isFABOpen = true;
        fab0.setVisibility(View.VISIBLE);
        fab1.setVisibility(View.VISIBLE);
        fab2.setVisibility(View.VISIBLE);
        fab3.setVisibility(View.VISIBLE);
        if (BuildConfig.HAS_NOTIFICATIONS) {
            notificationLinLay.setVisibility(View.VISIBLE);
            notificationText.setText(DataController.INSTANCE.countOfNonReadNotifications() <= 0 ? "Уведомления"
                    : "Уведомления (" + DataController.INSTANCE.countOfNonReadNotifications() + ")");
        }
        btnClose.setVisibility(View.VISIBLE);
        menuBg.setVisibility(View.VISIBLE);
        menuBg.animate().alpha(0.88f).start();
    }

    public void closeFABMenu() {
        isFABOpen = false;
        fab0.setVisibility(View.GONE);
        fab1.setVisibility(View.GONE);
        fab2.setVisibility(View.GONE);
        fab3.setVisibility(View.GONE);
        notificationLinLay.setVisibility(View.GONE);
        btnClose.setVisibility(View.GONE);
        menuBg.setVisibility(View.GONE);
        menuBg.animate().alpha(0).start();
    }

    public void showMenu() {
        showFABMenu();
    }

    protected void onMenuOpen() {}

    public void showCatalogMenu() {
        isCatalogOpen = true;
        if (BuildConfig.HAS_PLAYLISTS) allNaborsLinLay.setVisibility(View.VISIBLE);
        allMultsLinLay.setVisibility(View.VISIBLE);
        allFavouritesLinLay.setVisibility(View.VISIBLE);
        if (BuildConfig.HAS_STREAMING_BUTTON) allStreamingLinLay.setVisibility(View.VISIBLE);
        if (BuildConfig.HAS_SONG_BUTTON) allSongsLinLay.setVisibility(View.VISIBLE);
        if (BuildConfig.HAS_VIPUSKI_BUTTON) allVipuskiLinLay.setVisibility(View.VISIBLE);
        if (BuildConfig.HAS_PERSONS_BUTTON) allPersonsLinLay.setVisibility(View.VISIBLE);
        closeCatalog.setVisibility(View.VISIBLE);
        menuBgCatalog.setVisibility(View.VISIBLE);
        menuBgCatalog.animate().alpha(0.88f).start();
    }

    public void closeCatalogMenu() {
        isCatalogOpen = false;
        allNaborsLinLay.setVisibility(View.GONE);
        allMultsLinLay.setVisibility(View.GONE);
        allFavouritesLinLay.setVisibility(View.GONE);
        allStreamingLinLay.setVisibility(View.GONE);
        allSongsLinLay.setVisibility(View.GONE);
        allVipuskiLinLay.setVisibility(View.GONE);
        allPersonsLinLay.setVisibility(View.GONE);
        closeCatalog.setVisibility(View.GONE);
        menuBgCatalog.setVisibility(View.GONE);
        menuBgCatalog.animate().alpha(0).start();
    }

    private void setSpannableText(TextView textView, String fromFragment) {
        String text = "";
        if (fromFragment.equals("playlistsFragment"))
            text = getResources().getString(R.string.nabors_catalog) +
                    (DataController.INSTANCE.getMainPlaylists(realm).size() > 0 ? String.valueOf(DataController.INSTANCE.getMainPlaylists(realm).size()) : 0) + ")";
        if (fromFragment.equals("videoFragment"))
            text = getResources().getString(R.string.mults_catalog) +
                    (Objects.requireNonNull(DataController.INSTANCE.getMainVideos(realm)).size() > 0
                            ? String.valueOf(Objects.requireNonNull(DataController.INSTANCE.getMainVideos(realm)).size()) : 0) + ")";
        if (fromFragment.equals("favoritesFragment"))
            text = getResources().getString(R.string.favourites_catalog) +
                    (Objects.requireNonNull(DataController.INSTANCE.getFavoriteVideos(realm)).size() > 0
                            ? String.valueOf(Objects.requireNonNull(DataController.INSTANCE.getFavoriteVideos(realm)).size()) : 0) + ")";
        if (fromFragment.equals("streamingFragment")) {
            text = getResources().getString(R.string.streaming_catalog) + (StreamingFragment.isStreamingTime() && !userSettings.getLastStreamingVideoId().isEmpty()
                            ? String.valueOf(Objects.requireNonNull(DataController.INSTANCE.getStreamingVideos(realm)).size()) : 0) + ")";
        }
        if (fromFragment.equals("songFragment"))
            text = getResources().getString(R.string.songs_catalog) +
                    (Objects.requireNonNull(DataController.INSTANCE.getSongVideos(realm)).size() > 0
                            ? String.valueOf(Objects.requireNonNull(DataController.INSTANCE.getSongVideos(realm)).size()) : 0) + ")";
        if (fromFragment.equals("vipuskiPlaylistsFragment"))
            text = getResources().getString(R.string.vipuski_catalog) +
                    (Objects.requireNonNull(DataController.INSTANCE.getVipuskiPlaylists(realm)).size() > 0
                            ? String.valueOf(Objects.requireNonNull(DataController.INSTANCE.getVipuskiPlaylists(realm)).size()) : 0) + ")";
        if (fromFragment.equals("personsPlaylistsFragment"))
            text = getResources().getString(R.string.persons_catalog) +
                    (Objects.requireNonNull(DataController.INSTANCE.getPersonsPlaylists(realm)).size() > 0
                            ? String.valueOf(Objects.requireNonNull(DataController.INSTANCE.getPersonsPlaylists(realm)).size()) : 0) + ")";
        final SpannableStringBuilder textSpan = new SpannableStringBuilder(text);
        final ForegroundColorSpan style = new ForegroundColorSpan(getResources().getColor(R.color.light_gray));
        textSpan.setSpan(style, text.indexOf('('), text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(textSpan);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        notificationText.setText(DataController.INSTANCE.countOfNonReadNotifications() <= 0 ? "Уведомления"
                : "Уведомления (" + DataController.INSTANCE.countOfNonReadNotifications() + ")");
    }
}
