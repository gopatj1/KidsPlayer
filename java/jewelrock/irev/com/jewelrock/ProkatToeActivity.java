package jewelrock.irev.com.jewelrock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindBool;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.controller.UserSettingsController;
import jewelrock.irev.com.jewelrock.model.Playlist;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.subscribe.util.IabHelper;
import jewelrock.irev.com.jewelrock.subscribe.util.IabResult;
import jewelrock.irev.com.jewelrock.subscribe.util.Inventory;
import jewelrock.irev.com.jewelrock.subscribe.util.Purchase;
import jewelrock.irev.com.jewelrock.subscribe.util.SkuDetails;
import jewelrock.irev.com.jewelrock.ui.adapters.ProkatPlaylistToeAdapter;
import jewelrock.irev.com.jewelrock.utils.DialogUtils;
import jewelrock.irev.com.jewelrock.utils.SpeedyGridLayoutManager;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ProkatToeActivity extends BaseRealmActivity {
    private static final int RC_REQUEST = 15567;

    @BindView(R.id.main_layout_prokat)
    ConstraintLayout mainFrameLayout;
    @BindView(R.id.sampleRecyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.btn_close)
    ImageButton btnClose;
    @BindView(R.id.attention_text)
    TextView attentionText;

    @BindDrawable(R.drawable.rectangle_invisible_16dp)
    Drawable rectangle;
    private ArrayList<ImageView> allBanners = new ArrayList<>();

    @BindBool(R.bool.isBigScreen)
    boolean isBigScreen;
    private ProkatPlaylistToeAdapter playlistAdapter;
    private RealmResults<Playlist> playlists;

    private ArrayList<String> availableSkuIds = new ArrayList<>(); // id покупок
    IabHelper mHelper;
    private String mSubscribeItem;
    private String payload;

    private Observable<Long> timer = Observable.interval(3, TimeUnit.SECONDS);
    private ArrayList<Subscription> subscriptionArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prokat_toe);
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
        if (!isBigScreen) {
            recyclerView.setLayoutManager(new SpeedyGridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
        } else {
            recyclerView.setLayoutManager(new SpeedyGridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false));
        }

        btnClose.setOnClickListener(view -> {
            playSound(R.raw.tap);
            finish();
        });

        final SpannableStringBuilder text = new SpannableStringBuilder(getResources().getText(R.string.prokat_attention_message));
        final ForegroundColorSpan style = new ForegroundColorSpan(getResources().getColor(R.color.prokat_attention));
        text.setSpan(style, 0, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        attentionText.setText(text);
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
    private void initGallery(List<Playlist> playlists) {
        playlistAdapter = new ProkatPlaylistToeAdapter(playlists);
        playlistAdapter.setOnItemClick((item, pos) -> {
            playSound(R.raw.tap);
            if (Objects.requireNonNull(item.getVideos().get(0)).getGoogleLink() != null
                    && Objects.requireNonNull(item.getVideos().get(0)).getGoogleLink().contains("play.google.com")) {
                analyticsLogEvent("Видеопрокат", "Переход в Market на " + Objects.requireNonNull(item.getVideos().get(0)).getName());
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Objects.requireNonNull(item.getVideos().get(0)).getGoogleLink())));
                return;
            }
            if (!Objects.requireNonNull(item.getVideos().get(0)).isPurchase() && !(Objects.requireNonNull(item.getVideos().get(0)).getVideoOfCurrentApplication()
                    && UserSettingsController.loadUserSettings(Realm.getDefaultInstance()).isPaid())) {
                subscribeActionsBuyClick(item);
                return;
            }
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("playlistId", -6);
            intent.putExtra("videoId", Objects.requireNonNull(item.getVideos().get(0)).getId());
            startActivity(intent);
        });
        playlistAdapter.setOnBuyClick((item, pos) -> {
            playSound(R.raw.tap);
            subscribeActionsBuyClick(item);
        });
        playlistAdapter.setOnDownloadClick((item, pos) -> {
            playSound(R.raw.tap);
            if (Objects.requireNonNull(item.getVideos().get(0)).getVideoLoadingProgress() < 0) {
                DialogUtils.showDownloadDialog(ProkatToeActivity.this, item.getVideos().get(0), dialogInterface -> updateUI());
            }
        });
        playlistAdapter.setOnStopDownloadClick((item, pos) -> {
            playSound(R.raw.tap);
            DialogUtils.showCancelLoadingDialog(ProkatToeActivity.this, item.getVideos().get(0), dialogInterface -> updateUI());
        });
        recyclerView.setAdapter(playlistAdapter);
        recyclerView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                playSound(R.raw.scroll);
            }
            return false;
        });
    }

    private void loadData() {
        playlists = DataController.INSTANCE.getPlaylistsProkat(realm);
        initGallery(playlists);
        playlists.addChangeListener(element -> updateUI());
        for (Playlist p: playlists)
            try {
                if (Objects.requireNonNull(p.getVideos().first()).getPurchase() != null)
                    availableSkuIds.add(Objects.requireNonNull(p.getVideos().first()).getPurchase());
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        playlistAdapter.notifyDataSetChanged();
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
        playlists.removeAllChangeListeners();
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
                Log.e("ProkatToeActivity", ignored.getMessage());
            }
            mHelper = null;
        }
    }

    @Override
    public String getScreenName() {
        return "ProkatFragment";
    }

    /////////////////////////////////// Payment Methods ////////////////////////////////////////////

    private void subscribeActionsBuyClick(Playlist item) {
        String sku = Objects.requireNonNull(item.getVideos().first()).getPurchase();
        analyticsLogEvent("Видеопрокат", "Выбор покупки", Objects.requireNonNull(item.getVideos().first())
                .getName().replace("\\", "").replace("n", " "));
        if (BuildConfig.DEBUG) {
            DataController.INSTANCE.setPurchase(Objects.requireNonNull(item.getVideos().first()), true);
            setResult(RESULT_OK);
            return;
        }
        Log.d("ProkatToeActivity", "Buy: " + sku);
        subscribeClick(sku);
    }
    private void getInventory() {
        if (mHelper == null) return;
        try {
            mHelper.queryInventoryAsync(true, availableSkuIds, mGotInventoryListener);
        } catch (Exception ignored) {
            Log.e("ProkatToeActivity", ignored.getMessage());
        }
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;
            boolean hasPurchase = false;
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                DialogUtils.showNoInternetInShopDialog(ProkatToeActivity.this, buttonId -> {
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
                    Log.d("ProkatToeActivity", "SkuDetails: " + details.toString());
                }
                Purchase purchase = inventory.getPurchase(sku);
                if (purchase != null) {
                    Log.d("ProkatToeActivity", "Purchase: " + purchase.toString());
                    hasPurchase = true;
                }
                savePurchase(playlistAdapter.getItemByPurchase(sku), hasPurchase);

                if (hasPurchase) {
                    setResult(RESULT_OK);
                } else {
                    setResult(RESULT_CANCELED);
                    Log.d("!!!", "No purchase of " + sku + " : " + result.getMessage());
                }
            }
        }
    };

    private void savePurchase(Video video, boolean isPaid) {
        try {
            DataController.INSTANCE.setPurchase(video, isPaid);
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
            Log.e("ProkatToeActivity", ignored.getMessage());
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
                    DialogUtils.alert(ProkatToeActivity.this, "Ошибка покупки: " + result.getMessage(), dialogInterface -> setResult(RESULT_CANCELED));
                }
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                DialogUtils.alert(ProkatToeActivity.this, "Ошибка покупки. Не удалось авторизоваться.", dialogInterface -> setResult(RESULT_CANCELED));
                return;
            }

            if (purchase.getSku().equals(mSubscribeItem)) {
                savePurchase(playlistAdapter.getItemByPurchase(mSubscribeItem), true);
            }
        }
    };
}