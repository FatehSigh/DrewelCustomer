package com.os.drewel.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.os.drewel.R
import com.os.drewel.activity.HomeActivity
import com.os.drewel.activity.SearchSuggestionActivity
import com.os.drewel.adapter.CategoryAdapter
import com.os.drewel.adapter.HomeBannerAdapter
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.categoryresponsemodel.Category
import com.os.drewel.apicall.responsemodel.categoryresponsemodel.CategoryListResponse
import com.os.drewel.apicall.responsemodel.categoryresponsemodel.Home
import com.os.drewel.application.DrewelApplication
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.category_fragment.*

/**
 * Created by sharukhb on 3/13/2018.
 */

class CategoryFragment : BaseFragment(), View.OnClickListener {


    private var myadapter: CategoryAdapter? = null
    var disposable: Disposable? = null

    var categoryList: List<Category> = ArrayList()

    var homeBannerList: List<Home> = ArrayList()
    private var homeBanneradapter: HomeBannerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        return inflater.inflate(R.layout.category_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search_product.setOnClickListener(this)
        updateMenuTitles()
        /* call api if network available8*/
        if (isNetworkAvailable())
            callCategoryListApi()
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

    private fun setAdapter() {

        if (myadapter == null) {
            val llm = LinearLayoutManager(context)
            llm.orientation = LinearLayoutManager.VERTICAL
            recyclerView!!.layoutManager = llm
            myadapter = CategoryAdapter(context, categoryList)
            recyclerView!!.adapter = myadapter
        } else
            myadapter!!.notifyDataSetChanged()
        if (android.os.Build.VERSION.SDK_INT < 21) {
            ViewCompat.setNestedScrollingEnabled(recyclerView, false)
        } else
            recyclerView.isNestedScrollingEnabled = false
    }


    private fun setHomeBannerAdapter() {

        if (homeBanneradapter == null) {
            val llm = LinearLayoutManager(context)
            llm.orientation = LinearLayoutManager.HORIZONTAL
            homeBannerRecyclerView!!.layoutManager = llm
            homeBanneradapter = HomeBannerAdapter(context, homeBannerList)
            homeBannerRecyclerView!!.adapter = homeBanneradapter
        } else
            homeBanneradapter!!.notifyDataSetChanged()

    }

    private fun callCategoryListApi() {

        setProgressState(View.VISIBLE, false)
        val categoryRequest = HashMap<String, String>()
        categoryRequest.put("user_id", pref!!.getPreferenceStringData(pref!!.KEY_USER_ID))
        categoryRequest.put("language", DrewelApplication.getInstance().getLanguage())

        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getCategory(categoryRequest)
        disposable = signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE, true)
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, context!!)
                    if (result.response!!.status!!) {

                        categoryList = result.response!!.data!!.category!!
                        homeBannerList = result.response!!.data!!.homeBanner!!
                        setHomeBannerAdapter()
                        setAdapter()

                    } else
                        Toast.makeText(context, result.response!!.message, Toast.LENGTH_LONG).show()

                }, { error ->
                    setProgressState(View.GONE, true)
                    Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun setProgressState(visibility: Int, enableButton: Boolean) {
        if (visibility==View.VISIBLE)
            app_bar.visibility=View.GONE
        else
            app_bar.visibility=View.VISIBLE
        progressBar.visibility = visibility
        search_product.isEnabled = enableButton
    }


    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null)
            disposable!!.dispose()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.search_product -> {
                startActivity(Intent(activity, SearchSuggestionActivity::class.java))
            }
        }
    }

}