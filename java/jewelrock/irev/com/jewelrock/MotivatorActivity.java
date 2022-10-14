package jewelrock.irev.com.jewelrock;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.concurrent.TimeUnit;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmResults;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.model.Motivator;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.subscribe.PaymentActivity;

import static jewelrock.irev.com.jewelrock.utils.Constants.LES;


public class MotivatorActivity extends BaseRealmActivity {

    @BindBool(R.bool.isBigScreen)
    boolean isBigScreen;
    Motivator motivator;

    @BindView(R.id.btn_close)
    View btnClose;
    @BindView(R.id.btn_ok)
    View btnOk;
    @BindView(R.id.bg)
    ImageView background;
    @BindView(R.id.text)
    TextView mainText;
    @BindView(R.id.btn_ok_text)
    TextView okText;
    private Disposable typingSubscription;

    private Disposable autoFinishSubscription;

    @OnClick(R.id.btn_close)
    void onCloseClick() {
        playSound(R.raw.tap);
        finishAndResult();
    }

    @OnClick(R.id.btn_ok)
    void onOkClick() {
        playSound(R.raw.tap);
        startPlayer();
    }

    @OnClick(R.id.bg)
    void onBgClick() {
        //if (btnClose.getVisibility() == View.VISIBLE) startPlayer();
    }

    public static void start(Context context) {
        context.startActivity(new Intent(context, MotivatorActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_motivator);
        ButterKnife.bind(this);
        init();
    }

    private void show() {

        GlideApp.with(this)
                .load(isBigScreen ? motivator.getImageTablet() : motivator.getImagePhone())
                //.diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        finishAndResult();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (isConnected() && BuildConfig.MOTIVATOR_TYPE.equals("sound")) {
                            mainText.setText(motivator.getTextMotivator());
                            if (motivator.getSound() != null)
                                playSoundByUrl(Uri.parse(motivator.getSound()));
                            allTextIsVisible();
                        } else if (!isConnected() || BuildConfig.MOTIVATOR_TYPE.equals("typing"))
                            startTyping();
                        return false;
                    }
                })
                .into(background);
    }

    private boolean init() {
        RealmResults<Motivator> unshownMotivators = DataController.INSTANCE.getUnshownMotivators(realm);
        if (unshownMotivators == null || unshownMotivators.isEmpty()) {
            finishAndResult();
            return false;
        }
        motivator = unshownMotivators.get((int) (Math.random() * unshownMotivators.size()));
        return true;
    }

    private void startTyping() {
        String split = motivator.getTextMotivator();
        if (split.length() > 0) {
            typingSubscription = Observable.interval(100, TimeUnit.MILLISECONDS)
                    .map(n -> {
                        if (n < split.length()) return split.substring(0, n.intValue() + 1);
                        return "";
                    })
                    .onErrorResumeNext(throwable -> null)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        if (TextUtils.isEmpty(result)) {
                            stopTyping();
                            allTextIsVisible();
                            return;
                        }
                        playSound(R.raw.tap);
                        mainText.setText(result);
                    }, Throwable::printStackTrace);
        }
    }

    private void allTextIsVisible() {
        btnOk.setVisibility(View.VISIBLE);
        btnOk.setEnabled(true);
        btnClose.setVisibility(View.VISIBLE);
        DataController.INSTANCE.setMotivatorShown(motivator, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        show();
        mainText.setText("");
        btnClose.setVisibility(View.GONE);
        btnOk.setVisibility(View.INVISIBLE);
        btnOk.setEnabled(false);
        okText.setText(motivator.getTextInKey());

        autoFinishSubscription = Observable.timer(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> finishAndResult(),
                        Throwable::printStackTrace);
    }

    @Override
    public void onBackPressed() {
        if (btnClose.getVisibility() == View.VISIBLE)
            super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTyping();
        if (autoFinishSubscription != null) {
            System.out.println("stopped");
            autoFinishSubscription.dispose();
        }
    }

    private void stopTyping() {
        if (typingSubscription != null) {
            System.out.println("stopped");
            typingSubscription.dispose();
        }
    }

    private void startPlayer() {
        Video video = DataController.INSTANCE.getVideoForMotivator(motivator.getVideoId());
        if (video == null) {
            finishAndResult();
            return;
        }

        if (video.isFree() || userSettings.isPaid()) {
            PlayerActivity.startVideo(this, video);
            finishAndResult();
            return;
        }

        Intent intent = new Intent(getBaseContext(), PaymentActivity.class);
        intent.putExtra("videoId", video.getId());
        intent.putExtra("isRewarding", true);
        if (BuildConfig.HAS_REWARDING_AD)
            if (isCurrentApplicationId(LES)) {
                if (showRewardedAd(video.getId()))
                    return;
            } else intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FOR_REWARDING_VIDEO);
        else intent.putExtra(PaymentActivity.ARG_FROM, PaymentActivity.FROM_MOTIVATOR);
        startActivity(intent);
        finishAndResult();
    }

    @Override
    public String getScreenName() {
        return "Мотиватор";
    }

    private void finishAndResult() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
