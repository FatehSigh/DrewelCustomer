package com.drewel.drewel.apicall.responsemodel.myorderdetailresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MyOrderDetailResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
