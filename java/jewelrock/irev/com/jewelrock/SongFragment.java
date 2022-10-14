package jewelrock.irev.com.jewelrock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import jewelrock.irev.com.jewelrock.utils.Constants;
import jewelrock.irev.com.jewelrock.utils.DialogUtils;
import jewelrock.irev.com.jewelrock.utils.SpeedyGridLayoutManager;
import jewelrock.irev.com.jewelrock.utils.SpeedyLinearLayoutManager;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static jewelrock.irev.com.jewelrock.BaseRealmActivity.isCurrentApplicationId;
import static jewelrock.irev.com.jewelrock.utils.Constants.LES;

public class SongFragment extends BaseAnalyticsFragment {
    @BindView(R.id.sampleRecyclerView)
    RecyclerView galleryRecyclerView;

    @BindView(R.id.image_no_data)
    ImageView noDataImage;
    @BindDrawable(R.drawable.rectangle_invisible_16dp)
    Drawable rectangle;

    @BindView(R.id.seek_bar_gallery_recucler_view)
    SeekBar seekBarGalleryRecView;

    @BindBool(R.bool.isBigScreen)
    boolean isBigScreen;

    private Realm realm;
    private PlaylistVideoAdapter adapter;
    private Unbinder unbinder;
    private RealmResults<Video> songs;
    private UserSettings userSettings;

    private Observable<Long> timer = Observable.interval(1, TimeUnit.SECONDS);
    private ArrayList<Subscription> subscriptionArrayList = new ArrayList<>();

    public SongFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        realm = Realm.getDefaultInstance();
        userSettings = UserSettingsController.loadUserSettings(realm);
        noDataImage.setVisibility(View.GONE);
        galleryRecyclerView.setVisibility(View.VISIBLE);
        loadData();
        if (!isBigScreen) {
            galleryRecyclerView.setLayoutManager(new SpeedyLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        } else {
            galleryRecyclerView.setLayoutManager(new SpeedyGridLayoutManager(getActivity(), 2, GridLayoutManager.HORIZONTAL, false));
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(galleryRecyclerView.getContext(),
                    LinearLayoutManager.VERTICAL);
            dividerItemDecoration.setDrawable(rectangle);
            galleryRecyclerView.addItemDecoration(dividerItemDecoration);
        }
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(galleryRecyclerView.getContext(),
                LinearLayoutManager.HORIZONTAL);
        dividerItemDecoration.setDrawable(rectangle);
        galleryRecyclerView.addItemDecoration(dividerItemDecoration);
        userSettings.addChangeListener(element -> updateUI());

        seekBarGalleryRecView.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBarOnProgressChanged();
        startAutoScrollRecyclerView(4);
        return view;
    }

    private void loadData() {
        songs = DataController.INSTANCE.getSongVideos(realm);
        Objects.requireNonNull(songs).addChangeListener(element -> updateUI());
        initGallery(songs);
        updateUI();
    }

