package com.os.drewel.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatTextView
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.os.drewel.R
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.application.DrewelApplication
import com.os.drewel.fragment.*
import com.os.drewel.prefrences.Prefs
import com.os.drewel.utill.BadgeIntentService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.home_product_activity.*
import kotlinx.android.synthetic.main.layout_product.*
import me.leolin.shortcutbadger.ShortcutBadger

/**
 * Created by monikab on 3/12/2018.
 */

class HomeActivity : ProductBaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_product_activity)
        initView()
    }


    private fun initView() {
        driveActivityName = this.javaClass.name
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setDisplayShowHomeEnabled(false)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setHomeAsUpIndicator(R.mipmap.logo)
        navigationView!!.setOnNavigationItemSelectedListener(this)
        removeShiftMode(navigationView)
        if (isLanguageChange) {
            navigationView.selectedItemId = R.id.navigation_more
            isLanguageChange = false
        } else
            setFragment(CategoryFragment())
        addressTv.setOnClickListener(this)
        callUnreadNotifApi()
    }


    private fun setFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager

        fragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    @SuppressLint("CheckResult")
    private fun callUnreadNotifApi() {
        val notifyMeRequest = HashMap<String, String>()
        notifyMeRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        notifyMeRequest["language"] = DrewelApplication.getInstance().getLanguage()
        val notifyMeObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).unread_notification(notifyMeRequest)

        notifyMeObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    // setProgressState(View.GONE, true)
//                    Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()
                    if (result.response!!.status!!) {
                        val prefs = Prefs.getInstance(this)
                        prefs.setPreferenceIntData(prefs.UNREAD_COUNT, result.response!!.data!!.unread!!)
                        ShortcutBadger.applyCount(this, result.response!!.data!!.unread!!)
//                        startService(
//                                Intent(this, BadgeIntentService::class.java).putExtra("badgeCount", result.response!!.data!!.unread)
//                        )
                    }
                }, { error ->
                    // setProgressState(View.GONE, true)
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
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
                addressTv.text = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS)
                setFragment(CategoryFragment())
                return true
            }
            R.id.navigation_search -> {
                setTitle(getString(R.string.offers))
                setFragment(DiscountFragment())
                return true
            }

            R.id.navigation_my_order -> {
                setTitle(getString(R.string.myorder))
                setFragment(MyOrderFragment())
                return true

            }
            R.id.navigation_request -> {
                setTitle(getString(R.string.request))
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
                item.setShiftingMode(false)
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
                startActivity(Intent(this, DeliveryAddressActivity::class.java))
            }
        }
    }


    override fun onResume() {
        super.onResume()
        addressTv.text = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_NAME)
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
