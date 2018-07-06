package com.os.drewel.apicall.responsemodel.unreadnotificationresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {

    @SerializedName("unread")
    @Expose
    var unread: Int? = null

}
