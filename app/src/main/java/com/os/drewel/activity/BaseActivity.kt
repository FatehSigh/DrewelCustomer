package com.os.drewel.activity

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.blankj.utilcode.util.NetworkUtils
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.os.drewel.R
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.requestmodel.SignUpRequest
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.constant.Constants
import com.os.drewel.delegate.OnLanguageChangeListener
import com.os.drewel.prefrences.Prefs
import com.os.drewel.utill.CommonUtil
import com.os.drewel.utill.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by monikab on 2/21/2018.
 */
open class BaseActivity : AppCompatActivity() {

    private var baseDisposable: Disposable? = null
    private var callbackManager: CallbackManager? = null
    private var progressDialog: ProgressDialog? = null
    var pref: Prefs? = null

    companion object {
        var orderNetPrice = ""
        var orderItemQuantity = ""
        var isLanguageChange = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = Prefs.getInstance(this)
        callbackManager = CallbackManager.Factory.create()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(Utils.getInstance().updateBaseContextLocale(newBase!!))
    }

    fun isNetworkAvailable(): Boolean {
        if (NetworkUtils.isConnected()) {
            return true
        }
        Utils.getInstance().showToast(this,getString(R.string.error_network_connection))
//        Toast.makeText(this, getString(R.string.error_network_connection), Toast.LENGTH_LONG).show()
        return false
    }

    fun callFacebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "user_photos", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager!!, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                println("=========================onsuccess")
                val accessToken = AccessToken.getCurrentAccessToken()
                val request = GraphRequest.newMeRequest(accessToken) { `object`, response ->
                    println("===================JSON++" + `object`)

                    var SfacebookID = ""
                    var Sname = ""
                    var Semail = ""
                    var Sgender = ""
                    var Surl = ""
                    var Sphone = ""
                    var firstNAme = ""
                    var lastName = ""
                    try {

                        if (`object`.has("id")) {
                            SfacebookID = `object`.getString("id")
                        }

                        if (`object`.has("name")) {
                            Sname = `object`.getString("name")
                        }
                        if (`object`.has("first_name"))
                            firstNAme = `object`.getString("first_name")


                        if (`object`.has("last_name"))
                            lastName = `object`.getString("last_name")


                        if (`object`.has("email")) {
                            Semail = `object`.getString("email")
                        }

                        if (`object`.has("gender")) {
                            Sgender = `object`.getString("gender")
                        }
/*
                        if (`object`.has("picture")) {
                            Surl = `object`.getJSONObject("picture").getJSONObject("data").getString("url")
                        }*/
                        Surl = Uri.parse("https://graph.facebook.com/$SfacebookID/picture?width=600&height=600").toString()

                        // LoginManager.getInstance().logOut()
                        callSocialLoginApi(SfacebookID, firstNAme, lastName, Semail, Sphone, Surl)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "id,about,birthday,cover,location,email,first_name,gender,last_name,picture,name")
                request.parameters = parameters
                request.executeAsync()

            }

            override fun onCancel() {

                println("=========================onCancel")
                Utils.getInstance().showToast(this@BaseActivity, getString(R.string.cancel))
            }

