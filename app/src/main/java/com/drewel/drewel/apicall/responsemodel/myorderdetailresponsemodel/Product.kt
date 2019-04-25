package com.drewel.drewel.apicall.responsemodel.myorderdetailresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Product {

    @SerializedName("product_id")
    @Expose
    var productId: String? = null
    @SerializedName("quantity")
    @Expose
    var quantity: String? = null
    @SerializedName("product_name")
    @Expose
    var productName: String? = null

    @SerializedName("ar_product_name")
    @Expose
    var ar_product_name: String? = null

    @SerializedName("product_price")
    @Expose
    var productPrice: String? = null
    @SerializedName("product_image")
    @Expose
    var productImage: String? = null
    @SerializedName("Category")
    @Expose
    var category: List<Category>? = null

    @SerializedName("SubCategory")
    @Expose
    var subCategory: List<Category>? = null

}
