package com.os.drewel.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.os.drewel.R
import com.os.drewel.apicall.DrewelApi
import kotlinx.android.synthetic.main.activity_about_app.*
import kotlinx.android.synthetic.main.app_toolbar.*
import android.content.pm.PackageManager
import android.R.attr.versionName
import android.content.pm.PackageInfo
import com.os.drewel.application.DrewelApplication


class AboutAppActivity : BaseActivity(), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)
        initView()
    }

    override fun onResume() {
        super.onResume()
        DrewelApplication.getInstance().setLocale( DrewelApplication.getInstance().getLanguage(),this)
    }
    /* set toolbar and back button*/
    private fun initView() {
        toolbarTitleTv.text = getString(R.string.about_app)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        setOnClickListener()
    }

    private fun setOnClickListener() {

        ll_howItsWork.setOnClickListener(this)
        ll_termsOfUse.setOnClickListener(this)
        ll_privacyPolicy.setOnClickListener(this)
        ll_FAQ.setOnClickListener(this)
        try {
            val pInfo = this.packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            versionTv.text=version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        /* finish activity if user tap on action bar back button*/
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onClick(v: View) {
        when(v.id){
            R.id.ll_howItsWork ->{
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(DrewelApi.HOW_ITS_WORK_URL))
                startActivity(browserIntent)
            }
            R.id.ll_termsOfUse ->{
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(DrewelApi.TERMS_OF_USE_URL))
                startActivity(browserIntent)
            }
            R.id.ll_privacyPolicy ->{
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(DrewelApi.PRIVACY_POLICY_URL))
                startActivity(browserIntent)
            }
            R.id.ll_FAQ ->{
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(DrewelApi.FAQ_URL))
                startActivity(browserIntent)
            }
        }
    }
}


