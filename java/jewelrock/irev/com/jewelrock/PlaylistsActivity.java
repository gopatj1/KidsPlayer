package jewelrock.irev.com.jewelrock;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.RealmResults;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.controller.DataLoader;
import jewelrock.irev.com.jewelrock.controller.UserSettingsController;
import jewelrock.irev.com.jewelrock.model.Playlist;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.subscribe.PaymentActivity;
import jewelrock.irev.com.jewelrock.ui.welcome.WelcomeActivity;
import jewelrock.irev.com.jewelrock.utils.DialogUtils;
import kotlin.jvm.JvmOverloads;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static jewelrock.irev.com.jewelrock.utils.Constants.BIS;
import static jewelrock.irev.com.jewelrock.utils.Constants.ERALASH;
import static jewelrock.irev.com.jewelrock.utils.Constants.GS;
import static jewelrock.irev.com.jewelrock.utils.Constants.TOE;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PlaylistsActivity extends BaseFabActivity {
    @BindView(R.id.content_holder)
    View contentHolder;
    @BindView(R.id.main_frame_layout_activity_playlist)
    FrameLayout mainFrameLayout;
    @BindView(R.id.liner_layout_fragment_container)
    LinearLayout fragmentContainer;
    @BindView(R.id.frame_layout_fragment_container_streaming_mobile)
    FrameLayout fragmentContainerStreamingMobile;
    @BindView(R.id.bg_toolbar)
    ImageView bgToolbar;

    @BindView(R.id.btn_download_set)
    View downloadSetBtn;
    @BindView(R.id.menu_catalog_in_playlist)
    View menuCatalogInPlaylist;
    @BindView(R.id.menu_floating)
    View menuFloatingInPlaylist;
    @BindView(R.id.menu)
    ImageView menuSettingsHelp;

    @BindView(R.id.time_line)
    ImageView timeLine;
    @BindView(R.id.doska_pocheta)
    ImageView doskaPocheta;
    @BindView(R.id.prokat)
    ImageView prokat;
    @BindView(R.id.all_mult)
    ImageView allMult;
    @BindView(R.id.favorite_mult)
    ImageView favMult;
    @BindView(R.id.mult_streaming)
    ImageView multStreaming;
    @BindView(R.id.song)
    ImageView song;
    @BindView(R.id.popular)
    ImageView popular;

    @BindView(R.id.time_line_selector)
    ImageView timeLineSelector;
    @BindView(R.id.doska_pocheta_selector)
    ImageView doskaPochetaSelector;
    @BindView(R.id.prokat_selector)
    ImageView prokatSelector;
    @BindView(R.id.all_mult_selector)
    ImageView allMultSelector;
    @BindView(R.id.favorite_mult_selector)
    ImageView favMultSelector;
    @BindView(R.id.mult_streaming_selector)
    ImageView multStreamingSelector;
    @BindView(R.id.song_selector)
    ImageView songSelector;
    @BindView(R.id.popular_selector)
    ImageView popularSelector;

    @BindView(R.id.time_line_text)
    TextView timeLineText;
    @BindView(R.id.doska_pocheta_text)
    TextView doskaPochetaText;
    @BindView(R.id.prokat_text)
    TextView prokatText;
    @BindView(R.id.all_mult_text)
    TextView allMultText;
    @BindView(R.id.favorite_mult_text)
    TextView favMultText;
    @BindView(R.id.mult_streaming_text)
    TextView multStreamingText;
    @BindView(R.id.song_text)
    TextView songText;
    @BindView(R.id.popular_text)
    TextView popularText;

    @BindView(R.id.time_line_liner_layout)
    LinearLayout timeLineLinerLayout;
    @BindView(R.id.doska_pocheta_liner_layout)
    LinearLayout doskaPochetaLinerLayout;
    @BindView(R.id.streaming_liner_layout)
    LinearLayout streamingLinerLayout;
    @BindView(R.id.all_mult_liner_layout)
    LinearLayout allMultLinerLayout;
    @BindView(R.id.prokat_liner_layout)
    LinearLayout prokatLinerLayout;
    @BindView(R.id.song_liner_layout)
    LinearLayout songLinerLayout;
    @BindView(R.id.favorite_mult_liner_layout)
    LinearLayout favMultLinerLayout;
    @BindView(R.id.popular_liner_layout)
    LinearLayout popularLinerLayout;
    @BindView(R.id.btn_shop)
    View btnShop;
    @BindColor(R.color.custom_toolbar_text_color)
    int textColor;
    @BindColor(R.color.custom_toolbar_text_color_selected)
    int textColorSelected;

    @BindView(R.id.all_mults)
    View btnAllMults;
    @BindView(R.id.motivator22frameLay)
    FrameLayout motivator22frameLay;
    @BindView(R.id.doska_pocheta_frameLay)
    FrameLayout doskaPochetaFrameLay;
    @BindView(R.id.time_line_frameLay)
    FrameLayout timeLineFrameLay;

    PlaylistsFragment playlistsFragment;
    VideoFragment videoFragment;
    FavoritesFragment favoritesFragment;
    StreamingFragment streamingFragment;
    SongFragment songFragment;
    SongPlaylistsFragment songPlaylistsFragment;
    boolean silentClick = false;
    int resCode = 123;
    BaseAnalyticsFragment currentFragment;
    private RealmResults<Video> video;
    private ArrayList<ImageView> allBanners = new ArrayList<>();

    private Observable<Long> timer = Observable.interval(5, TimeUnit.SECONDS);
    private ArrayList<Subscription> subscriptionArrayList = new ArrayList<>();

    @OnClick(R.id.all_mults)
    void openAllMult() {
        playSound(R.raw.tap);
        Intent intent = new Intent(this, AllVideosActivity.class);
        if (currentFragment == playlistsFragment)
            intent.putExtra("fromFragment", "playlistsFragment");
        if (currentFragment == videoFragment)
            intent.putExtra("fromFragment", "videoFragment");
        if (currentFragment == favoritesFragment)
            intent.putExtra("fromFragment", "favoritesFragment");
        if (currentFragment == streamingFragment)
            intent.putExtra("fromFragment", "streamingFragment");
        if (currentFragment == songFragment || currentFragment == songPlaylistsFragment)
            intent.putExtra("fromFragment", "songFragment");
        startActivity(intent);
        showAd();
    }

    @OnClick(R.id.btnCloseMotivator22)
    void closeMotivator22() {
        playSound(R.raw.tap);
        motivator22frameLay.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_time_line_home)
    void closeTimePreview() {
        playSound(R.raw.tap);
        timeLineFrameLay.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_doska_pocheta_home)
    void closeDoskaPreview() {
        playSound(R.raw.tap);
        doskaPochetaFrameLay.setVisibility(View.GONE);
    }

    @JvmOverloads
    public static void start(Context context, boolean notFirstStart, boolean silentClickToCatalog) {
        Intent intent = new Intent(context, PlaylistsActivity.class);
        intent.putExtra("not_first_start", notFirstStart);
        intent.putExtra("silentClickToCatalog", silentClickToCatalog);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playlists);
        ButterKnife.bind(this);

        timeLineLinerLayout.setVisibility(BuildConfig.HAS_TIME_LINE_BUTTON ? View.VISIBLE : View.GONE);
        doskaPochetaLinerLayout.setVisibility(BuildConfig.HAS_DOSKA_POCHETA_BUTTON ? View.VISIBLE : View.GONE);
        btnAllMults.setVisibility(BuildConfig.HAS_PLAYLISTS ? View.VISIBLE : View.GONE);
        streamingLinerLayout.setVisibility(BuildConfig.HAS_STREAMING_BUTTON ? View.VISIBLE : View.GONE);
        popularLinerLayout.setVisibility(BuildConfig.HAS_POPULAR_BUTTON ? View.VISIBLE : View.GONE);
        prokatLinerLayout.setVisibility(BuildConfig.HAS_PROKAT_BUTTON ? View.VISIBLE : View.GONE);
        songLinerLayout.setVisibility(BuildConfig.HAS_SONG_BUTTON ? View.VISIBLE : View.GONE);
        closeCatalog.setVisibility(View.GONE);
        menuCatalogInPlaylist.setVisibility(View.VISIBLE);
        menuFloatingInPlaylist.setVisibility(View.VISIBLE);
        // bis имеет свой кастомный тулбар (прозрачный). То есть будет виден орнамент главного изображения
        // с наложением цветогового оттенка. Поэтому добавляем в позицию 0 кастомный ImageView элемент с заданной высотой и цветом,
        // и поверх него рисуем орнамент. При этом bg_toolbar остается прозрачным и не используется.
        if (isCurrentApplicationId(BIS)) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.custom_toolbar_line_padding)));
            imageView.setBackgroundColor(getResources().getColor(R.color.violet));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mainFrameLayout.addView(imageView, 0);
            bgToolbar.setImageAlpha(0);
        }

        setupFabAndCatalogMenu();
        playlistsFragment = new PlaylistsFragment();
        favoritesFragment = new FavoritesFragment();
        videoFragment = new VideoFragment();
        streamingFragment = new StreamingFragment();
        songFragment = new SongFragment();
        songPlaylistsFragment = new SongPlaylistsFragment();

        userSettings.addChangeListener(element -> btnShop.setVisibility(userSettings.isPaid() ? View.GONE : View.VISIBLE));

        UserSettingsController.setAdShowTime(realm, System.currentTimeMillis());
        UserSettingsController.setShowAd(realm, !userSettings.isPaid());
        setupFabAndCatalogMenu();
        setupButtons();
        if (DataController.INSTANCE.getFavoriteVideos(realm).size() > 0) {
            silentClick = true;
            favMult.performClick();
        } else {
            silentClick = true;
            switch (userSettings.getLastFragmentName()) {
                case "allMults": allMult.performClick(); break;
                case "song": song.performClick(); break;
                case "prokat": prokat.performClick(); break;
                default: allMult.performClick();
            }
        }

        if (BuildConfig.DEBUG) UserSettingsController.setUserPaid(false);
        userSettings.addChangeListener(element -> updateUI());
        DataController.INSTANCE.restartDownloadingVideos();

        if (!getIntent().getBooleanExtra("not_first_start", false)) {
            if (DataController.INSTANCE.getUnshownMotivators(realm).size() > 0) {
                if (userSettings.isShowMotivator()) startActivityForResult(new Intent(this, MotivatorActivity.class), 1);
            } else {
                DataController.INSTANCE.resetMotivators();
                if (DataController.INSTANCE.getUnshownMotivators(realm).size() > 0)
                    if (userSettings.isShowMotivator()) startActivityForResult(new Intent(this, MotivatorActivity.class), 1);
            }
        }

        if (getIntent().getBooleanExtra("silentClickToCatalog", false))
            showCatalogMenu();

        if (DataController.INSTANCE.getUnshownWelcomeScreens(realm).size() > 0) {
            WelcomeActivity.start(this, false);
        } else if (DataController.INSTANCE.getUnshownAdvertisingScreens(realm, userSettings.isPaid()).size() > 0 && isConnected()) {
            WelcomeActivity.startAdvertising(this);
        }

        if (!BuildConfig.HAS_PLAYLISTS) {
            video = DataController.INSTANCE.getMainVideos(realm);
            Objects.requireNonNull(video).addChangeListener(element -> updateUI());
        }
        subscriptionArrayList.add(timer.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> updateUI(),
                        e -> System.out.println("Error: " + e),
                        () -> System.out.println("Completed")));
    }

    private void updateUI() {
        if (getIntent().getBooleanExtra("not_first_start", false) || resCode == MotivatorActivity.RESULT_CANCELED)
             if (allBanners.isEmpty() || (allBanners.get(0).getY() <= -BaseFabActivity.displayMetrics.heightPixels / 2.5
                     || allBanners.get(0).getY() >= BaseFabActivity.displayMetrics.heightPixels * 1.2 || allBanners.get(0).getY() == 0.0)) {
             ImageView banner = addBanner(this);
             allBanners.clear();
             allBanners.add(banner);
             mainFrameLayout.addView(banner);
        }
        btnShop.setVisibility(userSettings.isPaid() ? View.GONE : View.VISIBLE);
        downloadSetBtn.setVisibility((!userSettings.isPaid() || !DataController.INSTANCE.hasNotLoadedVideos() || BuildConfig.HAS_PLAYLISTS) ? View.GONE : View.VISIBLE);
        if (BuildConfig.HAS_NOTIFICATIONS)
            menuSettingsHelp.setImageDrawable(DataController.INSTANCE.countOfNonReadNotifications() == 0
                ? getResources().getDrawable(R.drawable.ic_view_control)
                : getResources().getDrawable(R.drawable.ic_view_control_red_point));
    }

    @Override
    public void onBackPressed() {
        if (isFABOpen || isCatalogOpen) {
            closeFABMenu();
            closeCatalog();
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if (timeLineFrameLay.getVisibility() == View.VISIBLE) {
            closeTimePreview();
        } else if (currentFragment == favoritesFragment ||
                currentFragment == streamingFragment ||
                currentFragment == songPlaylistsFragment ||
                currentFragment == songFragment) {
            allMult.performClick();
        } else {
            super.onBackPressed();
        }
    }

    private void setupButtons() {
        timeLine.setOnClickListener(view -> {
            showAd();
            if (!silentClick) playSound(R.raw.tap);
            silentClick = false;
            analyticsLogEvent("Пункт экрана", "Кнопка лента времени");
            timeLineFrameLay.setVisibility(View.VISIBLE);
            playSound(R.raw.cloud);
        });
        doskaPocheta.setOnClickListener(view -> {
            showAd();
            if (!silentClick) playSound(R.raw.tap);
            silentClick = false;
            analyticsLogEvent("Пункт экрана", "Кнопка доска почета");
            doskaPochetaFrameLay.setVisibility(View.VISIBLE);
            playSound(R.raw.cloud);
        });
        multStreaming.setOnClickListener(view -> {
            showAd();
            timeLineSelector.setVisibility(View.GONE);
            doskaPochetaSelector.setVisibility(View.GONE);
            prokatSelector.setVisibility(View.GONE);
            allMultSelector.setVisibility(View.GONE);
            favMultSelector.setVisibility(View.GONE);
            songSelector.setVisibility(View.GONE);
            popularSelector.setVisibility(View.GONE);
            multStreamingSelector.setVisibility(View.VISIBLE);
            timeLineText.setTypeface(null, Typeface.NORMAL);
            doskaPochetaText.setTypeface(null, Typeface.NORMAL);
            prokatText.setTypeface(null, Typeface.NORMAL);
            allMultText.setTypeface(null, Typeface.NORMAL);
            favMultText.setTypeface(null, Typeface.NORMAL);
            songText.setTypeface(null, Typeface.NORMAL);
            popularText.setTypeface(null, Typeface.NORMAL);
            multStreamingText.setTypeface(null, Typeface.BOLD);
            timeLineText.setTextColor(textColor);
            doskaPochetaText.setTextColor(textColor);
            prokatText.setTextColor(textColor);
            allMultText.setTextColor(textColor);
            favMultText.setTextColor(textColor);
            songText.setTextColor(textColor);
            popularText.setTextColor(textColor);
            multStreamingText.setTextColor(textColorSelected);
            mainFrameLayout.setBackgroundColor(getResources().getColor(R.color.yellow_back));
            contentHolder.setBackground(getResources().getDrawable(R.drawable.ornament_tiled));
//            В трансляции 2 разных фрагмента. У планшета стандартный фрагмент с 2мя строчками. У смартфона фрагмент, в котором превью мультов
//            сделаны в 2 строчки и опущены ниже, нежели в снадарном - они приблизительно по середине относительно высоты кнопок "каталог" и "настройки"
//            Нужно раскоментировать тут, закрывающую фигурную скобку ниже, а также в функции replace fragment
//            if (isBigScreen) {
//                fragmentContainer.setVisibility(View.GONE);
//                fragmentContainerStreamingMobile.setVisibility(View.VISIBLE);
//            } else {
            fragmentContainer.setVisibility(View.VISIBLE);
            fragmentContainerStreamingMobile.setVisibility(View.GONE);
            btnAllMults.setVisibility(StreamingFragment.isStreamingTime() ? View.VISIBLE : View.GONE);
//            }
            // bis имеет свой кастомный тулбар (прозрачный). Его мы создаем выше. Все остальные имеют стандартный тулбар
            if (!isCurrentApplicationId(BIS))
                bgToolbar.setImageDrawable(getResources().getDrawable(R.drawable.bg_toolbar));
            // eralash на кнопке favMult имеет отличный от других орнамент, а на кнопке allMult имеет отличный от других цветовой оттенок
            if (isCurrentApplicationId(ERALASH)) {
                contentHolder.setBackground(getResources().getDrawable(R.drawable.ornament_tiled));
                mainFrameLayout.setBackgroundColor(getResources().getColor(R.color.yellow_back));
            }
            UserSettingsController.setLastFragmentName(realm,"allMults");
            replaceFragment(streamingFragment);
            if (!silentClick) playSound(R.raw.tap);
            silentClick = false;
            analyticsLogEvent("Пункт экрана", "Кнопка трансляция");
        });
        popular.setOnClickListener(view -> {
            showAd();
            if (!silentClick) playSound(R.raw.tap);
            silentClick = false;
            analyticsLogEvent("Пункт экрана", "Кнопка популярные");
            startActivity(new Intent(this, PopularActivity.class));
        });
        prokat.setOnClickListener(view -> {
            showAd();
            if (!silentClick) playSound(R.raw.tap);
            silentClick = false;
            // каждое приложение имеет свое название раздела "Видеопрокат" и свой соответствующий фрагмент
            if (isCurrentApplicationId(GS)) {
                analyticsLogEvent("Пункт экрана", "Кнопка Еще...");
                startActivity(new Intent(this, ProkatGsActivity.class));
            } else if (isCurrentApplicationId(TOE)){
                analyticsLogEvent("Пункт экрана", "Кнопка Видеопрокат");
                startActivity(new Intent(this, ProkatToeActivity.class));
            }
        });
        allMult.setOnClickListener(view -> {
            showAd();
            timeLineSelector.setVisibility(View.GONE);
            doskaPochetaSelector.setVisibility(View.GONE);
            prokatSelector.setVisibility(View.GONE);
            allMultSelector.setVisibility(View.VISIBLE);
            favMultSelector.setVisibility(View.GONE);
            songSelector.setVisibility(View.GONE);
            multStreamingSelector.setVisibility(View.GONE);
            popularSelector.setVisibility(View.GONE);
            timeLineText.setTypeface(null, Typeface.NORMAL);
            doskaPochetaText.setTypeface(null, Typeface.NORMAL);
            prokatText.setTypeface(null, Typeface.NORMAL);
            allMultText.setTypeface(null, Typeface.BOLD);
            songText.setTypeface(null, Typeface.NORMAL);
            favMultText.setTypeface(null, Typeface.NORMAL);
            multStreamingText.setTypeface(null, Typeface.NORMAL);
            popularText.setTypeface(null, Typeface.NORMAL);
            timeLineText.setTextColor(textColor);
            doskaPochetaText.setTextColor(textColor);
            prokatText.setTextColor(textColor);
            allMultText.setTextColor(textColorSelected);
            favMultText.setTextColor(textColor);
            songText.setTextColor(textColor);
            multStreamingText.setTextColor(textColor);
            popularText.setTextColor(textColor);
            mainFrameLayout.setBackgroundColor(getResources().getColor(R.color.yellow_back));
            contentHolder.setBackground(getResources().getDrawable(R.drawable.ornament_tiled));
            fragmentContainer.setVisibility(View.VISIBLE);
            fragmentContainerStreamingMobile.setVisibility(View.GONE);
            btnAllMults.setVisibility(View.VISIBLE);
            // bis имеет свой кастомный тулбар (прозрачный). Его мы создаем выше. Все остальные имеют стандартный тулбар
            if (!isCurrentApplicationId(BIS))
                bgToolbar.setImageDrawable(getResources().getDrawable(R.drawable.bg_toolbar));
            // eralash на кнопке favMult имеет отличный от других орнамент, а на кнопке allMult(тут) имеет отличный от других цветовой оттенок
            if (isCurrentApplicationId(ERALASH)) {
                contentHolder.setBackground(getResources().getDrawable(R.drawable.ornament_tiled));
                mainFrameLayout.setBackgroundColor(getResources().getColor(R.color.yellow_back_eralash_playlists));
            }
            UserSettingsController.setLastFragmentName(realm,"allMults");
            if (DataLoader.hasPlaylists()) {
                replaceFragment(playlistsFragment);
            } else {
                replaceFragment(videoFragment);
            }
            if (!silentClick) playSound(R.raw.tap);
            silentClick = false;
            analyticsLogEvent("Пункт экрана", "Кнопка мультфильмы");
        });
        song.setOnClickListener(view -> {
            showAd();
            timeLineSelector.setVisibility(View.GONE);
            doskaPochetaSelector.setVisibility(View.GONE);
            prokatSelector.setVisibility(View.GONE);
            allMultSelector.setVisibility(View.GONE);
            favMultSelector.setVisibility(View.GONE);
            songSelector.setVisibility(View.VISIBLE);
            multStreamingSelector.setVisibility(View.GONE);
            popularSelector.setVisibility(View.GONE);
            timeLineText.setTypeface(null, Typeface.NORMAL);
            doskaPochetaText.setTypeface(null, Typeface.NORMAL);
            prokatText.setTypeface(null, Typeface.NORMAL);
            allMultText.setTypeface(null, Typeface.NORMAL);
            songText.setTypeface(null, Typeface.BOLD);
            favMultText.setTypeface(null, Typeface.NORMAL);
            multStreamingText.setTypeface(null, Typeface.NORMAL);
            popularText.setTypeface(null, Typeface.NORMAL);
            timeLineText.setTextColor(textColor);
            doskaPochetaText.setTextColor(textColor);
            prokatText.setTextColor(textColor);
            allMultText.setTextColor(textColor);
            favMultText.setTextColor(textColor);
            songText.setTextColor(textColorSelected);
            multStreamingText.setTextColor(textColor);
            popularText.setTextColor(textColor);
            mainFrameLayout.setBackgroundColor(getResources().getColor(R.color.violet_song_background));
            contentHolder.setBackground(getResources().getDrawable(R.drawable.ornament_tiled_song));
            fragmentContainer.setVisibility(View.VISIBLE);
            fragmentContainerStreamingMobile.setVisibility(View.GONE);
            btnAllMults.setVisibility(View.VISIBLE);
            // bis имеет свой кастомный тулбар (прозрачный). Его мы создаем выше. Все остальные имеют стандартный тулбар
            if (!isCurrentApplicationId(BIS))
                bgToolbar.setImageDrawable(getResources().getDrawable(R.drawable.bg_toolbar));
            // eralash на кнопке favMult имеет отличный от других орнамент, а на кнопке allMult имеет отличный от других цветовой оттенок
            if (isCurrentApplicationId(ERALASH)) {
                contentHolder.setBackground(getResources().getDrawable(R.drawable.ornament_tiled));
                mainFrameLayout.setBackgroundColor(getResources().getColor(R.color.yellow_back));
            }
            UserSettingsController.setLastFragmentName(realm,"song");
            if (BuildConfig.HAS_SONG_PLAYLISTS) replaceFragment(songPlaylistsFragment);
            else replaceFragment(songFragment);
            if (!silentClick) playSound(R.raw.tap);
            silentClick = false;
            analyticsLogEvent("Пункт экрана", "Кнопка песни");
        });
        favMult.setOnClickListener(view -> {
            showAd();
            timeLineSelector.setVisibility(View.GONE);
            doskaPochetaSelector.setVisibility(View.GONE);
            prokatSelector.setVisibility(View.GONE);
            allMultSelector.setVisibility(View.GONE);
            favMultSelector.setVisibility(View.VISIBLE);
            songSelector.setVisibility(View.GONE);
            multStreamingSelector.setVisibility(View.GONE);
            popularSelector.setVisibility(View.GONE);
            timeLineText.setTypeface(null, Typeface.NORMAL);
            doskaPochetaText.setTypeface(null, Typeface.NORMAL);
            prokatText.setTypeface(null, Typeface.NORMAL);
            allMultText.setTypeface(null, Typeface.NORMAL);
            songText.setTypeface(null, Typeface.NORMAL);
            favMultText.setTypeface(null, Typeface.BOLD);
            multStreamingText.setTypeface(null, Typeface.NORMAL);
            popularText.setTypeface(null, Typeface.NORMAL);
            timeLineText.setTextColor(textColor);
            doskaPochetaText.setTextColor(textColor);
            prokatText.setTextColor(textColor);
            allMultText.setTextColor(textColor);
            favMultText.setTextColor(textColorSelected);
            songText.setTextColor(textColor);
            multStreamingText.setTextColor(textColor);
            popularText.setTextColor(textColor);
            mainFrameLayout.setBackgroundColor(getResources().getColor(R.color.yellow_back));
            contentHolder.setBackground(getResources().getDrawable(R.drawable.ornament_tiled));
            fragmentContainer.setVisibility(View.VISIBLE);
            fragmentContainerStreamingMobile.setVisibility(View.GONE);
            btnAllMults.setVisibility(View.VISIBLE);
            // bis имеет свой кастомный тулбар (прозрачный). Его мы создаем выше. Все остальные имеют стандартный тулбар
            if (!isCurrentApplicationId(BIS)) {
                // toe на кнопке favMult(тут) имеет отличный от других bgToolbar
                if (isCurrentApplicationId(TOE))
                    bgToolbar.setImageDrawable(getResources().getDrawable(R.drawable.bg_toolbar_favourite));
                else bgToolbar.setImageDrawable(getResources().getDrawable(R.drawable.bg_toolbar));
            }
            // eralash на кнопке favMult(тут) имеет отличный от других орнамент, а на кнопке allMult имеет отличный от других цветовой оттенок
            if (isCurrentApplicationId(ERALASH)) {
                contentHolder.setBackground(getResources().getDrawable(R.drawable.ornament_favourite));
                mainFrameLayout.setBackgroundColor(getResources().getColor(R.color.yellow_back));
            }
            UserSettingsController.setLastFragmentName(realm,"allMults");
            replaceFragment(favoritesFragment);
            if (!silentClick) playSound(R.raw.tap);
            silentClick = false;
            analyticsLogEvent("Пункт экрана", "Кнопка любимые");
        });
        btnShop.setOnClickListener(view -> {
            playSound(R.raw.tap);
            String from = (currentFragment == favoritesFragment) ? PaymentActivity.FROM_LIBIMIE : PaymentActivity.FROM_GLAVNI_EKRAN;
            PaymentActivity.start(PlaylistsActivity.this, from);
        });
        downloadSetBtn.setOnClickListener(view -> {
            Log.i("VideoActivity", "DownloadSet");
            Playlist playlist = DataController.INSTANCE.getPlaylist(realm, 1);
            DialogUtils.showDownloadDialog(PlaylistsActivity.this, playlist);
            playSound(R.raw.tap);
        });
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (Subscription sub : subscriptionArrayList)
            try {
                sub.unsubscribe();
            } catch (Exception e) {
                e.printStackTrace();
            }
        subscriptionArrayList.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscriptionArrayList.add(timer.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> updateUI(),
                        e -> System.out.println("Error: " + e),
                        () -> System.out.println("Completed")));
        UserSettingsController.setShowAd(realm, !userSettings.isPaid());
    }

    @Override
    public String getScreenName() {
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        resCode = resultCode;
    }

    private void replaceFragment(BaseAnalyticsFragment newFragment) {
        analyticsOpenScreen(newFragment.getScreenName());

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        Разные фрагменты для планшета и смартфона в трансляции
//        if (isBigScreen && newFragment == streamingFragment)
//            transaction.replace(R.id.fragment_container_streaming_mobile, newFragment);
//        else
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.commit();
        currentFragment = newFragment;
    }

    @Override
    protected void onDestroy() {
        if (video != null) video.removeAllChangeListeners();
        super.onDestroy();
        for (Subscription sub : subscriptionArrayList)
            try {
                sub.unsubscribe();
            } catch (Exception e) {
                e.printStackTrace();
            }
        subscriptionArrayList.clear();
    }
}