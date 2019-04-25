package com.drewel.drewel.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import com.blankj.utilcode.util.NetworkUtils
import com.drewel.drewel.R
import com.drewel.drewel.prefrences.Prefs

/**
 * Created by monikab on 2/21/2018.
 */
open class BaseFragment : Fragment() {

    var pref: Prefs?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref=Prefs.getInstance(context)
//        updateMenuTitles()
    }


    fun isNetworkAvailable() : Boolean{
        if (NetworkUtils.isConnected()) {
            return true
        }
        com.drewel.drewel.utill.Utils.getInstance().showToast(context,getString(R.string.error_network_connection))
            return false

    }
}