package com.os.drewel.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.os.drewel.R
import com.os.drewel.utill.Utils


import kotlinx.android.synthetic.main.activity_add_money.*

class AddMoneyActivity : BaseActivity() ,View.OnClickListener{


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_money)
        initView()
        addMoneyAPI()
    }
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_accept_order->finish()
        }
    }
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(Utils.getInstance().updateBaseContextLocale(newBase!!))
    }

    private fun addMoneyAPI() {


    }

    private fun tryAgain() {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        /* tv_status.setText(getText(R.string.accepted));
        btn_decline_order.setVisibility(View.GONE);
        btn_accept_order.setText(getText(R.string.pickup_from_vendor));*/
    }


}
