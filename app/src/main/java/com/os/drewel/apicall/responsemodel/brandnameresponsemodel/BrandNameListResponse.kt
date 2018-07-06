package com.os.drewel.apicall.responsemodel.brandnameresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BrandNameListResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
