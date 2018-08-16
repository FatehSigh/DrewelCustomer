package com.os.drewel.apicall.responsemodel.categoryresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Subcategory : Serializable {

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
