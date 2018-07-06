package com.os.drewel.apicall.responsemodel.unreadnotificationresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UnreadNotificationResponse {

    @SerializedName("response")
    @Expose
    var response: Response? = null

}
