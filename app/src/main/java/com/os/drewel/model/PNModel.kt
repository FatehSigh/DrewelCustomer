package com.os.drewel.model

import android.os.Parcel
import android.os.Parcelable

class PNModel() : Parcelable {
    var notification_type: String? = null
    var notification_id: String? = null
    var second_user_id: String? = null
    var image: String? = null
    var amount: String? = null
    var user_id: String? = null
    var item_id: String? = null
    var last_name: String? = null
    var title: String? = null
    var first_name: String? = null
    var badge: Int? = 0
    var message: String? = null
    var profile_image: String? = null

    constructor(parcel: Parcel) : this() {
        notification_type = parcel.readString()
        notification_id = parcel.readString()
        second_user_id = parcel.readString()
        image = parcel.readString()
        amount = parcel.readString()
        user_id = parcel.readString()
        item_id = parcel.readString()
        last_name = parcel.readString()
        title = parcel.readString()
        first_name = parcel.readString()
        badge = parcel.readValue(Int::class.java.classLoader) as? Int
        message = parcel.readString()
        profile_image = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(notification_type)
        parcel.writeString(notification_id)
        parcel.writeString(second_user_id)
        parcel.writeString(image)
        parcel.writeString(amount)
        parcel.writeString(user_id)
        parcel.writeString(item_id)
        parcel.writeString(last_name)
        parcel.writeString(title)
        parcel.writeString(first_name)
        parcel.writeValue(badge)
        parcel.writeString(message)
        parcel.writeString(profile_image)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "PNModel(notification_type=$notification_type, notification_id=$notification_id, second_user_id=$second_user_id, image=$image, amount=$amount, user_id=$user_id, item_id=$item_id, last_name=$last_name, title=$title, first_name=$first_name, badge=$badge, message=$message, profile_image=$profile_image)"
    }

    companion object CREATOR : Parcelable.Creator<PNModel> {
        override fun createFromParcel(parcel: Parcel): PNModel {
            return PNModel(parcel)
        }

        override fun newArray(size: Int): Array<PNModel?> {
            return arrayOfNulls(size)
        }
    }


}