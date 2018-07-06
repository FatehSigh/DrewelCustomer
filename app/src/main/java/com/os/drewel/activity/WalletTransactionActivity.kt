package com.os.drewel.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.os.drewel.R
import com.os.drewel.adapter.ProductAdapter
import com.os.drewel.adapter.TransactionAdpater
import com.os.drewel.utill.EqualSpacingItemDecoration
import kotlinx.android.synthetic.main.wallet_transaction.*

class WalletTransactionActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_accept_order -> startActivity(Intent(this@WalletTransactionActivity, AddMoneyActivity::class.java))
        }
    }

    private var productAdapter: TransactionAdpater? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wallet_transaction)
        initView()
        addMoneyAPI()
        setAdapter()
    }

    private fun addMoneyAPI() {

    }

    private fun setAdapter() {
        if (productAdapter == null) {
            productAdapter = TransactionAdpater(this)
            transactionRV.layoutManager = GridLayoutManager(this, 1)
            transactionRV.addItemDecoration(EqualSpacingItemDecoration(16, EqualSpacingItemDecoration.GRID))
            transactionRV.adapter = productAdapter
        } else {
            productAdapter!!.notifyDataSetChanged()
        }
    }

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
        btn_accept_order.setOnClickListener(this)
    }


}
