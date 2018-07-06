package com.os.drewel.apicall.responsemodel.categoryresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Home {

    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("offer_text")
    @Expose
    var offerText: String? = null
    @SerializedName("position")
    @Expose
    var position: String? = null
    @SerializedName("img")
    @Expose
    var img: String? = null

}
