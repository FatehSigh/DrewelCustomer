package com.drewel.drewel.model

import android.os.Parcel
import android.os.Parcelable

class MessageModel() :Parcelable{


    public var message: String? = null
    public var msg_channel: String? = null
    public var receiver_id: String? = null
    public var receiver_name: String? = null
    public var receiver_profile_image: String? = null
    public var sender_id: String? = null
    public var sender_name: String? = null
    public var time: String? = null
    public var sender_profile_image: String? = null

    constructor(parcel: Parcel) : this() {
        message = parcel.readString()
        msg_channel = parcel.readString()
        receiver_name = parcel.readString()
        receiver_profile_image = parcel.readString()
        sender_id = parcel.readString()
        sender_name = parcel.readString()
        time = parcel.readString()
        sender_profile_image = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeString(msg_channel)
        parcel.writeString(receiver_name)
        parcel.writeString(receiver_profile_image)
        parcel.writeString(sender_id)
        parcel.writeString(sender_name)
        parcel.writeString(time)
        parcel.writeString(sender_profile_image)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "MessageModel(message=$message, msg_channel=$msg_channel, receiver_id=$receiver_id, receiver_name=$receiver_name, receiver_profile_image=$receiver_profile_image, sender_id=$sender_id, sender_name=$sender_name, time=$time, sender_profile_image=$sender_profile_image)"
    }

    companion object CREATOR : Parcelable.Creator<MessageModel> {
        override fun createFromParcel(parcel: Parcel): MessageModel {
            return MessageModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageModel?> {
            return arrayOfNulls(size)
        }
    }


}
