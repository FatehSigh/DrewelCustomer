package com.drewel.drewel.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.drewel.drewel.R
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.application.DrewelApplication.Companion.admin_unread_count
import com.drewel.drewel.application.DrewelApplication.Companion.chat_id
import com.drewel.drewel.application.DrewelApplication.Companion.user_unread_count
import com.drewel.drewel.firebase.MessageDataSource
import com.drewel.drewel.fragment.*
import com.drewel.drewel.model.ChatModel
import com.drewel.drewel.prefrences.Prefs
import com.drewel.drewel.rxbus.NotificationRxJavaBus
import com.drewel.drewel.rxbus.UnreadCountRxJavaBus
import com.drewel.drewel.utill.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.action_bar_unread_icon.view.*
import kotlinx.android.synthetic.main.home_product_activity.*
import kotlinx.android.synthetic.main.layout_product.*
import me.leolin.shortcutbadger.ShortcutBadger

/**
 * Created by monikab on 3/12/2018.
 */

class HomeActivity : ProductBaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener, MessageDataSource.MessagesCallbacks {
    override fun onChannelAdded(message: ChatModel?) {
        if (message!!.channel_info != null) {
            if (message.channel_info!!.admin_count != null)
                admin_unread_count = message.channel_info!!.admin_count!!.toInt()
            if (message.channel_info!!.user_count != null) {
                user_unread_count = message.channel_info!!.user_count!!.toInt()
                UnreadCountRxJavaBus.getInstance().unreadCountRxJavaBus.onNext(user_unread_count.toString())
            }
        }
    }

    override fun onMessageAdded(message: ChatModel?) {

    }

    private fun setDynamicallyParam(cartItemQuantity: String) {
        if (cartItemQuantity.length > 2) {
            countItemView.unreadCountTv.measure(0, 0)
            val width = countItemView.unreadCountTv.measuredWidth

            val linearPram = RelativeLayout.LayoutParams(width, width)
            val marginInDp = -5
            val marginInPx = marginInDp * resources.displayMetrics.density
            linearPram.setMargins(marginInPx.toInt(), marginInPx.toInt(), 0, 0)
//            linearPram.addRule(RelativeLayout.END_OF, R.id.nav_more)

            countItemView.unreadCountTv.layoutParams = linearPram
        }
    }

    private lateinit var countItemView: View
    private fun getViewOfCountMenuItem(menu: BottomNavigationView): View {
        var mbottomNavigationMenuView: BottomNavigationMenuView = menu.getChildAt(0) as BottomNavigationMenuView
        var views = mbottomNavigationMenuView.getChildAt(4);
        var itemView: BottomNavigationItemView = views as BottomNavigationItemView;
        countItemView = LayoutInflater.from(this)
                .inflate(R.layout.action_bar_unread_icon,
                        mbottomNavigationMenuView, false);
        itemView.addView(countItemView)

        setDynamicallyParam(user_unread_count.toString())
        if (user_unread_count.toString().isEmpty() || user_unread_count.toString() == "0")
            itemView.unreadCountTv.visibility = View.GONE
        else
            itemView.unreadCountTv.visibility = View.VISIBLE
        unreadCountRxJavaBus = UnreadCountRxJavaBus.getInstance().unreadCountRxJavaBus.subscribe(
                { cartItemQuantity ->
                    itemView.unreadCountTv.text = cartItemQuantity

                    setDynamicallyParam(cartItemQuantity)

                    if (cartItemQuantity.isEmpty() || cartItemQuantity == "0")
                        itemView.unreadCountTv.visibility = View.GONE
                    else
                        itemView.unreadCountTv.visibility = View.VISIBLE
                },
                { error ->
                    Log.d("error", error.message)
                }
        )

        return countItemView

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_product_activity)
        initView()
    }

    private var firstTimeBackPressed = true
    override fun onBackPressed() {
        if (firstTimeBackPressed) {
            Toast.makeText(this, getText(R.string.Press_again_to_exit), Toast.LENGTH_SHORT).show()
            firstTimeBackPressed = false
        } else {
            super.onBackPressed()
        }
    }

    var prefs: Prefs? = null
    //    private var onlineListener: MessageDataSource.UserListener? = null
    private var mListener: MessageDataSource.ChannelListener? = null

    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setDisplayShowHomeEnabled(false)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setHomeAsUpIndicator(R.mipmap.logo)
        driveActivityName = this.javaClass.name
        navigationView!!.setOnNavigationItemSelectedListener(this)
        removeShiftMode(navigationView)
        if (isLanguageChange) {
            navigationView.selectedItemId = R.id.navigation_more
            isLanguageChange = false
        } else if (intent != null) {
            if (intent.getIntExtra("FROM", 0) == 2) {
                setFragment(MyOrderFragment())
                navigationView.selectedItemId = R.id.navigation_my_order
            } else if (intent.getIntExtra("FROM", 0) == 3) {
                setFragment(CategoryFragment())
                showAlert(getString(R.string.app_name), intent.getStringExtra("msg"))
            } else if (intent.getIntExtra("FROM", 0) == 4) {
                setFragment(MyOrderFragment())
                navigationView.selectedItemId = R.id.navigation_my_order
                showAlert(getString(R.string.app_name), intent.getStringExtra("msg"))
            } else
                setFragment(CategoryFragment())
        } else
            setFragment(CategoryFragment())
        addressTv.setOnClickListener(this)
        prefs = Prefs(this)
        getViewOfCountMenuItem(navigationView)
