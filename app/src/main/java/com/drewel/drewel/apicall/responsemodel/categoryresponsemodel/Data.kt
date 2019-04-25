package com.drewel.drewel.apicall.responsemodel.categoryresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Data : Serializable {

    @SerializedName("category")
    @Expose
    var category: List<Category>? = null
    @SerializedName("all_category_img")
    @Expose
    var img: String? = null

    @SerializedName("home")
    @Expose
    var homeBanner: List<Home>? = null

}
