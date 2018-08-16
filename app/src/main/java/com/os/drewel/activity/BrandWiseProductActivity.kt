package com.os.drewel.activity

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import com.os.drewel.R
import com.os.drewel.adapter.ProductAdapter
import com.os.drewel.adapter.ProductAdapter2
import com.os.drewel.apicall.responsemodel.Product
import com.os.drewel.apicall.responsemodel.productlistresponsemodel.Brand
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.utill.EqualSpacingItemDecoration
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_brand_wise_product.*
import kotlinx.android.synthetic.main.content_brand_wise_products.*
import java.util.*

/**
 * Created by monikab on 3/15/2018.
 */
class BrandWiseProductActivity : ProductBaseActivity() {

    private var productAdapter: ProductAdapter2? = null

    var disposable: Disposable? = null

    var brand: Brand = Brand()
    var productList: List<Product> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brand_wise_product)
        initView()
    }

    private fun initView() {
        driveActivityName = this.javaClass.name
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        if (intent.hasExtra(AppIntentExtraKeys.BRAND_WISE_PRODUCT)) {

            brand = intent.getSerializableExtra(AppIntentExtraKeys.BRAND_WISE_PRODUCT) as Brand
            productList = brand.products!!
            productTitle.text = brand.brandName
            setAdapter()
        }
    }

    private fun setAdapter() {
        if (productAdapter == null) {
            productAdapter = ProductAdapter2(this)
            productAdapter!!.productList = productList
            brandWiseProductRecyclerView.layoutManager = GridLayoutManager(this, 2)
            brandWiseProductRecyclerView.addItemDecoration(EqualSpacingItemDecoration(10, EqualSpacingItemDecoration.GRID))
            brandWiseProductRecyclerView.adapter = productAdapter

        } else {
            productAdapter!!.productList = productList
            productAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null)
            disposable!!.dispose()
    }


}