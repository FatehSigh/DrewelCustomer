package com.os.drewel.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.os.drewel.R
import kotlinx.android.synthetic.main.app_toolbar.*
import kotlinx.android.synthetic.main.layout_wallet_options.*


class WalletOptionsActivity : BaseActivity(), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_wallet_options)
        initView()
    }

    /* set toolbar and back button*/
    private fun initView() {
        toolbarTitleTv.text = getString(R.string.wallet)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        loyaltyPointsTv.setOnClickListener(this)
        walletTv.setOnClickListener(this)
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


    override fun onClick(view: View) {

        when (view.id) {

            R.id.walletTv -> {
                startActivity(Intent(this, WalletTransactionActivity::class.java))
            }

            R.id.loyaltyPointsTv -> {
                val intent = Intent(this, LoyaltyPointsActivity::class.java)
                startActivity(intent)
            }
        }
    }

}


