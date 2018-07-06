package com.os.drewel.utill

import android.content.Context
import android.content.pm.PackageManager

/**
 * Created by hemendrag on 1/20/2016.
 */
object ShareAppConstant {

    /**
     * PlayStore fprwarding link URL
     */
    var playStoreURL = "market://details?id="
    var dummyTextShare = "Track My Safari !"
    var INTENT_TYPE = "image/*"
    /**
     * Packages name of application at which the data to be shared
     */
    var PACKAGE_FACEBOOK_KATANA = "com.facebook.katana"
    var PACKAGE_WHATSAPP = "com.whatsapp"
    var PACKAGE_GMAIL = "com.google.android.gm"
    var PACKAGE_TWITTER = "com.twitter.android"


    /**
     * Setectting app is installed or not in the device
     * @param appPackageName
     * @param context
     * @return
     */

    fun isAppInstalled(appPackageName: String, context: Context): Boolean {
        val pm = context.packageManager
        val appInstalled: Boolean
        appInstalled = try {
            pm.getPackageInfo(appPackageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return appInstalled
    }

}
