package com.os.drewel.fragment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import com.nostra13.universalimageloader.core.ImageLoader
import com.os.drewel.R
import com.os.drewel.activity.*
import com.os.drewel.activity.BaseActivity.Companion.isLanguageChange
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.application.DrewelApplication
import com.os.drewel.application.DrewelApplication.Companion.user_unread_count
import com.os.drewel.constant.Constants
import com.os.drewel.prefrences.Prefs.Companion.prefs
import com.os.drewel.rxbus.NotificationRxJavaBus
import com.os.drewel.rxbus.UnreadCountRxJavaBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_setting_activity.*


/**
 * Created by sharukhb on 3/13/2018.
 */

class SettingFragment : BaseFragment(), View.OnClickListener {

    private var logoutDisposable: Disposable? = null
    private var notificationOnOffDisposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.setting_activity, container, false)
    }

    private fun updateMenuTitles() {
        var activity = activity as HomeActivity
        if (activity.menu == null)
            return
        activity.menu!!.findItem(R.id.menu_addrequest).isVisible = false
        activity.menu!!.findItem(R.id.menu_filter).isVisible = false
        activity.menu!!.findItem(R.id.menu_carts).isVisible = true
        activity.menu!!.findItem(R.id.menu_whishlist).isVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateMenuTitles()
        /* set click listeners of buttons*/
        setClickListeners()
        if (user_unread_count.toInt() > 0) {
            tv_unread_count.text = user_unread_count.toString()
            tv_unread_count.visibility = View.VISIBLE
//                    setDynamicallyParam(pref!!.getPreferenceIntData(pref!!.UNREAD_COUNT).toString())
        } else {
            tv_unread_count.visibility = View.GONE
        }
        if (pref!!.getPreferenceBooleanData(pref!!.KEY_SOCIAL_LOGIN)) {
            tv_change_password.visibility = View.GONE
            changePwView.visibility = View.GONE
        } else {
            tv_change_password.visibility = View.VISIBLE
            changePwView.visibility = View.VISIBLE
        }
        NotificationRxJavaBus.getInstance().notificationPublishSubject.subscribe { addToWishList ->
            if (isAdded) {
                if (pref!!.getPreferenceIntData(pref!!.UNREAD_COUNT) > 0) {
                    tv_notifications_count.text = pref!!.getPreferenceIntData(pref!!.UNREAD_COUNT).toString()
                    tv_notifications_count.visibility = View.VISIBLE
                    setDynamicallyParam(pref!!.getPreferenceIntData(pref!!.UNREAD_COUNT).toString())
                } else {
                    tv_notifications_count.visibility = View.GONE
                }
            }
        }

        UnreadCountRxJavaBus.getInstance().unreadCountRxJavaBus.subscribe { count ->
            if (isAdded) {
                if (user_unread_count > 0) {
                    tv_unread_count.text = count
                    tv_unread_count.visibility = View.VISIBLE
//                    setDynamicallyParam(pref!!.getPreferenceIntData(pref!!.UNREAD_COUNT).toString())
                } else {
                    tv_unread_count.visibility = View.GONE
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        tv_profile_name.text = pref!!.getPreferenceStringData(pref!!.KEY_FIRST_NAME) + " " + pref!!.getPreferenceStringData(pref!!.KEY_LAST_NAME)
        tv_profile_email.text = pref!!.getPreferenceStringData(pref!!.KEY_EMAIL)
        ImageLoader.getInstance().displayImage(pref!!.getPreferenceStringData(pref!!.KEY_PHOTO), imv_profile_img, DrewelApplication.getInstance().options)
        if (pref!!.getPreferenceIntData(pref!!.UNREAD_COUNT) > 0) {
            tv_notifications_count.text = pref!!.getPreferenceIntData(pref!!.UNREAD_COUNT).toString()
            tv_notifications_count.visibility = View.VISIBLE
            setDynamicallyParam(pref!!.getPreferenceIntData(pref!!.UNREAD_COUNT).toString())
        }
    }

    private fun setDynamicallyParam(cartItemQuantity: String) {
        if (cartItemQuantity.length > 2) {
            tv_notifications_count.measure(0, 0)
            val width = tv_notifications_count.measuredWidth
            val linearPram = LinearLayout.LayoutParams(width, width)
            tv_notifications_count.layoutParams = linearPram
        }
    }

    override fun onResume() {
        super.onResume()
        if (pref!!.getPreferenceIntData(pref!!.UNREAD_COUNT) > 0) {
            tv_notifications_count.text = pref!!.getPreferenceIntData(pref!!.UNREAD_COUNT).toString()
            tv_notifications_count.visibility = View.VISIBLE
            setDynamicallyParam(pref!!.getPreferenceIntData(pref!!.UNREAD_COUNT).toString())
        }
    }

    private fun setClickListeners() {
        tv_sign_out.setOnClickListener(this)
        tv_change_password.setOnClickListener(this)
        tv_change_language.setOnClickListener(this)
        tv_delivery_address.setOnClickListener(this)
        contactUsTv.setOnClickListener(this)
        rel_profile.setOnClickListener(this)
        tv_about_app.setOnClickListener(this)
        tv_notifications.setOnClickListener(this)
        tv_wallet.setOnClickListener(this)
        tv_rate_us.setOnClickListener(this)

        switch_off_on_notification.isChecked = !pref!!.getPreferenceStringData(pref!!.KEY_NOTIFICATION_STATUS).equals("off")

        switch_off_on_notification.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (isNetworkAvailable()) {
                switch_off_on_notification.isEnabled = false
                if (b)
                    callNotificationOnOffAPI("on")
                else
                    callNotificationOnOffAPI("off")
            }
        }
    }


    /*r show logout confirmation popup to use*/
    private fun showLogoutDialog() {
        val logoutAlertDialog = AlertDialog.Builder(this.context!!, R.style.DeliveryTypeTheme).create()
        logoutAlertDialog.setTitle(getString(R.string.app_name))
        logoutAlertDialog.setMessage(getString(R.string.want_to_logout))
        logoutAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), DialogInterface.OnClickListener { dialog, id ->
            logoutAlertDialog.dismiss()
            if (isNetworkAvailable())
                callLogoutAPI()
        })
        logoutAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), DialogInterface.OnClickListener { dialog, id ->
            logoutAlertDialog.dismiss()
        })
        logoutAlertDialog.show()
    }


    /* show change language confirmation popup to user*/
    private fun showChangeLanguageDialog() {

        val changeLanguageAlertDialog = AlertDialog.Builder(this.context!!, R.style.DeliveryTypeTheme).create()

//        changeLanguageAlertDialog.setTitle(getString(R.string.app_name))
        changeLanguageAlertDialog.setMessage(getString(R.string.choose_language))


        changeLanguageAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.english), DialogInterface.OnClickListener { dialog, id ->
            changeLanguageAlertDialog.dismiss()
            changeLanguage(Constants.LANGUAGE_ENGLISH)
        })

        changeLanguageAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.arabic), DialogInterface.OnClickListener { dialog, id ->

            changeLanguageAlertDialog.dismiss()
            changeLanguage(Constants.LANGUAGE_ARABIC)
        })

        changeLanguageAlertDialog.show()
    }

    private fun changeLanguage(language: String) {
        pref!!.setPreferenceStringData(pref!!.KEY_LANGUAGE, language)
        DrewelApplication.getInstance().setLocale(language, context!!)
        isLanguageChange = true

        refreshActivity()
        activity?.recreate()
    }

    override fun onClick(view: View) {

        when (view.id) {
            R.id.tv_sign_out -> {
                if (isNetworkAvailable())
                    showLogoutDialog()
            }

            R.id.tv_change_password -> {
                val intent = Intent(activity, ChangePasswordActivity::class.java)
                startActivity(intent)
            }

            R.id.tv_change_language -> {
                showChangeLanguageDialog()
            }

            R.id.tv_delivery_address -> {
                val intent = Intent(activity, DeliveryAddressActivity::class.java)
                startActivity(intent)
            }

            R.id.contactUsTv -> {
                contactUs()
            }

            R.id.rel_profile -> {
                val intent = Intent(activity, ProfileActivity::class.java)
                startActivity(intent)
            }

            R.id.tv_about_app -> {
                val intent = Intent(activity, AboutAppActivity::class.java)
                startActivity(intent)
            }

            R.id.tv_notifications -> {
                val intent = Intent(activity, NotificationActivity::class.java)
                startActivity(intent)
            }

            R.id.tv_wallet -> {
                val intent = Intent(activity, WalletOptionsActivity::class.java)
                startActivity(intent)
            }
            R.id.tv_rate_us -> {
                val uri = Uri.parse("market://details?id=" + context!!.packageName)
                val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(goToMarket)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + context!!.packageName)))
                }
            }
        }
    }

    fun call(context: Context, number: String) {
        var uri = "tel:" + number.trim()
        var intent = Intent(Intent.ACTION_DIAL)
        intent.setData(Uri.parse(uri))
        context.startActivity(intent)
    }

    private fun contactUs() {

        val items = arrayOf<CharSequence>(getString(R.string.call_us), getString(R.string.mail_us), getString(R.string.chat_us), getString(R.string.cancel))

        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle(getString(R.string.contact_us))

        builder.setItems(items) { dialog, item ->
            when {
                items[item] == getString(R.string.call_us) -> {
                    dialog.dismiss()
                    try {
                        call(context!!, "+638 125458")
                    } catch (e: ActivityNotFoundException) {
                    }
                }
                items[item] == getString(R.string.chat_us) -> {
                    callChatAPI()

                    dialog.dismiss()
                }
                items[item] == getString(R.string.mail_us) -> {
                    dialog.dismiss()
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "support.drewel@gmail.com"))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Drewel")
                        //intent.putExtra(Intent.EXTRA_TEXT, "your_text");
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                    }
                }
                items[item] == getString(R.string.cancel) -> dialog.dismiss()
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun callChatAPI() {
        setProgressState(View.VISIBLE, false)
        val logoutRequest = HashMap<String, String>()
        logoutRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        logoutRequest["language"] = DrewelApplication.getInstance().getLanguage()

        val logoutObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).add_chat(logoutRequest)
        logoutDisposable = logoutObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    // DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!,context!!)
                    setProgressState(View.GONE, true)
