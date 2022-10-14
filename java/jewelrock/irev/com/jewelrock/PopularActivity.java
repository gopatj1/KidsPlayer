package jewelrock.irev.com.jewelrock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmResults;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.controller.DataLoader;
import jewelrock.irev.com.jewelrock.controller.RateThisAppQuestController;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.navigators.AllVideoActions;
import jewelrock.irev.com.jewelrock.subscribe.PaymentActivity;
import jewelrock.irev.com.jewelrock.ui.adapters.AllVideoAdapter;
import jewelrock.irev.com.jewelrock.utils.DialogUtils;
import rx.Observable;
import rx.Subscription;

import static jewelrock.irev.com.jewelrock.utils.Constants.LES;
import static jewelrock.irev.com.jewelrock.utils.Constants.TOE;

public class PopularActivity extends BaseRealmActivity implements AllVideoActions {
    @BindView(R.id.main_frame_layout_activity_popular)
    FrameLayout mainFrameLayout;
    @BindView(R.id.sampleRecyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.btn_back)
    ImageButton backBtn;
    @BindView(R.id.popular_toolbar)
    ImageView popularToolbar;

    @BindView(R.id.motivator22frameLay)
    FrameLayout motivator22frameLay;
    private ArrayList<ImageView> allBanners = new ArrayList<>();

    private AllVideoAdapter adapter;

    private Observable<Long> timer = Observable.interval(5, TimeUnit.SECONDS);
    private ArrayList<Subscription> subscriptionArrayList = new ArrayList<>();

    @OnClick(R.id.btnCloseMotivator22)
    void closeMotivator22(View button) {
        playSound(R.raw.tap);
        motivator22frameLay.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular);
        ButterKnife.bind(this);

        loadUserSettings();
        loadData();
        backBtn.setOnClickListener(view -> {
            playSound(R.raw.tap);
            finish();
        });
        if (isCurrentApplicationId(TOE)) {
            popularToolbar.setBackgroundColor(getResources().getColor(R.color.violet));
            popularToolbar.setImageDrawable(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        playSound(R.raw.tap);

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    private void loadUserSettings() {
        userSettings.addChangeListener(element -> updateUI());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initGallery(List<Video> videos) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new AllVideoAdapter(videos).setPaid(userSettings.isPaid()).setPopularStatus(true).setActions(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                playSound(R.raw.scroll);
            }
            return false;
        });
    }

    @SuppressLint("CheckResult")
    private void loadData() {
        DataLoader.updateMainData(BuildConfig.BASE_URL)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                }, Throwable::printStackTrace);
        DataLoader.updateSongMainData()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                }, Throwable::printStackTrace);
        RealmResults<Video> videos = DataController.INSTANCE.getPopularVideos(realm);
        ArrayList<Video> videosForAdapter = new ArrayList<>(Objects.requireNonNull(videos));
        while (videosForAdapter.size() > 20)
            videosForAdapter.remove(20);
        initGallery(videosForAdapter);
        videos.removeAllChangeListeners();
        videos.addChangeListener(element -> updateUI());
        updateUI();
        subscriptionArrayList.add(timer.subscribeOn(rx.schedulers.Schedulers.newThread())
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
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
        adapter.setPaid(userSettings.isPaid()).notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onPause() {
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
    public String getScreenName() {
        return "PopularFragment";
    }

    @Override
    public void changeLikeStatusOfVideo(Video video) {
        playSound(R.raw.tap);
        if (!video.isFavorite() && DataController.INSTANCE.getFavoriteVideos(realm).size() >= 2 && !userSettings.isPaid()){
            PaymentActivity.start(this, PaymentActivity.FROM_PLEER_LUBIMIE);
            return;
        }
        DataController.INSTANCE.setFavorite(this, video, !video.isFavorite());
        RateThisAppQuestController.addFavorite();
        updateUI();
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
        intent.putExtra("isRewarding", true);
        if (BuildConfig.HAS_REWARDING_AD)
            if (isCurrentApplicationId(LES)) {
                if (showRewardedAd(video.getId()))
                    return;
            } else intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FOR_REWARDING_VIDEO);
        else intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FROM_PERECHEN_MULTOV);
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
        intent.putExtra("playlistId", -4);
        intent.putExtra("videoId", video.getId());
        startActivity(intent);
    }
}