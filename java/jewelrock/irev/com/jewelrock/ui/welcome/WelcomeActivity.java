package jewelrock.irev.com.jewelrock.ui.welcome;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.circlenavigator.CircleNavigator;

import java.util.List;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import jewelrock.irev.com.jewelrock.BaseRealmActivity;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.model.WelcomeScreen;

public class WelcomeActivity extends BaseRealmActivity implements WelcomeFragment.OnFragmentInteractionListener {

    public static final String ADVERT = "advert";
    public static final String ALL_SCREENS = "all_screens";
    @BindView(R.id.pager)
    ViewPager mViewPager;
    @BindBool(R.bool.isBigScreen)
    boolean isBigScreen;
    SectionsPagerAdapter mSectionsPagerAdapter;

    Realm realm;
    private List<WelcomeScreen> welcomeScreens;

    @OnClick(R.id.btn_close)
    void onCloseClick() {
        finish();
    }

    public static void start(Context context, boolean allScreens) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.putExtra(ALL_SCREENS, allScreens);
        context.startActivity(intent);
    }

    public static void startAdvertising(Context context) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.putExtra(ADVERT, true);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);

        realm = Realm.getDefaultInstance();

        welcomeScreens = getIntent().getBooleanExtra(ADVERT, false)
                ? DataController.INSTANCE.getUnshownAdvertisingScreens(realm, userSettings.isPaid())
                : getIntent().getBooleanExtra(ALL_SCREENS, false)
                ? DataController.INSTANCE.getHelpWelcomeScreens(realm)
                :DataController.INSTANCE.getUnshownWelcomeScreens(realm);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        for (WelcomeScreen screen : welcomeScreens)
            DataController.INSTANCE.setWelcomeScreenShown(screen);

        initMagicIndicator();
    }

    private void initMagicIndicator() {
        MagicIndicator magicIndicator = findViewById(R.id.magic_indicator);
        if (welcomeScreens.size() > 1) {
            CircleNavigator circleNavigator = new CircleNavigator(this);
            circleNavigator.setCircleCount(welcomeScreens.size());
            circleNavigator.setCircleColor(Color.WHITE);
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, r.getDisplayMetrics());
            circleNavigator.setRadius((int) px);
            circleNavigator.setCircleSpacing((int) px * 4);
            circleNavigator.setStrokeWidth((int) (px / 4));
            circleNavigator.setCircleClickListener(index -> mViewPager.setCurrentItem(index));
            magicIndicator.setNavigator(circleNavigator);
            ViewPagerHelper.bind(magicIndicator, mViewPager);
        } else {
            magicIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public String getScreenName() {
        return "Велком экраны";
    }

    @Override
    public void onLoadError(int id) {
        DataController.INSTANCE.setWelcomeScreenError(id);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            WelcomeScreen welcomeScreen = welcomeScreens.get(position);
            return WelcomeFragment.newInstance(welcomeScreen, isBigScreen);
        }

        @Override
        public int getCount() {
            return welcomeScreens.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "" + (position + 1);
        }
    }
}