//                    com.os.drewel.utill.Utils.getInstance().showToast(activity, result.response!!.message!!)
                    if (result.response!!.status!!) {

                        startActivity(Intent(activity, MessageDetail_Activity::class.java).putExtra("admin_id", result.response!!.data!!.admin_id).putExtra("admin_img", result.response!!.data!!.img))
//                        val intent = Intent(activity, WelcomeActivity::class.java)
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                        startActivity(intent)
//                        activity!!.finish()
                    }
                }, { error ->
                    setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(activity, error.message!!)
                    Log.e("TAG", "{$error.message}")
                })
    }

    private fun callLogoutAPI() {
        setProgressState(View.VISIBLE, false)
        val logoutRequest = HashMap<String, String>()
        logoutRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        logoutRequest["language"] = DrewelApplication.getInstance().getLanguage()

        val logoutObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).logout(logoutRequest)
        logoutDisposable = logoutObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    // DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!,context!!)
                    setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(activity, result.response!!.message!!)
                    if (result.response!!.status!!) {

                        prefs!!.setPreferenceStringData(prefs!!.KEY_USER_ID, "")
                        prefs!!.setPreferenceStringData(prefs!!.KEY_FIRST_NAME, "")
                        prefs!!.setPreferenceStringData(prefs!!.KEY_LAST_NAME, "")
                        prefs!!.setPreferenceStringData(prefs!!.KEY_EMAIL, "")
                        prefs!!.setPreferenceStringData(prefs!!.KEY_PHOTO, "")
                        prefs!!.setPreferenceStringData(prefs!!.KEY_ROLE_ID, "")
                        prefs!!.clearSharedPreference()
                        val intent = Intent(activity, WelcomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        activity!!.finish()
                    }
                }, { error ->
                    setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(activity, error.message!!)
                    Log.e("TAG", "{$error.message}")
                })
    }


    private fun callNotificationOnOffAPI(status: String) {
        setProgressState(View.VISIBLE, false)
        val notificationStatusRequest = HashMap<String, String>()
        notificationStatusRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        notificationStatusRequest["language"] = DrewelApplication.getInstance().getLanguage()
        notificationStatusRequest["is_notification"] = status

        val notificationStatusObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).notificationOnOff(notificationStatusRequest)
        notificationOnOffDisposable = notificationStatusObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, context!!)
                    switch_off_on_notification.isEnabled = true
                    setProgressState(View.GONE, true)

                    com.os.drewel.utill.Utils.getInstance().showToast(activity, result.response!!.message!!)
                    if (result.response!!.status!!) {
                        pref!!.setPreferenceStringData(pref!!.KEY_NOTIFICATION_STATUS, status)
                    }
                }, { error ->
                    switch_off_on_notification.isEnabled = true
                    setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(activity, error.message!!)
                    Log.e("TAG", "{$error.message}")
                })
    }

    private fun setProgressState(progressVisibility: Int, enable: Boolean) {
        progressBar.visibility = progressVisibility
        settingsOptionLL.isEnabled = enable
        for (i in 0 until settingsOptionLL.childCount) {
            val child = settingsOptionLL.getChildAt(i)
            child.isEnabled = enable
        }
    }

    override fun onStop() {
        super.onStop()
        /* if user navigate to another activity then stop calling logout api and hide progressbar*/
        setProgressState(View.GONE, true)

        if (logoutDisposable != null)
            logoutDisposable!!.dispose()

    }

    private fun refreshActivity() {
        val intent = activity!!.intent
        startActivity(intent)
        activity!!.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationOnOffDisposable?.dispose()
    }

}