package com.os.drewel.apicall.responsemodel.notificationresponsemodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Notification {

    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("item_id")
    @Expose
    var item_id: String? = null
    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("message")
    @Expose
    var message: String? = null
    @SerializedName("message_arabic")
    @Expose
    var message_arabic: String? = null

    @SerializedName("send_by")
    @Expose
    var sendBy: String? = null
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("is_read")
    @Expose
    var isRead: String? = null
    @SerializedName("created")
    @Expose
    var created: String? = null
    @SerializedName("user_id")
    @Expose
    var userId: String? = null

}
