package com.drewel.drewel.prefrences

import android.content.Context
import android.content.SharedPreferences
import com.drewel.drewel.application.DrewelApplication

/**
 * Created by monikab on 3/14/2018.
 */
class Prefs(internal  var context: Context?) {

    private val PREFS_FILENAME = "com.os.drewel.prefs"

    private val sharedPreferences: SharedPreferences = context!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);

    val KEY_USER_ID = "KEY_USER_ID"
    val KEY_FIRST_NAME = "KEY_FIRST_NAME"
    val KEY_LAST_NAME = "KEY_LAST_NAME"
    val KEY_EMAIL = "KEY_EMAIL"
    val KEY_PHOTO = "KEY_PHOTO"
    val KEY_MOBILE = "KEY_MOBILE"
    val KEY_COUNTRY_CODE = "KEY_COUNTRY_CODE"
    val KEY_ROLE_ID = "KEY_ROLE_ID"
    val KEY_DELIVERY_ADDRESS = "KEY_DELIVERY_ADDRESS"
    val KEY_DELIVERY_ADDRESS_ID = "KEY_DELIVERY_ADDRESS_ID"
    val KEY_DELIVERY_ADDRESS_NAME = "KEY_DELIVERY_ADDRESS_NAME"
    val KEY_DELIVERY_ADDRESS_LATITUDE = "KEY_DELIVERY_ADDRESS_LATITUDE"
    val KEY_DELIVERY_ADDRESS_LONGITUDE = "KEY_DELIVERY_ADDRESS_LONGITUDE"
    val KEY_DELIVERY_ADDRESS_USERNAME = "KEY_DELIVERY_ADDRESS_USERNAME"
    val KEY_DELIVERY_ADDRESS_PHONE_NUMBER = "KEY_DELIVERY_ADDRESS_PHONE_NUMBER"
    val KEY_FULL_DELIVERY_ADDRESS = "KEY_FULL_DELIVERY_ADDRESS"
    val KEY_DELIVERY_ADDRESS_lANDMARK = "KEY_DELIVERY_ADDRESS_lANDMARK"
    val KEY_DELIVERY_ADDRESS_TYPE = "KEY_DELIVERY_ADDRESS_TYPE"
    val KEY_ZIP_CODE = "KEY_ZIP_CODE"

    val KEY_CART_ID = "KEY_CART_ID"
    val KEY_CART_ITEM_COUNT = "KEY_CART_ITEM_COUNT"
    val KEY_DEVICE_ID = "KEY_DEVICE_ID"
    val KEY_NOTIFICATION_STATUS = "KEY_NOTIFICATION_STATUS"
    val KEY_SOCIAL_LOGIN = "KEY_SOCIAL_LOGIN"
    val KEY_LANGUAGE = "KEY_LANGUAGE"
    val UNREAD_COUNT: String = "UNREAD_COUNT"

    companion object {

        var prefs: Prefs? = null

        fun getInstance(context: Context?): Prefs {
            if (prefs == null) {
                prefs = Prefs(context)
            }
            return prefs as Prefs
        }
    }

    fun setPreferenceStringData(preferenceKey: String, preferenceData: String) {

        sharedPreferences.edit().putString(preferenceKey, preferenceData).apply()

    }

    fun setPreferenceIntData(preferenceKey: String, preferenceData: Int) {

        sharedPreferences.edit().putInt(preferenceKey, preferenceData).apply()

    }

    fun getPreferenceIntData(preferenceKey: String): Int {

        return sharedPreferences.getInt(preferenceKey, 0)
    }

    fun getPreferenceStringData(preferenceKey: String): String {

        return sharedPreferences.getString(preferenceKey, "")
    }


    fun setPreferenceBooleanData(preferenceKey: String, preferenceData: Boolean) {

        sharedPreferences.edit().putBoolean(preferenceKey, preferenceData).apply()
    }

    fun getPreferenceBooleanData(preferenceKey: String): Boolean {

        return sharedPreferences.getBoolean(preferenceKey, false)
    }
    fun clearSharedPreference() {
        try {
            var deviceid= getInstance(context).getPreferenceStringData(getInstance(context).KEY_DEVICE_ID)
            var language= DrewelApplication.getInstance().getLanguage()
            sharedPreferences!!.edit().clear().commit()
            getInstance(context).setPreferenceStringData(getInstance(context).KEY_DEVICE_ID, deviceid)
            getInstance(context).setPreferenceStringData(getInstance(context).KEY_LANGUAGE, language)
            DrewelApplication.getInstance().setLocale(language, context!!)
        } catch (e: Exception) {
        }

    }
}