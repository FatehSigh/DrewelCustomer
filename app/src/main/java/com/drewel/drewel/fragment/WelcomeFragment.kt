package com.drewel.drewel.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.drewel.drewel.R
import com.drewel.drewel.constant.AppIntentExtraKeys

/**
 * Created by monikab on 2/21/2018.
 */
class WelcomeFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome_view_pager, container, false)
    }

    companion object {
        fun newInstance(pos: Int): WelcomeFragment {
            val args: Bundle = Bundle()
            args.putInt(AppIntentExtraKeys.WelcomeFragmentPosition, pos)
            val fragment = WelcomeFragment()
            fragment.arguments = args
            return fragment
        }
    }

}