            override fun onError(error: FacebookException) {
                println("=========================onError" + error.toString())
                Utils.getInstance().showToast(this@BaseActivity, error.toString())
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    fun showLoading() {
        hideLoading()
        try {
            progressDialog = CommonUtil.showLoadingDialog(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideLoading() {
        try {
            if (progressDialog != null && progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
                progressDialog!!.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun callSocialLoginApi(fbId: String, firstNAme: String, lastName: String, semail: String, sphone: String, surl: String) {

        val socialLoginRequest = HashMap<String, String>()
        socialLoginRequest["fb_id"] = fbId
        socialLoginRequest["device_id"] = pref!!.getPreferenceStringData(pref!!.KEY_DEVICE_ID)
        socialLoginRequest["device_type"] = "android"
        socialLoginRequest["language"] = DrewelApplication.getInstance().getLanguage()

        val socialLoginObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).socialLogin(socialLoginRequest)
        baseDisposable = socialLoginObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    //  Log.d("social login",result.string())
                    /*  setProgressState(View.GONE, true)*/

                    if (result.response!!.status!!) {
                        if (result.response!!.data!!.isMobileverify.equals("0")) {
                            val intent = Intent(this, OTPVerificationActivity::class.java)
                            intent.putExtra(AppIntentExtraKeys.MOBILE_NUMBER, result.response!!.data!!.country_code + result.response!!.data!!.mobile_number)
                            intent.putExtra(AppIntentExtraKeys.USER_ID, result.response!!.data!!.userId)

                            intent.putExtra(AppIntentExtraKeys.OTP, result.response!!.data!!.authotp)
                            startActivity(intent)
                            finish()
                        } else {
                            Utils.getInstance().showToast(this, result.response!!.message!!)
//                            Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()
                            val prefs = Prefs.getInstance(this)
                            prefs.setPreferenceStringData(prefs.KEY_USER_ID, result.response!!.data!!.userId!!)
                            prefs.setPreferenceStringData(prefs.KEY_FIRST_NAME, result.response!!.data!!.firstName!!)
                            prefs.setPreferenceStringData(prefs.KEY_LAST_NAME, result.response!!.data!!.lastName!!)
                            if (result.response!!.data!!.email != null)
                                prefs.setPreferenceStringData(prefs.KEY_EMAIL, result.response!!.data!!.email!!)
                            prefs.setPreferenceStringData(prefs.KEY_PHOTO, result.response!!.data!!.img!!)
                            prefs.setPreferenceStringData(prefs.KEY_ROLE_ID, result.response!!.data!!.roleId!!)
                            prefs.setPreferenceStringData(prefs.KEY_CART_ID, result.response!!.data!!.cartId!!)
                            prefs.setPreferenceStringData(prefs.KEY_CART_ITEM_COUNT, result.response!!.data!!.cartQuantity!!)

                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS, result.response!!.data!!.address!!)
                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_ID, result.response!!.data!!.addressId!!)
                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_NAME, result.response!!.data!!.addressName!!)
                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_LATITUDE, result.response!!.data!!.addressLatitude!!)
                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_LONGITUDE, result.response!!.data!!.addressLongitude!!)

                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_USERNAME, result.response!!.data?.username
                                    ?: "")

                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_PHONE_NUMBER, result.response!!.data?.mobileNumber
                                    ?: "")

                            prefs.setPreferenceStringData(prefs.KEY_FULL_DELIVERY_ADDRESS, result.response!!.data?.fullAddress
                                    ?: "")

                            prefs.setPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS_lANDMARK, result.response!!.data?.landmark
                                    ?: "")


                            prefs.setPreferenceBooleanData(prefs.KEY_SOCIAL_LOGIN, true)

                            val intent: Intent

                            intent = if (prefs.getPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS).isEmpty())
                                Intent(this, DeliveryAddressActivity::class.java)
                            else
                                Intent(this, HomeActivity::class.java)

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }

                    } else if (result.response!!.isDeactivate!! == "0") {
                        val signUpRequest = SignUpRequest()
                        signUpRequest.fbId = "15184874"
//                        signUpRequest.fbId = fbId
                        signUpRequest.firstName = firstNAme
                        signUpRequest.lastName = lastName
                        signUpRequest.email = semail
                        signUpRequest.mobileNumber = sphone
                        signUpRequest.profileImage = surl

                        val intent = Intent(this, SignUpActivity::class.java)
                        val extras = Bundle()
                        extras.putParcelable(AppIntentExtraKeys.SignUpRequest, signUpRequest);
                        intent.putExtras(extras)
                        startActivity(intent)
                    }
                }, { error ->
                    /*setProgressState(View.GONE, true)*/
                    Utils.getInstance().showToast(this, error.message!!)
//                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (baseDisposable != null)
            baseDisposable!!.dispose()
    }

    fun showChangeLanguageDialog(changeLanguageTv: TextView) {
        DrewelApplication.getInstance().setLocale(pref!!.getPreferenceStringData(pref!!.KEY_LANGUAGE), this)
        val changeLanguageAlertDialog = AlertDialog.Builder(this, R.style.DeliveryTypeTheme).create()
//        changeLanguageAlertDialog.setTitle(getString(R.string.app_name))
        changeLanguageAlertDialog.setMessage(getString(R.string.choose_language))

        changeLanguageAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.english), DialogInterface.OnClickListener { dialog, id ->
            changeLanguageTv.text = getString(R.string.english)
            changeLanguageAlertDialog.dismiss()
            changeLanguage(Constants.LANGUAGE_ENGLISH)
        })

        changeLanguageAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.arabic), DialogInterface.OnClickListener { dialog, id ->
            changeLanguageTv.text = getString(R.string.arabic)
            changeLanguageAlertDialog.dismiss()
            changeLanguage(Constants.LANGUAGE_ARABIC)
        })
        changeLanguageAlertDialog.show()
    }

    private fun changeLanguage(language: String) {
        pref!!.setPreferenceStringData(pref!!.KEY_LANGUAGE, language)
        DrewelApplication.getInstance().setLocale(language, this)
        val intent = intent
        finish()
        startActivity(intent)
//        recreate()
    }


    fun setLanguageText(changeLanguageTv: TextView) {
        if (pref!!.getPreferenceStringData(pref!!.KEY_LANGUAGE) == Constants.LANGUAGE_ARABIC)
            changeLanguageTv.text = getString(R.string.arabic)
        else
            changeLanguageTv.text = getString(R.string.english)
    }

}





