package com.drewel.drewel.apicall.responsemodel.profileresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProfileResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
