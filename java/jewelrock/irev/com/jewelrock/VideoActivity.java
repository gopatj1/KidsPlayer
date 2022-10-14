package jewelrock.irev.com.jewelrock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.model.Playlist;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.subscribe.PaymentActivity;
import jewelrock.irev.com.jewelrock.ui.adapters.PlaylistVideoAdapter;
import jewelrock.irev.com.jewelrock.utils.DialogUtils;
import jewelrock.irev.com.jewelrock.utils.SpeedyGridLayoutManager;
import jewelrock.irev.com.jewelrock.utils.SpeedyLinearLayoutManager;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static jewelrock.irev.com.jewelrock.utils.Constants.BIS;
import static jewelrock.irev.com.jewelrock.utils.Constants.LES;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoActivity extends BaseFabActivity {
    @BindView(R.id.main_frame_layout_activity_video)
    FrameLayout mainFrameLayout;
    @BindView(R.id.sampleRecyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.bg_toolbar)
    ImageView bgToolbar;
    @BindDrawable(R.drawable.rectangle_invisible_16dp)
    Drawable rectangle;
    @BindView(R.id.btn_download_set)
    View downloadSetBtn;
    @BindView(R.id.btn_shop)
    View btnShop;

    @BindView(R.id.seek_bar_gallery_recucler_view)
    SeekBar seekBarGalleryRecView;
    @BindView(R.id.btn_back)
    ImageButton backBtn;
    @BindDrawable(R.drawable.tablet_ic_back_blue)
    Drawable backForBigScreenDrawable;

    @BindView(R.id.motivator22frameLay)
    FrameLayout motivator22frameLay;
    @BindView(R.id.menu_floating)
    View menuFloatingInPlaylist;
    @BindView(R.id.menu)
    ImageView menuSettingsHelp;

    private PlaylistVideoAdapter adapter;
    private Playlist playlist;
    private ArrayList<ImageView> allBanners = new ArrayList<>();

    private Observable<Long> timer = Observable.interval(5, TimeUnit.SECONDS);
    private ArrayList<Subscription> subscriptionArrayList = new ArrayList<>();

    @OnClick(R.id.all_mults)
    void openAllMult() {
        playSound(R.raw.tap);
        Intent intent = new Intent(this, AllVideosActivity.class);
        if (getIntent().getBooleanExtra("isSongPlaylist", false))
            intent.putExtra("fromFragment", "songFragment");
        else
            intent.putExtra("fromFragment", "videoFragment");
        startActivity(intent);
        showAd();
    }

    @OnClick(R.id.btnCloseMotivator22)
    void closeMotivator22() {
        playSound(R.raw.tap);
        motivator22frameLay.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);
        setupFabAndCatalogMenu();
        setupButtons();
        loadUserSettings();
        loadData();
        seekBarGalleryRecView.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBarOnProgressChanged();
        startAutoScrollRecyclerView(4);
        updateUI();
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
        } else bgToolbar.setImageDrawable(getResources().getDrawable(R.drawable.bg_toolbar));
    }

    private void loadUserSettings() {
        userSettings.addChangeListener(element -> updateUI());
    }

    @SuppressLint({"ClickableViewAccessibility", "CheckResult"})
    private void initGallery(List<Video> playlists) {
        if (!isBigScreen) {
            recyclerView.setLayoutManager(new SpeedyLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        } else {
            recyclerView.setLayoutManager(new SpeedyGridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false));
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    LinearLayoutManager.VERTICAL);
            dividerItemDecoration.setDrawable(rectangle);
            recyclerView.addItemDecoration(dividerItemDecoration);
        }
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                LinearLayoutManager.HORIZONTAL);
        dividerItemDecoration.setDrawable(rectangle);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter = new PlaylistVideoAdapter(playlists, false)
                .setPaid(userSettings.isPaid());
        adapter.setOnItemClick((item, pos) -> {
            if (!item.isFree() && !userSettings.isPaid() && item.getVideoPreview() == null) {
                Intent intent = new Intent(VideoActivity.this, PaymentActivity.class);
                intent.putExtra("videoId", item.getId());
                intent.putExtra("isRewarding", true);
                if (BuildConfig.HAS_REWARDING_AD)
                    if (isCurrentApplicationId(LES)) {
                        if (showRewardedAd(item.getId()))
                            return;
                    } else intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FOR_REWARDING_VIDEO);
                else intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FROM_NABOR);
                startActivity(intent);
                return;
            }
            @SuppressLint("SimpleDateFormat")
            int currentTime = Integer.parseInt(new SimpleDateFormat("HHmmss").format(new Date()));
            if (userSettings.isPaid() && userSettings.isShowMotivatorBauBay22() && (currentTime > 220000 || currentTime < 70000)) {
                playSound(R.raw.cloud);
                motivator22frameLay.setVisibility(View.VISIBLE);
                io.reactivex.Observable.timer(10, TimeUnit.SECONDS)
                        .subscribeOn(io.reactivex.schedulers.Schedulers.newThread())
                        .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                        .subscribe(aLong -> motivator22frameLay.setVisibility(View.GONE));
                return;
            }
            Intent intent = new Intent(VideoActivity.this, PlayerActivity.class);
            intent.putExtra("playlistId", getIntent().getIntExtra("id", 0));
            intent.putExtra("videoId", item.getId());
            if (getIntent().getBooleanExtra("isSongPlaylist", false))
                intent.putExtra("isSongPlaylist", true);
            playSound(R.raw.tap);
            startActivity(intent);
        });
        adapter.setOnDownloadClick((video, pos) -> {
            playSound(R.raw.tap);
            if (video.getVideoLoadingProgress() < 0) {
                DialogUtils.showDownloadDialog(VideoActivity.this, video, dialogInterface -> {
                    updateUI();
                    subscriptionArrayList.add(timer.subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(s -> updateUI(),
                                    e -> System.out.println("Error: " + e),
                                    () -> System.out.println("Completed")));
                });
            } else {
                DialogUtils.showCancelLoadingDialog(VideoActivity.this, video, dialogInterface -> updateUI());
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                playSound(R.raw.scroll);
            }
            return false;
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisible = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                if (firstVisible == RecyclerView.NO_POSITION) return;
                seekBarGalleryRecView.setProgress(firstVisible);
                if (seekBarGalleryRecView.getMax() - firstVisible <= 1 && !isBigScreen)
                    seekBarGalleryRecView.setProgress(seekBarGalleryRecView.getMax());
                if (seekBarGalleryRecView.getMax() - firstVisible <= 2 && isBigScreen)
                    seekBarGalleryRecView.setProgress(seekBarGalleryRecView.getMax());
            }
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) startAutoScrollRecyclerView(2);
            }
        });
    }


    private void loadData() {
        playlist = DataController.INSTANCE.getPlaylist(realm, getIntent().getIntExtra("id", 0));
        title.setText(Objects.requireNonNull(playlist).getName().replace("\\n", " "));
        initGallery(playlist.getVideos());
        playlist.addChangeListener(element -> updateUI());
    }

    private void setupButtons() {
        menuFloatingInPlaylist.setVisibility(View.VISIBLE);
        if (isBigScreen) {
            backBtn.setImageDrawable(backForBigScreenDrawable);
        }
        downloadSetBtn.setOnClickListener(view -> {
            Log.i("VideoActivity", "DownloadSet");
            DialogUtils.showDownloadDialog(VideoActivity.this, playlist, dialogInterface -> {
                updateUI();
                subscriptionArrayList.add(timer.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(s -> updateUI(),
                                e -> System.out.println("Error: " + e),
                                () -> System.out.println("Completed")));
            });
            playSound(R.raw.tap);
        });
        backBtn.setOnClickListener(view -> {
            playSound(R.raw.tap);
            finish();
            showAd();
        });
        btnShop.setOnClickListener(view -> {
            playSound(R.raw.tap);
            PaymentActivity.start(VideoActivity.this, PaymentActivity.FROM_NABOR);
        });
    }

    private void updateUI() {
        if (allBanners.isEmpty() || (allBanners.get(0).getY() <= -BaseFabActivity.displayMetrics.heightPixels / 2.5
                || allBanners.get(0).getY() >= BaseFabActivity.displayMetrics.heightPixels * 1.2 || allBanners.get(0).getY() == 0.0)) {
            ImageView banner = addBanner(this);
            allBanners.clear();
            allBanners.add(banner);
            mainFrameLayout.addView(banner);
        }
        boolean paid = userSettings.isPaid();
        btnShop.setVisibility(paid ? View.GONE : View.VISIBLE);
        adapter.setPaid(paid).notifyDataSetChanged();

        boolean isAllLoading = true;
        for (Video v : adapter.getmItems()) {
            if (v.getVideoLoadingProgress() < 0) {
                isAllLoading = false;
                break;
            }
        }
        downloadSetBtn.setVisibility(paid && !isAllLoading ? View.VISIBLE : View.GONE);

        if (isBigScreen)
            if (recyclerView.getAdapter().getItemCount() <= 8) seekBarGalleryRecView.setVisibility(View.GONE);
            else seekBarGalleryRecView.setVisibility(View.VISIBLE);
        else if (recyclerView.getAdapter().getItemCount() <= 5) seekBarGalleryRecView.setVisibility(View.GONE);
            else seekBarGalleryRecView.setVisibility(View.VISIBLE);

        if (BuildConfig.HAS_NOTIFICATIONS)
            menuSettingsHelp.setImageDrawable(DataController.INSTANCE.countOfNonReadNotifications() == 0
                ? getResources().getDrawable(R.drawable.ic_view_control)
                : getResources().getDrawable(R.drawable.ic_view_control_red_point));
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING ||
                recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_SETTLING) return;
            seekBarOnProgressChanged();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING ||
                    recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_SETTLING)
                recyclerView.stopScroll();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            startAutoScrollRecyclerView(2);
        }
    };

    public void seekBarOnProgressChanged(){
        if (!isBigScreen) seekBarGalleryRecView.setMax(recyclerView.getAdapter().getItemCount() - 1);
        else seekBarGalleryRecView.setMax(recyclerView.getAdapter().getItemCount() - 4);
        recyclerView.scrollToPosition(seekBarGalleryRecView.getProgress());
    }

    @SuppressLint("CheckResult")
    public void startAutoScrollRecyclerView(int delay){
        if (!userSettings.isAutoScrollSeekBar()) return;
        io.reactivex.Observable.timer(delay, TimeUnit.SECONDS).subscribeOn(io.reactivex.schedulers.Schedulers.newThread())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    try {
                        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING ||
                                recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_SETTLING ||
                                seekBarGalleryRecView.getVisibility() == View.GONE) return;
                        if (seekBarGalleryRecView.getProgress() == seekBarGalleryRecView.getMax())
                            recyclerView.smoothScrollToPosition(0);
                        else recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscriptionArrayList.add(timer.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> updateUI(),
                        e -> System.out.println("Error: " + e),
                        () -> System.out.println("Completed")));
        updateUI();
        recyclerView.stopScroll();
        startAutoScrollRecyclerView(2);
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
    protected void onDestroy() {
        playlist.removeAllChangeListeners();
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
    public String getScreenName() {
        return "Мультфильмы группы";
    }

    @Override
    public void onBackPressed() {
        if (isFABOpen || isCatalogOpen) {
            closeFABMenu();
            closeCatalog();
            return;
        } else
            super.onBackPressed();
        showAd();
    }
}
