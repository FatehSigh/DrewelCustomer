package com.drewel.drewel.apicall.responsemodel.categoryresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class CategoryListResponse  : Serializable {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
