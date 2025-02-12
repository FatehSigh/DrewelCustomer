package com.drewel.drewel.apicall.responsemodel.couponresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Coupon {

    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("coupon_code")
    @Expose
    var couponCode: String? = null
    @SerializedName("discount")
    @Expose
    var discount: String? = null
    @SerializedName("discount_type")
    @Expose
    var discountType: String? = null
    @SerializedName("category_id")
    @Expose
    var categoryId: String? = null
    @SerializedName("category_name")
    @Expose
    var categoryName: String? = null
    @SerializedName("ar_category_name")
    @Expose
    var ar_category_name: String? = null

    @SerializedName("max_use")
    @Expose
    var maxUse: String? = null
    @SerializedName("coupon_description")
    @Expose
    var couponDescription: String? = null

    @SerializedName("ar_coupon_description")
    @Expose
    var ar_coupon_description: String? = null


    @SerializedName("expires_on")
    @Expose
    var expiresOn: String? = null
    @SerializedName("img")
    @Expose
    var img: String? = null
    @SerializedName("is_used")
    @Expose
    var isUsed: Int? = null

}
