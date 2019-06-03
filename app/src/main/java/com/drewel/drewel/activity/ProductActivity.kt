package com.drewel.drewel.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RatingBar
import com.drewel.drewel.R
import com.drewel.drewel.adapter.BrandAdapter
import com.drewel.drewel.adapter.BrandNameAdapter
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.apicall.responsemodel.categoryresponsemodel.Category
import com.drewel.drewel.apicall.responsemodel.categoryresponsemodel.Subcategory
import com.drewel.drewel.apicall.responsemodel.productlistresponsemodel.Brand
import com.drewel.drewel.apicall.responsemodel.productlistresponsemodel.Data
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.constant.AppIntentExtraKeys
import com.drewel.drewel.constant.Constants
import com.drewel.drewel.utill.PaginationScrollListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_product.*
import kotlinx.android.synthetic.main.content_products.*
import kotlinx.android.synthetic.main.filter_products.view.*
import kotlinx.android.synthetic.main.sort_product.view.*
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by monikab on 3/15/2018.
 */
class ProductActivity : ProductBaseActivity(), TabLayout.OnTabSelectedListener, View.OnClickListener {


    private lateinit var layoutManager: LinearLayoutManager
    private var brandAdapter: BrandAdapter? = null
    private var subCategoryList: List<Subcategory> = ArrayList()
    private var category: Category = Category()
    private var subCategoryId = ""
    private var categoryId = ""
    private var disposable: Disposable? = null
    private var brandList: ArrayList<Brand> = ArrayList()
    private var brandNameList: ArrayList<Brand> = ArrayList()
    private var filterPopupWindow: PopupWindow? = null

    private lateinit var popupWindowView: View

    private var productResponse: Data? = null

    private var selectedBrandPosArray: ArrayList<Int> = ArrayList()
    private var selectedRating: String = ""
    private var selectedMinPriceRange: String = ""
    private var selectedMaxPriceRange: String = ""
    private var brandNameAdapter: BrandNameAdapter? = null

    private var isFilterApplied = false

    private var sortPopupWindow: PopupWindow? = null

    private lateinit var sortPopupWindowView: View

    private var PAGE_START = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)
        setAdapter()
        initView()

        if (isNetworkAvailable())
            callGetProductApi()

    }

    private fun initView() {
        driveActivityName = this.javaClass.name
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        search_product.setOnClickListener(this)
        txt_sortby.setOnClickListener(this)
        if (intent.hasExtra(AppIntentExtraKeys.SELECTED_CATEGORY)) {

            category = intent.getSerializableExtra(AppIntentExtraKeys.SELECTED_CATEGORY) as Category
            subCategoryList = category.subcategories!!
            if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                productTitle.text = category.categoryName
            } else {
                productTitle.text = category.ar_category_name
            }

            categoryId = category.id!!
            if (subCategoryList.isNotEmpty())
                subCategoryId = subCategoryList[0].id!!
            else
                tabs.visibility = View.GONE
            for (i in subCategoryList.indices) {
                if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ENGLISH)) {
                    tabs!!.addTab(tabs!!.newTab().setText(subCategoryList[i].categoryName))
                } else {
                    tabs!!.addTab(tabs!!.newTab().setText(subCategoryList[i].ar_category_name))
                }
            }
        } else {
            productTitle.text = getString(R.string.all_products)
            tabs.visibility = View.GONE
        }

        tabs.addOnTabSelectedListener(this)
