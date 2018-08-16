package com.os.drewel.activity

import android.content.Intent
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
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.prefrences.Prefs
import com.os.drewel.utill.Utils
import com.os.drewel.utill.ValidationUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.progress_dialog_loading.*

/**
 * Created by monikab on 3/5/2018.
 */
class LoginActivity : BaseActivity(), View.OnClickListener {

    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()
    }

    private fun initView() {

        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        signUp.setOnClickListener(this)
        loginButton.setOnClickListener(this)
        facebookLoginButton.setOnClickListener(this)
        forgotPasswordTv.setOnClickListener(this)
        changeLanguageTv.setOnClickListener(this)
        dontHaveAccountTv.setOnClickListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val drawable = loadingProgressBar.indeterminateDrawable.mutate()
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN)
            loadingProgressBar.indeterminateDrawable = drawable
        }
        setLanguageText(changeLanguageTv)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.signUp -> {
                KeyboardUtils.hideSoftInput(this)
                startActivity(Intent(this, SignUpActivity::class.java))
            }
            R.id.dontHaveAccountTv -> {
                KeyboardUtils.hideSoftInput(this)
                startActivity(Intent(this, SignUpActivity::class.java))
            }
            R.id.loginButton -> {
                // startActivity(Intent(this, MainActivity::class.java))
                if (validateLogin()) {
                    KeyboardUtils.hideSoftInput(this)
                    if (NetworkUtils.isConnected()) {
                        setProgressState(View.VISIBLE, false)
                        callLoginApi()
                    } else
                        Utils.getInstance().showToast(this,getString(R.string.error_network_connection))
                }
            }
            R.id.facebookLoginButton -> {
                callFacebookLogin()
            }
            R.id.forgotPasswordTv -> {
                KeyboardUtils.hideSoftInput(this)
                startActivity(Intent(this, ForgotPasswordActivity::class.java))
            }
            R.id.changeLanguageTv -> {
                showChangeLanguageDialog(changeLanguageTv)
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        KeyboardUtils.hideSoftInput(this)
    }


    private fun validateLogin(): Boolean {

        if (TextUtils.isEmpty(emailAddressEditText.text.toString())) {
            emailAddressTextLayout.error = getString(R.string.enter_email_address)
            emailAddressTextLayout.requestFocus()
            return false
        }
        emailAddressTextLayout.isErrorEnabled = false

        if (!ValidationUtil.isEmailValid(emailAddressEditText.text.toString())) {
            emailAddressTextLayout.error = getString(R.string.enter_valid_email_address)
            emailAddressTextLayout.requestFocus()
            return false
        }
        emailAddressTextLayout.isErrorEnabled = false

        if (passwordTextLayout.visibility == View.VISIBLE && passwordEditText.text.toString().isEmpty()) {
            passwordTextLayout.error = getString(R.string.empty_password)
            passwordTextLayout.requestFocus()
            return false
        }
        passwordTextLayout.isErrorEnabled = false

        if (passwordTextLayout.visibility == View.VISIBLE && passwordEditText.text.toString().length < 6) {
            passwordTextLayout.error = getString(R.string.short_password)
            passwordTextLayout.requestFocus()
            return false
        }
        passwordTextLayout.isErrorEnabled = false
/*
        if (passwordTextLayout.visibility == View.VISIBLE && !(passwordEditText.text.toString().matches(".*[A-Za-z]+.*[0-9]+.*".toRegex()) || passwordEditText.text.toString().matches(".*[0-9]+.*[A-Za-z]+.*".toRegex()))) {
            passwordTextLayout.error = getString(R.string.invalid_password)
            passwordTextLayout.requestFocus()
            return false
        }*/
        passwordTextLayout.isErrorEnabled = false


        return true
    }


    fun callLoginApi() {

        val loginRequest = mutableMapOf<String, String>()
        loginRequest["email"] = emailAddressEditText.text.toString().trim()
        loginRequest["device_id"] = pref!!.getPreferenceStringData(pref!!.KEY_DEVICE_ID)
        loginRequest["device_type"] = "android"
        loginRequest["password"] = passwordEditText.text.toString().trim()
        loginRequest["language"] = DrewelApplication.getInstance().getLanguage()

        val loginObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).login(loginRequest)
        disposable = loginObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE, true)
                    Utils.getInstance().showToast(this,result.response!!.message!!)
                    if (result.response!!.status!!) {

                        if (result.response!!.data!!.isMobileverify.equals("0")) {
                            val intent = Intent(this, OTPVerificationActivity::class.java)
                            intent.putExtra(AppIntentExtraKeys.MOBILE_NUMBER, result.response!!.data!!.country_code + result.response!!.data!!.mobile_number)
                            intent.putExtra(AppIntentExtraKeys.USER_ID, result.response!!.data!!.userId)
                            intent.putExtra(AppIntentExtraKeys.OTP, result.response!!.data!!.authotp)
                            startActivity(intent)
                            finish()
                        } else {
                            val prefs = Prefs.getInstance(this)
                            prefs.setPreferenceStringData(prefs.KEY_USER_ID, result.response!!.data!!.userId!!)
                            prefs.setPreferenceStringData(prefs.KEY_FIRST_NAME, result.response!!.data!!.firstName!!)
                            prefs.setPreferenceStringData(prefs.KEY_MOBILE, result.response!!.data!!.mobile_number!!)

                            prefs.setPreferenceStringData(prefs.KEY_COUNTRY_CODE, result.response!!.data!!.country_code!!)
                            prefs.setPreferenceStringData(prefs.KEY_LAST_NAME, result.response!!.data?.lastName
                                    ?: "")
                            prefs.setPreferenceStringData(prefs.KEY_EMAIL, result.response!!.data!!.email!!)
                            prefs.setPreferenceStringData(prefs.KEY_PHOTO, result.response!!.data?.img
                                    ?: "")
                            prefs.setPreferenceStringData(prefs.KEY_ROLE_ID, result.response!!.data!!.roleId!!)
                            prefs.setPreferenceStringData(prefs.KEY_CART_ID, result.response!!.data?.cartId
                                    ?: "")
                            prefs.setPreferenceStringData(prefs.KEY_CART_ITEM_COUNT, result.response!!.data?.cartQuantity
                                    ?: "")

                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS, result.response!!.data?.address
                                    ?: "")
                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_ID, result.response!!.data?.addressId
                                    ?: "")
                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_NAME, result.response!!.data!!.addressName!!)

                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_ID, result.response!!.data?.addressId
                                    ?: "")

                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_USERNAME, result.response!!.data?.username
                                    ?: "")

                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_PHONE_NUMBER, result.response!!.data?.mobileNumber
                                    ?: "")

                            prefs.setPreferenceStringData(prefs.KEY_FULL_DELIVERY_ADDRESS, result.response!!.data?.fullAddress
                                    ?: "")

                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_lANDMARK, result.response!!.data?.landmark
                                    ?: "")

                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_LATITUDE, result.response!!.data!!.addressLatitude
                                    ?: "")
                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_LONGITUDE, result.response!!.data!!.addressLongitude
                                    ?: "")
                            prefs.setPreferenceBooleanData(prefs.KEY_SOCIAL_LOGIN, false)

//                            val intent = Intent(this, DeliveryAddressActivity::class.java)
//                            startActivity(intent) if (prefs.getPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS).isEmpty())
//                                Intent(this, DeliveryAddressActivity::class.java)
//                            else
                            val intent =  Intent(this, HomeActivity::class.java)

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                    }
                }, { error ->
                    setProgressState(View.GONE, true)
                    Utils.getInstance().showToast(this,error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun setProgressState(visibility: Int, enableButton: Boolean) {
        progressBar.visibility = visibility
        loginButton.isEnabled = enableButton
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null)
            disposable!!.dispose()
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
}