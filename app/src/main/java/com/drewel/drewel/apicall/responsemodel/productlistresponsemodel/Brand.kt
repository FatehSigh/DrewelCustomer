package com.drewel.drewel.apicall.responsemodel.productlistresponsemodel

import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.drewel.drewel.apicall.responsemodel.Product

class Brand : Serializable {

    @SerializedName("brand_name")
    @Expose
    var brandName: String? = null
    @SerializedName("ar_brand_name")
    @Expose
    var ar_brand_name: String? = null
    @SerializedName("brand_id")
    @Expose
    var brandId: String? = null
    @SerializedName("brand_logo")
    @Expose
    var brandLogo: String? = null
    @SerializedName("Products")
    @Expose
    var products: List<Product>? = null

}
