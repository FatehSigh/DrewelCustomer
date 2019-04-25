package com.drewel.drewel.apicall.responsemodel.loginresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoginResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
