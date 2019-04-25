package com.drewel.drewel.apicall.responsemodel.myorderdetailresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Category {

    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("category_name")
    @Expose
    var categoryName: String? = null

    @SerializedName("ar_category_name")
    @Expose
    var ar_category_name: String? = null
    @SerializedName("img")
    @Expose
    var img: String? = null

}
