package com.os.drewel.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Base64
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.os.drewel.R
import com.os.drewel.prefrences.Prefs
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit


/**
 * Created by monikab on 2/21/2018.
 */
class SplashActivity : BaseActivity() {

    //private var disposable: Disposable? = null
    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        getKeyHash()
//        getFirebaseRefreshToken()
        checkRequestPermission()
    }

    @SuppressLint("PackageManagerGetSignatures")
    private fun getKeyHash() {
        try {
            val packageName = applicationContext.packageName
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }
    }


    private fun callNextActivity() {
        //   var compositeDisposable: CompositeDisposable? = CompositeDisposable()
        disposable = Observable.timer(3000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            val prefs = Prefs.getInstance(this)
                            when {
                                prefs.getPreferenceStringData(prefs.KEY_USER_ID).isEmpty() -> startActivity(Intent(this, WelcomeActivity::class.java))

//                                prefs.getPreferenceStringData(prefs.KEY_DELIVERY_ADDRESS).isEmpty() -> startActivity(Intent(this, DeliveryAddressActivity::class.java))

                                else -> startActivity(Intent(this, HomeActivity::class.java))
                            }
                            finish()
                        },
                        { error -> Log.e("TAG", "{$error.message}") },
                        { Log.d("TAG", "completed") })
    }


    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null)
            disposable.dispose()
    }


    private fun checkRequestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            callNextActivity()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callNextActivity()
            } else {
                disposable = Observable.timer(3000, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                },
                                { error -> },
                                { })
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun getFirebaseRefreshToken() {
        val firebaseToken = FirebaseInstanceId.getInstance().token
        if (firebaseToken != null) {
            pref!!.setPreferenceStringData(pref!!.KEY_DEVICE_ID, firebaseToken)
        }
    }


}