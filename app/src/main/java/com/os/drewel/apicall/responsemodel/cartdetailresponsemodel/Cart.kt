package com.os.drewel.apicall.responsemodel.cartdetailresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.os.drewel.apicall.responsemodel.productdetailresponsemodel.Category

class Cart {

    @SerializedName("product_id")
    @Expose
    var productId: String? = null
    @SerializedName("quantity")
    @Expose
    var quantity: String? = null
    @SerializedName("product_name")
    @Expose
    var productName: String? = null
    @SerializedName("product_price")
    @Expose
    var productPrice: String? = null
    @SerializedName("product_image")
    @Expose
    var productImage: String? = null

    @SerializedName("out_of_stock")
    @Expose
    var outOfStock: Int? = null
    @SerializedName("Category")
    @Expose
    var category: List<Category>? = null

    @SerializedName("SubCategory")
    @Expose
    var subCategory: List<Category>? = null
    @SerializedName("offer_price")
    @Expose
    var offerPrice: String? = null
    @SerializedName("offer_expires_on")
    @Expose
    var offerExpiresOn: String? = null
}