    private void updateUI() {
        adapter.setPaid(userSettings.isPaid()).notifyDataSetChanged();
        subscriptionArrayList.add(timer.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> adapter.setPaid(userSettings.isPaid()).notifyDataSetChanged(),
                        e -> System.out.println("Error: " + e),
                        () -> System.out.println("Completed")));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getActivity()).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    @SuppressLint({"ClickableViewAccessibility", "CheckResult"})
    private void initGallery(List<Video> video) {
        adapter = new PlaylistVideoAdapter(video, false).setPaid(userSettings.isPaid());
        adapter.setOnItemClick((item, pos) -> {
            PlaylistsActivity activity = (PlaylistsActivity) getActivity();
            if (activity == null) return;
            activity.playSound(R.raw.tap);
            if (!item.isFree() && !userSettings.isPaid() && item.getVideoPreview() == null) {
                Intent intent = new Intent(getContext(), PaymentActivity.class);
                intent.putExtra("videoId", item.getId());
                intent.putExtra("isRewarding", true);
                if (BuildConfig.HAS_REWARDING_AD)
                    if (isCurrentApplicationId(LES)) {
                        if (activity.showRewardedAd(item.getId()))
                            return;
                    } else intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FOR_REWARDING_VIDEO);
                else intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FROM_NABOR);
                startActivity(intent);
                return;
            }
            @SuppressLint("SimpleDateFormat") int currentTime = Integer.parseInt(new SimpleDateFormat("HHmmss").format(new Date()));
            if (userSettings.isPaid() && userSettings.isShowMotivatorBauBay22() && (currentTime > 220000 || currentTime < 70000)) {
                activity.playSound(R.raw.cloud);
                activity.motivator22frameLay.setVisibility(View.VISIBLE);
                io.reactivex.Observable.timer(10, TimeUnit.SECONDS)
                        .subscribeOn(io.reactivex.schedulers.Schedulers.newThread())
                        .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                        .subscribe(aLong -> activity.motivator22frameLay.setVisibility(View.GONE));
                return;
            }
            Intent intent = new Intent(getActivity(), PlayerActivity.class);
            intent.putExtra("playlistId", getActivity().getIntent().getIntExtra("id", -3));
            intent.putExtra("videoId", item.getId());
            startActivity(intent);
        });
        adapter.setOnDownloadClick((videoOnItemClick, pos) -> {
            ((PlaylistsActivity) Objects.requireNonNull(getActivity())).playSound(R.raw.tap);
            if (videoOnItemClick.getVideoLoadingProgress() < 0) {
                DialogUtils.showDownloadDialog(getActivity(), videoOnItemClick);
            } else {
                DialogUtils.showCancelLoadingDialog(getActivity(), videoOnItemClick, dialogInterface -> updateUI());
            }
        });
        galleryRecyclerView.setAdapter(adapter);
        galleryRecyclerView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                ((PlaylistsActivity) Objects.requireNonNull(getActivity())).playSound(R.raw.scroll);
            }
            return false;
        });
        galleryRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
            if (galleryRecyclerView.getAdapter().getItemCount() <= 8) seekBarGalleryRecView.setVisibility(View.GONE);
            else seekBarGalleryRecView.setVisibility(View.VISIBLE);
        else if (galleryRecyclerView.getAdapter().getItemCount() <= 5) seekBarGalleryRecView.setVisibility(View.GONE);
            else seekBarGalleryRecView.setVisibility(View.VISIBLE);
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (galleryRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING ||
                    galleryRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_SETTLING) return;
            seekBarOnProgressChanged();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (galleryRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING ||
                    galleryRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_SETTLING)
                galleryRecyclerView.stopScroll();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            startAutoScrollRecyclerView(2);
        }
    };

    public void seekBarOnProgressChanged(){
        if (!isBigScreen) seekBarGalleryRecView.setMax(galleryRecyclerView.getAdapter().getItemCount() - 1);
        else seekBarGalleryRecView.setMax(galleryRecyclerView.getAdapter().getItemCount() - 4);
        galleryRecyclerView.scrollToPosition(seekBarGalleryRecView.getProgress());
    }

    @SuppressLint("CheckResult")
    public void startAutoScrollRecyclerView(int delay){
        if (!userSettings.isAutoScrollSeekBar()) return;
        io.reactivex.Observable.timer(delay, TimeUnit.SECONDS).subscribeOn(io.reactivex.schedulers.Schedulers.newThread())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    try {
                        if (galleryRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING ||
                                galleryRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_SETTLING ||
                                seekBarGalleryRecView.getVisibility() == View.GONE) return;
                        if (seekBarGalleryRecView.getProgress() == seekBarGalleryRecView.getMax())
                            galleryRecyclerView.smoothScrollToPosition(0);
                        else galleryRecyclerView.smoothScrollToPosition(galleryRecyclerView.getAdapter().getItemCount() - 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.BUNDLE_RECYCLER_LAYOUT, galleryRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(Constants.BUNDLE_RECYCLER_LAYOUT);
            galleryRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public String getScreenName() {
        return "Песни";
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        songs.removeAllChangeListeners();
        userSettings.removeAllChangeListeners();
        realm.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.setPaid(userSettings.isPaid()).notifyDataSetChanged();
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
