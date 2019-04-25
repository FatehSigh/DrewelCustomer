package com.drewel.drewel.apicall.responsemodel.unreadnotificationresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("unread")
    @Expose
    var unread: Int? = null

    @SerializedName("admin_id")
    @Expose
    var admin_id: String? = null
}
