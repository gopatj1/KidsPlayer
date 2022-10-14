package jewelrock.irev.com.jewelrock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindBool;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmResults;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.controller.UserSettingsController;
import jewelrock.irev.com.jewelrock.model.UserSettings;
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

import static jewelrock.irev.com.jewelrock.BaseRealmActivity.isCurrentApplicationId;
import static jewelrock.irev.com.jewelrock.utils.Constants.LES;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoFragment extends BaseAnalyticsFragment {

    @BindView(R.id.sampleRecyclerView)
    RecyclerView recyclerView;
    @BindDrawable(R.drawable.rectangle_invisible_16dp)
    Drawable rectangle;

    @BindView(R.id.seek_bar_gallery_recucler_view)
    SeekBar seekBarGalleryRecView;

    @BindBool(R.bool.isBigScreen)
    boolean isBigScreen;

    private PlaylistVideoAdapter adapter;

    protected UserSettings userSettings;
    private Realm realm;
    private Unbinder unbinder;
    private RealmResults<Video> videos;

    private Observable<Long> timer = Observable.interval(5, TimeUnit.SECONDS);
    private ArrayList<Subscription> subscriptionArrayList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        realm = Realm.getDefaultInstance();
        loadUserSettings();
        loadData();
        updateUI();

        seekBarGalleryRecView.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBarOnProgressChanged();
        startAutoScrollRecyclerView(4);
        return view;
    }

    private void loadUserSettings() {
        userSettings = UserSettingsController.loadUserSettings(realm);
        userSettings.addChangeListener(element -> updateUI());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initGallery(List<Video> playlists) {
        if (!isBigScreen) {
            recyclerView.setLayoutManager(new SpeedyLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        } else {
            recyclerView.setLayoutManager(new SpeedyGridLayoutManager(getActivity(), 2, GridLayoutManager.HORIZONTAL, false));
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
                Intent intent = new Intent(getContext(), PaymentActivity.class);
                intent.putExtra("videoId", item.getId());
                intent.putExtra("isRewarding", true);
                if (BuildConfig.HAS_REWARDING_AD)
                    if (isCurrentApplicationId(LES)) {
                        PlaylistsActivity activity = (PlaylistsActivity) getActivity();
                        if (Objects.requireNonNull(activity).showRewardedAd(item.getId()))
                            return;
                    } else intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FOR_REWARDING_VIDEO);
                else intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FROM_NABOR);
                startActivity(intent);
                return;
            }
            Intent intent = new Intent(getActivity(), PlayerActivity.class);
            intent.putExtra("playlistId", 0);
            intent.putExtra("videoId", item.getId());
            startActivity(intent);
            playSound(R.raw.tap);
        });
        adapter.setOnDownloadClick((video, pos) -> {

            playSound(R.raw.tap);
            if (video.getVideoLoadingProgress() < 0) {
                DialogUtils.showDownloadDialog(getActivity(), video);
            } else {
                DialogUtils.showCancelLoadingDialog(getActivity(), video, dialogInterface -> updateUI());
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP){
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

        if (isBigScreen)
            if (recyclerView.getAdapter().getItemCount() <= 8) seekBarGalleryRecView.setVisibility(View.GONE);
            else seekBarGalleryRecView.setVisibility(View.VISIBLE);
        else if (recyclerView.getAdapter().getItemCount() <= 5) seekBarGalleryRecView.setVisibility(View.GONE);
            else seekBarGalleryRecView.setVisibility(View.VISIBLE);
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

    private void playSound(int soundId) {
        PlaylistsActivity activity = (PlaylistsActivity) getActivity();
        if (activity == null) return;
        activity.playSound(soundId);
    }

    private void loadData() {
        videos = DataController.INSTANCE.getMainVideos(realm);
        initGallery(videos);
        videos.addChangeListener(element -> updateUI());
    }

    private void updateUI() {
        adapter.setPaid(userSettings.isPaid()).notifyDataSetChanged();
        subscriptionArrayList.add(timer.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> adapter.setPaid(userSettings.isPaid()).notifyDataSetChanged(),
                        e -> System.out.println("Error: " + e),
                        () -> System.out.println("Completed")));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        videos.removeAllChangeListeners();
        realm.close();
        unbinder.unbind();
    }

    @Override
    public String getScreenName() {
        return "Все мультфильмы";
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
    public void onDestroy() {
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
