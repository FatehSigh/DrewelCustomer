package com.os.drewel.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class ChannelInfoModel() : Parcelable {


    public var admin_count: Int? = null
    public var message: String? = null
    public var receiver_id: String? = null
    public var receiver_name: String? = null
    public var receiver_profile_image: String? = null
    public var sender_id: String? = null
    public var time: String? = null
    public var user_count: Int? = null

    constructor(parcel: Parcel) : this() {
        admin_count = parcel.readValue(Int::class.java.classLoader) as? Int
        message = parcel.readString()
        receiver_id = parcel.readString()
        receiver_name = parcel.readString()
        receiver_profile_image = parcel.readString()
        sender_id = parcel.readString()
        time = parcel.readString()
        user_count = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(admin_count)
        parcel.writeString(message)
        parcel.writeString(receiver_id)
        parcel.writeString(receiver_name)
        parcel.writeString(receiver_profile_image)
        parcel.writeString(sender_id)
        parcel.writeString(time)
        parcel.writeValue(user_count)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChannelInfoModel> {
        override fun createFromParcel(parcel: Parcel): ChannelInfoModel {
            return ChannelInfoModel(parcel)
        }

        override fun newArray(size: Int): Array<ChannelInfoModel?> {
            return arrayOfNulls(size)
        }
    }


}
