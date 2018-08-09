package com.os.drewel.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.text.Html
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.blankj.utilcode.util.NetworkUtils
import com.facebook.login.LoginManager
import com.os.drewel.R
import com.os.drewel.adapter.WelcomeAdapter
import kotlinx.android.synthetic.main.activity_welcome.*

/**
 * Created by monikab on 2/21/2018.
 */
class WelcomeActivity : BaseActivity() , View.OnClickListener{


    private var welcomeAdapter: WelcomeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        setAdapter()
        initView()
        LoginManager.getInstance().logOut()
        welcome_viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                bottomDots(position)
            }

        })
    }

    private fun initView() {

        SignUpButton.setOnClickListener(this)
        LoginButton.setOnClickListener(this)
        facebookLoginButton.setOnClickListener(this)
        facebookLoginLayout.setOnClickListener(this)
        changeLanguageTv.setOnClickListener(this)

        setLanguageText(changeLanguageTv)
    }

    private fun bottomDots(position: Int) {
        welcome_page_dots.removeAllViews()
        for (i in 0 until 3) {

            val tvDot = TextView(this)
            tvDot.text = Html.fromHtml("&#8226;")
            tvDot.textSize = resources.getDimension(R.dimen._15sdp)

            if (position == i) {
                tvDot.setTextColor(ContextCompat.getColor(this, R.color.orange_1))
            } else {
                tvDot.setTextColor(ContextCompat.getColor(this, R.color.light_grey_2))
            }
            welcome_page_dots.addView(tvDot)
        }

    }

    private fun setAdapter() {
        if (welcomeAdapter == null) {
            welcomeAdapter = WelcomeAdapter(supportFragmentManager)
            welcome_viewpager.adapter = welcomeAdapter
            bottomDots(welcome_viewpager.currentItem)
        } else {
            welcomeAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onClick(view: View) {

            when(view.id){
                R.id.facebookLoginButton -> {
                    if (NetworkUtils.isConnected()) {
                        callFacebookLogin()
                    }else
                        com.os.drewel.utill.Utils.getInstance().showToast(this,getString(R.string.error_network_connection))


                }
                R.id.facebookLoginLayout -> {
                    if (NetworkUtils.isConnected()) {
                        callFacebookLogin()
                    }else
                        com.os.drewel.utill.Utils.getInstance().showToast(this,getString(R.string.error_network_connection))


                }
                R.id.SignUpButton -> {
                    startActivity(Intent(this, SignUpActivity::class.java))
                }
                R.id.LoginButton -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }

                R.id.changeLanguageTv ->
                {
                    showChangeLanguageDialog(changeLanguageTv)
                }
            }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.change_language_menu, menu)
        return true
    }

}