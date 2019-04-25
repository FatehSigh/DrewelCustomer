package com.drewel.drewel.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by heena on 23/8/17.
 */
class ChatUserModel() : Parcelable {

    var user_name: String = ""
    var admin_unread: String = ""
    var timestamp: String = ""
    var user_img: String = ""
    var user_unread: String = ""

    constructor(parcel: Parcel) : this() {
        user_name = parcel.readString()
        admin_unread = parcel.readString()
        timestamp = parcel.readString()
        user_img = parcel.readString()
        user_unread = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user_name)
        parcel.writeString(admin_unread)
        parcel.writeString(timestamp)
        parcel.writeString(user_img)
        parcel.writeString(user_unread)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "ChatUserModel(user_name='$user_name', admin_unread='$admin_unread', timestamp='$timestamp', user_img='$user_img', user_unread='$user_unread')"
    }

    companion object CREATOR : Parcelable.Creator<ChatUserModel> {
        override fun createFromParcel(parcel: Parcel): ChatUserModel {
            return ChatUserModel(parcel)
        }

        override fun newArray(size: Int): Array<ChatUserModel?> {
            return arrayOfNulls(size)
        }
    }


}