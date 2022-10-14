package jewelrock.irev.com.jewelrock.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import jewelrock.irev.com.jewelrock.BaseRealmActivity
import jewelrock.irev.com.jewelrock.BuildConfig
import jewelrock.irev.com.jewelrock.R
import jewelrock.irev.com.jewelrock.SplashActivity
import jewelrock.irev.com.jewelrock.controller.UserSettingsController
import jewelrock.irev.com.jewelrock.model.SettingsItem
import jewelrock.irev.com.jewelrock.subscribe.PaymentActivity
import jewelrock.irev.com.jewelrock.utils.DialogUtils
import java.util.*

class SettingsActivity : BaseRealmActivity() {

    @BindView(R.id.recyclerView)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    lateinit var menu: ArrayList<SettingsItem>

    private val settings: ArrayList<SettingsItem>
        get() {
            val menu = ArrayList<SettingsItem>()
            val titles = resources.getStringArray(R.array.settings_titles)
            val isSwitch = resources.getIntArray(R.array.settings_switch)
            for (i in titles.indices) {
                var isOn = false
                var index = i
                if (index >= 3 && !BuildConfig.HAS_MOTIVATOR) index++
                if (index >= 4 && !BuildConfig.HAS_STREAMING_BUTTON) index++
                if (index >= 5 && !BuildConfig.HAS_MOTIVATOR_BAUBAY_22) index++
                if (index >= 6 && !BuildConfig.VIDEO_SPLASH) index++
                if (index >= 7 && !BuildConfig.HAS_AUTOSKROLL_BAR) index++
                when (index) {
                    1 -> isOn = userSettings.isPlayCycle
                    2 -> isOn = userSettings.isShowAd
                    3 -> isOn = userSettings.isShowMotivator
                    4 -> isOn = userSettings.isShowStreamingBauBay
                    5 -> isOn = userSettings.isShowMotivatorBauBay22
                    6 -> isOn = userSettings.isVideoSplash
                    7 -> isOn = userSettings.isAutoScrollSeekBar
                    8 -> isOn = userSettings.isSoundsOn
                }
                val item = SettingsItem(titles[i], isOn, isSwitch[i])
                menu.add(item)
            }
            this.menu = menu
            return menu
        }

    private val itemClickListener = View.OnClickListener { v ->
        val item = v?.tag as SettingsItem
        var index = menu.indexOf(item)
        if (index >= 3 && !BuildConfig.HAS_MOTIVATOR) index++
        if (index >= 4 && !BuildConfig.HAS_STREAMING_BUTTON) index++
        if (index >= 5 && !BuildConfig.HAS_MOTIVATOR_BAUBAY_22) index++
        if (index >= 6 && !BuildConfig.VIDEO_SPLASH) index++
        if (index >= 7 && !BuildConfig.HAS_AUTOSKROLL_BAR) index++
        if (index < 0) return@OnClickListener
        when (index) {
            0 -> {
                PaymentActivity.start(this@SettingsActivity, true, SplashActivity.PAYMENT_ACTIVITY_REQUEST_CODE, PaymentActivity.FROM_GLAVNI_EKRAN)
            }
            1 -> {
                if (userSettings.isPaid) {
                    item.isOn = !item.isOn
                    UserSettingsController.setStartOver(realm, item.isOn)
                } else
                    PaymentActivity.start(this@SettingsActivity, PaymentActivity.FROM_NASTROIKI_ZACIKLIVANIE)
            }
            2 -> {
                if (userSettings.isPaid) {
                    item.isOn = !item.isOn
                    UserSettingsController.setShowAd(realm, item.isOn)
                } else
                    PaymentActivity.start(this@SettingsActivity, PaymentActivity.FROM_NASTROIKI_REKLAMA)
            }
            3 -> {
                if (userSettings.isPaid) {
                    item.isOn = !item.isOn
                    UserSettingsController.setShowMotivator(realm, item.isOn)
                } else
                    PaymentActivity.start(this@SettingsActivity, PaymentActivity.FROM_NASTROIKI_MOTIVATOR)
            }
            4 -> {
                if (userSettings.isPaid) {
                    item.isOn = !item.isOn
                    UserSettingsController.setShowStreamingBauBay(realm, item.isOn)
                } else
                    PaymentActivity.start(this@SettingsActivity, PaymentActivity.FROM_NASTROIKI_STREAMING_BAUBAY)
            }
            5 -> {
                if (userSettings.isPaid) {
                    item.isOn = !item.isOn
                    UserSettingsController.setShowMotivatorBauBay22(realm, item.isOn)
                } else
                    PaymentActivity.start(this@SettingsActivity, PaymentActivity.FROM_NASTROIKI_MOTIVATOR_BAUBAY_22)
            }
            6 -> {
                if (userSettings.isPaid) {
                    item.isOn = !item.isOn
                    UserSettingsController.setVideoSplash(realm, item.isOn)
                } else
                    PaymentActivity.start(this@SettingsActivity, PaymentActivity.FROM_NASTROIKI_VIDEO_SPLASH)
            }
            7 -> {
                if (userSettings.isPaid) {
                    item.isOn = !item.isOn
                    UserSettingsController.setAutoScrollSeekBar(realm, item.isOn)
                } else
                    PaymentActivity.start(this@SettingsActivity, PaymentActivity.FROM_NASTROIKI_AUTOSKROLL_SEEKBAR)
            }
            8 -> {
                item.isOn = !item.isOn
                UserSettingsController.setSoundOn(item.isOn)
            }
            9 -> {
                val uri = Uri.parse("market://search?q=pub:iRevolution+Ltd.")
                val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
                try {
                    startActivity(myAppLinkToMarket)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this@SettingsActivity, "You don't have Google Play installed", Toast.LENGTH_LONG).show()

                }

            }
        }
        recyclerView.adapter.notifyItemChanged(menu.indexOf(item))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ButterKnife.bind(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SettingsAdapter(settings, itemClickListener)

        initToolbar()
    }

    private fun initToolbar() {
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.search_cancel_color))
        setSupportActionBar(toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SplashActivity.PAYMENT_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK)
            DialogUtils.alert(this, "Покупки успешно восстановлены")
            else DialogUtils.alert(this, "Нет покупок для восстановления")
        }
    }

    override fun getScreenName(): String {
        return "Настройки"
    }

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}