//        setDividerForTab()

    }

    override fun onStart() {
        super.onStart()


    }

    private fun setDividerForTab() {
        val linearLayout = tabs!!.getChildAt(0) as LinearLayout
        linearLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        val drawable = GradientDrawable()
        drawable.setColor(ContextCompat.getColor(this, R.color.violet))
        drawable.setSize(1, 1)
        linearLayout.dividerPadding = 0
        linearLayout.dividerDrawable = drawable
    }


    private fun callGetProductApi() {
        setProgressState(View.VISIBLE, false)
        val getProductRequest = HashMap<String, Any>()
        getProductRequest["page"] = "" + PAGE_START
        getProductRequest["category_id"] = categoryId
        getProductRequest["sub_category_id"] = subCategoryId
        if (pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
            getProductRequest["user_id"] = "1"
        else
            getProductRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        getProductRequest["language"] = DrewelApplication.getInstance().getLanguage()
        getProductRequest["min_price"] = selectedMinPriceRange
        getProductRequest["max_price"] = selectedMaxPriceRange
        getProductRequest["ratings"] = selectedRating
        getProductRequest["sort_by"] = sort_by
        val brandAry = ArrayList<String?>()
        for (i in selectedBrandPosArray.indices)
            try {
                Log.e("Value is==", brandNameList[selectedBrandPosArray[i]].brandId + ", Position is=" + selectedBrandPosArray[i])
                brandAry.add(brandNameList[selectedBrandPosArray[i]].brandId)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        getProductRequest["brands_id"] = brandAry

        val getProductObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getProduct(getProductRequest/*, brandAry*/)
        disposable = getProductObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE, true)
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    if (result.response!!.status!!) {

                        isLoading = false;
                        brandRecyclerView.visibility = View.VISIBLE
                        /*search_product.visibility=View.VISIBLE
                        searchProductView.visibility=View.VISIBLE*/
                        productResponse = result.response!!.data!!
                        brandList = result.response!!.data!!.brands!! as ArrayList<Brand>
                        if (brandNameList.isEmpty())
                            brandNameList = result.response!!.data!!.brands!! as ArrayList<Brand>


                        if (brandList.size > 0) {

                              brandAdapter!!.addData(brandList)

                            /* change brands filter*/
                            if (filterPopupWindow != null && !isFilterApplied) {
                                setAdapterOfBrandName()
                                setMinMaxRangeOfPrice()
                            } else
                                isFilterApplied = false

                        }


                    } else {
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this, result.response!!.message!!)
                       /* if (subCategoryList.isNotEmpty())
                            brandRecyclerView.visibility = View.GONE*/
                        /*search_product.visibility=View.GONE
                        searchProductView.visibility=View.GONE*/
                    }
                }, { error ->
                    setProgressState(View.GONE, true)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null)
            disposable!!.dispose()
    }

    private fun setProgressState(visibility: Int, enableButton: Boolean) {
        progressBar.visibility = visibility
        search_product.isEnabled = enableButton
    }


    override fun onTabReselected(tab: TabLayout.Tab?) {
        Log.d("dfdf", "dff")
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        if (disposable != null)
            disposable!!.dispose()
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        Handler().postDelayed({

            PAGE_START=1
            brandList.clear()
            brandAdapter!!.updateData()
            selectedBrandPosArray.clear()
            selectedMaxPriceRange = ""
            selectedMinPriceRange = ""
            selectedRating = ""
            if (filterPopupWindow == null) {

            } else {
                popupWindowView.ratingBar.rating = 0f
                setAdapterOfBrandName()
                setMinMaxRangeOfPrice()
            }



            if (tab != null) {
                subCategoryId = subCategoryList[tab.position].id!!
            }

            if (isNetworkAvailable())
                callGetProductApi()
        }, 200)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_filter -> {
                val view = findViewById<View>(R.id.menu_filter)
                if (brandList.isNotEmpty()) {
                    if (filterPopupWindow == null)
                        initFilterPopUp(view)
                    else
                        showFilterPopUp(view)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun initFilterPopUp(anchorView: View) {
        try {

            /* set view for filter popup window*/
            filterPopupWindow = PopupWindow(this)
            popupWindowView = layoutInflater.inflate(R.layout.filter_products, null) as View
            filterPopupWindow!!.contentView = popupWindowView

            /* set adapter for brands*/
            val llm_brand_name = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            popupWindowView.recyclerView_tv_brand_name.layoutManager = llm_brand_name
            setAdapterOfBrandName()

            /* set price rande*/
            popupWindowView.rsbAge.setOnRangeSeekbarChangeListener { minValue, maxValue ->
                val min = minValue as Long
                val max = maxValue as Long
                popupWindowView.tv_min_amount.text = min.toString()
                popupWindowView.tv_max_amount.text = max.toString()
            }
            setMinMaxRangeOfPrice()
            /* set rating*/
            popupWindowView.ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                //val rateValue = ratingBar.rating.toString()
                popupWindowView.ratingBar.rating = rating
            }
            /* set visibility of brands list*/
            popupWindowView.tv_brand_name.setOnClickListener(this)
            popupWindowView.applyFilterBt.setOnClickListener(this)
            popupWindowView.cancelFilterBt.setOnClickListener(this)
            popupWindowView.clearFilterBt.setOnClickListener(this)
            popupWindowView.img_sortby.setOnClickListener(this)
            popupWindowView.txt_sort_by.setOnClickListener(this)
            /* show popup window*/
            showFilterPopUp(anchorView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var sort_by = "0"
    val SORT_PRICE_LOW_TO_HIGH = "1"
    val SORT_PRICE_HIGH_TO_LOW = "2"
    val SORT_NEW_PRODUCTS = "3"
    val SORT_MOST_POPULAR_PRODUCTS = "4"
    val SORT_DISCOUNTED_PRODUCTS = "5"


    private fun initSortPopUp(anchorView: View) {
        try {
            /* set view for filter popup window*/
            sortPopupWindow = PopupWindow(this)
            sortPopupWindowView = layoutInflater.inflate(R.layout.sort_product, null) as View
            sortPopupWindow!!.contentView = sortPopupWindowView

            sortPopupWindowView.sort_by.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.rb_price_low_high -> sort_by = SORT_PRICE_LOW_TO_HIGH
                    R.id.rb_price_high_low -> sort_by = SORT_PRICE_HIGH_TO_LOW
                    R.id.rb_newly_added -> sort_by = SORT_NEW_PRODUCTS
                    R.id.rb_most_popular_products -> sort_by = SORT_MOST_POPULAR_PRODUCTS
                    R.id.rb_discounted_products -> sort_by = SORT_DISCOUNTED_PRODUCTS
                }
//                txt_sortby.text = sortPopupWindowView.findViewById<RadioButton>(checkedId).text.toString()
                sortPopupWindow!!.dismiss()
                if (isNetworkAvailable())
                    callGetProductApi()
            }

            /* show popup window*/
            showSortPopUp(anchorView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showSortPopUp(anchorView: View) {
        sortPopupWindow!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        sortPopupWindow!!.width = WindowManager.LayoutParams.WRAP_CONTENT
        sortPopupWindow!!.isOutsideTouchable = true
        sortPopupWindow!!.isFocusable = true
        //popup.showAtLocation(anchorView,Gravity.NO_GRAVITY,50,120);
        sortPopupWindow!!.isFocusable = true
        sortPopupWindow!!.setBackgroundDrawable(BitmapDrawable(resources, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)))
        val rectangle = Rect()
        val window = window
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        val statusBarHeight = rectangle.top
        var contentViewTop = window.findViewById<View>(Window.ID_ANDROID_CONTENT).getTop();
        var titleBarHeight = contentViewTop + statusBarHeight;
        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ARABIC))
            sortPopupWindow!!.showAtLocation(anchorView, Gravity.NO_GRAVITY, 0, anchorView.height + statusBarHeight);
        sortPopupWindow!!.showAsDropDown(anchorView)
    }

    private fun setAdapterOfBrandName() {
        brandNameAdapter = BrandNameAdapter(brandNameList)
        popupWindowView.recyclerView_tv_brand_name.adapter = brandNameAdapter
    }

    private fun setMinMaxRangeOfPrice() {
//        popupWindowView.rsbAge.setMaxValue(productResponse!!.maxPrice!!.toFloat())
//        popupWindowView.rsbAge.setMinValue(productResponse!!.minPrice!!.toFloat())

        popupWindowView.tv_min_amount.text = NumberFormat.getInstance().format(productResponse!!.minPrice!!.toDouble())
        popupWindowView.tv_max_amount.text = NumberFormat.getInstance().format(productResponse!!.maxPrice!!.toDouble())

        popupWindowView.rsbAge.setMinStartValue(productResponse!!.minPrice!!.toFloat()).apply()
        popupWindowView.rsbAge.setMaxStartValue(productResponse!!.maxPrice!!.toFloat()).apply()
    }


    private fun showFilterPopUp(anchorView: View) {
        filterPopupWindow!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        filterPopupWindow!!.width = WindowManager.LayoutParams.WRAP_CONTENT
        filterPopupWindow!!.isOutsideTouchable = true
        filterPopupWindow!!.isFocusable = true
        //  popup.showAtLocation(anchorView,Gravity.NO_GRAVITY,50,120);
        filterPopupWindow!!.isFocusable = true
        filterPopupWindow!!.setBackgroundDrawable(BitmapDrawable(resources, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)))
        val rectangle = Rect()
        val window = window
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        val statusBarHeight = rectangle.top
        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ARABIC))
            filterPopupWindow!!.showAtLocation(anchorView, Gravity.NO_GRAVITY, 0, anchorView.height + statusBarHeight);
        filterPopupWindow!!.showAsDropDown(anchorView)
    }

    override fun onClick(view: View) {

        when (view.id) {
            R.id.applyFilterBt -> {
                PAGE_START=1
                brandList.clear()
                brandAdapter!!.updateData()
                isFilterApplied = true
                filterPopupWindow!!.dismiss()
                selectedBrandPosArray.clear()
                Log.e("selectedPosArray==>", brandNameAdapter!!.selectedPosArray.toString())
                selectedBrandPosArray = brandNameAdapter!!.selectedPosArray.clone() as ArrayList<Int>
                selectedMaxPriceRange = popupWindowView.tv_max_amount.text.toString()
                selectedMinPriceRange = popupWindowView.tv_min_amount.text.toString()
                selectedRating = popupWindowView.ratingBar.rating.toString()

                if (isNetworkAvailable())
                    callGetProductApi()
            }
            R.id.txt_sortby -> {
                if (sortPopupWindow == null)
                    initSortPopUp(view)
                else
                    showSortPopUp(view)
            }
            R.id.clearFilterBt -> {
                PAGE_START=1
                brandList.clear()
                brandAdapter!!.updateData()
                filterPopupWindow!!.dismiss()
                selectedBrandPosArray.clear()
                selectedMaxPriceRange = ""
                selectedMinPriceRange = ""
                selectedRating = ""

                popupWindowView.ratingBar.rating = 0f
                sort_by = ""
                setAdapterOfBrandName()
                popupWindowView.tv_min_amount.text = "1"
                popupWindowView.tv_max_amount.text = "100"

                popupWindowView.rsbAge.setMinStartValue(1f).apply()
                popupWindowView.rsbAge.setMaxStartValue(100f).apply()

                if (isNetworkAvailable())
                    callGetProductApi()
            }
            R.id.cancelFilterBt -> {
                /* if user cancel filter then set previous values to filter window*/
//                brandNameAdapter!!.selectedPosArray.clear()
//                brandNameAdapter!!.selectedPosArray = selectedBrandPosArray.clone() as ArrayList<Int>
//                popupWindowView.recyclerView_tv_brand_name.adapter = brandNameAdapter
//                if (selectedMaxPriceRange.isNotEmpty()) {
//                    popupWindowView.rsbAge.setMinStartValue(selectedMinPriceRange.toFloat()).apply()
//                    popupWindowView.rsbAge.setMaxStartValue(selectedMaxPriceRange.toFloat()).apply()
//                }
//                if (selectedRating.isNotEmpty()) {
//                    popupWindowView.ratingBar.rating = selectedRating.toFloat()
//                }
                filterPopupWindow!!.dismiss()
                selectedBrandPosArray.clear()
                selectedMaxPriceRange = ""
                selectedMinPriceRange = ""
                selectedRating = ""
                sort_by = ""
                popupWindowView.ratingBar.rating = 0f
                setAdapterOfBrandName()
                setMinMaxRangeOfPrice()
                filterPopupWindow!!.dismiss()

            }
            R.id.txt_sort_by -> {
                val view = findViewById<View>(R.id.menu_filter)
                filterPopupWindow!!.dismiss()
                if (sortPopupWindow == null)
                    initSortPopUp(view)
                else
                    showSortPopUp(view)
            }
            R.id.img_sortby -> {
                val view = findViewById<View>(R.id.menu_filter)
                filterPopupWindow!!.dismiss()
                if (sortPopupWindow == null)
                    initSortPopUp(view)
                else
                    showSortPopUp(view)
            }
            R.id.tv_brand_name -> {

                if (popupWindowView.recyclerView_tv_brand_name.visibility == View.VISIBLE) {
                    popupWindowView.recyclerView_tv_brand_name.visibility = View.GONE
                    // upRightImage(tv_brand_name)
                } else {
                    //downRightImage(tv_brand_name)
                    popupWindowView.recyclerView_tv_brand_name.visibility = View.VISIBLE
                }
            }
            R.id.search_product -> {
                startActivity(Intent(this, SearchSuggestionActivity::class.java))
            }

        }
    }


    var isLastPage: Boolean = false
    var isLoading: Boolean = false
    private fun setAdapter() {
        brandAdapter = BrandAdapter(this, brandList, tabs.visibility)
        layoutManager = LinearLayoutManager(this)
        brandRecyclerView.layoutManager = layoutManager
        brandRecyclerView.adapter = brandAdapter

        brandRecyclerView?.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun loadMoreItems() {
                Log.d("NextPage", "" + PAGE_START++)
                isLoading = true
                callGetProductApi()
            }
        })


    }


}