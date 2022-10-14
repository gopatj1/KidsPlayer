package jewelrock.irev.com.jewelrock.subscribe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;
import jewelrock.irev.com.jewelrock.BaseRealmActivity;
import jewelrock.irev.com.jewelrock.BuildConfig;
import jewelrock.irev.com.jewelrock.GlideApp;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.controller.InitSettingsController;
import jewelrock.irev.com.jewelrock.controller.UserSettingsController;
import jewelrock.irev.com.jewelrock.model.ScreenText;
import jewelrock.irev.com.jewelrock.subscribe.util.IabHelper;
import jewelrock.irev.com.jewelrock.subscribe.util.IabResult;
import jewelrock.irev.com.jewelrock.subscribe.util.Inventory;
import jewelrock.irev.com.jewelrock.subscribe.util.Purchase;
import jewelrock.irev.com.jewelrock.subscribe.util.SkuDetails;
import jewelrock.irev.com.jewelrock.utils.DialogUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static jewelrock.irev.com.jewelrock.utils.Constants.BIS;
import static jewelrock.irev.com.jewelrock.utils.Constants.ERALASH;
import static jewelrock.irev.com.jewelrock.utils.Constants.LES;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PaymentActivity extends BaseRealmActivity {

    public final static String FROM_GLAVNI_EKRAN = "glavnyi_ekran";
    public final static String FROM_NABOR = "nabor";
    public final static String FROM_LIBIMIE = "lubimie";
    public final static String FROM_PERECHEN_MULTOV = "perechen_multov";
    public final static String FROM_PLEER = "pleer";
    public final static String FROM_MOTIVATOR = "motivator";
    public final static String FROM_NASTROIKI_ZACIKLIVANIE = "nastroyki_zaciklivanie";
    public final static String FROM_NASTROIKI_REKLAMA = "nastroyki_reklama";
    public final static String FROM_NASTROIKI_MOTIVATOR = "nastroyki_motivator";
    public final static String FROM_NASTROIKI_STREAMING_BAUBAY = "nastroyki_bau";
    public final static String FROM_NASTROIKI_MOTIVATOR_BAUBAY_22 = "nastroyki_22";
    public final static String FROM_NASTROIKI_VIDEO_SPLASH = "nastroyki_zastavka";
    public final static String FROM_NASTROIKI_AUTOSKROLL_SEEKBAR = "nastroyki_motion";
    public final static String FROM_PLEER_LUBIMIE = "pleer_lubimie";
    public final static String FROM_PLEER_STREAMING_BAUBAY = "translacia";
    public final static String FROM_PLEER_SONGS = "pesni";
    public final static String FOR_REWARDING_VIDEO = "choicePaid";

    private final static String ARG_RESTORE_PURCHASE = "restore_purchase";
    public final static String ARG_FROM = "from";
    private final static String ARG_CLOSE_TIMER = "close_timer";

    private static final int RC_REQUEST = 15567;

    @BindView(R.id.close)
    ImageView close;
    @BindView(R.id.buy_all)
    View buyAll;
    @BindView(R.id.buy_month)
    View buyMonth;
    @BindView(R.id.buy_3month)
    View buy3Month;
    @BindView(R.id.img_01)
    ImageView img1;
    @BindView(R.id.img_02)
    ImageView img2;
    @BindView(R.id.img_03)
    ImageView img3;
    @BindView(R.id.progress)
    View progress;
    @BindView(R.id.titleBuyAll)
    TextView titleBuyAll;
    @BindView(R.id.titleSubMonth)
    TextView titleSubMonth;
    @BindView(R.id.titleSub3Month)
    TextView titleSub3Month;
    @BindView(R.id.background)
    View background;
    @BindView(R.id.content)
    View content;
    @BindView(R.id.title_from)
    TextView titleFrom;
    @BindView(R.id.topCloud)
    FrameLayout topCloud;
    @BindBool(R.bool.isBigScreen)
    boolean isBigScreen;

    private final String SKU_ALL = "sku_by_all";
    private final String SKU_SUB_1_MONTH = "sku_subscribe_month";
    private final String SKU_SUB_1_MONTH_SALE160818 = "sku_subscribe_month_sale160818";
    private final String SKU_SUB_3_MONTHS = "sku_subscribe_3_month";
    private final String SKU_SUB_3_MONTHS_SALE160818 = "sku_subscribe_3_month_sale160818";
    private final String SKU_SUB_6_MONTHS = "sku_subscribe_6_month";
    private final String[] AVAILABLE_SKU_IDS = new String[]{
        SKU_ALL, SKU_SUB_1_MONTH, SKU_SUB_3_MONTHS, SKU_SUB_6_MONTHS,
        SKU_SUB_1_MONTH_SALE160818, SKU_SUB_3_MONTHS_SALE160818}; //TODO внести названия покупок
    private HashMap<String, String> prices = new HashMap<>();
    IabHelper mHelper;
    private String mSubscribeItem;
    private String payload;

    private boolean isRestoreOnly = false;
    private boolean closeByTimeout = false;
    private int closeTimeout;

    public static void start(Activity context, String from) {
        start(context, false, 0, from);
    }

    public static void start(Activity context, boolean restorePurchase, int code, String from) {
        Intent intent = new Intent(context, PaymentActivity.class);
        intent.putExtra(ARG_RESTORE_PURCHASE, restorePurchase);
        intent.putExtra(ARG_FROM, from);
        context.startActivityForResult(intent, code);
    }

    public static void start(Activity context, int time, int code, String from) {
        Intent intent = new Intent(context, PaymentActivity.class);
        intent.putExtra(ARG_CLOSE_TIMER, time);
        intent.putExtra(ARG_FROM, from);
        context.startActivityForResult(intent, code);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        isRestoreOnly = Objects.requireNonNull(extras).getBoolean(ARG_RESTORE_PURCHASE, false);
        setContentView(R.layout.activity_payment);
        ButterKnife.bind(this);
        closeTimeout = extras.getInt(ARG_CLOSE_TIMER, -1);
        topCloud.animate().translationY(-getResources().getDimension(R.dimen.payment_bg_cloud_height)).alpha(1f).start();
        String key = getValidKey();
        mHelper = new IabHelper(this, key);
        mHelper.enableDebugLogging(BuildConfig.DEBUG);

        mHelper.startSetup(result -> {
            if (!result.isSuccess()) {
                // Oh noes, there was a problem.
                Log.d("!!!", "Start setup result success false: " + result.getMessage());
                closePaymentActivity();
                return;
            }
            getInventory();
        });
        progress.setVisibility(View.VISIBLE);

        setupButtons();
        setupTitle(extras.getString(ARG_FROM, FROM_GLAVNI_EKRAN));

        if (BuildConfig.HAS_REWARDING_AD && (!getIntent().getBooleanExtra("isRewarding", false) || isCurrentApplicationId(LES)))
            buy3Month.setVisibility(View.GONE);

        try {
            GlideApp.with(img1.getContext())
                    .load(DataController.INSTANCE.getPaymentImagesAndOther(realm).get(0).getImageTabletOrPhone(isBigScreen))
                    .fallback(R.drawable.im_no_image)
                    .placeholder(R.drawable.im_no_image)
                    .into(img1);
            GlideApp.with(img2.getContext())
                    .load(DataController.INSTANCE.getPaymentImagesAndOther(realm).get(1).getImageTabletOrPhone(isBigScreen))
                    .fallback(R.drawable.im_no_image)
                    .placeholder(R.drawable.im_no_image)
                    .into(img2);
            if (buy3Month.getVisibility() == View.VISIBLE)
                GlideApp.with(img3.getContext())
                        .load(BuildConfig.HAS_REWARDING_AD && getIntent().getBooleanExtra("isRewarding", false)
                                ? DataController.INSTANCE.getPaymentImagesAndOther(realm).get(3).getImageTabletOrPhone(isBigScreen)
                                : DataController.INSTANCE.getPaymentImagesAndOther(realm).get(2).getImageTabletOrPhone(isBigScreen))
                        .fallback(R.drawable.im_no_image)
                        .placeholder(R.drawable.im_no_image)
                        .into(img3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getInventory() {
        if (mHelper == null) return;
        try {
            mHelper.queryInventoryAsync(true, Arrays.asList(AVAILABLE_SKU_IDS), mGotInventoryListener);
        } catch (Exception ignored) {
            Log.e("PaymentActivity", ignored.getMessage());
        }
    }

    private void setupTitle(String from) {
        ScreenText screenText = InitSettingsController.getScreenTexts(realm, from);
        if (screenText == null) {
            titleFrom.setVisibility(View.GONE);
            return;
        }
        titleFrom.setText(Html.fromHtml(screenText.getText().replace("\\n", "<br>")));
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;
            boolean hasPurchase = false;
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                DialogUtils.showNoInternetInShopDialog(PaymentActivity.this, buttonId -> {
                    if (buttonId == 1) {
                        getInventory();
                    } else {
                        setResult(RESULT_CANCELED);
                        closePaymentActivity();
                    }
                });
                return;
            }

            progress.setVisibility(View.GONE);

            for (String sku : AVAILABLE_SKU_IDS) {
                prices.remove(sku);
                SkuDetails details = inventory.getSkuDetails(sku);
                if (details != null) {
                    prices.put(sku, inventory.getSkuDetails(sku).getPrice());
                    updateProductTitle(details);
                    Log.d("PaymentActivity", "SkuDetails: " + details.toString());
                }
                Purchase purchase = inventory.getPurchase(sku);
                if (purchase != null) {
                    Log.d("PaymentActivity", "Purchase: " + purchase.toString());
                    hasPurchase = true;
                }
            }
            savePurchase(hasPurchase);

            if (hasPurchase) {
                setResult(RESULT_OK);
                closePaymentActivity();
                return;
            } else if (isRestoreOnly) {
                setResult(RESULT_CANCELED);
                Log.d("!!!", "No purchases : " + result.getMessage());
                closePaymentActivity();
            } else {
                background.setVisibility(View.VISIBLE);
                content.setVisibility(View.VISIBLE);
                playSound(R.raw.cloud);
                topCloud.animate()
                        .setDuration(1000)
                        .translationY(0).start();
            }

            if (closeTimeout > 0) {
                closeByTimeout = true;
                Observable<Long> timer = Observable.timer(closeTimeout, TimeUnit.SECONDS);
                timer.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(s -> {
                            if (closeByTimeout && !PaymentActivity.this.isFinishing())
                                closePaymentActivity();
                        });
            }
        }
    };

    private void updateProductTitle(SkuDetails details) {
        String title = details.getPrice();
        switch (details.getSku()) {
            case SKU_ALL: {
                setPriceText(titleBuyAll, isCurrentApplicationId(ERALASH) || isCurrentApplicationId(LES)
                        ? "Купить все<br>сюжеты<br>за " : "Купить все<br>мультфильмы<br>за ", title);
                break;
            }
            case SKU_SUB_1_MONTH: {
                setPriceText(isCurrentApplicationId(BIS) ? titleBuyAll : titleSubMonth,
                        "Оформить подписку<br>на месяц<br>за ", title);
                break;
            }
            case SKU_SUB_3_MONTHS: {
                if (getIntent().getBooleanExtra("isRewarding", false) && BuildConfig.HAS_REWARDING_AD)
                    titleSub3Month.setText(Html.fromHtml("С рекламой\n<b>БЕСПЛАТНО</b>"));
                else setPriceText(isCurrentApplicationId(BIS) ? titleSubMonth : titleSub3Month,
                        "Оформить подписку<br>на 3 месяца<br>за ", title);
                break;
            }
            case SKU_SUB_6_MONTHS: {
                if (!isCurrentApplicationId(BIS)) break;
                if (getIntent().getBooleanExtra("isRewarding", false) && BuildConfig.HAS_REWARDING_AD)
                    titleSub3Month.setText(Html.fromHtml("С рекламой\n<b>БЕСПЛАТНО</b>"));
                else setPriceText(titleSub3Month, "Оформить подписку<br>на 6 месяцев<br>за ", title);
                break;
            }
        }
    }

    private void setPriceText(TextView textView, String title, String price){
        try {
            String[] split = price.split(" ");
            String text = title + "<b>" + split[0] + "</b> " + split[1];
            textView.setText(Html.fromHtml(text));
        } catch (Exception e){
            textView.setText(Html.fromHtml(title +  "<b>" + price + "</b> "));
        }
    }

    private void savePurchase(boolean isPaid) {
        UserSettingsController.setUserPaid(isPaid);
        if (isPaid) close.performClick();
    }

    void complain(String message) {
        if (BuildConfig.DEBUG) DialogUtils.alert(this, "Error: " + message);
        Log.e("!!!", "Error: " + message);
    }

    private String getValidKey() {
        return BuildConfig.IN_APP_KEY;
    }

    private void closePaymentActivity() {
        finish();
    }

    private void setupButtons() {
        close.setOnClickListener(view -> {
            analyticsLogEvent("Магазин", "Закрытие магазина");
            closeByTimeout = false;
            setResult(RESULT_CANCELED);
            closePaymentActivity();
        });
        buyAll.setTag(isCurrentApplicationId(BIS) ? SKU_SUB_1_MONTH : SKU_ALL);
        buyAll.setOnClickListener(subscribeListener);
        buyMonth.setTag(isCurrentApplicationId(BIS) ? SKU_SUB_3_MONTHS : SKU_SUB_1_MONTH);
        buyMonth.setOnClickListener(subscribeListener);
        buy3Month.setTag(isCurrentApplicationId(BIS) ? SKU_SUB_6_MONTHS : SKU_SUB_3_MONTHS);
        if (getIntent().getBooleanExtra("isRewarding", false) && BuildConfig.HAS_REWARDING_AD)
            buy3Month.setOnClickListener(view -> showRewardedAd(PaymentActivity.this.getIntent().getIntExtra("videoId", 0)));
        else buy3Month.setOnClickListener(subscribeListener);
    }

    View.OnClickListener subscribeListener = v -> {
        String sku = (String) v.getTag();
        addAnalyticsEvent(sku);
        if (BuildConfig.DEBUG) {
            UserSettingsController.setUserPaid(true);
            setResult(RESULT_OK);
            closePaymentActivity();
            return;
        }
        closeByTimeout = false;
        Log.d("PaymentActivity", "Buy: " + sku);
        subscribeClick(sku);
    };

    private void addAnalyticsEvent(String sku) {
        if (getIntent().getBooleanExtra("isRewarding", false) && BuildConfig.HAS_REWARDING_AD) {
            analyticsLogEvent("Магазин", "Выбор покупки", "Просмотр видео с вознаграждением");
            return;
        }
        switch (sku) {
            case SKU_ALL: {
                analyticsLogEvent("Магазин", "Выбор покупки", "Все");
                break;
            }
            case SKU_SUB_1_MONTH: {
                analyticsLogEvent("Магазин", "Выбор покупки", "Подписка 1 месяц");
                break;
            }
            case SKU_SUB_3_MONTHS: {
                analyticsLogEvent("Магазин", "Выбор покупки", "Подписка 3 месяца");
                break;
            }
            case SKU_SUB_6_MONTHS: {
                analyticsLogEvent("Магазин", "Выбор покупки", "Подписка 6 месяцев");
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            try {
                mHelper.dispose();
            } catch (Exception ignored) {
                Log.e("PaymentActivity", ignored.getMessage());
            }
            mHelper = null;
        }
    }

    @Override
    public String getScreenName() {
        return "Магазин";
    }

    @Override
    public boolean showRewardedAd(int id){
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
            videoIdForRewarding = PaymentActivity.this.getIntent().getIntExtra("videoId", 0);
            return true;
        } else {
            DialogUtils.showRewardAdNotReadyDialog(this, buttonId -> {
                switch (buttonId) {
                    case 1:
                        showRewardedAd(videoIdForRewarding);
                        break;
                    case 2:
                        setResult(RESULT_CANCELED);
                        break;
                }
            });
            return false;
        }
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

        int index = -1;
        for (int i = 0; i < AVAILABLE_SKU_IDS.length; i++) {
            if (AVAILABLE_SKU_IDS[i].equals(sku)) {
                index = i;
                break;
            }
        }
        if (index == -1) return;
        try {
            if (index == 0) {
                mHelper.launchPurchaseFlow(this, sku, RC_REQUEST, mPurchaseFinishedListener, payload);
            } else {
                mHelper.launchPurchaseFlow(this, sku, IabHelper.ITEM_TYPE_SUBS, RC_REQUEST, mPurchaseFinishedListener, payload);
            }
        } catch (Exception ignored) {
            Log.e("PaymentActivity", ignored.getMessage());
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
                    DialogUtils.alert(PaymentActivity.this, "Ошибка покупки: " + result.getMessage(), dialogInterface -> {
                        setResult(RESULT_CANCELED);
                        closePaymentActivity();
                    });
                }
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                DialogUtils.alert(PaymentActivity.this, "Ошибка покупки. Не удалось авторизоваться.", dialogInterface -> {
                    setResult(RESULT_CANCELED);
                    closePaymentActivity();
                });
                return;
            }

            if (purchase.getSku().equals(mSubscribeItem)) {
                savePurchase(true);
            }
        }
    };
}
