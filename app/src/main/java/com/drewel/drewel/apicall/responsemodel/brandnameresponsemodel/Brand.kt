package com.drewel.drewel.apicall.responsemodel.brandnameresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Brand {

    @SerializedName("brand_name")
    @Expose
    var brandName: String? = null
    @SerializedName("brand_id")
    @Expose
    var brandId: String? = null
    @SerializedName("brand_logo")
    @Expose
    var brandLogo: String? = null
    @SerializedName("total_products")
    @Expose
    var totalProducts: Int? = null

}
