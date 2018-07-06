package com.os.drewel.activity

import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.NetworkUtils
import com.os.drewel.R
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.application.DrewelApplication
import com.os.drewel.utill.ValidationUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.content_activity_forgot_password.*
import kotlinx.android.synthetic.main.progress_dialog_loading.*

/**
 * Created by monikab on 3/5/2018.
 */
class ForgotPasswordActivity : BaseActivity(), View.OnClickListener {

    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        initView()
    }

    private fun initView() {

        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        forgotPwButton.setOnClickListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val drawable = loadingProgressBar.indeterminateDrawable.mutate()
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN)
            loadingProgressBar.indeterminateDrawable = drawable
        }
    }


    override fun onClick(view: View) {

        when (view.id) {
            R.id.forgotPwButton -> {
                if(validateForgotPassword()){
                    KeyboardUtils.hideSoftInput(this)

                    if (NetworkUtils.isConnected()) {
                        callForgotPasswordApi()
                    }else
                        Toast.makeText(this,getString(R.string.error_network_connection), Toast.LENGTH_LONG).show()


                }
            }
        }

    }

    private fun validateForgotPassword() : Boolean{
        if (TextUtils.isEmpty(emailAddressEditText.text.toString())) {
            emailAddressTextLayout.error = getString(R.string.enter_email_address)
            emailAddressTextLayout.requestFocus()
            return false
        }
        emailAddressTextLayout.isErrorEnabled = false

        if (!ValidationUtil.isEmailValid(emailAddressEditText.text.toString())) {
            emailAddressTextLayout.error = getString(R.string.enter_email_address)
            emailAddressTextLayout.requestFocus()
            return false
        }
        emailAddressTextLayout.isErrorEnabled = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        KeyboardUtils.hideSoftInput(this)
        super.onBackPressed()
    }


    private fun callForgotPasswordApi() {
        setProgressState(View.VISIBLE, false)

        val signupRequest = HashMap<String, String>()
        signupRequest["email"] = emailAddressEditText.text.toString().trim()
        signupRequest["language"] = DrewelApplication.getInstance().getLanguage()

        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).forgotPassword(signupRequest)
        disposable = signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE, true)
                    Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()
                    if (result.response!!.status!!) {
                            finish()
                    }
                }, { error ->
                    setProgressState(View.GONE, true)
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                )
    }
    private fun setProgressState(visibility: Int, enableButton: Boolean) {
        progressBar.visibility = visibility
        forgotPwButton.isEnabled = enableButton
    }

}