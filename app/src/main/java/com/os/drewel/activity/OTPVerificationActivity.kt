package com.os.drewel.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.NetworkUtils
import com.os.drewel.R
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.prefrences.Prefs
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_activity_otp_verification.*
import kotlinx.android.synthetic.main.otp_verification.*
import kotlinx.android.synthetic.main.progress_dialog_loading.*
import java.util.concurrent.TimeUnit

/**
 * Created by monikab on 3/14/2018.
 */
class OTPVerificationActivity : AppCompatActivity(), View.OnClickListener {

    var disposable: Disposable? = null
    private var userId: String = ""
    private var mobileNumber: String = ""
    private var oTP: String = ""
    private val wait = 60
    private var timer: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otp_verification)
        initView()
        getDataFromBundle()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(com.os.drewel.utill.Utils.getInstance().updateBaseContextLocale(newBase!!))
    }

    private fun getDataFromBundle() {
        if (intent.hasExtra(AppIntentExtraKeys.USER_ID)) {
            userId = intent.getStringExtra(AppIntentExtraKeys.USER_ID)
            mobileNumber = intent.getStringExtra(AppIntentExtraKeys.MOBILE_NUMBER)
            if (intent.getStringExtra(AppIntentExtraKeys.OTP) != null)
                oTP = intent.getStringExtra(AppIntentExtraKeys.OTP)
//            val otpCharacters = oTP.toCharArray()
//            otp_tv_otp_1.setText(otpCharacters[0].toString())
//            otp_tv_otp_2.setText(otpCharacters[1].toString())
//            otp_tv_otp_3.setText(otpCharacters[2].toString())
//            otp_tv_otp_4.setText(otpCharacters[3].toString())
        }
    }


    private fun initView() {

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        otp_tv_otp_1.addTextChangedListener(TextWATCHER(otp_tv_otp_2, otp_tv_otp_1))
        otp_tv_otp_1.requestFocus()
        otp_tv_otp_2.addTextChangedListener(TextWATCHER(otp_tv_otp_3, otp_tv_otp_1))
        otp_tv_otp_3.addTextChangedListener(TextWATCHER(otp_tv_otp_4, otp_tv_otp_2))
        otp_tv_otp_4.addTextChangedListener(TextWATCHER(otp_tv_otp_4, otp_tv_otp_3))

        verifyOTPButton.setOnClickListener(this)
        resendOTPLayout.setOnClickListener(this)

        resendOTPLayout.isEnabled = false
        setTimerForOTP()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val drawable = loadingProgressBar.indeterminateDrawable.mutate()
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN)
            loadingProgressBar.indeterminateDrawable = drawable
        }
    }

    private fun setTimerForOTP() {
        timer = Observable.intervalRange(0, 61, 0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe({ result ->
                    val remainingTime = wait - result
                    val resendTxt = getString(R.string.resend_in) + " " + remainingTime.toString() + " " + getString(R.string.sec)
                    resendOTPTv.text = resendTxt
                    if (result >= wait) {
                        timer!!.dispose()
                        Log.d("10", ">>Done")
                        resendOTPTv.text = getString(R.string.resend_otp)
                        resendOTPLayout.isEnabled = true
                    }
                },
                        { error -> Log.e("TAG", "{$error.message}") },
                        { Log.d("TAG", "completed") })
    }


    override fun onClick(view: View) {
        KeyboardUtils.hideSoftInput(this)
        when (view.id) {
            R.id.verifyOTPButton -> {
                if (!otp_tv_otp_1.text.toString().isEmpty() && !otp_tv_otp_2.text.toString().isEmpty()
                        && !otp_tv_otp_3.text.toString().isEmpty() && !otp_tv_otp_4.text.toString().isEmpty()) {

                    if (NetworkUtils.isConnected())
                        otpVerificationAPI(otp_tv_otp_1.text.toString() + otp_tv_otp_2.text.toString() + otp_tv_otp_3.text.toString() + otp_tv_otp_4.text.toString())
                    else
                        com.os.drewel.utill.Utils.getInstance().showToast(this, getString(R.string.error_network_connection))

                } else {
                    com.os.drewel.utill.Utils.getInstance().showToast(this, getString(R.string.incomplete_otp))
                }
            }

            R.id.resendOTPLayout -> {
                if (NetworkUtils.isConnected()) {
                    resendOtpVerificationAPI()
                } else
                    com.os.drewel.utill.Utils.getInstance().showToast(this, getString(R.string.error_network_connection))

            }

        }

    }

    private fun otpVerificationAPI(otp: String) {
        setProgressState(View.VISIBLE, false)
        val otpVerificationRequest = HashMap<String, String>()
        otpVerificationRequest["mobile_number"] = mobileNumber
        otpVerificationRequest["user_id"] = userId
        otpVerificationRequest["otp"] = otp
        otpVerificationRequest["language"] = DrewelApplication.getInstance().getLanguage()

        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).otpVerification(otpVerificationRequest)
        disposable = signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                    if (result.response!!.status!!) {

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
                        if (result.response!!.data!!.fbId.isNullOrEmpty())
                            prefs.setPreferenceBooleanData(prefs.KEY_SOCIAL_LOGIN, false)
                        else
                            prefs.setPreferenceBooleanData(prefs.KEY_SOCIAL_LOGIN, true)

                        val intent = Intent(this, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()

                    }
                }, { error ->
                    setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )


    }


    private fun resendOtpVerificationAPI() {

        setProgressState(View.VISIBLE, false)
        val resendOTPRequest = HashMap<String, String>()
        resendOTPRequest["user_id"] = userId
        resendOTPRequest["language"] = DrewelApplication.getInstance().getLanguage()

        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).resendOtpVerification(resendOTPRequest)
        disposable = signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                    if (result.response!!.status!!) {
                        resendOTPLayout.isEnabled = false
                        setTimerForOTP()
//                        val otpCharacters = result.response!!.data!!.authotp!!.toCharArray()
//                        otp_tv_otp_1.setText(otpCharacters[0].toString())
//                        otp_tv_otp_2.setText(otpCharacters[1].toString())
//                        otp_tv_otp_3.setText(otpCharacters[2].toString())
//                        otp_tv_otp_4.setText(otpCharacters[3].toString())
                    }
                }, { error ->
                    setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }


    inner class TextWATCHER(private var editText: EditText, private var previousEditText: EditText) : TextWatcher {
        //  var previous_edt: EditText? = null

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            /* if (charSequence.toString().length() == 0) {
                previous_edt.requestFocus();
            }*/
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            /*if (charSequence.toString().length() == 0) {
                previous_edt.requestFocus();
            }*/
        }

        override fun afterTextChanged(editable: Editable) {
            if (editable.toString().length == 1) {
                editText.requestFocus()
            } else if (editable.toString().isEmpty()) {
                previousEditText.requestFocus()
            }
        }
    }


    private fun setProgressState(visibility: Int, enableButton: Boolean) {
        progressBar.visibility = visibility
        verifyOTPButton.isEnabled = enableButton
        resendOTPLayout.isEnabled = enableButton

    }


    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null)
            disposable!!.dispose()

        if (timer != null)
            timer!!.dispose()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        KeyboardUtils.hideSoftInput(this)
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