package com.os.drewel.apicall.responsemodel.baseresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BaseResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
