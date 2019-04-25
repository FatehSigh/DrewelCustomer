package com.drewel.drewel.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.drewel.drewel.fragment.WelcomeFragment

/**
 * Created by monikab on 2/21/2018.
 */
class WelcomeAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {

        return WelcomeFragment.newInstance(position)
    }

    override fun getCount(): Int {
        return 3;
    }
}