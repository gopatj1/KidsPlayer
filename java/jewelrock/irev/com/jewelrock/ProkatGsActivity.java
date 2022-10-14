package jewelrock.irev.com.jewelrock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindBool;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.subscribe.util.IabHelper;
import jewelrock.irev.com.jewelrock.subscribe.util.IabResult;
import jewelrock.irev.com.jewelrock.subscribe.util.Inventory;
import jewelrock.irev.com.jewelrock.subscribe.util.Purchase;
import jewelrock.irev.com.jewelrock.subscribe.util.SkuDetails;
import jewelrock.irev.com.jewelrock.ui.adapters.ProkatPlaylistGsAdapter;
import jewelrock.irev.com.jewelrock.utils.DialogUtils;
import jewelrock.irev.com.jewelrock.utils.SpeedyGridLayoutManager;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ProkatGsActivity extends BaseRealmActivity {
    private static final int RC_REQUEST = 15567;

    @BindView(R.id.main_layout_prokat)
    ConstraintLayout mainFrameLayout;
    @BindView(R.id.sampleRecyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.btn_close)
    ImageButton btnClose;
    @BindView(R.id.btn_buy)
    ImageButton btnBuy;
    @BindView(R.id.downloading_progress_frameLay)
    FrameLayout downloadingProgressFrameLay;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.progress_text)
    TextView progressText;
    @BindView(R.id.progress_text_shadow)
    TextView progressTextShadow;

    @BindDrawable(R.drawable.rectangle_invisible_gs_prokat)
    Drawable rectangle;

    @BindBool(R.bool.isBigScreen)
    boolean isBigScreen;
    private ProkatPlaylistGsAdapter videosAdapter;
    private RealmList<Video> videos;

    private ArrayList<String> availableSkuIds = new ArrayList<>(); // id покупок
    IabHelper mHelper;
    private String mSubscribeItem;
    private String payload;
    private boolean isAllLoaded;
    private boolean isAllLoading;
    private int progress;
    private ArrayList<ImageView> allBanners = new ArrayList<>();

    private Observable<Long> timer = Observable.interval(3, TimeUnit.SECONDS);
    private ArrayList<Subscription> subscriptionArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // double set content and ButterKnife for correct choose phone or tablet layout
        setContentView(isBigScreen ? R.layout.activity_prokat_gs_tablet : R.layout.activity_prokat_gs_phone);
        ButterKnife.bind(this);
        setContentView(isBigScreen ? R.layout.activity_prokat_gs_tablet : R.layout.activity_prokat_gs_phone);
        ButterKnife.bind(this);

        String key = getValidKey();
        mHelper = new IabHelper(this, key);
        mHelper.enableDebugLogging(BuildConfig.DEBUG);

        mHelper.startSetup(result -> {
            if (!result.isSuccess()) {
                // Oh noes, there was a problem.
                Log.d("!!!", "Start setup result success false: " + result.getMessage());
                return;
            }
            getInventory();
        });

        loadUserSettings();
        loadData();
        updateUI();
        if (isBigScreen)
            recyclerView.setLayoutManager(new SpeedyGridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL);
        dividerItemDecoration.setDrawable(rectangle);
        recyclerView.addItemDecoration(dividerItemDecoration);

        btnClose.setOnClickListener(view -> {
            playSound(R.raw.tap);
            finish();
        });
        btnBuy.setOnClickListener(view -> {
            if (isAllLoaded) return;
            playSound(R.raw.tap);
            if (!videosAdapter.getItemByPos(0).isPurchase()) {
                subscribeActionsBuyClick(videosAdapter.getItemByPos(0));
                return;
            }
            if (!isAllLoading && !isAllLoaded) {
                Log.i("ProkatGsActivity", "DownloadSet");
                DialogUtils.showDownloadDialog(ProkatGsActivity.this, videosAdapter.getItemByPos(0).getPlaylist(), dialogInterface -> updateUI());
            }
        });
        downloadingProgressFrameLay.setOnClickListener(view -> {
            playSound(R.raw.tap);
            if (isAllLoading) {
                DialogUtils.showCancelLoadingDialog(ProkatGsActivity.this, videos, dialogInterface -> updateUI());
            }
        });
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
        videosAdapter = new ProkatPlaylistGsAdapter(videos);
        videosAdapter.setOnItemClick((item, pos) -> {
            playSound(R.raw.tap);
            if (!item.isPurchase()) {
                subscribeActionsBuyClick(item);
            }
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("playlistId", -6);
            intent.putExtra("videoId", item.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(videosAdapter);
        recyclerView.setLayoutFrozen(true);

        HorizontalScrollView horizontalScrollView;
        if (!isBigScreen) {
            horizontalScrollView = findViewById(R.id.horizontal_scroll);
            horizontalScrollView.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    playSound(R.raw.scroll);
                }
                return false;
            });
        }
    }

    private void loadData() {
        videos = Objects.requireNonNull(DataController.INSTANCE.getPlaylistsProkat(realm).get(0)).getVideos();
        initGallery(videos);
        videos.addChangeListener(element -> updateUI());
            try {
                if (Objects.requireNonNull(videos.get(0)).getPlaylist().getPurchase() != null)
                    availableSkuIds.add(Objects.requireNonNull(videos.get(0)).getPlaylist().getPurchase());
            } catch (Exception e) {
                e.printStackTrace();
            }
        subscriptionArrayList.add(timer.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> updateUI(),
                        e -> System.out.println("Error: " + e),
                        () -> System.out.println("Completed")));
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        if (allBanners.isEmpty() || (allBanners.get(0).getY() <= -BaseFabActivity.displayMetrics.heightPixels / 2.5
                || allBanners.get(0).getY() >= BaseFabActivity.displayMetrics.heightPixels * 1.2 || allBanners.get(0).getY() == 0.0)) {
            ImageView banner = addBanner(this);
            allBanners.clear();
            allBanners.add(banner);
            mainFrameLayout.addView(banner);
        }
        videosAdapter.notifyDataSetChanged();

        progress = 0;
        for (Video v : videosAdapter.getItems())
            progress += v.getVideoLoadingProgress();
        progress /= videosAdapter.getItemCount();
        isAllLoaded = progress == 100;
        isAllLoading = progress >= 0 && progress < 100;

        btnBuy.setVisibility(View.VISIBLE);
        downloadingProgressFrameLay.setVisibility(View.GONE);
        if (!videosAdapter.getItemByPos(0).isPurchase()) {
            btnBuy.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.prokat_gs_btn_buy));
            return;
        }
        if (!isAllLoading && !isAllLoaded) {
            btnBuy.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.prokat_btn_download));
            return;
        }
        if (isAllLoading) {
            btnBuy.setVisibility(View.GONE);
            downloadingProgressFrameLay.setVisibility(View.VISIBLE);
            progressBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.prokat_downloading));
            progressBar.setProgress(progress);
            progressText.setText(progress + "%");
            progressTextShadow.setText(progress + "%");
            progressTextShadow.getPaint().setStrokeWidth(4);
            progressTextShadow.getPaint().setStyle(Paint.Style.STROKE);
            return;
        }
        if (isAllLoaded) {
            btnBuy.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.prokat_text_downloaded));
        }
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
    protected void onDestroy() {
        videos.removeAllChangeListeners();
        for (Subscription sub : subscriptionArrayList)
            try {
                sub.unsubscribe();
            } catch (Exception e) {
                e.printStackTrace();
            }
        subscriptionArrayList.clear();
        super.onDestroy();
        if (mHelper != null) {
            try {
                mHelper.dispose();
            } catch (Exception ignored) {
                Log.e("ProkatGsActivity", ignored.getMessage());
            }
            mHelper = null;
        }
    }

    @Override
    public String getScreenName() {
        return "ProkatFragment";
    }

    /////////////////////////////////// Payment Methods ////////////////////////////////////////////

    private void subscribeActionsBuyClick(Video item) {
        String sku = item.getPlaylist().getPurchase();
        analyticsLogEvent("Еще...", "Выбор покупки", "Сбрник Мультфильмов Татарского");
        if (BuildConfig.DEBUG) {
            DataController.INSTANCE.setPurchaseToAllProkatVideos(true);
            setResult(RESULT_OK);
            return;
        }
        Log.d("ProkatGsActivity", "Buy: " + sku);
        subscribeClick(sku);
    }
    private void getInventory() {
        if (mHelper == null) return;
        try {
            mHelper.queryInventoryAsync(true, availableSkuIds, mGotInventoryListener);
        } catch (Exception ignored) {
            Log.e("ProkatGsActivity", ignored.getMessage());
        }
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;
            boolean hasPurchase = false;
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                DialogUtils.showNoInternetInShopDialog(ProkatGsActivity.this, buttonId -> {
                    if (buttonId == 1) {
                        getInventory();
                    } else {
                        setResult(RESULT_CANCELED);
                    }
                });
                return;
            }

            for (int i = 0; i < availableSkuIds.size(); i++) {
                String sku = availableSkuIds.get(i);
                SkuDetails details = inventory.getSkuDetails(sku);
                if (details != null) {
                    Log.d("ProkatGsActivity", "SkuDetails: " + details.toString());
                }
                Purchase purchase = inventory.getPurchase(sku);
                if (purchase != null) {
                    Log.d("ProkatGsActivity", "Purchase: " + purchase.toString());
                    hasPurchase = true;
                }
                savePurchase(hasPurchase);

                if (hasPurchase) {
                    setResult(RESULT_OK);
                } else {
                    setResult(RESULT_CANCELED);
                    Log.d("!!!", "No purchase of " + sku + " : " + result.getMessage());
                }
            }
        }
    };

    private void savePurchase(boolean isPaid) {
        try {
            DataController.INSTANCE.setPurchaseToAllProkatVideos(isPaid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void complain(String message) {
        if (BuildConfig.DEBUG) DialogUtils.alert(this, "Error: " + message);
        Log.e("!!!", "Error: " + message);
    }

    private String getValidKey() {
        return BuildConfig.IN_APP_KEY;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) return;
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Subscribe button clicked. Explain to user, then start purchase
    public void subscribeClick(String sku) {
        mSubscribeItem = sku;
        payload = generatePayload();
        try {
            mHelper.launchPurchaseFlow(this, sku, RC_REQUEST, mPurchaseFinishedListener, payload);
        } catch (Exception ignored) {
            Log.e("ProkatGsActivity", ignored.getMessage());
        }
    }

    private String generatePayload() {
        StringBuilder sb = new StringBuilder();

        char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
                'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

        for (int i = 0; i < 32; i++) {
            sb.append(alphabet[((int) (Math.random() * alphabet.length)) % alphabet.length]);
        }
        sb.insert(15, "$$$");
        return sb.toString();
    }

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        if (payload.equals(this.payload) && payload.contains("$$$")) {
            return this.payload.charAt(15) == '$';
        }
        return false;
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                if (result.getResponse() != IabHelper.IABHELPER_USER_CANCELLED) {
                    DialogUtils.alert(ProkatGsActivity.this, "Ошибка покупки: " + result.getMessage(), dialogInterface -> setResult(RESULT_CANCELED));
                }
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                DialogUtils.alert(ProkatGsActivity.this, "Ошибка покупки. Не удалось авторизоваться.", dialogInterface -> setResult(RESULT_CANCELED));
                return;
            }

            if (purchase.getSku().equals(mSubscribeItem)) {
                savePurchase(true);
            }
        }
    };
}
