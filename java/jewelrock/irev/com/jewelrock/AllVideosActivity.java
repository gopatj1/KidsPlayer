package jewelrock.irev.com.jewelrock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.RealmResults;
import jewelrock.irev.com.jewelrock.api.Api;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.controller.DataLoader;
import jewelrock.irev.com.jewelrock.controller.RateThisAppQuestController;
import jewelrock.irev.com.jewelrock.model.Playlist;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.navigators.AllPlaylistActions;
import jewelrock.irev.com.jewelrock.navigators.AllVideoActions;
import jewelrock.irev.com.jewelrock.subscribe.PaymentActivity;
import jewelrock.irev.com.jewelrock.ui.adapters.AllPlaylistsAdapter;
import jewelrock.irev.com.jewelrock.ui.adapters.AllVideoAdapter;
import jewelrock.irev.com.jewelrock.utils.DialogUtils;
import jewelrock.irev.com.jewelrock.utils.UIUtils;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static jewelrock.irev.com.jewelrock.utils.Constants.ERALASH;
import static jewelrock.irev.com.jewelrock.utils.Constants.LES;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AllVideosActivity extends BaseRealmActivity implements AllVideoActions, AllPlaylistActions {

    @BindView(R.id.main_frame_layout_activity_all_videos)
    FrameLayout mainFrameLayout;
    @BindView(R.id.content_holder)
    View contentHolder;

    @BindView(R.id.sampleRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.empty)
    TextView emptyHolder;
    @BindView(R.id.appBar)
    AppBarLayout appBar;

    @BindView(R.id.searchText)
    EditText searchText;

    @BindView(R.id.motivator22frameLay)
    FrameLayout motivator22frameLay;
    private ArrayList<ImageView> allBanners = new ArrayList<>();

    private Observable<Long> timer = Observable.interval(5, TimeUnit.SECONDS);
    private ArrayList<Subscription> subscriptionArrayList = new ArrayList<>();

    private AllVideoAdapter adapter;
    private AllPlaylistsAdapter allPlaylistsAdapter;

    private RealmResults<Video> videos;
    private RealmResults<Playlist> playlists;

    @OnClick(R.id.btn_cancel)
    void onCancelSearchClick() {
        appBar.setExpanded(false, true);
        searchText.setText("");
        UIUtils.hideKeyboard(this);
    }

    @OnClick(R.id.btnCloseMotivator22)
    void closeMotivator22() {
        playSound(R.raw.tap);
        motivator22frameLay.setVisibility(View.GONE);
    }

    @OnClick(R.id.close_catalog_activity)
    void closeAllVideosActivity() {
        playSound(R.raw.tap);
        finish();
    }

    public static void start(Context context, String fromFragment) {
        Intent intent = new Intent(context, AllVideosActivity.class);
        intent.putExtra("fromFragment", fromFragment);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_videos);
        ButterKnife.bind(this);

        initToolbar();
        loadUserSettings();
        loadData("");

        KeyboardVisibilityEvent.setEventListener(
                this,
                isOpen -> {
                    // some code depending on keyboard visibility status
                    contentHolder.animate().translationY(!isOpen ? 0 : -getResources().getDimension(R.dimen.actionbar_height)).start();
                });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        playSound(R.raw.tap);

        if (id == android.R.id.home) {
            PlaylistsActivity.start(this, true, true);
            finish();
            finishAffinity();
            return true;
        }
        return false;
    }

    private void initToolbar() {
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.search_cancel_color));
        collapsingToolbarLayout.setTitleEnabled(false);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
    }

    private void loadUserSettings() {
        userSettings.addChangeListener(element -> updateUI());
    }

    private void initGallery(List<Video> videos) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new AllVideoAdapter(videos)
                .setPaid(userSettings.isPaid())
                .setStreaming(getIntent().getStringExtra("fromFragment").equals("streamingFragment"))
                .setActions(this);
        recyclerView.setAdapter(adapter);
    }

    private void initGalleryByPlaylists(List<Playlist> playlists) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        allPlaylistsAdapter = new AllPlaylistsAdapter(playlists)
                .setPaid(userSettings.isPaid())
                .setActions(this);
        recyclerView.setAdapter(allPlaylistsAdapter);
    }

    private void loadData(String text) {
        switch (getIntent().getStringExtra("fromFragment")) {
            case "playlistsFragment":
                playlists = DataController.INSTANCE.getMainPlaylists(realm, "name", text);
                Objects.requireNonNull(getSupportActionBar()).setTitle("Перечень наборов");
                break;
            case "videoFragment":
                videos = DataController.INSTANCE.getMainVideos(realm, "name", text);
                Objects.requireNonNull(getSupportActionBar()).setTitle(isCurrentApplicationId(ERALASH) || isCurrentApplicationId(LES)
                        ? "Перечень сюжетов" : "Перечень мультиков");
                break;
            case "favoritesFragment":
                videos = DataController.INSTANCE.getFavoriteVideos(realm, "name", text);
                Objects.requireNonNull(getSupportActionBar()).setTitle("Перечень любимых");
                break;
            case "streamingFragment":
                if (!userSettings.getLastStreamingVideoId().isEmpty())
                    videos = DataController.INSTANCE.getStreamingVideos(realm, "name", text);
                Objects.requireNonNull(getSupportActionBar()).setTitle("Перечень мультфильмов трансляции");
                break;
            case "songFragment":
                videos = DataController.INSTANCE.getSongVideos(realm, "name", text);
                Objects.requireNonNull(getSupportActionBar()).setTitle("Перечень песен");
                break;
            case "vipuskiPlaylistsFragment":
                playlists = DataController.INSTANCE.getVipuskiPlaylists(realm, text);
                Objects.requireNonNull(getSupportActionBar()).setTitle("Перечень выпусков");
                break;
            case "personsPlaylistsFragment":
                playlists = DataController.INSTANCE.getPersonsPlaylists(realm, "name", text);
                Objects.requireNonNull(getSupportActionBar()).setTitle("Перечень персон");
                break;
        }
        if (videos != null && !videos.isEmpty()) {
            initGallery(videos);
            videos.removeAllChangeListeners();
            videos.addChangeListener(element -> updateUI());
        } else if (playlists != null && !playlists.isEmpty()) {
            initGalleryByPlaylists(playlists);
            playlists.removeAllChangeListeners();
            playlists.addChangeListener(element -> updateUI());
        }
        updateUI();
        subscriptionArrayList.add(timer.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> updateUI(),
                        e -> System.out.println("Error: " + e),
                        () -> System.out.println("Completed")));
    }

    private void updateUI() {
        if (allBanners.isEmpty() || (allBanners.get(0).getY() <= -BaseFabActivity.displayMetrics.heightPixels / 2.5
                || allBanners.get(0).getY() >= BaseFabActivity.displayMetrics.heightPixels * 1.2 || allBanners.get(0).getY() == 0.0)) {
            ImageView banner = addBanner(this);
            allBanners.clear();
            allBanners.add(banner);
            mainFrameLayout.addView(banner);
        }
        emptyHolder.setText(searchText.getText().toString().equals("") ? "Раздел пуст" : getResources().getString(R.string.no_mults));
        if (videos != null) emptyHolder.setVisibility(videos.size() > 0 ? View.GONE : View.VISIBLE);
        else if (playlists != null) {
            emptyHolder.setVisibility(playlists.size() > 0 ? View.GONE : View.VISIBLE);
            emptyHolder.setText(getResources().getString(R.string.no_mults_nabor));
        }
        if (adapter != null) {
            adapter.setPaid(userSettings.isPaid()).notifyDataSetChanged();
            recyclerView.setVisibility(videos != null && videos.size() <= 0 ? View.GONE : View.VISIBLE);
        } else if (allPlaylistsAdapter != null) {
            allPlaylistsAdapter.setPaid(userSettings.isPaid()).notifyDataSetChanged();
            recyclerView.setVisibility(playlists != null && playlists.size() <= 0 ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public String getScreenName() {
        switch (getIntent().getStringExtra("fromFragment")) {
            case "playlistsFragment":
                return ("Перечень наборов");
            case "videoFragment":
                return (isCurrentApplicationId(ERALASH) || isCurrentApplicationId(LES) ? "Перечень сюжетов" : "Перечень мультиков");
            case "favoritesFragment":
                return ("Перечень любимых");
            case "streamingFragment":
                return ("Перечень мультфильмов трансляции");
            case "songFragment":
                return ("Перечень песен");
            case "vipuskiPlaylistsFragment":
                return ("Перечень выпусков");
            case "personsPlaylistsFragment":
                return ("Перечень персон");
        }
        return (isCurrentApplicationId(ERALASH) || isCurrentApplicationId(LES) ? "Перечень сюжетов" : "Перечень мультиков");
    }

    ///////////////////////// action for video /////////////////////////////////////////////////////
    @Override
    public void changeLikeStatusOfVideo(Video video) {
        playSound(R.raw.tap);
        if (!video.isFavorite() && DataController.INSTANCE.getFavoriteVideos(realm).size() >= 2 && !userSettings.isPaid()) {
            PaymentActivity.start(this, PaymentActivity.FROM_PLEER_LUBIMIE);
            return;
        }
        DataController.INSTANCE.setFavorite(this, video, !video.isFavorite());
        RateThisAppQuestController.addFavorite();
    }

    @Override
    public void deleteVideo(Video video) {
        playSound(R.raw.tap);
        ArrayList<Video> videos = new ArrayList<>();
        videos.add(video);
        DialogUtils.showDeleteDialog(this, videos, view -> updateUI());
    }

    @Override
    public void buyVideo(Video video) {
        playSound(R.raw.tap);
        Intent intent = new Intent(getBaseContext(), PaymentActivity.class);
        intent.putExtra("videoId", video.getId());
        if (BuildConfig.HAS_REWARDING_AD) {
            intent.putExtra("isRewarding", true);
            if (isCurrentApplicationId(LES)) {
                if (showRewardedAd(video.getId()))
                    return;
            } else intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FOR_REWARDING_VIDEO);
        } else intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FROM_PERECHEN_MULTOV);

        startActivity(intent);
    }

    @Override
    public void downloadVideo(Video video) {
        playSound(R.raw.tap);
        if (video.getVideoLoadingProgress() < 0) {
            DialogUtils.showDownloadDialog(this, video);
        } else {
            DialogUtils.showCancelLoadingDialog(this, video, dialogInterface -> updateUI());
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void openVideo(Video video) {
        hideKeyboard(this);
        playSound(R.raw.tap);
        @SuppressLint("SimpleDateFormat") int currentTime = Integer.parseInt(new SimpleDateFormat("HHmmss").format(new Date()));
        if (userSettings.isPaid() && userSettings.isShowMotivatorBauBay22() && (currentTime > 220000 || currentTime < 70000)) {
            playSound(R.raw.cloud);
            motivator22frameLay.setVisibility(View.VISIBLE);
            io.reactivex.Observable.timer(10, TimeUnit.SECONDS)
                    .subscribeOn(io.reactivex.schedulers.Schedulers.newThread())
                    .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                    .subscribe(aLong -> motivator22frameLay.setVisibility(View.GONE));
            return;
        }
        Intent intent = new Intent(this, PlayerActivity.class);
        switch (getIntent().getStringExtra("fromFragment")) {
            case "favoritesFragment":
                intent.putExtra("playlistId", -1);
                break;
            case "streamingFragment":
                intent.putExtra("playlistId", -2);
                break;
            case "songFragment":
                intent.putExtra("playlistId", -3);
                break;
            default:
                intent.putExtra("playlistId", video.getPlaylist().getId());
        }
        intent.putExtra("videoId", video.getId());
        if (video.getId() < 0 && BuildConfig.HAS_SONG_PLAYLISTS && !getIntent()
                .getStringExtra("fromFragment").equals("favoritesFragment")) // is song video but not in favourite list
            intent.putExtra("isSongPlaylist", true);
        startActivity(intent);
    }

    ///////////////////////// action for playlist //////////////////////////////////////////////////
    @Override
    public void deletePlaylist(Playlist playlist) {
        playSound(R.raw.tap);
        ArrayList<Video> videos = new ArrayList<>(playlist.getVideos());
        DialogUtils.showDeleteDialog(this, videos, view -> updateUI());
    }

    @Override
    public void buyPlaylist(Playlist playlist) {
        Intent intent = new Intent(getBaseContext(), PaymentActivity.class);
        intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FROM_PERECHEN_MULTOV);
        startActivity(intent);
    }

    @Override
    public void downloadPlaylist(Playlist playlist) {
        boolean isLoading = true;
        for (Video v : playlist.getVideos())
            if (v.getVideoLoadingProgress() < 0) isLoading = false;
        if (!isLoading)
            DialogUtils.showDownloadDialog(this, playlist, dialogInterface -> updateUI());
        else
            DialogUtils.showCancelLoadingDialog(this, playlist.getVideos(), dialogInterface -> updateUI());
    }

    @Override
    public void openPlaylist(Playlist playlist) {
        hideKeyboard(this);
        playSound(R.raw.tap);
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra("id", playlist.getId());
        if (playlist.getId() < 0 && BuildConfig.HAS_SONG_PLAYLISTS)
            intent.putExtra("isSongPlaylist", true);
        startActivity(intent);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void hideKeyboard(Activity activity) {
        if (activity == null) return;
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getWindow().getDecorView();
        if (view == null) return;
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
        recyclerView.stopScroll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        for (Subscription sub : subscriptionArrayList)
            try {
                sub.unsubscribe();
            } catch (Exception e) {
                e.printStackTrace();
            }
        subscriptionArrayList.clear();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        playSound(R.raw.tap);
        PlaylistsActivity.start(this, true, true);
        finish();
        finishAffinity();
    }
}
