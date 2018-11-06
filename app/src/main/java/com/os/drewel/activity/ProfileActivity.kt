package com.os.drewel.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.KeyboardUtils
import com.nostra13.universalimageloader.core.ImageLoader
import com.os.drewel.R
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.profileresponsemodel.Data
import com.os.drewel.apicall.responsemodel.profileresponsemodel.ProfileResponse
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.constant.AppRequestCodes
import com.os.drewel.prefrences.Prefs.Companion.prefs
import com.os.drewel.utill.Utils
import com.os.drewel.utill.ValidationUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_profile_activity.*
import kotlinx.android.synthetic.main.profile_activity.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

class ProfileActivity : BaseActivity(), View.OnClickListener {

    private var getProfileDisposable: Disposable? = null
    private var imageURI: Uri? = null
    private var profileResponse: ProfileResponse? = null
    private var profileResponseData: Data? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)
        initView()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        fab_change_profile.setOnClickListener(this)
        btn_profile_save.setOnClickListener(this)
    }

    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        txt_verify.setOnClickListener {
            if (!pref!!.getPreferenceStringData(prefs!!.KEY_MOBILE).equals(edt_profile_mobile_number.text.toString()))
                resendOtpVerificationAPI()
        }
        edt_profile_mobile_number.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                txt_verify.isEnabled = true
                txt_verify.text = getString(R.string.verify)
                if (!pref!!.getPreferenceStringData(prefs!!.KEY_MOBILE).equals(s.toString()))
                    txt_verify.visibility = View.VISIBLE
                else
                    txt_verify.visibility = View.GONE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        if (isNetworkAvailable())
            callGetProfiledApi()
    }

    private fun resendOtpVerificationAPI() {
        setProgressState(View.VISIBLE, false)
        val resendOTPRequest = HashMap<String, String>()
        resendOTPRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        resendOTPRequest["language"] = DrewelApplication.getInstance().getLanguage()
        resendOTPRequest["mobile_number"] = edt_profile_mobile_number.text.toString().trim()
        resendOTPRequest["country_code"] = countryCodeEditText.text.toString().trim()

        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).resendOtpVerificationProfile(resendOTPRequest)
        getProfileDisposable = signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                    if (result.response!!.status!!) {
                        val intent = Intent(this, OTPVerificationActivityProfile::class.java)
                        intent.putExtra(AppIntentExtraKeys.MOBILE_NUMBER, edt_profile_mobile_number.text.toString().trim())
                        intent.putExtra(AppIntentExtraKeys.COUNTRY_CODE, countryCodeEditText.text.toString().trim())
                        intent.putExtra(AppIntentExtraKeys.OTP, result.response!!.data!!.authotp)
                        startActivityForResult(intent, 1001)
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
        super.onBackPressed()
        KeyboardUtils.hideSoftInput(this)
    }


    private fun callGetProfiledApi() {
        setProgressState(View.VISIBLE, false)

        val getProfileRequest = HashMap<String, String>()
        getProfileRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        getProfileRequest["language"] = DrewelApplication.getInstance().getLanguage()

        val profileObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).profile(getProfileRequest)
        getProfileDisposable = profileObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE, true)

                    if (result.response!!.status!!) {
                        profileResponse = result
                        profileResponseData = result.response!!.data
                        setProfileData()
                    } else {
                        com.os.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                    }
                }, { error ->
                    setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }


    private fun callEditProfiledApi() {
        setProgressState(View.VISIBLE, false)
        val dataObject = JSONObject()
        dataObject.put("user_id", pref!!.getPreferenceStringData(pref!!.KEY_USER_ID))
        dataObject.put("language", DrewelApplication.getInstance().getLanguage())
        dataObject.put("first_name", edt_profile_first_name.text.toString().trim())
        dataObject.put("last_name", edt_profile_last_name.text.toString().trim())
        dataObject.put("country_code", countryCodeEditText.text.toString())
        dataObject.put("mobile_number", edt_profile_mobile_number.text.toString().trim())

        val requestBody = RequestBody.create(MediaType.parse("application/json"), dataObject.toString())

        val editProfileObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).editProfile(requestBody, prepareFilePart("image", imageURI))
        getProfileDisposable = editProfileObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                    if (result.response!!.status!!) {
                        prefs!!.setPreferenceStringData(prefs!!.KEY_FIRST_NAME, result.response!!.data!!.firstName!!)
                        prefs!!.setPreferenceStringData(prefs!!.KEY_MOBILE, result.response!!.data!!.mobileNumber!!)

                        prefs!!.setPreferenceStringData(prefs!!.KEY_COUNTRY_CODE, result.response!!.data!!.countryCode!!)
                        prefs!!.setPreferenceStringData(prefs!!.KEY_LAST_NAME, result.response!!.data?.lastName
                                ?: "")
                        prefs!!.setPreferenceStringData(prefs!!.KEY_EMAIL, result.response!!.data!!.email!!)
                        prefs!!.setPreferenceStringData(prefs!!.KEY_PHOTO, result.response!!.data?.img
                                ?: "")
//                        if (!profileResponseData!!.mobileNumber!!.trim().equals(result.response!!.data!!.mobileNumber!!.trim())) {
//                            val intent = Intent(this, OTPVerificationActivityProfile::class.java)
//                            intent.putExtra(AppIntentExtraKeys.MOBILE_NUMBER, result.response!!.data!!.countryCode + result.response!!.data!!.mobileNumber!!.trim())
//                            intent.putExtra(AppIntentExtraKeys.USER_ID, result.response!!.data!!.userId)
////                            intent.putExtra(AppIntentExtraKeys.OTP, "")
//                            startActivity(intent)
//
//                        }
                        finish()
                    }
                }, { error ->
                    setProgressState(View.GONE, true)
                    com.os.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }


    private fun prepareFilePart(partName: String, fileUri: Uri?): MultipartBody.Part? {
        if (fileUri != null) {
            val file = File(fileUri.path)
            val requestFile = RequestBody.create(MediaType.parse("image/*"), file)

            // MultipartBody.Part is used to send also the actual file name
            return MultipartBody.Part.createFormData(partName, file.name, requestFile)
        }
        return null
    }

    /* after getting response from api set data to profile screen*/
    private fun setProfileData() {
        edt_profile_first_name.setText(profileResponseData!!.firstName)
        edt_profile_last_name.setText(profileResponseData!!.lastName)
        edt_profile_mobile_number.setText(profileResponseData!!.mobileNumber)
        edit_profile_email_address.setText(profileResponseData!!.email)
        ImageLoader.getInstance().displayImage(profileResponseData!!.img, imv_profile_images, DrewelApplication.getInstance().options)
    }


    private fun setProgressState(visibility: Int, enableButton: Boolean) {
        progressBar.visibility = visibility
        btn_profile_save.isEnabled = enableButton
    }

    override fun onClick(view: View) {

        when (view.id) {
            R.id.fab_change_profile -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 10)
                } else {
                    selectImage()
                }
            }
            R.id.btn_profile_save -> {
                if (validateEditProfile()) {
                    if (!pref!!.getPreferenceStringData(prefs!!.KEY_MOBILE).equals(edt_profile_mobile_number.text.toString())) {

                        txt_verify.visibility = View.VISIBLE
                        if (txt_verify.text.toString().equals(getString(R.string.verified))) {
                            if (isNetworkAvailable())
                                callEditProfiledApi()
                        } else com.os.drewel.utill.Utils.getInstance().showToast(this, getString(R.string.verify_otp))

                    } else {
                        txt_verify.visibility = View.GONE
                        if (isNetworkAvailable())
                            callEditProfiledApi()
                    }
                }
            }

        }
    }

    private fun validateEditProfile(): Boolean {

        if (TextUtils.isEmpty(edt_profile_first_name.text.toString().trim())) {
            profile_til_first_name.error = getString(R.string.enter_first_name)
            profile_til_first_name.requestFocus()
            return false
        }
        profile_til_first_name.isErrorEnabled = false

        if (TextUtils.isEmpty(edt_profile_last_name.text.toString().trim())) {
            profile_til_last_name.error = getString(R.string.please_enter_lastname)
            profile_til_last_name.requestFocus()
            return false
        }
        profile_til_last_name.isErrorEnabled = false

        if (TextUtils.isEmpty(edt_profile_mobile_number.text.toString().trim())) {
            profile_til_mobile_number.error = getString(R.string.enter_phone_number)
            profile_til_mobile_number.requestFocus()
            return false
        }
        profile_til_mobile_number.isErrorEnabled = false

//        if (edt_profile_mobile_number.text.toString().length < 10) {
//            profile_til_mobile_number.error = getString(R.string.valid_phone_number)
//            profile_til_mobile_number.requestFocus()
//            return false
//        }
//        profile_til_mobile_number.isErrorEnabled = false

        profile_til_mobile_number.isErrorEnabled = false
        if (TextUtils.isEmpty(edit_profile_email_address.text.toString().trim())) {
            edit_profile_til_email_address.error = getString(R.string.enter_email_address)
            edit_profile_til_email_address.requestFocus()
            return false
        }
        edit_profile_til_email_address.isErrorEnabled = false

        if (!ValidationUtil.isEmailValid(edit_profile_email_address.text.toString())) {
            edit_profile_til_email_address.error = getString(R.string.enter_email_address)
            edit_profile_til_email_address.requestFocus()
            return false
        }

        return true
    }


    private fun selectImage() {
        val items = arrayOf<CharSequence>(getString(R.string.take_photo), getString(R.string.from_gallery), getString(R.string.cancel))
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.add_photo))

        builder.setItems(items) { dialog, item ->
            when {
                items[item] == getString(R.string.take_photo) -> cameraIntent()
                items[item] == getString(R.string.from_gallery) -> galleryIntent()
                items[item] == getString(R.string.cancel) -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun cameraIntent() {
        EasyImage.openCamera(this, AppRequestCodes.CAMERA_REQUEST_CODE)
    }

    private fun galleryIntent() {
        EasyImage.openGallery(this, AppRequestCodes.GALARY_REQUEST_CODE)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        //        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            selectImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            if (data != null) {
                if (data.getBooleanExtra(AppIntentExtraKeys.OTP, false)) {
                    txt_verify.setText(getString(R.string.verified))
                    txt_verify.isEnabled = false
                }
            }
        } else
            EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
                override fun onImagesPicked(imageFiles: MutableList<File>, source: EasyImage.ImageSource?, type: Int) {
                    val imagePath = Utils.getInstance().getCompressImagePath(Uri.fromFile(imageFiles[0]), this@ProfileActivity)
                    val file = File(imagePath)
                    imageURI = Uri.fromFile(file)
                    imv_profile_images.setImageURI(imageURI)

                }

            })
    }
}