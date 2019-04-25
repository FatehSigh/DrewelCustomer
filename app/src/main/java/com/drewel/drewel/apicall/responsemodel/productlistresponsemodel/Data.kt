package com.drewel.drewel.apicall.responsemodel.productlistresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("Brands")
    @Expose
    var brands: List<Brand>? = null

    @SerializedName("max_price")
    @Expose
    var maxPrice: String? = null

    @SerializedName("min_price")
    @Expose
    var minPrice: String? = null


    @SerializedName("Brands_list")
    @Expose
    var brandsNameList: List<com.drewel.drewel.apicall.responsemodel.brandnameresponsemodel.Brand>? = null
}
