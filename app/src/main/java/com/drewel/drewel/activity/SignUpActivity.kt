package com.drewel.drewel.activity

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.NetworkUtils
import com.drewel.drewel.R
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.apicall.requestmodel.SignUpRequest
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.constant.AppIntentExtraKeys
import com.drewel.drewel.constant.Constants
import com.drewel.drewel.utill.ValidationUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.content_activity_signup.*
import kotlinx.android.synthetic.main.progress_dialog_loading.*
import android.os.Build


/**
 * Created by monikab on 3/5/2018.
 */
class SignUpActivity : BaseActivity(), View.OnClickListener {

    var disposable: Disposable? = null
    private var signupRequest: SignUpRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        initView()
    }

    private fun initView() {

        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        if (intent.hasExtra(AppIntentExtraKeys.SignUpRequest)) {
            signupRequest = intent.extras.getParcelable(AppIntentExtraKeys.SignUpRequest)
            setData()
        }

        signUpButton.setOnClickListener(this)
        loginButton.setOnClickListener(this)
        changeLanguageTv.setOnClickListener(this)
        alreadyHaveAccountTv.setOnClickListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val drawable = loadingProgressBar.indeterminateDrawable.mutate()
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN)
            loadingProgressBar.indeterminateDrawable = drawable
        }

        /* val manager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
         val countryID = manager.simCountryIso.toUpperCase()

         countryCodeEditText.setText(countryID)*/
        setLanguageText(changeLanguageTv)
    }

    private fun setData() {
        passwordTextLayout.visibility = View.GONE
        confirmPasswordTextLayout.visibility = View.GONE

        emailAddressEditText.setText(signupRequest!!.email)
        phoneNumberEditText.setText(signupRequest!!.mobileNumber)
        firstNameEditText.setText(signupRequest!!.firstName)
        lastNameEditText.setText(signupRequest!!.lastName)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        KeyboardUtils.hideSoftInput(this)
    }

    override fun onClick(view: View) {

        when (view.id) {
            R.id.signUpButton -> {
                if (validateSignUp()) {
                    KeyboardUtils.hideSoftInput(this)

                    if (NetworkUtils.isConnected()) {
                        setProgressState(View.VISIBLE, false)
                        callSignUpApi()
                    } else
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this,getString(R.string.error_network_connection))

                }
                // startActivity(Intent(this, MainActivity::class.java))
            }

            R.id.loginButton -> {
                KeyboardUtils.hideSoftInput(this)
                startActivity(Intent(this, LoginActivity::class.java))
            }
            R.id.alreadyHaveAccountTv -> {
                KeyboardUtils.hideSoftInput(this)
                startActivity(Intent(this, LoginActivity::class.java))
            }
            R.id.facebookLoginButton -> {
                callFacebookLogin()
            }
            R.id.changeLanguageTv -> {
                showChangeLanguageDialog(changeLanguageTv)
            }
        }

    }

    private fun setProgressState(visibility: Int, enableButton: Boolean) {
        progressBar.visibility = visibility
        signUpButton.isEnabled = enableButton
    }


    private fun callSignUpApi() {

        /* val signupRequest = HashMap<String, String>()
         signupRequest.put("email", emailAddressEditText.text.toString().trim())
         signupRequest.put("device_id", "1234")
         signupRequest.put("device_type", "android")
         signupRequest.put("first_name", firstNameEditText.text.toString().trim())
         signupRequest.put("last_name", lastNameEditText.text.toString().trim())
         signupRequest.put("mobile_number", phoneNumberEditText.text.toString().trim())
         signupRequest.put("password", passwordEditText.text.toString().trim())
         signupRequest.put("language", DrewelApplication.getInstance().getLanguage())
         signupRequest.put("role_id", Constants.customerRoleId)*/

        if (signupRequest == null) {
            signupRequest = SignUpRequest()
            signupRequest!!.fbId = ""
        }
        signupRequest!!.firstName = firstNameEditText.text.toString().trim()
        signupRequest!!.lastName = lastNameEditText.text.toString().trim()
        signupRequest!!.email = emailAddressEditText.text.toString().trim()
        signupRequest!!.mobileNumber = phoneNumberEditText.text.toString().trim()
        signupRequest!!.password = passwordEditText.text.toString().trim()
        signupRequest!!.language = DrewelApplication.getInstance().getLanguage()
        signupRequest!!.roleId = Constants.customerRoleId
        signupRequest!!.deviceId = pref!!.getPreferenceStringData(pref!!.KEY_DEVICE_ID)
        signupRequest!!.deviceType = "android"
        signupRequest!!.countryCode = countryCodeEditText.text.toString().trim()

        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).signup(signupRequest!!)
        disposable = signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    /* hide progress bar*/
                    setProgressState(View.GONE, true)

                    com.drewel.drewel.utill.Utils.getInstance().showToast(this,result.response!!.message!!)

                    if (result.response!!.status!!) {
                        val intent = Intent(this, OTPVerificationActivity::class.java)
                        intent.putExtra(AppIntentExtraKeys.MOBILE_NUMBER, result.response!!.data!!.country_code + result.response!!.data!!.mobile_number)
                        intent.putExtra(AppIntentExtraKeys.USER_ID, result.response!!.data!!.userId)
                        intent.putExtra(AppIntentExtraKeys.OTP, result.response!!.data!!.authotp)
                        startActivity(intent)
                        finish()
                    } else {
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this,result.response!!.message!!)
                    }
                }, { error ->
                    setProgressState(View.GONE, true)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this,error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun validateSignUp(): Boolean {

        if (TextUtils.isEmpty(firstNameEditText.text.toString().trim())) {
            firstNameTextLayout.error = getString(R.string.enter_first_name)
            firstNameTextLayout.requestFocus()
            return false
        }
        firstNameTextLayout.isErrorEnabled = false
        if (TextUtils.isEmpty(phoneNumberEditText.text.toString().trim())) {
            phoneNumberTextLayout.error = getString(R.string.enter_phone_number)
            phoneNumberTextLayout.requestFocus()
            return false
        }
        phoneNumberTextLayout.isErrorEnabled = false
        if (phoneNumberEditText.text.toString().length < 8) {
            phoneNumberTextLayout.error = getString(R.string.valid_phone_number)
            phoneNumberTextLayout.requestFocus()
            return false
        }
        phoneNumberTextLayout.isErrorEnabled = false

        if (TextUtils.isEmpty(emailAddressEditText.text.toString().trim())) {
            emailAddressTextLayout.error = getString(R.string.enter_email_address)
            emailAddressTextLayout.requestFocus()
            return false
        }
        emailAddressTextLayout.isErrorEnabled = false

        if (!ValidationUtil.isEmailValid(emailAddressEditText.text.toString().trim())) {
            emailAddressTextLayout.error = getString(R.string.enter_valid_email_address)
            emailAddressTextLayout.requestFocus()
            return false
        }
        emailAddressTextLayout.isErrorEnabled = false

        if (passwordTextLayout.visibility == View.VISIBLE && passwordEditText.text.toString().trim().isEmpty()) {
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

        if (passwordTextLayout.visibility == View.VISIBLE && !(passwordEditText.text.toString().matches(".*[A-Za-z]+.*[0-9]+.*".toRegex()) || passwordEditText.text.toString().matches(".*[0-9]+.*[A-Za-z]+.*".toRegex()))) {
            passwordTextLayout.error = getString(R.string.invalid_password)
            passwordTextLayout.requestFocus()
            return false
        }
        passwordTextLayout.isErrorEnabled = false

        if (confirmPasswordTextLayout.visibility == View.VISIBLE && confirmPasswordEditText.text.toString().isEmpty()) {
            confirmPasswordTextLayout.error = getString(R.string.empty_confirm_password)
            confirmPasswordTextLayout.requestFocus()
            return false
        }
        confirmPasswordTextLayout.isErrorEnabled = false

        if (confirmPasswordTextLayout.visibility == View.VISIBLE && confirmPasswordEditText.text.toString() != passwordEditText.text.toString()) {
            confirmPasswordTextLayout.error = getString(R.string.dont_match_password)
            confirmPasswordTextLayout.requestFocus()
            return false
        }
        confirmPasswordTextLayout.isErrorEnabled = false

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

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null)
            disposable!!.dispose()
    }


}