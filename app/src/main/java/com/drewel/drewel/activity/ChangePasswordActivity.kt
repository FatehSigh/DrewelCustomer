package com.drewel.drewel.activity

import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.drewel.drewel.R
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.utill.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.content_activity_change_password.*
import kotlinx.android.synthetic.main.progress_dialog_loading.*

/**
 * Created by monikab on 3/5/2018.
 */
class ChangePasswordActivity : BaseActivity(), View.OnClickListener {

    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        initView()
    }

    private fun initView() {

        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        changePasswordButton.setOnClickListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val drawable = loadingProgressBar.indeterminateDrawable.mutate()
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN)
            loadingProgressBar.indeterminateDrawable = drawable
        }
    }


    override fun onClick(view: View) {

        when (view.id) {
            R.id.changePasswordButton -> {
                if(validateChangePassword()){
                    KeyboardUtils.hideSoftInput(this)

                    if (isNetworkAvailable())
                        callChangePasswordApi()

                }
            }
        }

    }

    private fun validateChangePassword() : Boolean{
        if (oldPasswordEditText.text.toString().isEmpty()) {
            oldPasswordTextLayout.error = getString(R.string.empty_password)
            oldPasswordTextLayout.requestFocus()
            return false
        }
        oldPasswordTextLayout.isErrorEnabled = false


        if (newPasswordEditText.text.toString().isEmpty()) {
            newPasswordTextLayout.error = getString(R.string.empty_password)
            newPasswordTextLayout.requestFocus()
            return false
        }
        newPasswordTextLayout.isErrorEnabled = false


        if (newPasswordEditText.text.toString().length < 6) {
            newPasswordTextLayout.error = getString(R.string.short_password)
            newPasswordTextLayout.requestFocus()
            return false
        }
        newPasswordTextLayout.isErrorEnabled = false

        if (!(newPasswordEditText.text.toString().matches(".*[A-Za-z]+.*[0-9]+.*".toRegex()) || newPasswordEditText.text.toString().matches(".*[0-9]+.*[A-Za-z]+.*".toRegex()))) {
            newPasswordTextLayout.error = getString(R.string.invalid_password)
            newPasswordTextLayout.requestFocus()
            return false
        }
        newPasswordTextLayout.isErrorEnabled = false

        if (newPasswordEditText.text.toString().equals(oldPasswordEditText.text.toString()) ) {
            newPasswordTextLayout.error = getString(R.string.password_same)
            newPasswordTextLayout.requestFocus()
            return false
        }
        newPasswordTextLayout.isErrorEnabled = false

        if ( confirmPasswordEditText.text.toString().isEmpty()) {
            confirmPasswordTextLayout.error = getString(R.string.empty_confirm_password)
            confirmPasswordTextLayout.requestFocus()
            return false
        }
        confirmPasswordTextLayout.isErrorEnabled = false

        if (confirmPasswordEditText.text.toString() != newPasswordEditText.text.toString()) {
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

    override fun onBackPressed() {
        KeyboardUtils.hideSoftInput(this)
        super.onBackPressed()
    }


    private fun callChangePasswordApi() {
        setProgressState(View.VISIBLE, false)
        val changePasswordRequest = HashMap<String, String>()
        changePasswordRequest["old_password"] = oldPasswordEditText.text.toString().trim()
        changePasswordRequest["new_password"] = newPasswordEditText.text.toString().trim()
        changePasswordRequest["language"] = DrewelApplication.getInstance().getLanguage()
        changePasswordRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).changePassword(changePasswordRequest)
        disposable = signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE, true)
                    Utils.getInstance().showToast(this,result.response!!.message!!)
                    if (result.response!!.status!!) {
                            finish()
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
        changePasswordButton.isEnabled = enableButton
    }

}