package com.os.drewel.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.os.drewel.R
import com.os.drewel.activity.HomeActivity
import kotlinx.android.synthetic.main.my_order.*
import java.util.ArrayList
import android.os.Build
import android.support.design.widget.TabLayout
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.Constants


/**
 * Created by monikab on 2/21/2018.
 */
class MyOrderFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.my_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateMenuTitles()
        setupViewPager()
        tabLayout.setupWithViewPager(viewpager)
    }

    private fun updateMenuTitles() {
        var activity = activity as HomeActivity
        if (activity.menu == null)
            return
        activity.menu!!.findItem(R.id.menu_addrequest).isVisible = false
        activity.menu!!.findItem(R.id.menu_filter).isVisible = false
        activity.menu!!.findItem(R.id.menu_carts).isVisible = true
        activity.menu!!.findItem(R.id.menu_whishlist).isVisible = true
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFrag(CurrentOrderFragment(), resources.getString(R.string.current_orders))
        adapter.addFrag(CompletedOrderFragment(), resources.getString(R.string.previous_order))
        viewpager.adapter = adapter
        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ARABIC))
            if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                tabLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR)
//                viewpager.setCurrentItem(adapter.getCount())
            }
    }

    private inner class ViewPagerAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFrag(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }


        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }
    }

}