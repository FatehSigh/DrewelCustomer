package com.drewel.drewel.firebase

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.drewel.drewel.prefrences.Prefs

/**
 * Created by monikab on 2/23/2018.
 */
class DrewelFirebaseInstanceIDService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        super.onTokenRefresh()

        val refreshedToken = FirebaseInstanceId.getInstance().token

        if (refreshedToken != null) {
            val pref=Prefs.getInstance(applicationContext)
            pref.setPreferenceStringData(pref.KEY_DEVICE_ID, refreshedToken)
        }
    }
}