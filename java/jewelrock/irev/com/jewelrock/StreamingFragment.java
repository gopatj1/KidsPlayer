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
import android.widget.LinearLayout;

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
import jewelrock.irev.com.jewelrock.ui.adapters.PlaylistVideoAdapter;
import jewelrock.irev.com.jewelrock.utils.Constants;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class StreamingFragment extends BaseAnalyticsFragment {
    @BindView(R.id.streaming_fragment_linear_layout)
    LinearLayout streamingLinearLayout;

    @BindView(R.id.sampleRecyclerView)
    RecyclerView galleryRecyclerView;

    @BindView(R.id.streaming_fragment_image_no_data)
    ImageView noDataImage;
    @BindDrawable(R.drawable.rectangle_invisible_16dp)
    Drawable rectangle;

    @BindBool(R.bool.isBigScreen)
    boolean isBigScreen;

    private Realm realm;
    private PlaylistVideoAdapter adapter;
    private Unbinder unbinder;
    private RealmResults<Video> streamingVideo;
    private UserSettings userSettings;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
    private int currentTime;
    private Observable<Long> timer = Observable.interval(30, TimeUnit.SECONDS);
    private ArrayList<Subscription> subscriptionArrayList = new ArrayList<>();

    public static final int STREAMING_DAY_TIME_START = 120000;
    public static final int STREAMING_DAY_TIME_END = 130000;
    public static final int STREAMING_NIGHT_TIME_START = 204500;
    public static final int STREAMING_NIGHT_TIME_END = 214500;

    public StreamingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_streaming, container, false);
        unbinder = ButterKnife.bind(this, view);
        realm = Realm.getDefaultInstance();
        userSettings = UserSettingsController.loadUserSettings(realm);

        setCurrentTime();
        loadData();
        if (!isBigScreen) {
            galleryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        } else {
            galleryRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2, GridLayoutManager.HORIZONTAL, false));
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
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        streamingVideo.removeAllChangeListeners();
        userSettings.removeAllChangeListeners();
        realm.close();
    }

    private void loadData() {
        if (!userSettings.isStreamingWasAlready()) {
            UserSettingsController.setLastStreamingDate(realm);
            streamingVideo = DataController.INSTANCE.setRandomStreamingVideos(realm);
            UserSettingsController.setLastStreamingVideoId(realm, streamingVideo);
        } else {
            streamingVideo = DataController.INSTANCE.setStreamingVideosById(realm, userSettings.getLastStreamingVideoId());
        }
        streamingVideo.addChangeListener(element -> updateUI());
        initGallery(streamingVideo);
        updateUI();
    }

    private void updateUI() {
        adapter.setPaid(userSettings.isPaid()).notifyDataSetChanged();
        checkNoData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getActivity()).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    public void setCurrentTime() {
        currentTime = Integer.parseInt(sdf.format(new Date()));
        subscriptionArrayList.add(timer.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                            currentTime = Integer.parseInt(sdf.format(new Date()));
                            loadData();
                        },
                        e -> System.out.println("Error: " + e),
                        () -> System.out.println("Completed")));
    }

    private void checkNoData() {
        boolean hasNoData = adapter.getItemCount() < 1;
        boolean isDayWatchTime = currentTime > STREAMING_DAY_TIME_START && currentTime < STREAMING_DAY_TIME_END;
        boolean isNightWatchTime = currentTime > STREAMING_NIGHT_TIME_START && currentTime < STREAMING_NIGHT_TIME_END;
        if (hasNoData || (!isDayWatchTime && !isNightWatchTime)) {
            noDataImage.setVisibility(View.VISIBLE);
            noDataImage.setImageResource(isBigScreen ? R.drawable.placeholder_image : R.drawable.tv_translation_placeholder_mobile);
            streamingLinearLayout.setVisibility(View.GONE);
        } else {
            noDataImage.setVisibility(View.GONE);
            noDataImage.setImageDrawable(null);
            streamingLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initGallery(List<Video> video) {
        adapter = new PlaylistVideoAdapter(video, false).setPaid(userSettings.isPaid()).setStreaming(true);
        adapter.setOnItemClick((item, pos) -> {
            PlaylistsActivity activity = (PlaylistsActivity) getActivity();
            if (activity == null) return;
            activity.playSound(R.raw.tap);
            Intent intent = new Intent(getActivity(), PlayerActivity.class);
            intent.putExtra("playlistId", getActivity().getIntent().getIntExtra("id", -2));
            intent.putExtra("videoId", item.getId());
            intent.putExtra("videoPos", pos);
            intent.putExtra("isStreaming", true);
            startActivity(intent);
        });
        galleryRecyclerView.setAdapter(adapter);
        galleryRecyclerView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                ((PlaylistsActivity) Objects.requireNonNull(getActivity())).playSound(R.raw.scroll);
            }
            return false;
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
        if(savedInstanceState != null)
        {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(Constants.BUNDLE_RECYCLER_LAYOUT);
            galleryRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public String getScreenName() {
        return "Трансляция";
    }

    @Override
    public void onResume() {
        super.onResume();
        for (Subscription sub : subscriptionArrayList)
            try {
                sub.unsubscribe();
            } catch (Exception e) {
                e.printStackTrace();
            }
        subscriptionArrayList.clear();
        setCurrentTime();
        loadData();
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

    public static boolean isStreamingTime() {
        @SuppressLint("SimpleDateFormat") int currentTime = Integer.parseInt(new SimpleDateFormat("HHmmss").format(new Date()));
        return (currentTime > STREAMING_DAY_TIME_START && currentTime < STREAMING_DAY_TIME_END) || (currentTime > STREAMING_NIGHT_TIME_START && currentTime < STREAMING_NIGHT_TIME_END);
    }
}