//        subscribeUnreadCountBus()

//        onlineListener = MessageDataSource.UserListener(chat_id, this)
        if (isNetworkAvailable())
            callUnreadNotifApi()
    }

    private fun setFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    fun showAlert(Title: String, Message: String) {
        try {
            val mAlert = AlertDialog.Builder(this, R.style.DeliveryTypeTheme).create()
            mAlert.setCancelable(false)
            mAlert.setTitle(Title)
            mAlert.setMessage(Message)
            mAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), DialogInterface.OnClickListener { dialog, id ->
                mAlert.dismiss()
                getString(R.string.ok)
            })

//            mAlert.setPositiveButton(
//                    getString(R.string.ok)
//            ) { dialog, which -> dialog.dismiss() }

            mAlert.show()
        } catch (e: Exception) {
        }

    }

    @SuppressLint("CheckResult")
    private fun callUnreadNotifApi() {
        if(pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
            return

        val notifyMeRequest = HashMap<String, String>()
        notifyMeRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        notifyMeRequest["language"] = DrewelApplication.getInstance().getLanguage()
        val notifyMeObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).unread_notification(notifyMeRequest)

        notifyMeObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    if (result.response!!.status!!) {
                        chat_id = result.response!!.data!!.admin_id + Prefs(DrewelApplication.getInstance()).getPreferenceStringData(Prefs(DrewelApplication.getInstance()).KEY_USER_ID)
                        mListener = MessageDataSource.addChannelListener(chat_id, this)
                        val prefs = Prefs.getInstance(this)
                        prefs.setPreferenceIntData(prefs.UNREAD_COUNT, result.response!!.data!!.unread!!)
                        ShortcutBadger.applyCount(this, result.response!!.data!!.unread!!)
                    }
                }, { error ->
                    // setProgressState(View.GONE, true)
                    Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.navigation_home) {
            addressTv.visibility = View.VISIBLE
            toolbarTitle.visibility = View.GONE
        } else {
            addressTv.visibility = View.GONE
            toolbarTitle.visibility = View.VISIBLE
        }


        when (item.itemId) {
            R.id.navigation_home -> {
                if (pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_NAME).isNotEmpty())
                    addressTv.text = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_NAME)
                else
                    addressTv.text = getString(R.string.add_new_address)
                setFragment(CategoryFragment())
                return true
            }

            R.id.navigation_search -> {

                if(pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
                {
                    startActivity(Intent(this@HomeActivity,WelcomeActivity::class.java))
                    finish()
                    return true
                }

                setTitle(getString(R.string.discount))
                setFragment(OfferFragment())
                return true
            }

            R.id.navigation_my_order -> {

                if(pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
                {
                    startActivity(Intent(this@HomeActivity,WelcomeActivity::class.java))
                    finish()
                    return true
                }

                setTitle(getString(R.string.myorder))
                setFragment(MyOrderFragment())
                return true
            }

            R.id.navigation_request -> {

                if(pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
                {
                    startActivity(Intent(this@HomeActivity,WelcomeActivity::class.java))
                    finish()
                    return true
                }

                setTitle(getString(R.string.requests))
                setFragment(RequestFragment())
                return true
            }

            R.id.navigation_more -> {
                setTitle(getString(R.string.setting))
                setFragment(SettingFragment())
                return true
            }
        }
        return false
    }


    @SuppressLint("RestrictedApi")
    private fun removeShiftMode(view: BottomNavigationView) {
        val menuView = view.getChildAt(0) as BottomNavigationMenuView
        try {
            val shiftingMode = menuView.javaClass.getDeclaredField("mShiftingMode")
            shiftingMode.isAccessible = true
            shiftingMode.setBoolean(menuView, false)
            shiftingMode.isAccessible = false
            for (i in 0 until menuView.childCount) {
                val item = menuView.getChildAt(i) as BottomNavigationItemView
                item.setShifting(false)
                // set once again checked value, so view will be updated
                item.setChecked(item.itemData.isChecked)
            }
        } catch (e: NoSuchFieldException) {
            Log.e("ERROR NO SUCH FIELD", "Unable to get shift mode field")
        } catch (e: IllegalAccessException) {
            Log.e("ERROR ILLEGAL ALG", "Unable to change value of shift mode")
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.addressTv -> {
                if (pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty()) {
                    startActivity(Intent(this@HomeActivity, WelcomeActivity::class.java))
                    finish()
                    return
                }
                startActivity(Intent(this, DeliveryAddressActivity::class.java))
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_NAME).isNotEmpty())
            addressTv.text = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_NAME)
        else
            addressTv.text = getString(R.string.add_new_address)

        if (supportFragmentManager.findFragmentById(R.id.container) is SettingFragment)
            NotificationRxJavaBus.getInstance().notificationPublishSubject.onNext("")
    }

    /* set title of action bar*/
    private fun setTitle(title: String) {
        toolbarTitle.text = title
    }

    override fun onDestroy() {
        super.onDestroy()
        cartRxBustDisposable.dispose()
    }
}
