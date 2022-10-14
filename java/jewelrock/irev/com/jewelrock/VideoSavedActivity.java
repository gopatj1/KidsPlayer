package jewelrock.irev.com.jewelrock;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.RealmResults;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.controller.UserSettingsController;
import jewelrock.irev.com.jewelrock.model.Playlist;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.ui.adapters.StickyHeadersVideoSavedAdapter;
import jewelrock.irev.com.jewelrock.utils.DialogUtils;
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
public class VideoSavedActivity extends BaseRealmActivity {
    @BindView(R.id.main_frame_layout_activity_video_saved)
    FrameLayout mainFrameLayout;
    @BindView(R.id.content_holder)
    View contentHolder;

    @BindView(R.id.sampleRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_support)
    View toolbarSupport;
    @BindView(R.id.empty)
    View emptyHolder;
    private ArrayList<ImageView> allBanners = new ArrayList<>();

    private StickyHeadersVideoSavedAdapter adapter;

    private boolean selectionState = false;
    private boolean allSelected = false;
    private RealmResults<Video> videos;
    private ArrayList<Video> videosArrayList;
    private StickyRecyclerHeadersDecoration headersDecor;

    private Observable<Long> timer = Observable.interval(3, TimeUnit.SECONDS);
    private ArrayList<Subscription> subscriptionArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_saved);
        ButterKnife.bind(this);

        initToolbar();
        loadUserSettings();
        loadData();
        setNormalState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_saved, menu);

        menu.findItem(R.id.action_delete).setVisible(!selectionState && adapter != null && adapter.getItemCount() > 0);
        menu.findItem(R.id.action_select_all).setVisible(selectionState && !allSelected);
        menu.findItem(R.id.action_deselect_all).setVisible(selectionState && allSelected);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.action_delete) {
            setSelectionState();
            return true;
        }

        if (id == R.id.action_select_all) {
            selectAll();
            return true;
        }
        if (id == R.id.action_deselect_all) {
            deselectAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.cancel)
    public void cancel(View button) {
        setNormalState();
    }

    @OnClick(R.id.delete)
    public void delete(View button) {
        DialogUtils.showDeleteDialog(this, adapter.getSelectedItems(), view -> {
            adapter.removeSelected();

            // recreate adapter items and headers
            fillAdaptedItems();
            initGallery(videosArrayList, true);

            setNormalState();
            updateUI();
        });
    }

    private void setSelectionState() {
        selectionState = true;
        toolbar.setNavigationIcon(null);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.search_cancel_color));
        adapter.setSelectionState(true);
        deselectAll();
        toolbarSupport.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
    }

    private void setNormalState() {
        selectionState = false;
        Objects.requireNonNull(getSupportActionBar()).setTitle(isCurrentApplicationId(ERALASH) || isCurrentApplicationId(LES) ? "Размещение сюжетов" : "Размещение мультиков");
        toolbar.setNavigationIcon(R.drawable.ic_back_w);
        toolbarSupport.setVisibility(View.GONE);
        if (adapter != null) adapter.setSelectionState(false);
        invalidateOptionsMenu();
    }

    private void deselectAll() {
        allSelected = false;
        adapter.selectNone();
        updateSelectionCountText();
        invalidateOptionsMenu();
    }

    private void updateSelectionCountText() {
        Objects.requireNonNull(getSupportActionBar()).setTitle("Выбрано: " + adapter.getSelectedItemsCount());
    }

    private void selectAll() {
        allSelected = true;
        adapter.selectAll();
        updateSelectionCountText();
        invalidateOptionsMenu();
    }

    private void initToolbar() {
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.search_cancel_color));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
    }

    private void loadUserSettings() {
        userSettings = UserSettingsController.loadUserSettings(realm);
        userSettings.addChangeListener(element -> updateUI());
    }

    private void initGallery(List<Video> videos, boolean isReInitial) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new StickyHeadersVideoSavedAdapter(videos);
        adapter.setOnItemClick((item, pos) -> {
            if (selectionState) updateSelectionCountText();
        });

        recyclerView.setAdapter(adapter);

        //delete old decoration if reinit gallery
        if (isReInitial) recyclerView.removeItemDecoration(headersDecor);

        // Add the sticky headers decoration
        headersDecor = new StickyRecyclerHeadersDecoration(adapter);
        recyclerView.addItemDecoration(headersDecor);

        // Add decoration for dividers between list items
        //recyclerView.addItemDecoration(new DividerDecoration(this));
    }

    private void fillAdaptedItems() {
        videos = DataController.INSTANCE.getLoadingVideos(realm);
        videosArrayList = new ArrayList<>(videos);

        /*create duplicate items for videos, which using by different playlists
        firstly find it, then find video with the same new playlist and put duplicate
        video behind video  with the same new playlist*/
        for (int v = videos.size() - 1; v >= 0; v--)
            for (int repeatNumber = 1; repeatNumber < Objects.requireNonNull(videos.get(v)).getPlaylistCount(); repeatNumber++) {
                for (int index = videosArrayList.size() - 1; index >= 0; index--) {
                    if (videosArrayList.get(index).getPlaylist(0).equals(Objects.requireNonNull(videos.get(v)).getPlaylist(repeatNumber))) {
                        videosArrayList.add(index + 1, videos.get(v));
                        break;
                    } else if (index == 0) {
                        for (int i = videosArrayList.size() - 1; i >= 0; i--) {
                            Playlist playlist;
                            try {
                                playlist = videosArrayList.get(i).getPlaylist(repeatNumber);
                            } catch (Exception e) {
                                e.printStackTrace();
                                continue;
                            }
                            if (playlist.equals(Objects.requireNonNull(videos.get(v)).getPlaylist(repeatNumber))) {
                                videosArrayList.add(i, videos.get(v));
                                break;
                            }
                        }
                    }
                }
            }
    }

    private void loadData() {
        fillAdaptedItems();
        initGallery(videosArrayList, false);
        videos.removeAllChangeListeners();
        videos.addChangeListener(element -> updateUI());
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
        emptyHolder.setVisibility(videosArrayList.size() > 0 ? View.GONE: View.VISIBLE);
        adapter.notifyItemRangeChanged(0, videosArrayList.size());
        invalidateOptionsMenu();
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
    public String getScreenName() {
        return "Размещение мультов";
    }
}
