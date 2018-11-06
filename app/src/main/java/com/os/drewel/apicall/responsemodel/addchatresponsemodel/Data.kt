package com.os.drewel.apicall.responsemodel.addchatresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("admin_id")
    @Expose
    var admin_id: String? = null

    @SerializedName("img")
    @Expose
    var img: String? = null

}
