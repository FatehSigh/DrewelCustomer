package com.os.drewel.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.os.drewel.R
import com.os.drewel.adapter.NotificationAdapter
import com.os.drewel.adapter.ProductAdapter
import com.os.drewel.adapter.TransactionAdpater
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.notificationresponsemodel.Notification
import com.os.drewel.apicall.responsemodel.walletTransactionResponseModel.Transaction
import com.os.drewel.application.DrewelApplication
import com.os.drewel.prefrences.Prefs
import com.os.drewel.utill.EqualSpacingItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.wallet_transaction.*

class WalletTransactionActivity : BaseActivity(), View.OnClickListener {
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
        getTransactionApi()
        addMoneyAPI()
//        setAdapter()
    }

    private fun addMoneyAPI() {

    }

    var transaction: MutableList<Transaction>? = null
    private var disposable: Disposable? = null
    private fun getTransactionApi() {
        setProgressState(View.VISIBLE, false)
        val notificationRequest = HashMap<String, String>()
        notificationRequest["language"] = DrewelApplication.getInstance().getLanguage()
        notificationRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getTransactions(notificationRequest)
        disposable = signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Log.e("result==>", result.toString())
                    setProgressState(View.GONE, true)
                    if (result.response!!.status!!) {
                        transaction = (result.response!!.data!!.transactions as MutableList<Transaction>?)!!
                        tv_blc.setText(result.response!!.data!!.wallet_balance + " " + getString(R.string.omr))
                        setAdapter()

                    } else {
                        com.os.drewel.utill.Utils.getInstance().showToast(this,result.response!!.message!!)
                    }

                }, { error ->
                    setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(this,error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun setAdapter() {
        if (productAdapter == null) {
            productAdapter = TransactionAdpater(this, transaction!!)
            transactionRV.layoutManager = GridLayoutManager(this, 1)
            transactionRV.addItemDecoration(EqualSpacingItemDecoration(16, EqualSpacingItemDecoration.GRID))
            transactionRV.adapter = productAdapter
        } else {
            productAdapter!!.notifyDataSetChanged()
        }
    }

    private fun setProgressState(visibility: Int, enableButton: Boolean) {
        progressBar.visibility = visibility
        transactionRV.isEnabled = enableButton
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
